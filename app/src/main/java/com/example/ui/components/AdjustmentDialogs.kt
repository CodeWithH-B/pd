package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AdjustmentParams
import com.example.model.AdjustmentType
import com.example.theme.StudioAccentBlue
import com.example.theme.StudioAccentCyan
import com.example.theme.StudioSurface
import com.example.theme.StudioSurfaceElevated
import com.example.theme.StudioSurfaceHighlight
import com.example.theme.StudioTextMuted
import com.example.theme.StudioTextPrimary
import com.example.theme.StudioTextSecondary
import com.example.viewmodel.PhotoCraftViewModel

@Composable
fun AdjustmentDialog(
    initialParams: AdjustmentParams,
    viewModel: PhotoCraftViewModel,
    onDismiss: () -> Unit
) {
    var params by remember { mutableStateOf(initialParams) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("adjustment_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = params.type.label,
                    color = StudioTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                when (params.type) {
                    AdjustmentType.BRIGHTNESS_CONTRAST -> {
                        AdjustmentSliderRow(
                            label = "Brightness",
                            value = params.brightness,
                            range = -100f..100f,
                            onValueChange = {
                                params = params.copy(brightness = it)
                                viewModel.updateActiveLayerAdjustment(params)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AdjustmentSliderRow(
                            label = "Contrast",
                            value = params.contrast,
                            range = -100f..100f,
                            onValueChange = {
                                params = params.copy(contrast = it)
                                viewModel.updateActiveLayerAdjustment(params)
                            }
                        )
                    }

                    AdjustmentType.HUE_SATURATION -> {
                        AdjustmentSliderRow(
                            label = "Hue",
                            value = params.hue,
                            range = -180f..180f,
                            onValueChange = {
                                params = params.copy(hue = it)
                                viewModel.updateActiveLayerAdjustment(params)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AdjustmentSliderRow(
                            label = "Saturation",
                            value = params.saturation,
                            range = -100f..100f,
                            onValueChange = {
                                params = params.copy(saturation = it)
                                viewModel.updateActiveLayerAdjustment(params)
                            }
                        )
                    }

                    AdjustmentType.LEVELS -> {
                        // Simulated histogram visualizer bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(StudioSurfaceHighlight)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Shadows", color = StudioTextMuted, fontSize = 10.sp)
                                Text("Midtones (Gamma)", color = StudioTextMuted, fontSize = 10.sp)
                                Text("Highlights", color = StudioTextMuted, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        AdjustmentSliderRow(
                            label = "Black Point (Shadows)",
                            value = params.blackPoint,
                            range = 0f..255f,
                            onValueChange = {
                                params = params.copy(blackPoint = it)
                                viewModel.updateActiveLayerAdjustment(params)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AdjustmentSliderRow(
                            label = "White Point (Highlights)",
                            value = params.whitePoint,
                            range = 0f..255f,
                            onValueChange = {
                                params = params.copy(whitePoint = it)
                                viewModel.updateActiveLayerAdjustment(params)
                            }
                        )
                    }

                    AdjustmentType.VIBRANCE -> {
                        AdjustmentSliderRow(
                            label = "Vibrance",
                            value = params.vibrance,
                            range = -100f..100f,
                            onValueChange = {
                                params = params.copy(vibrance = it)
                                viewModel.updateActiveLayerAdjustment(params)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AdjustmentSliderRow(
                            label = "Saturation",
                            value = params.saturation,
                            range = -100f..100f,
                            onValueChange = {
                                params = params.copy(saturation = it)
                                viewModel.updateActiveLayerAdjustment(params)
                            }
                        )
                    }

                    AdjustmentType.COLOR_BALANCE -> {
                        AdjustmentSliderRow(
                            label = "Cyan - Red",
                            value = params.redShift,
                            range = -100f..100f,
                            onValueChange = {
                                params = params.copy(redShift = it)
                                viewModel.updateActiveLayerAdjustment(params)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AdjustmentSliderRow(
                            label = "Magenta - Green",
                            value = params.greenShift,
                            range = -100f..100f,
                            onValueChange = {
                                params = params.copy(greenShift = it)
                                viewModel.updateActiveLayerAdjustment(params)
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AdjustmentSliderRow(
                            label = "Yellow - Blue",
                            value = params.blueShift,
                            range = -100f..100f,
                            onValueChange = {
                                params = params.copy(blueShift = it)
                                viewModel.updateActiveLayerAdjustment(params)
                            }
                        )
                    }

                    AdjustmentType.BLACK_AND_WHITE -> {
                        Text(
                            text = "Converts image to monochrome black & white with calibrated luminance weighting.",
                            color = StudioTextSecondary,
                            fontSize = 13.sp
                        )
                    }

                    AdjustmentType.INVERT -> {
                        Text(
                            text = "Inverts color values (255 - RGB). Creates a photographic negative effect.",
                            color = StudioTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_adjustment_button")
                    ) {
                        Text("Cancel", color = StudioTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.commitAdjustment() },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioAccentBlue),
                        modifier = Modifier.testTag("apply_adjustment_button")
                    ) {
                        Text("Apply", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AdjustmentSliderRow(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = StudioTextSecondary, fontSize = 12.sp)
            Text("${value.toInt()}", color = StudioAccentCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = StudioAccentBlue,
                activeTrackColor = StudioAccentBlue
            )
        )
    }
}
