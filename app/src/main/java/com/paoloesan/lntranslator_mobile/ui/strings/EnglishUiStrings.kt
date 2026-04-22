package com.paoloesan.lntranslator_mobile.ui.strings

object EnglishUiStrings : UiStrings {
    override val appName = "LN Translator"
    override val navHome = "Home"
    override val navSettings = "Settings"
    override val navBack = "Back"
    override val topbarPrompts = "Prompts"

    override val settingsProviderTitle = "Translation Provider"
    override val settingsProviderDescription = "Select the translation provider to use."
    override val settingsApikeyTitle = "API Key"
    override val settingsApikeyDescription = "The API Key that gemini will use."
    override val settingsThemeTitle = "Theme"
    override val settingsThemeDescription = "System default/light/dark."
    override val settingsLanguageTitle = "Language"
    override val settingsLanguageDescription = "Languages: Spanish, English."
    override val settingsUpdateTitle = "Updates"
    override val settingsUpdateDescription = "Check whether a new version is available."
    override val updateCurrentVersion: (String) -> String = { version -> "Current version: $version" }
    override val updateUnknownVersion = "unknown"
    override val updateChecking = "Checking for updates..."
    override val updateNoPublishedVersion = "No published version was found."
    override val updateNoPublishedReleasesYet = "This repository has no published releases yet."
    override val updateHttpError: (Int) -> String = { code -> "Could not reach GitHub (HTTP $code)." }
    override val updateConnectionError = "Could not reach GitHub. Check your connection."
    override val updateUpToDate = "Your app is up to date."
    override val updateNewVersionAvailable: (String) -> String = { version -> "New version available: $version" }
    override val updateAt = "Update at:"
    override val updateCheckButton = "Check for updates"
    override val updateViewChangelogButton = "View changes"
    override val updateLatestChangesTitle = "Latest release changes"
    override val updateLatestChangesLoading = "Loading changes..."
    override val updateLatestChangesEmpty = "This release does not include changelog notes."

    override val buttonClose = "Close"
    override val buttonSave = "Save"
    override val buttonCancel = "Cancel"
    override val buttonDelete = "Delete"

    override val keySectionDescription: (Int) -> String =
        { max -> "You can add up to $max API keys for automatic rotation:" }
    override val keyLabel: (Int) -> String = { index -> "API Key $index" }
    override val keyDeleteContentDescription = "Delete API Key"
    override val keyAddButton = "Add another API Key"
    override val keyGetHere = "Get your key here: "
    override val keyRotationInfo =
        "Keys will rotate automatically if one fails or reaches its limit."

    override val themeSystem = "System default"
    override val themeLight = "Light"
    override val themeDark = "Dark"

    // Home
    override val homeWelcome = "Enter a prompt and start translating."
    override val homePromptLabel = "Context Prompt"
    override val homeViewPrompts = "View prompts"
    override val homeSavePrompt = "Save prompt"
    override val homeStartButton = "Start Translator"
    override val homePermissionDenied = "Capture permission denied"

    // Prompts
    override val deletePromptTitle = "Are you sure you want to delete this prompt?"
    override val deletePromptContentDescription = "Delete Prompt"
    override val savePromptTitle = "Save"
    override val promptTitleLabel = "Prompt title"
    override val promptTitleRequired = "Title is required"
    override val promptContextRequired = "Context prompt cannot be empty"
    override val promptsEmptyTitle = "No prompts yet"
    override val promptsEmptySubtitle = "Save your first prompt to see it here."

    // Overlay
    override val overlayOpen = "Open Translator"
    override val overlayTitle = "Translator"
    override val overlayPrevious = "Previous"
    override val overlayTranslate = "Translate"
    override val overlayNext = "Next"
    override val overlayClose = "Close"
    override val overlayLoading = "Translating..."
    override val overlayHelp = """
        Press the translate button to capture the screen...
        
        You can swipe left or right to switch to the next or previous translation.
    """.trimIndent()
    override val overlayIncreaseFont = "Increase font size"
    override val overlayDecreaseFont = "Decrease font size"
    override val overlayConfig = "Settings"

    // Config Overlay
    override val configFontSizeLabel = "Font Size"
    override val configLineSpacingLabel = "Line Spacing"
    override val configFontFamilyLabel = "Font"
    override val configFontRoboto = "Roboto"
    override val configFontTimesNewRoman = "Times New Roman"
    override val configFontMonospace = "Monospace"
    override val configPreviewText =
        "The quick brown fox jumps over the lazy dog. This is a sample text to preview the font size."
    override val configInvertNavigation = "Invert navigation"
    override val configBottomTouchSpace = "Leave touch space on the sides"
    override val configSideTouchMarginLabel = "Side touch space"
    override val configBack = "Back"
    override val configClose = "Close Overlay"

    // Notifications
    override val notifTitle = "Translator Active"
    override val notifDesc = "The translator is running over other apps"

    // Errors
    override val errorNoProvider = "No translation provider configured"
    override val errorNoApiKey: (String) -> String =
        { provider -> "Set up your API Key in settings for $provider" }
    override val errorImageCorrupt = "Error: Corrupt or very small image"
    override val errorEmptyResponse = "Error: Empty response from AI"
    override val errorRateLimited = "Rate limit reached"
    override val errorInvalidApiKey = "Invalid API Key"
    override val errorModelOverloaded = "Model overloaded. Try again."
    override val errorUnknown = "Unknown error"
}
