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
    val settingsUpdateTitle: String
    val settingsUpdateDescription: String

    val updateCurrentVersion: (String) -> String
    val updateUnknownVersion: String
    val updateChecking: String
    val updateNoPublishedVersion: String
    val updateNoPublishedReleasesYet: String
    val updateHttpError: (Int) -> String
    val updateConnectionError: String
    val updateUpToDate: String
    val updateNewVersionAvailable: (String) -> String
    val updateAt: String
    val updateCheckButton: String
    val updateViewChangelogButton: String
    val updateLatestChangesTitle: String
    val updateLatestChangesLoading: String
    val updateLatestChangesEmpty: String

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
    val promptTitleRequired: String
    val promptContextRequired: String
    val promptsEmptyTitle: String
    val promptsEmptySubtitle: String

    // Overlay
    val overlayOpen: String
    val overlayTitle: String
    val overlayPrevious: String
    val overlayTranslate: String
    val overlayNext: String
    val overlayClose: String
    val overlayLoading: String
    val overlayHelp: String
    val overlayIncreaseFont: String
    val overlayDecreaseFont: String
    val overlayConfig: String

    // Config Overlay
    val configFontSizeLabel: String
    val configLineSpacingLabel: String
    val configFontFamilyLabel: String
    val configFontRoboto: String
    val configFontTimesNewRoman: String
    val configFontMonospace: String
    val configPreviewText: String
    val configInvertGestures: String
    val configBottomTouchSpace: String
    val configBack: String
    val configClose: String

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
