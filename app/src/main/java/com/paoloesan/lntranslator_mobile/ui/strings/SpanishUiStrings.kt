package com.paoloesan.lntranslator_mobile.ui.strings

object SpanishUiStrings : UiStrings {
    override val appName = "LN Translator"
    override val navHome = "Inicio"
    override val navSettings = "Ajustes"
    override val navBack = "Volver"
    override val topbarPrompts = "Prompts"

    override val settingsProviderTitle = "Proveedor de Traducción"
    override val settingsProviderDescription = "Selecciona el proveedor de traducción a usar."
    override val settingsApikeyTitle = "API Key"
    override val settingsApikeyDescription = "El API Key que usará gemini."
    override val settingsThemeTitle = "Tema"
    override val settingsThemeDescription = "Predeterminado del sistema/claro/oscuro."
    override val settingsLanguageTitle = "Idioma"
    override val settingsLanguageDescription = "Idiomas: Español, Inglés."
    override val settingsUpdateTitle = "Actualizaciones"
    override val settingsUpdateDescription = "Comprueba si hay una nueva versión disponible."
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
    override val keyGetHere = "Consigue tu clave aquí: "
    override val keyRotationInfo =
        "Las claves rotarán automáticamente si una falla o alcanza su límite."

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

    // Notifications
    override val notifTitle = "Traductor Activo"
    override val notifDesc = "El traductor está funcionando sobre otras apps"

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
}
