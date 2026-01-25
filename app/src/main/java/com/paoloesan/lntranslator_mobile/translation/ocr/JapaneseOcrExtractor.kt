package com.paoloesan.lntranslator_mobile.translation.ocr

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class JapaneseOcrExtractor {

    private val recognizer: TextRecognizer = TextRecognition.getClient(
        JapaneseTextRecognizerOptions.Builder().build()
    )

    companion object {
        private const val TAG = "JapaneseOcrExtractor"
        private const val VERTICAL_RATIO_THRESHOLD = 1.5f
        private const val HEADER_ZONE_PERCENT = 0.15f
        private const val COLUMN_GROUPING_THRESHOLD = 50
    }

    suspend fun extractText(bitmap: Bitmap): OcrResult {
        Log.d(TAG, "Ejecutando OCR local...")
        Log.d(TAG, "Imagen: ${bitmap.width}x${bitmap.height}")

        return suspendCoroutine { continuation ->
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            val imageHeight = bitmap.height

            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    Log.d(TAG, "OCR completado. Bloques: ${visionText.textBlocks.size}")

                    if (visionText.textBlocks.isEmpty()) {
                        continuation.resume(
                            OcrResult.Error("No se detectó texto en la imagen")
                        )
                        return@addOnSuccessListener
                    }

                    val extractedText = extractVerticalTextOnly(visionText, imageHeight)

                    if (extractedText.isBlank()) {
                        continuation.resume(
                            OcrResult.Error("No se encontró texto vertical")
                        )
                    } else {
                        Log.d(TAG, "Texto extraído (${extractedText.length} chars)")
                        continuation.resume(OcrResult.Success(extractedText))
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error en OCR: ${e.localizedMessage}")
                    continuation.resume(
                        OcrResult.Error("Error en OCR: ${e.localizedMessage}")
                    )
                }
        }
    }

    private fun extractVerticalTextOnly(visionText: Text, imageHeight: Int): String {
        val blocks = visionText.textBlocks
        if (blocks.isEmpty()) return ""

        val headerZoneLimit = (imageHeight * HEADER_ZONE_PERCENT).toInt()
        val verticalBlocks = mutableListOf<VerticalBlockInfo>()

        for (block in blocks) {
            val boundingBox = block.boundingBox ?: continue

            if (boundingBox.top < headerZoneLimit) continue

            val aspectRatio = boundingBox.height().toFloat() / boundingBox.width().toFloat()
            if (aspectRatio < VERTICAL_RATIO_THRESHOLD) continue

            verticalBlocks.add(
                VerticalBlockInfo(
                    text = block.text,
                    centerX = boundingBox.centerX(),
                    top = boundingBox.top,
                    lines = block.lines
                )
            )
        }

        if (verticalBlocks.isEmpty()) return ""
        return buildTextFromVerticalBlocks(verticalBlocks)
    }

    private fun buildTextFromVerticalBlocks(blocks: List<VerticalBlockInfo>): String {
        val columns = mutableListOf<MutableList<VerticalBlockInfo>>()
        val sortedBlocks = blocks.sortedByDescending { it.centerX }

        for (block in sortedBlocks) {
            var addedToColumn = false
            for (column in columns) {
                val columnCenterX = column.map { it.centerX }.average().toInt()
                if (kotlin.math.abs(block.centerX - columnCenterX) < COLUMN_GROUPING_THRESHOLD) {
                    column.add(block)
                    addedToColumn = true
                    break
                }
            }
            if (!addedToColumn) {
                columns.add(mutableListOf(block))
            }
        }

        columns.sortByDescending { column -> column.maxOf { it.centerX } }

        val result = StringBuilder()
        for ((columnIndex, column) in columns.withIndex()) {
            column.sortBy { it.top }
            for (block in column) {
                for (line in block.lines) {
                    result.append(line.text)
                }
            }
            if (columnIndex < columns.size - 1) {
                result.append("\n")
            }
        }

        return result.toString().trim()
    }

    private data class VerticalBlockInfo(
        val text: String,
        val centerX: Int,
        val top: Int,
        val lines: List<Text.Line>
    )
}

sealed class OcrResult {
    data class Success(val extractedText: String) : OcrResult()
    data class Error(val message: String) : OcrResult()
}
