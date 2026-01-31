package com.paoloesan.lntranslator_mobile.service

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.translation.TranslationService
import com.paoloesan.lntranslator_mobile.ui.strings.StringsProvider
import com.paoloesan.lntranslator_mobile.ui.theme.LNTranslatormobileTheme

class OverlayService : LifecycleService(), SavedStateRegistryOwner {

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null

    private lateinit var params: WindowManager.LayoutParams

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "overlay_channel"

        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            context.startForegroundService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        showOverlay()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    private fun createNotification(): android.app.Notification {
        val prefs = getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
        val idiomaActual = prefs.getString("idioma_app", null)
        val strings = StringsProvider.getStrings(idiomaActual)

        return androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(strings.notifTitle)
            .setContentText(strings.notifDesc)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = android.app.NotificationChannel(
            CHANNEL_ID, "Overlay Service",
            android.app.NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(android.app.NotificationManager::class.java).createNotificationChannel(
            channel
        )
    }

    private val controller by lazy { TranslationController(TranslationService(applicationContext)) }
    private fun showOverlay() {
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@OverlayService)
            setViewTreeSavedStateRegistryOwner(this@OverlayService)
            setViewTreeViewModelStoreOwner(object : androidx.lifecycle.ViewModelStoreOwner {
                override val viewModelStore: ViewModelStore = ViewModelStore()
            })

            setContent {
                val prefs = remember { getSharedPreferences("settings_prefs", MODE_PRIVATE) }
                var idiomaActual by remember { mutableStateOf(prefs.getString("idioma_app", null)) }
                var temaActual by remember {
                    mutableStateOf(
                        prefs.getString(
                            "tema_app",
                            "Predeterminado del sistema"
                        )
                    )
                }

                LaunchedEffect(Unit) {
                    val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
                        when (key) {
                            "idioma_app" -> idiomaActual = p.getString(key, null)
                            "tema_app" -> temaActual =
                                p.getString(key, "Predeterminado del sistema")
                        }
                    }
                    prefs.registerOnSharedPreferenceChangeListener(listener)
                }

                val strings = StringsProvider.getStrings(idiomaActual)
                val isDarkTheme = when (temaActual) {
                    "Oscuro" -> true
                    "Claro" -> false
                    else -> androidx.compose.foundation.isSystemInDarkTheme()
                }

                val uiState by controller.uiState.collectAsState()

                CompositionLocalProvider(LocalStrings provides strings) {
                    LNTranslatormobileTheme(darkTheme = isDarkTheme) {
                        FloatingOverlayUI(
                            onClose = { stopSelf() },
                            onDrag = { dx, dy ->
                                params.x += dx.toInt()
                                params.y += dy.toInt()
                                windowManager?.updateViewLayout(this, params)
                            },
                            onExpand = ::updateWindowSize,
                            onTranslate = {
                                processTranslation()
                            },
                            onPreload = {
                                processPreload()
                            },
                            uiState = uiState,
                            onAnterior = { controller.irAnterior() },
                            onSiguiente = { controller.irSiguiente() }
                        )
                    }
                }
            }
        }

        windowManager?.addView(composeView, params)
    }

    private fun processTranslation() {
        captureScreen { bitmap ->
            bitmap?.let {
                controller.traducirCaptura(it, lifecycleScope)
            }
        }
    }

    private fun processPreload() {
        // COMENTADO: Servicio de accesibilidad para precargar la siguiente traduccion deshabilitado temporalmente por politicas de android
    }

    private fun captureScreen(onCaptured: (Bitmap?) -> Unit) {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            ScreenCaptureService.instance?.captureScreen { bitmap ->
                if (bitmap != null) {
                    android.util.Log.d(
                        "Overlay",
                        "Captura realizada: ${bitmap.width}x${bitmap.height}"
                    )
                    onCaptured(bitmap)
                } else {
                    android.util.Log.e("Overlay", "No se pudo capturar la pantalla")
                    onCaptured(null)
                }
            }
        }, 100)
    }

    private fun updateWindowSize(isExpanded: Boolean) {
        if (isExpanded) {
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.MATCH_PARENT
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        } else {
            params.width = WindowManager.LayoutParams.WRAP_CONTENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        }

        composeView?.let {
            windowManager?.updateViewLayout(composeView, params)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        composeView?.let {
            windowManager?.removeView(it)
        }
        ScreenCaptureService.stop(this)
    }
}
