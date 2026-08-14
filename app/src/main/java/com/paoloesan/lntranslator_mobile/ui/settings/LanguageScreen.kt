package com.paoloesan.lntranslator_mobile.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paoloesan.lntranslator_mobile.LocalStrings
import com.paoloesan.lntranslator_mobile.LocalTopAppBarVisible
import com.paoloesan.lntranslator_mobile.data.DataStoreManager
import com.paoloesan.lntranslator_mobile.ui.components.CollapsingHeaderScaffold

import com.paoloesan.lntranslator_mobile.ui.strings.AppLanguages

@Composable
fun LanguageScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    val topBarVisible = LocalTopAppBarVisible.current

    DisposableEffect(Unit) {
        topBarVisible.value = false
        onDispose {
            topBarVisible.value = true
        }
    }

    var seleccionActual by remember {
        mutableStateOf(
            DataStoreManager.getString(context, "idioma_app", AppLanguages.SYSTEM) ?: AppLanguages.SYSTEM
        )
    }

    fun seleccionarIdioma(code: String) {
        seleccionActual = code
        DataStoreManager.putStringSync(context, "idioma_app", code)
    }

    val isSystemSelected = seleccionActual == AppLanguages.SYSTEM ||
            seleccionActual.equals("Predeterminado del sistema", ignoreCase = true) ||
            seleccionActual.equals("System default", ignoreCase = true)

    CollapsingHeaderScaffold(
        title = strings.settingsLanguageTitle,
        onBack = onBack,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Language Selection Section
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = strings.settingsLanguageTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp)
                        .padding(top = 4.dp, bottom = 8.dp),
                )

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // System default option
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { seleccionarIdioma(AppLanguages.SYSTEM) }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            RadioButton(
                                selected = isSystemSelected,
                                onClick = { seleccionarIdioma(AppLanguages.SYSTEM) }
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = strings.languageFollowSystem,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                    fontWeight = if (isSystemSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = strings.languageFollowSystemDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )

                        // Data-driven Language list
                        val languages = AppLanguages.supportedLanguages

                        languages.forEachIndexed { index, lang ->
                            val isSelected = !isSystemSelected && (
                                seleccionActual == lang.code ||
                                seleccionActual.equals(lang.nativeName, ignoreCase = true)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { seleccionarIdioma(lang.code) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { seleccionarIdioma(lang.code) }
                                )
                                Text(
                                    text = lang.nativeName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (index < languages.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                    modifier = Modifier.padding(start = 56.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
