package com.paoloesan.lntranslator_mobile.ui.strings

object SpanishUiStrings : UiStrings {
    override val appName = "LN Translator"
    override val navHome = "Inicio"
    override val navSettings = "Ajustes"
    override val navBack = "Volver"
    override val topbarPrompts = "Prompts"
    override val topbarTranslationConfig = "Configuración de Traducción"

    override val settingsProviderTitle = "Configuración de Traducción"
    override val settingsProviderDescription = "Configura modelos de IA, claves API y ajustes de OCR."
    override val settingsApikeyTitle = "API Key"
    override val settingsApikeyDescription = "El API Key que usará gemini."
    override val settingsThemeTitle = "Tema"
    override val settingsThemeDescription = "Predeterminado del sistema/claro/oscuro."
    override val settingsLanguageTitle = "Idioma"
    override val settingsLanguageDescription = "Idiomas: Español, Inglés."
    override val settingsUpdateTitle = "Actualizaciones"
    override val settingsUpdateDescription = "Comprueba si hay una nueva versión disponible."

    override val configAiModelTitle = "Modelo de IA"
    override val configActiveModel = "Modelo activo"
    override val configTextExtraction = "Extracción de Texto"
    override val configImageOnly = "Solo Imagen"
    override val configImageSubtitle = "Más tokens · mejor calidad"
    override val configLocalOcr = "OCR Local"
    override val configOcrSubtitle = "Menos tokens · menor calidad"
    override val configShowDescription = "Mostrar descripción"
    override val configHideDescription = "Ocultar descripción"
    override val configImageOnlyDescription = "Se le enviará la captura de la pantalla al modelo."
    override val configLocalOcrDescription = "Se le enviará el texto extraído con OCR local al modelo, lo que puede reducir el tiempo al igual que los tokens."

    override val configShowPrices = "Ver precios de API"
    override val configHidePrices = "Ocultar precios de API"
    override val configModelPricingTitle = "Referencia de Precios de API"
    override val configGemini3FlashPricing = "Gratis: 20 peticiones/día · Pago: ~$1.00 / 1M tokens"
    override val configGemini31FlashLitePricing = "Gratis: 500 peticiones/día · Pago: ~$0.50 / 1M tokens"
    override val configGemini35FlashPricing = "Gratis: 20 peticiones/día · Pago: ~$3.00 / 1M tokens"
    override val updateCurrentVersion: (String) -> String =
        { version -> "Version actual: $version" }
    override val updateUnknownVersion = "desconocida"
    override val updateChecking = "Buscando actualizaciones..."
    override val updateNoPublishedVersion = "No se encontró una versión publicada."
    override val updateNoPublishedReleasesYet = "El repositorio no tiene releases publicadas aún."
    override val updateHttpError: (Int) -> String =
        { code -> "No se pudo consultar GitHub (HTTP $code)." }
    override val updateConnectionError = "No se pudo consultar GitHub. Revisa tu conexión."
    override val updateUpToDate = "Tu aplicación está actualizada."
    override val updateNewVersionAvailable: (String) -> String =
        { version -> "Nueva versión disponible: $version" }
    override val updateAt = "Actualiza en:"
    override val updateCheckButton = "Buscar actualizaciones"
    override val updateViewChangelogButton = "Ver cambios"
    override val updateLatestChangesTitle = "Cambios de la última versión"
    override val updateLatestChangesLoading = "Cargando cambios..."
    override val updateLatestChangesEmpty = "Esta versión no incluye notas de cambios."

    override val buttonClose = "Cerrar"
    override val buttonSave = "Guardar"
    override val buttonCancel = "Cancelar"
    override val buttonDelete = "Borrar"

    override val keySectionDescription: (Int) -> String =
        { max -> "Puedes agregar hasta $max claves API para rotar automáticamente:" }
    override val keyLabel: (Int) -> String = { index -> "API Key $index" }
    override val keyDeleteContentDescription = "Eliminar API Key"
    override val keyAddButton = "Agregar otra API Key"
    override val keyGetHere = "Consigue tu clave aquí (gratis con una cuenta de Google): "
    override val keyRotationInfo =
        "Las claves rotarán automáticamente si una falla o alcanza su límite."

    override val configAdvancedOptions = "Opciones Avanzadas"
    override val configShowAdvanced = "Mostrar opciones avanzadas"
    override val configHideAdvanced = "Ocultar opciones avanzadas"

    override val themeSystem = "Predeterminado del sistema"
    override val themeLight = "Claro"
    override val themeDark = "Oscuro"

    // Home
    override val homeWelcome = "Ingresa un prompt y empieza a traducir."
    override val homePromptLabel = "Prompt de contexto (Opcional)"
    override val homeViewPrompts = "Ver prompts"
    override val homeSavePrompt = "Guardar prompt"
    override val homeStartButton = "Iniciar Traductor"
    override val homePermissionDenied = "Permiso de captura denegado"

    // API Key Warning Dialog
    override val apiKeyWarningTitle = "Falta API Key"
    override val apiKeyWarningMessage = "No tienes una API key, debes tener una para empezar a traducir."
    override val apiKeyWarningContinueWithout = "Continuar sin API key"
    override val apiKeyWarningCancel = "Cancelar"
    override val apiKeyWarningConfigure = "Configurar"

    // Novels
    override val homeNovelsSectionTitle = "Novela Actual"
    override val homeSelectNovel = "Seleccionar Novela"
    override val novelsTitle = "Mis Novelas"
    override val novelsSelected: (Int) -> String = { count -> "$count seleccionada(s)" }
    override val novelsEmptyTitle = "No hay novelas"
    override val novelsEmptySubtitle = "Agrega una novela para guardar sus traducciones."
    override val novelsAddTitle = "Agregar Novela"
    override val novelsEditTitle = "Editar Novela"
    override val novelsAddNameLabel = "Nombre de la novela"
    override val novelsCoverOptional = "Portada (Opcional)"
    override val novelsNone = "Ninguna"

    override val novelDetailsEmptyTitle = "No hay traducciones todavía"
    override val novelDetailsEmptySubtitle = "Abre el traductor sobre alguna aplicación y empieza a capturar para ver las traducciones aquí."
    override val novelDetailsViewPages = "Ver todas las páginas"
    override val novelDetailsGoToPage = "Editar páginas"
    override val novelDetailsPageNumber = "Página"
    override val novelDetailsPageOf = "de"
    override val readerShowOriginalContent = "Ver contenido sin traducir"
    override val readerReadingOrientation = "Orientación de lectura"
    override val readerHorizontal = "Horizontal"
    override val readerVertical = "Vertical"
    override val readerTranslationHeader = "Traducción"
    override val readerOriginalTextHeader = "Texto Original"
    override val buttonGo = "Ir"

    override val pageManagementTitle = "Páginas"
    override val pageManagementFinishEdit = "Finalizar edición"
    override val pageManagementEditPages = "Editar páginas"
    override val pageManagementAddPage = "Añadir página"
    override val pageManagementAddPageTitle = "Añadir página"
    override val pageManagementEditPageTitle = "Editar página"
    override val pageManagementInsertPageTitle = "Insertar página"
    override val pageManagementJumpToPage = "Ir a la página"
    override val pageManagementGo = "Ir"
    override val pageManagementNoPages = "No hay páginas"
    override val pageManagementPressEditToAdd = "Presiona Editar para añadir una"
    override val pageManagementPageNumber: (Int) -> String = { number -> "Página $number" }
    override val pageManagementImageOnlyPage = "Página solo de imagen"
    override val pageManagementOptions = "Opciones"
    override val pageManagementDragToReorder = "Arrastrar para reordenar"
    override val pageManagementInsertAbove = "Insertar página arriba"
    override val pageManagementInsertBelow = "Insertar página abajo"
    override val pageManagementEditPage = "Editar página"
    override val pageManagementDeletePage = "Eliminar página"
    override val pageManagementTranslatedTextLabel = "Texto traducido"
    override val pageManagementOriginalTextLabel = "Texto original (opcional)"
    override val pageManagementPageImageLabel = "Imagen de la página"
    override val pageManagementRemoveImage = "Quitar imagen"
    override val pageManagementSelectImage = "Seleccionar imagen"
    override val pageManagementOnlyImage = "Solo imagen"
    override val pageManagementCoverPage = "Portada"
    override val readerSearchPlaceholder = "Buscar en la novela..."
    override val readerSearchNoResults = "No se encontraron resultados"
    override val readerInvertNavigation = "Invertir navegación horizontal"

    override val deleteNovelConfirmationTitle = "¿Eliminar novela?"
    override val deleteNovelConfirmationMessage: (Int) -> String = { count ->
        if (count == 1) "¿Estás seguro de que quieres eliminar la novela seleccionada y todos sus datos?"
        else "¿Estás seguro de que quieres eliminar las $count novelas seleccionadas y todos sus datos?"
    }
    override val deletePageConfirmationTitle = "¿Eliminar página?"
    override val deletePageConfirmationMessage = "¿Estás seguro de que quieres eliminar esta página?"



    // Prompts
    override val deletePromptTitle = "¿Estas seguro de borrar el prompt?"
    override val deletePromptContentDescription = "Eliminar Prompt"
    override val savePromptTitle = "Guardar"
    override val editPromptTitle = "Editar prompt"
    override val editPromptContentDescription = "Editar prompt"
    override val promptTitleLabel = "Titulo del prompt"
    override val promptDescriptionLabel = "Descripcion del prompt"
    override val promptTitleRequired = "El titulo es obligatorio"
    override val promptContextRequired = "El prompt de contexto no puede estar vacio"
    override val promptsEmptyTitle = "Aun no tienes prompts"
    override val promptsEmptySubtitle = "Guarda tu primer prompt para verlo aqui."

    // Overlay
    override val overlayOpen = "Abrir Traductor"
    override val overlayTitle = "Traductor"
    override val overlayPrevious = "Anterior"
    override val overlayTranslate = "Traducir"
    override val overlayNext = "Siguiente"
    override val overlayClose = "Cerrar"
    override val overlayErrorTitle = "Error"
    override val overlayLoading = "Traduciendo..."
    override val overlayHelp = """
        Presiona el botón de traducir para capturar la pantalla...
        
        Puedes hacer gestos a la derecha o izquierda para ir a una traducción siguiente o anterior.
    """.trimIndent()
    override val overlayIncreaseFont = "Aumentar tamaño de letra"
    override val overlayDecreaseFont = "Disminuir tamaño de letra"
    override val overlayConfig = "Configuración"

    // Config Overlay
    override val configFontSizeLabel = "Tamaño de letra"
    override val configLineSpacingLabel = "Espacio entre líneas"
    override val configFontFamilyLabel = "Fuente"
    override val configFontRoboto = "Roboto"
    override val configFontTimesNewRoman = "Times New Roman"
    override val configFontMonospace = "Monoespaciada"
    override val configPreviewText =
        "El veloz murciélago hindú comía feliz cardillo y kiwi. Este es un texto de muestra para ver el tamaño de la fuente."
    override val configInvertNavigation = "Invertir navegación"
    override val configBottomTouchSpace = "Dejar espacio táctil en los lados"
    override val configSideTouchMarginLabel = "Espacio táctil lateral"
    override val configBack = "Volver"
    override val configClose = "Cerrar Overlay"
    override val configSaveIllustration = "Guardar pantalla actual"
    override val configIllustrationSavedSuccess = "Ilustración guardada correctamente"
    override val configIllustrationSavedError = "Error: no hay novela seleccionada"
    override val configSavedLabel = "Guardado"

    // Notifications
    override val notifTitle = "Traductor Activo"
    override val notifDesc = "El traductor está funcionando sobre otras apps"

    // Content Descriptions
    override val cdCancelSelection = "Cancelar selección"
    override val cdEdit = "Editar"
    override val cdDelete = "Borrar"
    override val cdListView = "Vista de lista"
    override val cdGridView = "Vista de cuadrilla"
    override val cdChangeView = "Cambiar vista"
    override val cdMenu = "Menú"
    override val cdLogo = "Logo de LN Translator"

    // Errors
    override val errorNoProvider = "No hay proveedor de traducción configurado"
    override val errorNoApiKey: (String) -> String =
        { provider -> "Configura tu API Key en ajustes para $provider" }
    override val errorImageCorrupt = "Error: Imagen corrupta o muy pequeña"
    override val errorEmptyResponse = "Error: Respuesta vacía de la IA"
    override val errorRateLimited = "Límite de peticiones alcanzado"
    override val errorInvalidApiKey = "API Key inválida"
    override val errorModelOverloaded = "Modelo sobrecargado. Intenta de nuevo."
    override val errorUnknown = "Error desconocido"

    override val cdShare = "Compartir"
    override val menuShare = "Compartir"
    override val menuImport = "Importar Novela"
    override val importSuccess: (String) -> String = { name -> "Novela importada exitosamente: $name" }
    override val importError = "Error al importar la novela"
    override val novelEmptyError = "La novela está vacía"
    override val importInvalidZip = "Archivo ZIP inválido o corrupto"
    override val cdMenuActions = "Acciones de novela"

    override val shareDialogTitle = "Compartir Novela"
    override val shareDialogMessage = "¿Cómo quieres guardar la novela para compartirla? (El formato .zip solo funciona para esta app)"
    override val shareDialogDownload = "Guardar .zip"
    override val shareDialogPdf = "Guardar .pdf"
    override val shareDialogEpub = "Guardar .epub"
    override val shareDialogShare = "Compartir con otras apps"
    override val shareDialogSaveSuccess = "Archivo guardado exitosamente"
    override val shareDialogSaveError = "Error al guardar el archivo"
}
