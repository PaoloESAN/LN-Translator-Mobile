package com.paoloesan.lntranslator_mobile.ui.home

import android.content.Context
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

object Prompt {
    private const val PREFS_NAME = "settings_prefs"
    private const val KEY_PROMPTS = "prompts_app"
    private val gson = Gson()

    fun guardarPrompt(nuevoPrompt: PromptData, context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val listaExistente = obtenerPrompts(context).toMutableList()

        listaExistente.add(nuevoPrompt)

        val json = gson.toJson(listaExistente)
        prefs.edit { putString(KEY_PROMPTS, json) }
        Toast.makeText(context, "Prompt guardado", Toast.LENGTH_SHORT).show()
    }

    fun obtenerPrompts(context: Context): List<PromptData> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_PROMPTS, null) ?: return emptyList()

        val type = object : TypeToken<List<PromptData>>() {}.type
        return gson.fromJson(json, type)
    }
}