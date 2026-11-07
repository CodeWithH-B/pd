package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdjustmentType
import com.example.model.CropRatio
import com.example.model.FilterParams
import com.example.model.FilterType
import com.example.model.ShapeType
import com.example.model.ToolType
import com.example.theme.StudioAccentBlue
import com.example.theme.StudioAccentCyan
import com.example.theme.StudioBorder
import com.example.theme.StudioSurfaceElevated
import com.example.theme.StudioSurfaceHighlight
import com.example.theme.StudioTextMuted
import com.example.theme.StudioTextPrimary
import com.example.theme.StudioTextSecondary
import com.example.viewmodel.ColorPickerTarget
import com.example.viewmodel.PhotoCraftUiState
import com.example.viewmodel.PhotoCraftViewModel

@Composable
fun ToolOptionsBar(
    uiState: PhotoCraftUiState,
    viewModel: PhotoCraftViewModel,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tool_options_bar"),
        color = StudioSurfaceElevated,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (uiState.activeTool) {
                ToolType.BRUSH -> {
                    // Color button
                    ColorThumbnailButton(
                        color = Color(uiState.brushColor),
                        label = "Color",
                        onClick = { viewModel.setColorPickerVisible(true, ColorPickerTarget.BRUSH) },
                        testTag = "brush_color_picker_button"
                    )

                    // Size slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Size: ${uiState.brushSize.toInt()}px", color = StudioTextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Slider(
                            value = uiState.brushSize,
                            onValueChange = { viewModel.updateBrushSize(it) },
                            valueRange = 2f..100f,
                            modifier = Modifier
                                .width(120.dp)
                                .testTag("brush_size_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = StudioAccentBlue,
                                activeTrackColor = StudioAccentBlue
                            )
                        )
                    }

                    // Opacity slider
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Opacity: ${(uiState.brushOpacity * 100).toInt()}%", color = StudioTextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Slider(
                            value = uiState.brushOpacity,
                            onValueChange = { viewModel.updateBrushOpacity(it) },
                            valueRange = 0.05f..1.0f,
                            modifier = Modifier
                                .width(100.dp)
                                .testTag("brush_opacity_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = StudioAccentBlue,
                                activeTrackColor = StudioAccentBlue
                            )
                        )
                    }
                }

                ToolType.ERASER -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Eraser Size: ${uiState.brushSize.toInt()}px", color = StudioTextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Slider(
                            value = uiState.brushSize,
                            onValueChange = { viewModel.updateBrushSize(it) },
                            valueRange = 4f..120f,
                            modifier = Modifier
                                .width(160.dp)
                                .testTag("eraser_size_slider"),
                            colors = SliderDefaults.colors(
                                thumbColor = StudioAccentCyan,
                                activeTrackColor = StudioAccentCyan
                            )
                        )
                    }
                }

                ToolType.SHAPE -> {
                    ShapeType.values().forEach { type ->
                        OptionChip(
                            label = type.label,
                            selected = false,
                            onClick = { viewModel.addShapeLayer(type) }
                        )
                    }
                }

                ToolType.TEXT -> {
                    var textInput by remember { mutableStateOf("New Headline") }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioSurfaceHighlight)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        BasicTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                            cursorBrush = SolidColor(StudioAccentBlue),
                            modifier = Modifier
                                .width(140.dp)
                                .testTag("text_input_field")
                        )
                    }

                    Button(
                        onClick = { viewModel.addTextLayer(textInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioAccentBlue),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("add_text_layer_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Type", fontSize = 11.sp)
                    }
                }

                ToolType.ADJUST -> {
                    AdjustmentType.values().forEach { adjType ->
                        OptionChip(
                            label = "+ ${adjType.label}",
                            selected = false,
                            onClick = { viewModel.addAdjustmentLayer(adjType) }
                        )
                    }
                }

                ToolType.FILTER -> {
                    FilterType.values().filter { it != FilterType.NONE }.forEach { filterType ->
                        OptionChip(
                            label = filterType.label,
                            selected = false,
                            onClick = {
                                viewModel.applyFilterToActiveLayer(
                                    FilterParams(type = filterType, radius = 12f, intensity = 60f)
                                )
                            }
                        )
                    }
                }

                ToolType.CROP -> {
                    CropRatio.values().forEach { crop ->
                        OptionChip(
                            label = crop.label,
                            selected = uiState.activeCropRatio == crop,
                            onClick = { viewModel.applyCrop(crop) }
                        )
                    }
                }

                ToolType.MOVE -> {
                    Text(
                        text = "Drag layer on canvas to reposition • Bounding box handles active",
                        color = StudioTextSecondary,
                        fontSize = 11.sp
                    )
                }

                ToolType.EYEDROPPER -> {
                    ColorThumbnailButton(
                        color = Color(uiState.pickedColor),
                        label = "Sampled",
                        onClick = {},
                        testTag = "eyedropper_color_preview"
                    )
                    Text(
                        text = "Tap any pixel on canvas to sample color into brush",
                        color = StudioTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OptionChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) StudioAccentBlue else StudioSurfaceHighlight)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else StudioTextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ColorThumbnailButton(
    color: Color,
    label: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(StudioSurfaceHighlight)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, Color.White, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = StudioTextPrimary, fontSize = 11.sp)
    }
}
