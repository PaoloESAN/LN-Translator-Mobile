package com.paoloesan.lntranslator_mobile.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paoloesan.lntranslator_mobile.LocalStrings

@Composable
fun ConfigOverlayContent(
    currentFontSize: Int,
    currentLineSpacing: Int,
    invertGestures: Boolean,
    bottomPassThroughEnabled: Boolean,
    currentFontFamily: OverlayFontOption,
    onFontSizeChange: (Int) -> Unit,
    onLineSpacingChange: (Int) -> Unit,
    onInvertGesturesChange: (Boolean) -> Unit,
    onBottomPassThroughChange: (Boolean) -> Unit,
    onFontFamilyChange: (OverlayFontOption) -> Unit,
    onClose: () -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalStrings.current
    val scrollState = rememberScrollState()
    var showFontFamilyOptions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TEXTO
        Text(
            text = strings.configPreviewText,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = currentFontSize.sp,
            lineHeight = (currentFontSize + currentLineSpacing).sp,
            fontFamily = currentFontFamily.toComposeFontFamily(),
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // FAMILIA DE FUENTE
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .clickable { showFontFamilyOptions = !showFontFamilyOptions }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = strings.configFontFamilyLabel,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = currentFontFamily.toLabel(),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .padding(start = 12.dp)
                )
            }

            if (showFontFamilyOptions) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    OverlayFontOption.entries.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onFontFamilyChange(option)
                                    showFontFamilyOptions = false
                                }
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = option == currentFontFamily,
                                onClick = {
                                    onFontFamilyChange(option)
                                    showFontFamilyOptions = false
                                }
                            )

                            Text(
                                text = option.toLabel(),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // CONTROLES DE TAMAÑO DE FUENTE
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = strings.configFontSizeLabel,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (currentFontSize > 10) {
                            onFontSizeChange(currentFontSize - 1)
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        ),
                    enabled = currentFontSize > 10
                ) {
                    Icon(
                        Icons.Rounded.Remove,
                        contentDescription = strings.overlayDecreaseFont,
                        tint = if (currentFontSize > 10) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.38f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "$currentFontSize",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = {
                        if (currentFontSize < 30) {
                            onFontSizeChange(currentFontSize + 1)
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        ),
                    enabled = currentFontSize < 30
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = strings.overlayIncreaseFont,
                        tint = if (currentFontSize < 30) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.38f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ESPACIO ENTRE LÍNEAS
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = strings.configLineSpacingLabel,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (currentLineSpacing > 0) {
                            onLineSpacingChange(currentLineSpacing - 1)
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        ),
                    enabled = currentLineSpacing > 0
                ) {
                    Icon(
                        Icons.Rounded.Remove,
                        contentDescription = strings.overlayDecreaseFont,
                        tint = if (currentLineSpacing > 0) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.38f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "$currentLineSpacing",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = {
                        if (currentLineSpacing < 20) {
                            onLineSpacingChange(currentLineSpacing + 1)
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            RoundedCornerShape(8.dp)
                        ),
                    enabled = currentLineSpacing < 20
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = strings.overlayIncreaseFont,
                        tint = if (currentLineSpacing < 20) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.38f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // INVERTIR GESTOS
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = strings.configInvertGestures,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = invertGestures,
                onCheckedChange = onInvertGesturesChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // DEJAR ZONA TACTIL ABAJO
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = strings.configBottomTouchSpace,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = bottomPassThroughEnabled,
                onCheckedChange = onBottomPassThroughChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Buttons at the bottom
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = strings.configBack,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Button(
                onClick = onClose,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = strings.configClose,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}