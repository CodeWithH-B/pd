package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AdjustmentParams
import com.example.model.LayerKind
import com.example.theme.StudioDarkBg
import com.example.ui.components.AdjustmentDialog
import com.example.ui.components.CanvasView
import com.example.ui.components.ColorPickerDialog
import com.example.ui.components.ExportDialog
import com.example.ui.components.HistoryDialog
import com.example.ui.components.LayersSheet
import com.example.ui.components.ToolBar
import com.example.ui.components.ToolOptionsBar
import com.example.ui.components.TopMenuBar
import com.example.viewmodel.PhotoCraftViewModel

@Composable
fun PhotoCraftApp(viewModel: PhotoCraftViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    // Back handling to dismiss overlays
    BackHandler(enabled = uiState.isLayersSheetOpen) {
        viewModel.setLayersSheetOpen(false)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .testTag("photocraft_app_scaffold"),
        containerColor = StudioDarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isExpandedScreen = maxWidth >= 700.dp

            if (isExpandedScreen) {
                // Tablet / Expanded screen layout with side layers pane
                Column(modifier = Modifier.fillMaxSize()) {
                    TopMenuBar(uiState = uiState, viewModel = viewModel)
                    ToolOptionsBar(uiState = uiState, viewModel = viewModel)

                    Row(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            CanvasView(uiState = uiState, viewModel = viewModel)
                        }

                        // Side panel for Layers
                        Box(
                            modifier = Modifier
                                .width(320.dp)
                                .fillMaxHeight()
                        ) {
                            LayersSheet(uiState = uiState, viewModel = viewModel)
                        }
                    }

                    ToolBar(uiState = uiState, viewModel = viewModel)
                }
            } else {
                // Mobile compact layout
                Column(modifier = Modifier.fillMaxSize()) {
                    TopMenuBar(uiState = uiState, viewModel = viewModel)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        CanvasView(uiState = uiState, viewModel = viewModel)

                        // Floating / Slide-up Layers Bottom Sheet
                        if (uiState.isLayersSheetOpen) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                            ) {
                                LayersSheet(uiState = uiState, viewModel = viewModel)
                            }
                        }
                    }

                    ToolOptionsBar(uiState = uiState, viewModel = viewModel)
                    ToolBar(uiState = uiState, viewModel = viewModel)
                }
            }

            // Dialogs
            if (uiState.isAdjustDialogVisible) {
                val active = viewModel.getActiveLayer()
                val initialParams = active?.adjustmentParams ?: AdjustmentParams()
                AdjustmentDialog(
                    initialParams = initialParams,
                    viewModel = viewModel,
                    onDismiss = { viewModel.setAdjustDialogVisible(false) }
                )
            }

            if (uiState.isColorPickerVisible) {
                ColorPickerDialog(
                    initialColor = uiState.brushColor,
                    target = uiState.colorPickerTarget,
                    viewModel = viewModel,
                    onDismiss = { viewModel.setColorPickerVisible(false) }
                )
            }

            if (uiState.isExportDialogVisible) {
                ExportDialog(
                    uiState = uiState,
                    viewModel = viewModel,
                    onDismiss = { viewModel.setExportDialogVisible(false) }
                )
            }

            if (uiState.isHistoryDialogVisible) {
                HistoryDialog(
                    uiState = uiState,
                    viewModel = viewModel,
                    onDismiss = { viewModel.setHistoryDialogVisible(false) }
                )
            }
        }
    }
}
