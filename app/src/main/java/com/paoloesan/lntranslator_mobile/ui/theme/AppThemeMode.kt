package com.paoloesan.lntranslator_mobile.ui.theme

import androidx.appcompat.app.AppCompatDelegate
import com.paoloesan.lntranslator_mobile.ui.strings.UiStrings

enum class AppThemeMode(val key: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");

    val nightMode: Int
        get() = when (this) {
            LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            DARK -> AppCompatDelegate.MODE_NIGHT_YES
            SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

    fun getDisplayName(strings: UiStrings): String = when (this) {
        LIGHT -> strings.themeLight
        DARK -> strings.themeDark
        SYSTEM -> strings.themeSystem
    }

    companion object {
        fun fromKey(key: String?): AppThemeMode = when (key?.lowercase()?.trim()) {
            "light", "claro" -> LIGHT
            "dark", "oscuro" -> DARK
            else -> SYSTEM
        }
    }
}
