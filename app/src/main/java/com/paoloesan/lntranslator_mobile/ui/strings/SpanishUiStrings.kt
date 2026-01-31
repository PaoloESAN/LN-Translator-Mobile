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
    
    override val buttonClose = "Cerrar"
    override val buttonSave = "Guardar"
    override val buttonCancel = "Cancelar"
    override val buttonDelete = "Borrar"
    
    override val keySectionDescription: (Int) -> String = { max -> "Puedes agregar hasta $max claves API para rotar automáticamente:" }
    override val keyLabel: (Int) -> String = { index -> "API Key $index" }
    override val keyDeleteContentDescription = "Eliminar API Key"
    override val keyAddButton = "Agregar otra API Key"
    override val keyGetHere = "Consigue tu clave aquí: "
    override val keyRotationInfo = "Las claves rotarán automáticamente si una falla o alcanza su límite."
    
    override val themeSystem = "Predeterminado del sistema"
    override val themeLight = "Claro"
    override val themeDark = "Oscuro"

    // Home
    override val homeWelcome = "Ingresa un prompt y empieza a traducir."
    override val homePromptLabel = "Prompt de contexto"
    override val homeViewPrompts = "Ver prompts guardados"
    override val homeSavePrompt = "Guardar prompt actual"
    override val homeStartButton = "Iniciar Traductor"
    override val homePermissionDenied = "Permiso de captura denegado"

    // Prompts
    override val deletePromptTitle = "¿Estas seguro de borrar el prompt?"
    override val deletePromptContentDescription = "Eliminar Prompt"
    override val savePromptTitle = "Guardar"
    override val promptTitleLabel = "Titulo del prompt"

    // Overlay
    override val overlayOpen = "Abrir Traductor"
    override val overlayTitle = "Traductor"
    override val overlayPrevious = "Anterior"
    override val overlayTranslate = "Traducir"
    override val overlayNext = "Siguiente"
    override val overlayClose = "Cerrar"
    override val overlayLoading = "Traduciendo..."
    override val overlayHelp = "Presiona el botón de traducir para capturar la pantalla..."
    override val overlayIncreaseFont = "Aumentar tamaño de letra"
    override val overlayDecreaseFont = "Disminuir tamaño de letra"

    // Notifications
    override val notifTitle = "Traductor Activo"
    override val notifDesc = "El traductor está funcionando sobre otras apps"

    // Errors
    override val errorNoProvider = "No hay proveedor de traducción configurado"
    override val errorNoApiKey: (String) -> String = { provider -> "Configura tu API Key en ajustes para $provider" }
    override val errorImageCorrupt = "Error: Imagen corrupta o muy pequeña"
    override val errorEmptyResponse = "Error: Respuesta vacía de la IA"
    override val errorRateLimited = "Límite de peticiones alcanzado"
    override val errorInvalidApiKey = "API Key inválida"
    override val errorModelOverloaded = "Modelo sobrecargado. Intenta de nuevo."
    override val errorUnknown = "Error desconocido"
}
