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
    override val novelsSelected: (Int) -> String = { count -> "$count selected" }
    override val novelsEmptyTitle = "No novels"
    override val novelsEmptySubtitle = "Add a novel to save its translations."
    override val novelsAddTitle = "Add Novel"
    override val novelsEditTitle = "Edit Novel"
    override val novelsAddNameLabel = "Novel name"
    override val novelsCoverOptional = "Cover (Optional)"
    override val novelsNone = "None"

    override val novelDetailsEmptyTitle = "No translations yet"
    override val novelDetailsEmptySubtitle = "Open the translator over any app and start capturing to see translations here."
    override val novelDetailsViewPages = "View all pages"
    override val novelDetailsGoToPage = "Edit pages"
    override val novelDetailsPageNumber = "Page"
    override val novelDetailsPageOf = "of"
    override val readerShowOriginalContent = "Show untranslated content"
    override val readerReadingOrientation = "Reading orientation"
    override val readerHorizontal = "Horizontal"
    override val readerVertical = "Vertical"
    override val readerTranslationHeader = "Translation"
    override val readerOriginalTextHeader = "Original Text"
    override val buttonGo = "Go"

    override val pageManagementTitle = "Pages"
    override val pageManagementFinishEdit = "Finish editing"
    override val pageManagementEditPages = "Edit pages"
    override val pageManagementAddPage = "Add page"
    override val pageManagementAddPageTitle = "Add Page"
    override val pageManagementEditPageTitle = "Edit Page"
    override val pageManagementInsertPageTitle = "Insert Page"
    override val pageManagementJumpToPage = "Go to page"
    override val pageManagementGo = "Go"
    override val pageManagementNoPages = "No pages"
    override val pageManagementPressEditToAdd = "Press Edit to add one"
    override val pageManagementPageNumber: (Int) -> String = { number -> "Page $number" }
    override val pageManagementImageOnlyPage = "Image-only page"
    override val pageManagementOptions = "Options"
    override val pageManagementDragToReorder = "Drag to reorder"
    override val pageManagementInsertAbove = "Insert page above"
    override val pageManagementInsertBelow = "Insert page below"
    override val pageManagementEditPage = "Edit page"
    override val pageManagementDeletePage = "Delete page"
    override val pageManagementTranslatedTextLabel = "Translated text"
    override val pageManagementOriginalTextLabel = "Original text (optional)"
    override val pageManagementPageImageLabel = "Page image"
    override val pageManagementRemoveImage = "Remove image"
    override val pageManagementSelectImage = "Select image"
    override val pageManagementOnlyImage = "Only image"
    override val pageManagementCoverPage = "Cover"
    override val readerSearchPlaceholder = "Search in novel..."
    override val readerSearchNoResults = "No results found"

    override val deleteNovelConfirmationTitle = "Delete novel?"
    override val deleteNovelConfirmationMessage: (Int) -> String = { count ->
        if (count == 1) "Are you sure you want to delete the selected novel and all its data?"
        else "Are you sure you want to delete the $count selected novels and all their data?"
    }
    override val deletePageConfirmationTitle = "Delete page?"
    override val deletePageConfirmationMessage = "Are you sure you want to delete this page?"



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

    // Content Descriptions
    override val cdCancelSelection = "Cancel selection"
    override val cdEdit = "Edit"
    override val cdDelete = "Delete"
    override val cdListView = "List view"
    override val cdGridView = "Grid view"
    override val cdChangeView = "Change view"
    override val cdMenu = "Menu"
    override val cdLogo = "LN Translator Logo"

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

    override val cdShare = "Share"
    override val menuShare = "Share"
    override val menuImport = "Import Novel"
    override val importSuccess: (String) -> String = { name -> "Novel imported successfully: $name" }
    override val importError = "Error importing novel"
    override val novelEmptyError = "The novel is empty"
    override val importInvalidZip = "Invalid or corrupt ZIP file"
    override val cdMenuActions = "Novel actions"

    override val shareDialogTitle = "Share Novel"
    override val shareDialogMessage = "How would you like to save the novel for sharing? (.zip files are only compatible with this app)"
    override val shareDialogDownload = "Save .zip"
    override val shareDialogPdf = "Save .pdf"
    override val shareDialogEpub = "Save .epub"
    override val shareDialogShare = "Share with other apps"
    override val shareDialogSaveSuccess = "File saved successfully"
    override val shareDialogSaveError = "Error saving file"
}
