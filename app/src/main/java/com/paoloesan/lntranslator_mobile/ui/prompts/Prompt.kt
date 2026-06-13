package com.paoloesan.lntranslator_mobile.ui.prompts

import android.content.Context
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.paoloesan.lntranslator_mobile.data.DataStoreManager

object Prompt {
    private const val KEY_PROMPTS = "prompts_app"
    private val gson = Gson()

    fun guardarPrompt(nuevoPrompt: PromptData, context: Context) {
        val titulo = nuevoPrompt.titulo.trim()
        val descripcion = nuevoPrompt.descripcion.trim()
        if (titulo.isBlank() || descripcion.isBlank()) {
            return
        }

        val listaExistente = obtenerPrompts(context).toMutableList()

        listaExistente.add(PromptData(titulo, descripcion))

        val json = gson.toJson(listaExistente)
        DataStoreManager.putStringSync(context, KEY_PROMPTS, json)
        Toast.makeText(context, "Prompt guardado", Toast.LENGTH_SHORT).show()
    }

    fun obtenerPrompts(context: Context): List<PromptData> {
        val json = DataStoreManager.getString(context, KEY_PROMPTS, null) ?: return emptyList()

        val type = object : TypeToken<List<PromptData>>() {}.type
        return gson.fromJson(json, type)
    }

    fun eliminarPrompt(indice: Int, context: Context) {
        val listaExistente = obtenerPrompts(context).toMutableList()

        if (indice in listaExistente.indices) {
            listaExistente.removeAt(indice)
            val json = gson.toJson(listaExistente)
            DataStoreManager.putStringSync(context, KEY_PROMPTS, json)
        } else {
            Toast.makeText(context, "Índice inválido", Toast.LENGTH_SHORT).show()
        }
    }

    fun actualizarPrompt(indice: Int, nuevoPrompt: PromptData, context: Context) {
        val titulo = nuevoPrompt.titulo.trim()
        val descripcion = nuevoPrompt.descripcion.trim()
        if (titulo.isBlank() || descripcion.isBlank()) {
            return
        }

        val listaExistente = obtenerPrompts(context).toMutableList()

        if (indice in listaExistente.indices) {
            listaExistente[indice] = PromptData(titulo, descripcion)
            val json = gson.toJson(listaExistente)
            DataStoreManager.putStringSync(context, KEY_PROMPTS, json)
        } else {
            Toast.makeText(context, "Indice invalido", Toast.LENGTH_SHORT).show()
        }
    }
}