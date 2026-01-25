package com.paoloesan.lntranslator_mobile.ui.strings

import java.util.Locale

interface UiStrings {
    val appName: String
    val navHome: String
    val navSettings: String
    val navBack: String
    val topbarPrompts: String

    val settingsProviderTitle: String
    val settingsProviderDescription: String
    val settingsApikeyTitle: String
    val settingsApikeyDescription: String
    val settingsThemeTitle: String
    val settingsThemeDescription: String
    val settingsLanguageTitle: String
    val settingsLanguageDescription: String

    val buttonClose: String
    val buttonSave: String
    val buttonCancel: String
    val buttonDelete: String

    val keySectionDescription: (Int) -> String
    val keyLabel: (Int) -> String
    val keyDeleteContentDescription: String
    val keyAddButton: String
    val keyGetHere: String
    val keyRotationInfo: String

    val themeSystem: String
    val themeLight: String
    val themeDark: String

    // Home
    val homeWelcome: String
    val homePromptLabel: String
    val homeViewPrompts: String
    val homeSavePrompt: String
    val homeStartButton: String
    val homePermissionDenied: String

    // Prompts
    val deletePromptTitle: String
    val deletePromptContentDescription: String
    val savePromptTitle: String
    val promptTitleLabel: String

    // Overlay
    val overlayOpen: String
    val overlayTitle: String
    val overlayPrevious: String
    val overlayTranslate: String
    val overlayNext: String
    val overlayClose: String
    val overlayLoading: String
    val overlayHelp: String

    // Notifications
    val notifTitle: String
    val notifDesc: String

    // Errors
    val errorNoProvider: String
    val errorNoApiKey: (String) -> String
    val errorImageCorrupt: String
    val errorEmptyResponse: String
    val errorRateLimited: String
    val errorInvalidApiKey: String
    val errorModelOverloaded: String
    val errorUnknown: String
}

object StringsProvider {
    fun getStrings(languageId: String?): UiStrings {
        val id = languageId ?: Locale.getDefault().language

        return when {
            id.startsWith("es", ignoreCase = true) || id.equals(
                "Español",
                ignoreCase = true
            ) -> SpanishUiStrings

            else -> EnglishUiStrings
        }
    }
}
