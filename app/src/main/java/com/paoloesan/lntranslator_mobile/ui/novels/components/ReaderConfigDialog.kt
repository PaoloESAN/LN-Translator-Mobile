package com.paoloesan.lntranslator_mobile.ui.novels.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.paoloesan.lntranslator_mobile.ui.overlay.OverlayFontOption
import com.paoloesan.lntranslator_mobile.ui.overlay.toComposeFontFamily
import com.paoloesan.lntranslator_mobile.ui.overlay.toLabel
import com.paoloesan.lntranslator_mobile.ui.strings.UiStrings

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ReaderConfigDialog(
    fontSize: Int,
    lineSpacing: Int,
    fontFamily: OverlayFontOption,
    showOriginal: Boolean,
    onFontSizeChange: (Int) -> Unit,
    onLineSpacingChange: (Int) -> Unit,
    onFontFamilyChange: (OverlayFontOption) -> Unit,
    onShowOriginalChange: (Boolean) -> Unit,
    invertHorizontalNav: Boolean,
    onInvertHorizontalNavChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    strings: UiStrings
) {
    var showFontFamilyOptions by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = strings.overlayConfig,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = strings.configPreviewText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize + lineSpacing).sp,
                    fontFamily = fontFamily.toComposeFontFamily(),
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFontFamilyOptions = true }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = strings.configFontFamilyLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = fontFamily.toLabel(strings),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    DropdownMenuPopup(
                        expanded = showFontFamilyOptions,
                        onDismissRequest = { showFontFamilyOptions = false },
                        modifier = Modifier.width(200.dp)
                    ) {
                        DropdownMenuGroup(
                            shapes = MenuDefaults.groupShape(0, 1)
                        ) {
                            OverlayFontOption.entries.forEachIndexed { index, option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.toLabel(strings),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    onClick = {
                                        onFontFamilyChange(option)
                                        showFontFamilyOptions = false
                                    },
                                    selected = option == fontFamily,
                                    selectedLeadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = Color.Transparent
                                        )
                                    },
                                    shapes = MenuDefaults.itemShape(
                                        index,
                                        OverlayFontOption.entries.size
                                    ),
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.configFontSizeLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledIconButton(
                            shape = CircleShape,
                            onClick = {
                                if (fontSize > 10) {
                                    onFontSizeChange(fontSize - 1)
                                }
                            },
                            enabled = fontSize > 10,
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                Icons.Rounded.Remove,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "$fontSize",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.Center
                        )

                        FilledIconButton(
                            shape = CircleShape,
                            onClick = {
                                if (fontSize < 30) {
                                    onFontSizeChange(fontSize + 1)
                                }
                            },
                            enabled = fontSize < 30,
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = strings.configLineSpacingLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledIconButton(
                            shape = CircleShape,
                            onClick = {
                                if (lineSpacing > 0) {
                                    onLineSpacingChange(lineSpacing - 1)
                                }
                            },
                            enabled = lineSpacing > 0,
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                Icons.Rounded.Remove,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "$lineSpacing",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(32.dp),
                            textAlign = TextAlign.Center
                        )

                        FilledIconButton(
                            shape = CircleShape,
                            onClick = {
                                if (lineSpacing < 20) {
                                    onLineSpacingChange(lineSpacing + 1)
                                }
                            },
                            enabled = lineSpacing < 20,
                            modifier = Modifier.size(36.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Show/Hide Original Content Switch Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onShowOriginalChange(!showOriginal) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (showOriginal) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            strings.readerShowOriginalContent,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = showOriginal,
                        onCheckedChange = onShowOriginalChange,
                        thumbContent = {
                            if (showOriginal) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        }
                    )
                }

                // Invert Horizontal Navigation Switch Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onInvertHorizontalNavChange(!invertHorizontalNav) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            strings.readerInvertNavigation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Switch(
                        checked = invertHorizontalNav,
                        onCheckedChange = onInvertHorizontalNavChange,
                        thumbContent = {
                            if (invertHorizontalNav) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(SwitchDefaults.IconSize)
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = strings.buttonClose,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
