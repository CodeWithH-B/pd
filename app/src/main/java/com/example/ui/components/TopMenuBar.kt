package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.theme.StudioAccentBlue
import com.example.theme.StudioAccentCyan
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
fun TopMenuBar(
    uiState: PhotoCraftUiState,
    viewModel: PhotoCraftViewModel,
    modifier: Modifier = Modifier
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.loadFromUri(uri)
        }
    }

    var showPresetMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_menu_bar"),
        color = StudioSurface,
        tonalElevation = 4.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: App brand & Document selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showPresetMenu = true }
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(StudioAccentBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PC",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = uiState.document.title,
                            color = StudioTextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${uiState.document.width} × ${uiState.document.height} px • ${(uiState.zoomLevel * 100).toInt()}%",
                            color = StudioTextMuted,
                            fontSize = 10.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Preset Document",
                        tint = StudioTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )

                    DropdownMenu(
                        expanded = showPresetMenu,
                        onDismissRequest = { showPresetMenu = false },
                        modifier = Modifier.background(StudioSurfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text("The Great Wave (Hokusai)", color = StudioTextPrimary) },
                            onClick = {
                                viewModel.loadPresetSample("wave")
                                showPresetMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Impression, Sunrise (Monet)", color = StudioTextPrimary) },
                            onClick = {
                                viewModel.loadPresetSample("sunrise")
                                showPresetMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("New Blank Canvas (1080×1080)", color = StudioTextPrimary) },
                            onClick = {
                                viewModel.loadPresetSample("blank")
                                showPresetMenu = false
                            }
                        )
                    }
                }

                // Center & Right: Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Undo
                    val canUndo = uiState.historyIndex > 0
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = canUndo,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo",
                            tint = if (canUndo) StudioTextPrimary else StudioTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Redo
                    val canRedo = uiState.historyIndex < uiState.historyList.lastIndex
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = canRedo,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("redo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Redo,
                            contentDescription = "Redo",
                            tint = if (canRedo) StudioTextPrimary else StudioTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // History list dialog
                    IconButton(
                        onClick = { viewModel.setHistoryDialogVisible(true) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "History States",
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Zoom Fit / Reset
                    IconButton(
                        onClick = { viewModel.resetZoom() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("zoom_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Reset Zoom to 100%",
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Open / Import photo from gallery
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("import_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Import Photo",
                            tint = StudioAccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Export / Share
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StudioAccentBlue)
                            .clickable { viewModel.setExportDialogVisible(true) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("export_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export Image",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Export",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Status message toast banner if present
            uiState.statusMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StudioSurfaceHighlight)
                        .padding(horizontal = 12.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = msg,
                        color = StudioAccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
