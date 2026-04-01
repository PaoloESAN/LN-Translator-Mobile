package com.paoloesan.lntranslator_mobile.ui.overlay

import android.content.Context
import android.graphics.Typeface
import androidx.compose.ui.text.font.FontFamily
import androidx.core.content.res.ResourcesCompat
import com.paoloesan.lntranslator_mobile.R

enum class OverlayFontOption(val prefValue: String) {
    ROBOTO("roboto"),
    TIMES_NEW_ROMAN("times_new_roman"),
    MONOSPACE("monospace");

    companion object {
        fun fromPref(value: String?): OverlayFontOption {
            return entries.firstOrNull { it.prefValue == value } ?: ROBOTO
        }
    }
}

fun OverlayFontOption.toLabel(): String {
    return when (this) {
        OverlayFontOption.ROBOTO -> "Roboto"
        OverlayFontOption.TIMES_NEW_ROMAN -> "Times New Roman"
        OverlayFontOption.MONOSPACE -> "Monospace"
    }
}

fun OverlayFontOption.toComposeFontFamily(): FontFamily {
    return when (this) {
        OverlayFontOption.ROBOTO -> FontFamily.SansSerif
        OverlayFontOption.TIMES_NEW_ROMAN -> FontFamily.Serif
        OverlayFontOption.MONOSPACE -> FontFamily.Monospace
    }
}

fun OverlayFontOption.toAndroidTypeface(context: Context): Typeface {
    return when (this) {
        OverlayFontOption.ROBOTO ->
            ResourcesCompat.getFont(context, R.font.roboto_regular) ?: Typeface.SANS_SERIF

        OverlayFontOption.TIMES_NEW_ROMAN -> Typeface.SERIF
        OverlayFontOption.MONOSPACE -> Typeface.MONOSPACE
    }
}
