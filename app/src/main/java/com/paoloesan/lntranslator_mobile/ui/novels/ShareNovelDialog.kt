package com.paoloesan.lntranslator_mobile.ui.novels

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DriveFolderUpload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.paoloesan.lntranslator_mobile.ui.strings.UiStrings

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShareNovelDialog(
    novelName: String,
    strings: UiStrings,
    onDismiss: () -> Unit,
    onExportZip: () -> Unit,
    onExportPdf: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.shareDialogTitle) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(strings.shareDialogMessage)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.Center)
                ) {
                    SplitButtonLayout(
                        leadingButton = {
                            SplitButtonDefaults.LeadingButton(
                                onClick = {
                                    onExportPdf()
                                }
                            ) {
                                Icon(
                                    Icons.Default.Book,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(strings.shareDialogPdf)
                            }
                        },
                        trailingButton = {
                            SplitButtonDefaults.TrailingButton(
                                checked = isMenuExpanded,
                                onCheckedChange = { isMenuExpanded = it },
                            ) {
                                val rotation by animateFloatAsState(
                                    targetValue = if (isMenuExpanded) 180f else 0f,
                                    label = "Trailing Icon Rotation"
                                )
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    modifier = Modifier.graphicsLayer {
                                        rotationZ = rotation
                                    },
                                    contentDescription = null
                                )
                            }
                            DropdownMenuPopup(
                                expanded = isMenuExpanded,
                                onDismissRequest = { isMenuExpanded = false }
                            ) {
                                DropdownMenuGroup(
                                    shapes = MenuDefaults.groupShape(0, 1)
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(strings.shareDialogDownload) },
                                        onClick = {
                                            isMenuExpanded = false
                                            onExportZip()
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.DriveFolderUpload,
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
        },
        confirmButton = {}
    )
}
