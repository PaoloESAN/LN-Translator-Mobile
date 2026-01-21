package com.paoloesan.lntranslator_mobile.ui.home

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.navigation.NavController
import com.paoloesan.lntranslator_mobile.service.OverlayService
import com.paoloesan.lntranslator_mobile.service.ScreenCaptureService
import com.paoloesan.lntranslator_mobile.ui.prompts.PromptDialog

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    onNavigateToPrompts: () -> Unit
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    var textPrompt by rememberSaveable { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val mediaProjectionManager = remember {
        context.getSystemService(Activity.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            ScreenCaptureService.start(context, result.resultCode, result.data!!)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                OverlayService.start(context)
            }, 500)
        } else {
            Toast.makeText(context, "Permiso de captura denegado", Toast.LENGTH_SHORT).show()
        }
    }

    val result by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<String>("prompt_seleccionado")
        ?.observeAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(result) {
        result?.let {
            textPrompt = it
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("prompt_seleccionado")
        }
    }

    PromptDialog(
        descripcion = textPrompt,
        abierto = showDialog,
        contexto = context,
        onDismissRequest = { showDialog = false }
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {

            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.paoloesan.lntranslator_mobile.R.drawable.ln_translator_logo),
                contentDescription = "LN Translator Logo",
                modifier = Modifier
                    .size(200.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Text(
                text = "\"Ingresa un prompt y empieza a traducir.\"",
                fontStyle = FontStyle.Italic
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    label = { Text(text = "Prompt de contexto") },
                    value = textPrompt,
                    onValueChange = { textPrompt = it },
                    minLines = 3,
                    modifier = Modifier.weight(1f)
                )
                Column {
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        onClick = {
                            onNavigateToPrompts()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Ver prompts guardados"
                        )
                    }
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        onClick = {
                            showDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Guardar prompt actual"
                        )
                    }
                }
            }

            Button(onClick = {
                prefs.edit { putString("prompt_app", textPrompt) }
                if (!android.provider.Settings.canDrawOverlays(context)) {
                    val intent = android.content.Intent(
                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        "package:${context.packageName}".toUri()
                    )
                    context.startActivity(intent)
                } else {
                    val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
                    screenCaptureLauncher.launch(captureIntent)
                }
            }) {
                Text("Iniciar Traductor")
            }
        }
    }
}