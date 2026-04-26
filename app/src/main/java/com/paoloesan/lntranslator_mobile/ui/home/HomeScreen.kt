package com.paoloesan.lntranslator_mobile.ui.home

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.service.OverlayService
import com.paoloesan.lntranslator_mobile.service.ScreenCaptureService
import com.paoloesan.lntranslator_mobile.ui.prompts.PromptDialog
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    onNavigateToPrompts: () -> Unit
) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    val prefs = context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE)
    var textPrompt by rememberSaveable { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    val puedeGuardarPrompt = textPrompt.trim().isNotBlank()
    val navigateInteraction = remember { MutableInteractionSource() }
    val saveInteraction = remember { MutableInteractionSource() }
    val navigatePressed by navigateInteraction.collectIsPressedAsState()
    val savePressed by saveInteraction.collectIsPressedAsState()
    var highlightedButton by remember { mutableStateOf<Int?>(null) }
    var isNavigatingToPrompts by remember { mutableStateOf(false) }

    val navigateWeight by animateFloatAsState(
        targetValue = when (highlightedButton) {
            0 -> 1.15f
            1 -> 0.85f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 180),
        label = "navigateWeight"
    )
    val saveWeight by animateFloatAsState(
        targetValue = when (highlightedButton) {
            0 -> 0.85f
            1 -> 1.15f
            else -> 1f
        },
        animationSpec = tween(durationMillis = 180),
        label = "saveWeight"
    )

    LaunchedEffect(navigatePressed, savePressed) {
        when {
            navigatePressed -> highlightedButton = 0
            savePressed -> highlightedButton = 1
            highlightedButton != null -> {
                delay(220)
                highlightedButton = null
            }
        }
    }

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
            Toast.makeText(context, strings.homePermissionDenied, Toast.LENGTH_SHORT).show()
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
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
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
                text = "\"${strings.homeWelcome}\"",
                fontStyle = FontStyle.Italic
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier
                            .weight(navigateWeight)
                            .heightIn(min = 56.dp),
                        interactionSource = navigateInteraction,
                        shape = MaterialTheme.shapes.large,
                        enabled = !isNavigatingToPrompts,
                        onClick = {
                            if (!isNavigatingToPrompts) {
                                isNavigatingToPrompts = true
                                onNavigateToPrompts()
                            }
                        },
                    ) {
                        Text(strings.homeViewPrompts)
                    }
                    FilledTonalButton(
                        modifier = Modifier
                            .weight(saveWeight)
                            .heightIn(min = 56.dp),
                        interactionSource = saveInteraction,
                        shape = MaterialTheme.shapes.large,
                        enabled = puedeGuardarPrompt,
                        onClick = { showDialog = true },
                    ) {
                        Text(strings.homeSavePrompt)
                    }
                }

                OutlinedTextField(
                    label = { Text(text = strings.homePromptLabel) },
                    value = textPrompt,
                    onValueChange = { textPrompt = it },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                shape = MaterialTheme.shapes.extraLarge,
                contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
                colors = ButtonDefaults.buttonColors(),
                onClick = {
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
                Text(strings.homeStartButton)
            }
        }
    }
}