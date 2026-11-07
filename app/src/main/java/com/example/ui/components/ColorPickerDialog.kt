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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.theme.StudioAccentBlue
import com.example.theme.StudioAccentRed
import com.example.theme.StudioBorder
import com.example.theme.StudioSurface
import com.example.theme.StudioSurfaceElevated
import com.example.theme.StudioTextPrimary
import com.example.theme.StudioTextSecondary
import com.example.viewmodel.ColorPickerTarget
import com.example.viewmodel.PhotoCraftViewModel

val PaletteColors = listOf(
    0xFF000000L, 0xFFFFFFFFL, 0xFF2F80EDL, 0xFF00C0FFL,
    0xFF34C759L, 0xFFFFCC00L, 0xFFFF9500L, 0xFFFF3B30L,
    0xFFAF52DEL, 0xFF5856D6L, 0xFFFF2D55L, 0xFFA2845EL,
    0xFF8E8E93L, 0xFF636366L, 0xFF48484AL, 0xFF1C1C1EL
)

@Composable
fun ColorPickerDialog(
    initialColor: Long,
    target: ColorPickerTarget,
    viewModel: PhotoCraftViewModel,
    onDismiss: () -> Unit
) {
    var r by remember { mutableStateOf(((initialColor shr 16) and 0xFF).toFloat()) }
    var g by remember { mutableStateOf(((initialColor shr 8) and 0xFF).toFloat()) }
    var b by remember { mutableStateOf((initialColor and 0xFF).toFloat()) }

    val currentColorLong = 0xFF000000L or (r.toLong() shl 16) or (g.toLong() shl 8) or b.toLong()
    val currentColor = Color(currentColorLong)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("color_picker_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = StudioSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Select Color",
                    color = StudioTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Color Preview & Hex
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(currentColor)
                            .border(2.dp, StudioBorder, RoundedCornerShape(10.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "#${String.format("%06X", currentColorLong and 0xFFFFFFL)}",
                            color = StudioTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "RGB(${r.toInt()}, ${g.toInt()}, ${b.toInt()})",
                            color = StudioTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Palette Grid
                Text("Swatches", color = StudioTextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(PaletteColors) { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(colorHex))
                                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                .clickable {
                                    r = ((colorHex shr 16) and 0xFF).toFloat()
                                    g = ((colorHex shr 8) and 0xFF).toFloat()
                                    b = (colorHex and 0xFF).toFloat()
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // RGB Sliders
                ColorSlider(label = "R", value = r, activeColor = StudioAccentRed) { r = it }
                ColorSlider(label = "G", value = g, activeColor = Color(0xFF34C759)) { g = it }
                ColorSlider(label = "B", value = b, activeColor = StudioAccentBlue) { b = it }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel", color = StudioTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            when (target) {
                                ColorPickerTarget.BRUSH -> viewModel.updateBrushColor(currentColorLong)
                                else -> viewModel.updateBrushColor(currentColorLong)
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StudioAccentBlue),
                        modifier = Modifier.testTag("apply_color_button")
                    ) {
                        Text("Apply", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun ColorSlider(
    label: String,
    value: Float,
    activeColor: Color,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = activeColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.width(20.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..255f,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = activeColor,
                activeTrackColor = activeColor
            )
        )
        Text(
            text = "${value.toInt()}",
            color = StudioTextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.width(32.dp)
        )
    }
}
