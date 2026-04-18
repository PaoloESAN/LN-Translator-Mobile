package com.paoloesan.lntranslator_mobile.service

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowManager
import kotlin.math.max
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
import com.paoloesan.lntranslator_mobile.ui.overlay.FloatingOverlayUI
import com.paoloesan.lntranslator_mobile.ui.strings.StringsProvider
import com.paoloesan.lntranslator_mobile.ui.theme.LNTranslatormobileTheme

class OverlayService : LifecycleService(), SavedStateRegistryOwner {

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null

    private lateinit var params: WindowManager.LayoutParams
    private var isExpanded = false
    private var bottomPassThroughEnabled = false
    private var compactX = 100
    private var compactY = 100

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    companion object {
        private const val TAG = "OverlayDiag"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "overlay_channel"
        private const val PREF_BOTTOM_PASS_THROUGH = "overlay_bottom_pass_through"
        private const val PASS_THROUGH_GAP_DP = 24

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
        val prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)
        bottomPassThroughEnabled = prefs.getBoolean(PREF_BOTTOM_PASS_THROUGH, false)
        Log.d(TAG, "PREF init bottomPassThroughEnabled=$bottomPassThroughEnabled")

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = compactX
            y = compactY
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
                                compactX = params.x
                                compactY = params.y
                                windowManager?.updateViewLayout(this, params)
                            },
                            onExpand = ::updateWindowSize,
                            onBottomPassThroughChange = { enabled ->
                                Log.d(
                                    TAG,
                                    "TOGGLE onBottomPassThroughChange enabled=$enabled current=$bottomPassThroughEnabled isExpanded=$isExpanded"
                                )
                                bottomPassThroughEnabled = enabled
                                if (isExpanded) {
                                    Log.d(TAG, "TOGGLE reapplying expanded size after toggle")
                                    updateWindowSize(true)
                                }
                            },
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
        val beforeExpanded = this.isExpanded
        this.isExpanded = isExpanded

        Log.d(
            TAG,
            "LAYOUT updateWindowSize called requestedExpanded=$isExpanded prevExpanded=$beforeExpanded bottomPassThrough=$bottomPassThroughEnabled"
        )
        logParams("before")

        if (isExpanded) {
            if (!beforeExpanded) {
                compactX = params.x
                compactY = params.y
                Log.d(TAG, "LAYOUT save compact position compactX=$compactX compactY=$compactY")
            }

            val metrics = expandedLayoutMetrics()

            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = if (bottomPassThroughEnabled) {
                expandedOverlayHeightPx(true, metrics.availableHeight)
            } else {
                WindowManager.LayoutParams.MATCH_PARENT
            }
            params.x = 0
            params.y = 0
            params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            Log.d(
                TAG,
                "LAYOUT expanded targetHeight=${params.height} topInset=${metrics.topInset} bottomInset=${metrics.bottomInset}"
            )
        } else {
            params.width = WindowManager.LayoutParams.WRAP_CONTENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.x = compactX
            params.y = compactY
            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            Log.d(TAG, "LAYOUT collapsed restore compactX=$compactX compactY=$compactY")
        }

        logParams("after-calc")

        composeView?.let {
            windowManager?.updateViewLayout(composeView, params)
            logParams("after-apply")
        }
    }

    private fun bottomPassThroughGapPx(): Int {
        val density = resources.displayMetrics.density
        val px = (PASS_THROUGH_GAP_DP * density).toInt().coerceAtLeast(1)
        Log.d(TAG, "METRICS gapDp=$PASS_THROUGH_GAP_DP density=$density gapPx=$px")
        return px
    }

    private fun expandedOverlayHeightPx(withBottomGap: Boolean, availableHeight: Int): Int {
        Log.d(
            TAG,
            "METRICS sdk=${Build.VERSION.SDK_INT} availableHeight=$availableHeight withBottomGap=$withBottomGap"
        )

        if (!withBottomGap) return availableHeight

        // En pantallas altas, 24dp puede ser imperceptible; forzamos un minimo visible.
        val baseGapPx = bottomPassThroughGapPx()
        val minVisibleGapPx = (availableHeight * 0.10f).toInt().coerceAtLeast(baseGapPx)
        val effectiveGapPx = max(baseGapPx, minVisibleGapPx)

        val result = (availableHeight - effectiveGapPx).coerceAtLeast(1)
        Log.d(
            TAG,
            "METRICS expandedHeightWithGap=$result baseGapPx=$baseGapPx minVisibleGapPx=$minVisibleGapPx effectiveGapPx=$effectiveGapPx"
        )
        return result
    }

    private fun expandedLayoutMetrics(): ExpandedLayoutMetrics {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager?.currentWindowMetrics
            val boundsHeight = metrics?.bounds?.height() ?: resources.displayMetrics.heightPixels
            val insets = metrics?.windowInsets
                ?.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            val topInset = insets?.top ?: 0
            val bottomInset = insets?.bottom ?: 0
            val availableHeight = (boundsHeight - bottomInset).coerceAtLeast(1)

            Log.d(
                TAG,
                "METRICS boundsHeight=$boundsHeight topInset=$topInset bottomInset=$bottomInset availableHeight=$availableHeight"
            )
            return ExpandedLayoutMetrics(availableHeight, topInset, bottomInset)
        }

        val fallbackHeight = resources.displayMetrics.heightPixels
        return ExpandedLayoutMetrics(fallbackHeight, 0, 0)
    }

    private data class ExpandedLayoutMetrics(
        val availableHeight: Int,
        val topInset: Int,
        val bottomInset: Int
    )

    private fun logParams(stage: String) {
        val p = params
        Log.d(
            TAG,
            "LAYOUT[$stage] w=${p.width} h=${p.height} x=${p.x} y=${p.y} flags=${p.flags} (0x${Integer.toHexString(p.flags)})"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        composeView?.let {
            windowManager?.removeView(it)
        }
        ScreenCaptureService.stop(this)
    }
}
