package com.paoloesan.lntranslator_mobile.service

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.paoloesan.lntranslator_mobile.api.GeminiClient
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
        return androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Overlay Activo")
            .setContentText("El traductor está funcionando sobre otras apps")
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

    private val controller by lazy { TranslationController(GeminiClient(applicationContext)) }
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

                val uiState by controller.uiState.collectAsState()

                LNTranslatormobileTheme {
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

        windowManager?.addView(composeView, params)
    }

    private fun processTranslation() {
        // composeView?.visibility = View.GONE
        captureScreen { bitmap ->
            // composeView?.visibility = View.VISIBLE
            bitmap?.let {
                controller.traducirCaptura(it, lifecycleScope)
            }
        }
    }

    private fun processPreload() {
        // COMENTADO: Servicio de accesibilidad para precargar la siguiente traduccion deshabilitado temporalmente por politicas de android
        
        /*
        val accessibilityService = TapAccessibilityService.instance

        if (accessibilityService == null) {
            android.widget.Toast.makeText(
                this,
                "Activa el servicio de accesibilidad para usar esta función",
                android.widget.Toast.LENGTH_LONG
            ).show()
            TapAccessibilityService.openAccessibilitySettings(this)
            return
        }

        val handler = android.os.Handler(android.os.Looper.getMainLooper())

        composeView?.visibility = android.view.View.GONE

        handler.postDelayed({
            val displayMetrics = resources.displayMetrics
            val x = 5f
            val y = displayMetrics.heightPixels * 0.5f

            accessibilityService.performTap(x, y) {
                handler.postDelayed({
                    composeView?.visibility = android.view.View.VISIBLE
                    processTranslation()
                }, 300)
            }
        }, 100)
        */
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
