package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Colorize
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ToolType
import com.example.theme.StudioAccentBlue
import com.example.theme.StudioAccentCyan
import com.example.theme.StudioBorder
import com.example.theme.StudioSurface
import com.example.theme.StudioSurfaceHighlight
import com.example.theme.StudioTextMuted
import com.example.theme.StudioTextPrimary
import com.example.theme.StudioTextSecondary
import com.example.viewmodel.PhotoCraftUiState
import com.example.viewmodel.PhotoCraftViewModel

data class ToolItem(
    val type: ToolType,
    val label: String,
    val icon: ImageVector
)

val PrimaryTools = listOf(
    ToolItem(ToolType.MOVE, "Move", Icons.Default.OpenWith),
    ToolItem(ToolType.BRUSH, "Brush", Icons.Default.Brush),
    ToolItem(ToolType.ERASER, "Eraser", Icons.Default.Brush), // styled differently
    ToolItem(ToolType.SHAPE, "Shapes", Icons.Default.Category),
    ToolItem(ToolType.TEXT, "Type", Icons.Default.Title),
    ToolItem(ToolType.ADJUST, "Adjust", Icons.Default.Tune),
    ToolItem(ToolType.FILTER, "Filter", Icons.Default.AutoAwesome),
    ToolItem(ToolType.CROP, "Crop", Icons.Default.Crop),
    ToolItem(ToolType.EYEDROPPER, "Pick", Icons.Default.Colorize)
)

@Composable
fun ToolBar(
    uiState: PhotoCraftUiState,
    viewModel: PhotoCraftViewModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tool_bar"),
        color = StudioSurface,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Scrollable tool palette
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                PrimaryTools.forEach { tool ->
                    val isSelected = uiState.activeTool == tool.type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) StudioAccentBlue else Color.Transparent)
                            .clickable { viewModel.selectTool(tool.type) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("tool_button_${tool.type.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = tool.label,
                                tint = if (isSelected) Color.White else StudioTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = tool.label,
                                color = if (isSelected) Color.White else StudioTextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(32.dp)
                    .background(StudioBorder)
            )
            Spacer(modifier = Modifier.width(4.dp))

            // Layers Toggle Button with badge showing count of layers
            val layersCount = uiState.document.layers.size
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (uiState.isLayersSheetOpen) StudioSurfaceHighlight else Color.Transparent)
                    .clickable { viewModel.setLayersSheetOpen(!uiState.isLayersSheetOpen) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("toggle_layers_sheet_button"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    BadgedBox(
                        badge = {
                            Badge(containerColor = StudioAccentCyan, contentColor = Color.Black) {
                                Text(text = "$layersCount", fontSize = 9.sp)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Layers Panel",
                            tint = if (uiState.isLayersSheetOpen) StudioAccentCyan else StudioTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Layers",
                        color = if (uiState.isLayersSheetOpen) StudioAccentCyan else StudioTextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
