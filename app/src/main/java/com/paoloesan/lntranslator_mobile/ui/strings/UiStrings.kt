package com.paoloesan.lntranslator_mobile.ui.strings

import java.util.Locale

interface UiStrings {
    val appName: String
    val navHome: String
    val navSettings: String
    val navBack: String
    val topbarPrompts: String
    val topbarTranslationConfig: String

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

    val configAiModelTitle: String
    val configActiveModel: String
    val configTextExtraction: String
    val configImageOnly: String
    val configImageSubtitle: String
    val configLocalOcr: String
    val configOcrSubtitle: String
    val configShowDescription: String
    val configHideDescription: String
    val configImageOnlyDescription: String
    val configLocalOcrDescription: String

    val configShowPrices: String
    val configHidePrices: String
    val configModelPricingTitle: String
    val configGemini3FlashPricing: String
    val configGemini31FlashLitePricing: String
    val configGemini35FlashPricing: String

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
    val updateDownloadInstallButton: String
    val updateDownloadButton: String
    val updateInstallButton: String
    val updateDownloadingProgress: (Int) -> String
    val updateInstallingStatus: String
    val updateAutoCheckTitle: String
    val updateAutoCheckDescription: String
    val updateApkNotFound: String

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

    val configAdvancedOptions: String
    val configShowAdvanced: String
    val configHideAdvanced: String

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

    // API Key Warning Dialog
    val apiKeyWarningTitle: String
    val apiKeyWarningMessage: String
    val apiKeyWarningContinueWithout: String
    val apiKeyWarningCancel: String
    val apiKeyWarningConfigure: String

    // Novels
    val homeNovelsSectionTitle: String
    val homeSelectNovel: String
    val novelsTitle: String
    val novelsSelected: (Int) -> String
    val novelsEmptyTitle: String
    val novelsEmptySubtitle: String
    val novelsAddTitle: String
    val novelsEditTitle: String
    val novelsAddNameLabel: String
    val novelsPromptLabel: String
    val novelsCoverOptional: String
    val novelsNone: String
    val novelDetailsEmptyTitle: String
    val novelDetailsEmptySubtitle: String
    val novelDetailsViewPages: String
    val novelDetailsGoToPage: String
    val novelDetailsPageNumber: String
    val novelDetailsPageOf: String
    val readerShowOriginalContent: String
    val readerReadingOrientation: String
    val readerHorizontal: String
    val readerVertical: String
    val readerTranslationHeader: String
    val readerOriginalTextHeader: String
    val buttonGo: String

    val pageManagementTitle: String
    val pageManagementFinishEdit: String
    val pageManagementEditPages: String
    val pageManagementAddPage: String
    val pageManagementAddPageTitle: String
    val pageManagementEditPageTitle: String
    val pageManagementInsertPageTitle: String
    val pageManagementJumpToPage: String
    val pageManagementGo: String
    val pageManagementNoPages: String
    val pageManagementPressEditToAdd: String
    val pageManagementPageNumber: (Int) -> String
    val pageManagementImageOnlyPage: String
    val pageManagementOptions: String
    val pageManagementDragToReorder: String
    val pageManagementInsertAbove: String
    val pageManagementInsertBelow: String
    val pageManagementEditPage: String
    val pageManagementDeletePage: String
    val pageManagementTranslatedTextLabel: String
    val pageManagementOriginalTextLabel: String
    val pageManagementPageImageLabel: String
    val pageManagementRemoveImage: String
    val pageManagementSelectImage: String
    val pageManagementOnlyImage: String
    val pageManagementCoverPage: String
    val readerSearchPlaceholder: String
    val readerSearchNoResults: String
    val readerInvertNavigation: String

    val deleteNovelConfirmationTitle: String
    val deleteNovelConfirmationMessage: (Int) -> String
    val deletePageConfirmationTitle: String
    val deletePageConfirmationMessage: String



    // Prompts
    val deletePromptTitle: String
    val deletePromptContentDescription: String
    val savePromptTitle: String
    val editPromptTitle: String
    val editPromptContentDescription: String
    val promptTitleLabel: String
    val promptDescriptionLabel: String
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
    val overlayErrorTitle: String
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
    val configInvertNavigation: String
    val configBottomTouchSpace: String
    val configSideTouchMarginLabel: String
    val configBack: String
    val configClose: String
    val configSaveIllustration: String
    val configIllustrationSavedSuccess: String
    val configIllustrationSavedError: String
    val configSavedLabel: String

    // Notifications
    val notifTitle: String
    val notifDesc: String

    // Content Descriptions
    val cdCancelSelection: String
    val cdEdit: String
    val cdDelete: String
    val cdListView: String
    val cdGridView: String
    val cdChangeView: String
    val cdMenu: String
    val cdLogo: String

    // Errors
    val errorNoProvider: String
    val errorNoApiKey: (String) -> String
    val errorImageCorrupt: String
    val errorEmptyResponse: String
    val errorRateLimited: String
    val errorInvalidApiKey: String
    val errorModelOverloaded: String
    val errorUnknown: String

    val cdShare: String
    val menuShare: String
    val menuImport: String
    val importSuccess: (String) -> String
    val importError: String
    val novelEmptyError: String
    val importInvalidZip: String
    val cdMenuActions: String

    val shareDialogTitle: String
    val shareDialogMessage: String
    val shareDialogDownload: String
    val shareDialogPdf: String
    val shareDialogEpub: String
    val shareDialogShare: String
    val shareDialogSaveSuccess: String
    val shareDialogSaveError: String
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
