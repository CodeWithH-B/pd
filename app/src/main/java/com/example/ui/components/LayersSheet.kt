package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Layer
import com.example.model.LayerBlendMode
import com.example.model.LayerKind
import com.example.theme.StudioAccentBlue
import com.example.theme.StudioAccentCyan
import com.example.theme.StudioAccentRed
import com.example.theme.StudioBorder
import com.example.theme.StudioSurface
import com.example.theme.StudioSurfaceElevated
import com.example.theme.StudioSurfaceHighlight
import com.example.theme.StudioTextMuted
import com.example.theme.StudioTextPrimary
import com.example.theme.StudioTextSecondary
import com.example.viewmodel.PhotoCraftUiState
import com.example.viewmodel.PhotoCraftViewModel

@Composable
fun LayersSheet(
    uiState: PhotoCraftUiState,
    viewModel: PhotoCraftViewModel,
    modifier: Modifier = Modifier
) {
    val activeLayer = viewModel.getActiveLayer()
    var showBlendMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("layers_sheet_panel"),
        color = StudioSurface,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Layers",
                        color = StudioTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${uiState.document.layers.size})",
                        color = StudioTextMuted,
                        fontSize = 13.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Add new layer
                    IconButton(
                        onClick = { viewModel.addEmptyLayer() },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("add_layer_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Layer", tint = StudioAccentCyan, modifier = Modifier.size(18.dp))
                    }
                    // Duplicate
                    IconButton(
                        onClick = { viewModel.duplicateActiveLayer() },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("duplicate_layer_button")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate Layer", tint = StudioTextSecondary, modifier = Modifier.size(18.dp))
                    }
                    // Merge down
                    IconButton(
                        onClick = { viewModel.mergeDown() },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("merge_down_button")
                    ) {
                        Icon(Icons.Default.CallMerge, contentDescription = "Merge Down", tint = StudioTextSecondary, modifier = Modifier.size(18.dp))
                    }
                    // Flatten
                    IconButton(
                        onClick = { viewModel.flattenImage() },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("flatten_image_button")
                    ) {
                        Icon(Icons.Default.LayersClear, contentDescription = "Flatten Image", tint = StudioTextSecondary, modifier = Modifier.size(18.dp))
                    }
                    // Delete
                    IconButton(
                        onClick = { viewModel.deleteActiveLayer() },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_layer_button")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Layer", tint = StudioAccentRed, modifier = Modifier.size(18.dp))
                    }
                    // Close panel
                    IconButton(
                        onClick = { viewModel.setLayersSheetOpen(false) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_layers_sheet_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = StudioTextMuted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Active Layer Controls (Blend Mode & Opacity)
            if (activeLayer != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioSurfaceElevated)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Blend mode dropdown
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StudioSurfaceHighlight)
                                .clickable { showBlendMenu = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeLayer.blendMode.label,
                                color = StudioAccentCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = StudioTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showBlendMenu,
                            onDismissRequest = { showBlendMenu = false },
                            modifier = Modifier.background(StudioSurfaceElevated)
                        ) {
                            LayerBlendMode.values().forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.label, color = StudioTextPrimary) },
                                    onClick = {
                                        viewModel.setLayerBlendMode(activeLayer.id, mode)
                                        showBlendMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Opacity slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Opacity ${(activeLayer.opacity * 100).toInt()}%",
                            color = StudioTextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Slider(
                            value = activeLayer.opacity,
                            onValueChange = { viewModel.setLayerOpacity(activeLayer.id, it) },
                            valueRange = 0f..1f,
                            modifier = Modifier
                                .width(120.dp)
                                .testTag("layer_opacity_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = StudioAccentBlue,
                                activeTrackColor = StudioAccentBlue
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Layers List (displayed top-to-bottom, matching visual stacking order)
            val reversedLayers = uiState.document.layers.reversed()
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(reversedLayers) { _, layer ->
                    val isSelected = layer.id == uiState.document.activeLayerId
                    LayerItemRow(
                        layer = layer,
                        isSelected = isSelected,
                        onSelect = { viewModel.setActiveLayer(layer.id) },
                        onToggleVisibility = { viewModel.toggleLayerVisibility(layer.id) },
                        onToggleLock = { viewModel.toggleLayerLock(layer.id) },
                        onMoveUp = { viewModel.moveActiveLayerUp() },
                        onMoveDown = { viewModel.moveActiveLayerDown() }
                    )
                }
            }
        }
    }
}

@Composable
fun LayerItemRow(
    layer: Layer,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onToggleVisibility: () -> Unit,
    onToggleLock: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    val bg = if (isSelected) StudioSurfaceHighlight else StudioSurfaceElevated
    val borderCol = if (isSelected) StudioAccentBlue else StudioBorder

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("layer_item_${layer.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Visibility Eye Toggle
        IconButton(
            onClick = onToggleVisibility,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = "Toggle Visibility",
                tint = if (layer.isVisible) StudioTextPrimary else StudioTextMuted,
                modifier = Modifier.size(16.dp)
            )
        }

        // Lock Toggle
        IconButton(
            onClick = onToggleLock,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = "Toggle Lock",
                tint = if (layer.isLocked) StudioAccentCyan else StudioTextMuted,
                modifier = Modifier.size(14.dp)
            )
        }

        // Layer Kind Icon
        val kindIcon: ImageVector = when (layer.kind) {
            LayerKind.IMAGE -> Icons.Default.Image
            LayerKind.SHAPE -> Icons.Default.Category
            LayerKind.TEXT -> Icons.Default.Title
            LayerKind.ADJUSTMENT -> Icons.Default.Tune
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(StudioSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = kindIcon,
                contentDescription = null,
                tint = StudioAccentBlue,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Layer Name & Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = layer.name,
                color = StudioTextPrimary,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${layer.kind.name.lowercase().replaceFirstChar { it.uppercase() }} • ${(layer.opacity * 100).toInt()}% • ${layer.blendMode.label}",
                color = StudioTextMuted,
                fontSize = 10.sp
            )
        }

        // Reorder arrows (if active)
        if (isSelected) {
            IconButton(onClick = onMoveUp, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", tint = StudioTextSecondary, modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onMoveDown, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", tint = StudioTextSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}
