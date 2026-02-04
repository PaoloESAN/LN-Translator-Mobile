package com.paoloesan.lntranslator_mobile.ui.utils

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.MetricAffectingSpan
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.paoloesan.lntranslator_mobile.R

private var extraBoldTypeface: Typeface? = null

/**
 * Aplica Roboto ExtraBold a los spans de negrita (StrongEmphasisSpan) en un TextView.
 * Esto reemplaza el bold estándar de Markdown con una fuente más gruesa.
 *
 * @param textView El TextView que contiene el texto Markdown renderizado
 * @param context El Context para cargar el recurso de fuente
 */
fun applyExtraBoldToMarkdown(textView: TextView, context: Context) {
    val typeface = extraBoldTypeface ?: run {
        ResourcesCompat.getFont(context, R.font.roboto_extrabold)?.also {
            extraBoldTypeface = it
        } ?: return
    }

    val text = textView.text
    if (text is Spannable) {
        val spannableText = SpannableString(text)
        val allSpans = spannableText.getSpans(0, spannableText.length, Any::class.java)

        for (span in allSpans) {
            if (span::class.java.simpleName == "StrongEmphasisSpan") {
                val start = spannableText.getSpanStart(span)
                val end = spannableText.getSpanEnd(span)
                val flags = spannableText.getSpanFlags(span)

                spannableText.removeSpan(span)
                spannableText.setSpan(CustomTypefaceSpan(typeface), start, end, flags)
            }
        }

        textView.text = spannableText
    }
}

/**
 * Span personalizado que permite aplicar un Typeface específico a un rango de texto.
 * Se usa para reemplazar el bold estándar con fuentes más gruesas como ExtraBold o Black.
 *
 * @param typeface El Typeface a aplicar
 */
class CustomTypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {

    override fun updateDrawState(paint: TextPaint) {
        applyTypeface(paint)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyTypeface(paint)
    }

    private fun applyTypeface(paint: TextPaint) {
        paint.typeface = typeface
    }
}
