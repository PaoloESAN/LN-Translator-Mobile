package com.paoloesan.lntranslator_mobile.ui.strings

object EnglishUiStrings : UiStrings {
    override val appName = "LN Translator"
    override val navHome = "Home"
    override val navSettings = "Settings"
    override val navBack = "Back"
    override val topbarPrompts = "Prompts"
    override val topbarTranslationConfig = "Translation Configuration"

    override val settingsProviderTitle = "Translation Config"
    override val settingsProviderDescription = "Configure AI models, API keys, and OCR modes."
    override val settingsApikeyTitle = "API Key"
    override val settingsApikeyDescription = "The API Key that gemini will use."
    override val settingsThemeTitle = "Theme"
    override val settingsThemeDescription = "System default/light/dark."
    override val settingsLanguageTitle = "Language"
    override val settingsLanguageDescription = "Languages: Spanish, English."
    override val settingsUpdateTitle = "Updates"
    override val settingsUpdateDescription = "Check whether a new version is available."

    override val configAiModelTitle = "AI Model"
    override val configActiveModel = "Active model"
    override val configTextExtraction = "Text Extraction"
    override val configVisionOnly = "Vision Only"
    override val configVisionSubtitle = "More tokens · better quality"
    override val configLocalOcr = "Local OCR"
    override val configOcrSubtitle = "Fewer tokens · lower quality"

    override val configShowPrices = "Show API prices"
    override val configHidePrices = "Hide API prices"
    override val configModelPricingTitle = "API Pricing Reference"
    override val configGemini3FlashPricing = "Free: 20 requests/day · Paid: ~$1.00 / 1M tokens"
    override val configGemini31FlashLitePricing = "Free: 500 requests/day · Paid: ~$0.50 / 1M tokens"
    override val configGemini35FlashPricing = "Free: 20 requests/day · Paid: ~$3.00 / 1M tokens"
    override val updateCurrentVersion: (String) -> String =
        { version -> "Current version: $version" }
    override val updateUnknownVersion = "unknown"
    override val updateChecking = "Checking for updates..."
    override val updateNoPublishedVersion = "No published version was found."
    override val updateNoPublishedReleasesYet = "This repository has no published releases yet."
    override val updateHttpError: (Int) -> String =
        { code -> "Could not reach GitHub (HTTP $code)." }
    override val updateConnectionError = "Could not reach GitHub. Check your connection."
    override val updateUpToDate = "Your app is up to date."
    override val updateNewVersionAvailable: (String) -> String =
        { version -> "New version available: $version" }
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
    override val homePromptLabel = "Context Prompt (Optional)"
    override val homeViewPrompts = "View prompts"
    override val homeSavePrompt = "Save prompt"
    override val homeStartButton = "Start Translator"
    override val homePermissionDenied = "Capture permission denied"

    // Novels
    override val homeNovelsSectionTitle = "Current Novel"
    override val homeSelectNovel = "Select Novel"
    override val novelsTitle = "My Novels"
    override val novelsEmptyTitle = "No novels"
    override val novelsEmptySubtitle = "Add a novel to save its translations."
    override val novelsAddTitle = "Add Novel"
    override val novelsAddNameLabel = "Novel name"
    override val novelsNone = "None"

    // Prompts
    override val deletePromptTitle = "Are you sure you want to delete this prompt?"
    override val deletePromptContentDescription = "Delete Prompt"
    override val savePromptTitle = "Save"
    override val editPromptTitle = "Edit prompt"
    override val editPromptContentDescription = "Edit prompt"
    override val promptTitleLabel = "Prompt title"
    override val promptDescriptionLabel = "Prompt description"
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
    override val overlayErrorTitle = "Error"
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
