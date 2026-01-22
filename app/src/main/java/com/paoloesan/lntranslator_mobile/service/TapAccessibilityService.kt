package com.paoloesan.lntranslator_mobile.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.provider.Settings
import android.view.accessibility.AccessibilityEvent

class TapAccessibilityService : AccessibilityService() {

    companion object {
        var instance: TapAccessibilityService? = null
            private set

        fun isEnabled(context: Context): Boolean {
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val serviceName =
                "${context.packageName}/${TapAccessibilityService::class.java.canonicalName}"
            return enabledServices.contains(serviceName)
        }

        fun openAccessibilitySettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        android.util.Log.d("TapAccessibility", "Servicio de accesibilidad conectado")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }

    override fun onInterrupt() {

    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        android.util.Log.d("TapAccessibility", "Servicio de accesibilidad desconectado")
    }

    fun performTap(x: Float, y: Float, callback: (() -> Unit)? = null) {
        val path = Path().apply {
            moveTo(x, y)
        }

        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                android.util.Log.d("TapAccessibility", "Toque completado en ($x, $y)")
                callback?.invoke()
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                android.util.Log.w("TapAccessibility", "Toque cancelado")
                callback?.invoke()
            }
        }, null)
    }
}
