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
    
    override val buttonClose = "Close"
    override val buttonSave = "Save"
    override val buttonCancel = "Cancel"
    override val buttonDelete = "Delete"
    
    override val keySectionDescription: (Int) -> String = { max -> "You can add up to $max API keys for automatic rotation:" }
    override val keyLabel: (Int) -> String = { index -> "API Key $index" }
    override val keyDeleteContentDescription = "Delete API Key"
    override val keyAddButton = "Add another API Key"
    override val keyGetHere = "Get your key here: "
    override val keyRotationInfo = "Keys will rotate automatically if one fails or reaches its limit."
    
    override val themeSystem = "System default"
    override val themeLight = "Light"
    override val themeDark = "Dark"

    // Home
    override val homeWelcome = "Enter a prompt and start translating."
    override val homePromptLabel = "Context Prompt"
    override val homeViewPrompts = "View saved prompts"
    override val homeSavePrompt = "Save current prompt"
    override val homeStartButton = "Start Translator"
    override val homePermissionDenied = "Capture permission denied"

    // Prompts
    override val deletePromptTitle = "Are you sure you want to delete this prompt?"
    override val deletePromptContentDescription = "Delete Prompt"
    override val savePromptTitle = "Save"
    override val promptTitleLabel = "Prompt title"

    // Overlay
    override val overlayOpen = "Open Translator"
    override val overlayTitle = "Translator"
    override val overlayPrevious = "Previous"
    override val overlayTranslate = "Translate"
    override val overlayNext = "Next"
    override val overlayClose = "Close"
    override val overlayLoading = "Translating..."
    override val overlayHelp = "Press the translate button to capture the screen..."
    override val overlayIncreaseFont = "Increase font size"
    override val overlayDecreaseFont = "Decrease font size"

    // Notifications
    override val notifTitle = "Translator Active"
    override val notifDesc = "The translator is running over other apps"

    // Errors
    override val errorNoProvider = "No translation provider configured"
    override val errorNoApiKey: (String) -> String = { provider -> "Set up your API Key in settings for $provider" }
    override val errorImageCorrupt = "Error: Corrupt or very small image"
    override val errorEmptyResponse = "Error: Empty response from AI"
    override val errorRateLimited = "Rate limit reached"
    override val errorInvalidApiKey = "Invalid API Key"
    override val errorModelOverloaded = "Model overloaded. Try again."
    override val errorUnknown = "Unknown error"
}
