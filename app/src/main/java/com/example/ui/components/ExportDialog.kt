package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.theme.StudioAccentBlue
import com.example.theme.StudioAccentCyan
import com.example.theme.StudioSurface
import com.example.theme.StudioSurfaceElevated
import com.example.theme.StudioSurfaceHighlight
import com.example.theme.StudioTextMuted
import com.example.theme.StudioTextPrimary
import com.example.theme.StudioTextSecondary
import com.example.viewmodel.PhotoCraftUiState
import com.example.viewmodel.PhotoCraftViewModel

@Composable
fun ExportDialog(
    uiState: PhotoCraftUiState,
    viewModel: PhotoCraftViewModel,
    onDismiss: () -> Unit
) {
    var selectedFormat by remember { mutableStateOf("PNG") }
    var quality by remember { mutableIntStateOf(90) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("export_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Export Image",
                    color = StudioTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Dimensions
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(StudioSurfaceElevated)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Resolution", color = StudioTextSecondary, fontSize = 11.sp)
                            Text(
                                "${uiState.document.width} × ${uiState.document.height} px",
                                color = StudioTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Layers", color = StudioTextSecondary, fontSize = 11.sp)
                            Text(
                                "${uiState.document.layers.size} visible",
                                color = StudioAccentCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Format Selector
                Text("Format", color = StudioTextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("PNG", "JPEG", "WEBP").forEach { fmt ->
                        val isSelected = selectedFormat == fmt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) StudioAccentBlue else StudioSurfaceHighlight)
                                .clickable { selectedFormat = fmt }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = fmt,
                                color = if (isSelected) Color.White else StudioTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Quality slider for JPEG and WEBP
                if (selectedFormat != "PNG") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Quality", color = StudioTextSecondary, fontSize = 12.sp)
                        Text("$quality%", color = StudioAccentCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Slider(
                        value = quality.toFloat(),
                        onValueChange = { quality = it.toInt() },
                        valueRange = 10f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = StudioAccentBlue,
                            activeTrackColor = StudioAccentBlue
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.isExporting) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = StudioAccentBlue)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Rendering & saving...", color = StudioTextSecondary, fontSize = 13.sp)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = StudioTextSecondary)
                        }

                        Button(
                            onClick = {
                                viewModel.exportImage(selectedFormat, quality, shareAfterSave = true)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioSurfaceHighlight),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_image_button")
                        ) {
                            Text("Share", color = StudioAccentCyan)
                        }

                        Button(
                            onClick = {
                                viewModel.exportImage(selectedFormat, quality, shareAfterSave = false)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StudioAccentBlue),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_image_button")
                        ) {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
