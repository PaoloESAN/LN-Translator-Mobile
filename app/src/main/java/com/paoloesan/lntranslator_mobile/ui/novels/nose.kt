package com.paoloesan.lntranslator_mobile.ui.novels

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.util.fastForEachIndexed

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun hola() {
    val groupInteractionSource = remember { MutableInteractionSource() }
    var expanded by remember { mutableStateOf(false) }
    val groupLabels = listOf("Modification", "Navigation")
    val groupItemLabels = listOf(listOf("Edit", "Settings"), listOf("Home", "More Options"))
    val groupItemLeadingIcons =
        listOf(
            listOf(Icons.Outlined.Edit, Icons.Outlined.Settings),
            listOf(null, Icons.Outlined.Info),
        )
    val groupItemCheckedLeadingIcons =
        listOf(
            listOf(Icons.Filled.Edit, Icons.Filled.Settings),
            listOf(Icons.Filled.Check, Icons.Filled.Info),
        )
    val groupItemTrailingIcons: List<List<ImageVector?>> =
        listOf(listOf(null, null), listOf(Icons.Outlined.Home, Icons.Outlined.MoreVert))
    val groupItemCheckedTrailingIcons: List<List<ImageVector?>> =
        listOf(listOf(null, null), listOf(Icons.Filled.Home, Icons.Filled.MoreVert))
    val groupItemSupportingText: List<List<String?>> =
        listOf(listOf("Edit mode", null), listOf(null, "Opens menu"))
    val checked = remember {
        listOf(mutableStateListOf(false, false), mutableStateListOf(false, false))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopStart)
    ) {
        // Icon button should have a tooltip associated with it for a11y.
        TooltipBox(
            positionProvider =
                TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = { PlainTooltip { Text("Localized description") } },
            state = rememberTooltipState(),
        ) {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Localized description")
            }
        }
        DropdownMenuPopup(expanded = expanded, onDismissRequest = { expanded = false }) {
            val groupCount = groupLabels.size
            groupLabels.fastForEachIndexed { groupIndex, label ->
                DropdownMenuGroup(
                    shapes = MenuDefaults.groupShape(groupIndex, groupCount),
                    interactionSource = groupInteractionSource,
                ) {
                    MenuDefaults.Label { Text(label) }
                    HorizontalDivider(
                        modifier = Modifier.padding(MenuDefaults.HorizontalDividerPadding)
                    )
                    val groupItemCount = groupItemLabels[groupIndex].size
                    groupItemLabels[groupIndex].fastForEachIndexed { itemIndex, itemLabel ->
                        DropdownMenuItem(
                            text = { Text(itemLabel) },
                            supportingText =
                                groupItemSupportingText[groupIndex][itemIndex]?.let { supportingText
                                    ->
                                    { Text(supportingText) }
                                },
                            shapes = MenuDefaults.itemShape(itemIndex, groupItemCount),
                            leadingIcon =
                                groupItemLeadingIcons[groupIndex][itemIndex]?.let { iconData ->
                                    {
                                        Icon(
                                            iconData,
                                            modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                            contentDescription = null,
                                        )
                                    }
                                },
                            checkedLeadingIcon = {
                                Icon(
                                    groupItemCheckedLeadingIcons[groupIndex][itemIndex],
                                    modifier = Modifier.size(MenuDefaults.LeadingIconSize),
                                    contentDescription = null,
                                )
                            },
                            trailingIcon =
                                if (checked[groupIndex][itemIndex]) {
                                    groupItemCheckedTrailingIcons[groupIndex][itemIndex]?.let { iconData ->
                                        {
                                            Icon(
                                                iconData,
                                                modifier =
                                                    Modifier.size(MenuDefaults.TrailingIconSize),
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                } else {
                                    groupItemTrailingIcons[groupIndex][itemIndex]?.let { iconData ->
                                        {
                                            Icon(
                                                iconData,
                                                modifier =
                                                    Modifier.size(MenuDefaults.TrailingIconSize),
                                                contentDescription = null,
                                            )
                                        }
                                    }
                                },
                            checked = checked[groupIndex][itemIndex],
                            onCheckedChange = { checked[groupIndex][itemIndex] = it },
                        )
                    }
                }

                if (groupIndex != groupCount - 1) {
                    Spacer(Modifier.height(MenuDefaults.GroupSpacing))
                }
            }
        }
    }
}
