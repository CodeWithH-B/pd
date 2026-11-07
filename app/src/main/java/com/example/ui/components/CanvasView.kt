package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.ImageProcessor
import com.example.model.LayerKind
import com.example.model.ToolType
import com.example.theme.CanvasCheckerboardDark
import com.example.theme.CanvasCheckerboardLight
import com.example.theme.StudioAccentBlue
import com.example.theme.StudioSurfaceHighlight
import com.example.viewmodel.PhotoCraftUiState
import com.example.viewmodel.PhotoCraftViewModel
import kotlin.math.max
import kotlin.math.min

@Composable
fun CanvasView(
    uiState: PhotoCraftUiState,
    viewModel: PhotoCraftViewModel,
    modifier: Modifier = Modifier
) {
    val doc = uiState.document
    val activeTool = uiState.activeTool

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("canvas_viewport"),
        contentAlignment = Alignment.Center
    ) {
        val viewportW = constraints.maxWidth.toFloat()
        val viewportH = constraints.maxHeight.toFloat()

        // Base scale to fit document inside viewport with padding
        val scaleFit = min(
            (viewportW - 48f) / max(1, doc.width),
            (viewportH - 48f) / max(1, doc.height)
        )
        val currentScale = scaleFit * uiState.zoomLevel

        val docDisplayW = doc.width * currentScale
        val docDisplayH = doc.height * currentScale

        val canvasOriginX = (viewportW - docDisplayW) / 2f + uiState.panOffsetX
        val canvasOriginY = (viewportH - docDisplayH) / 2f + uiState.panOffsetY

        // Gesture handling according to active tool
        val gestureModifier = when (activeTool) {
            ToolType.BRUSH, ToolType.ERASER -> {
                Modifier.pointerInput(doc.id, activeTool, currentScale, canvasOriginX, canvasOriginY) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val docX = (offset.x - canvasOriginX) / currentScale
                            val docY = (offset.y - canvasOriginY) / currentScale
                            viewModel.onDrawStart(docX, docY)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val docX = (change.position.x - canvasOriginX) / currentScale
                            val docY = (change.position.y - canvasOriginY) / currentScale
                            viewModel.onDrawMove(docX, docY)
                        },
                        onDragEnd = {
                            viewModel.onDrawEnd()
                        },
                        onDragCancel = {
                            viewModel.onDrawEnd()
                        }
                    )
                }
            }
            ToolType.MOVE -> {
                Modifier.pointerInput(doc.id, currentScale) {
                    detectDragGestures(
                        onDragStart = {},
                        onDrag = { change, dragAmount ->
                            change.consume()
                            viewModel.translateActiveLayer(
                                dragAmount.x / currentScale,
                                dragAmount.y / currentScale
                            )
                        },
                        onDragEnd = {
                            viewModel.finishTransform()
                        }
                    )
                }
            }
            ToolType.EYEDROPPER -> {
                Modifier.pointerInput(doc.id, currentScale, canvasOriginX, canvasOriginY) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val docX = ((offset.x - canvasOriginX) / currentScale).toInt()
                            val docY = ((offset.y - canvasOriginY) / currentScale).toInt()
                            viewModel.sampleColorAt(docX, docY)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val docX = ((change.position.x - canvasOriginX) / currentScale).toInt()
                            val docY = ((change.position.y - canvasOriginY) / currentScale).toInt()
                            viewModel.sampleColorAt(docX, docY)
                        }
                    )
                }
            }
            else -> {
                // Free pan & pinch zoom when not using drawing tools
                Modifier.pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        viewModel.panCanvas(pan.x, pan.y)
                        viewModel.updateZoom(zoom)
                    }
                }
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .then(gestureModifier)
                .testTag("drawing_canvas")
        ) {
            // 1. Draw checkerboard transparency pattern in document bounds
            val checkSize = 16f * currentScale.coerceIn(0.5f, 2f)
            val numCols = (docDisplayW / checkSize).toInt() + 1
            val numRows = (docDisplayH / checkSize).toInt() + 1

            for (r in 0 until numRows) {
                for (c in 0 until numCols) {
                    val isEven = (r + c) % 2 == 0
                    val rectX = canvasOriginX + c * checkSize
                    val rectY = canvasOriginY + r * checkSize
                    val w = min(checkSize, canvasOriginX + docDisplayW - rectX)
                    val h = min(checkSize, canvasOriginY + docDisplayH - rectY)
                    if (w > 0 && h > 0) {
                        drawRect(
                            color = if (isEven) CanvasCheckerboardLight else CanvasCheckerboardDark,
                            topLeft = Offset(rectX, rectY),
                            size = Size(w, h)
                        )
                    }
                }
            }

            // 2. Render Document Layers
            drawIntoCanvas { composeCanvas ->
                val nativeCanvas = composeCanvas.nativeCanvas
                nativeCanvas.save()
                nativeCanvas.translate(canvasOriginX, canvasOriginY)
                nativeCanvas.scale(currentScale, currentScale)

                // Clip to document boundary
                nativeCanvas.clipRect(0f, 0f, doc.width.toFloat(), doc.height.toFloat())

                // Draw composite image onto canvas
                val composite = ImageProcessor.renderComposite(doc)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                nativeCanvas.drawBitmap(composite, 0f, 0f, paint)

                nativeCanvas.restore()
            }

            // 3. Document Outer Border
            drawRect(
                color = Color(0xFF4A4E5A),
                topLeft = Offset(canvasOriginX, canvasOriginY),
                size = Size(docDisplayW, docDisplayH),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )

            // 4. Highlight Active Layer Boundary if in MOVE mode
            if (activeTool == ToolType.MOVE) {
                val activeLayer = doc.layers.find { it.id == doc.activeLayerId }
                if (activeLayer != null && activeLayer.isVisible) {
                    val layerScreenX = canvasOriginX + activeLayer.offsetX * currentScale
                    val layerScreenY = canvasOriginY + activeLayer.offsetY * currentScale
                    val layerW = when (activeLayer.kind) {
                        LayerKind.IMAGE -> (activeLayer.bitmap?.width ?: doc.width) * currentScale * activeLayer.scale
                        LayerKind.SHAPE -> (activeLayer.shapeConfig?.width ?: 240f) * currentScale
                        LayerKind.TEXT -> (doc.width * 0.8f) * currentScale
                        LayerKind.ADJUSTMENT -> docDisplayW
                    }
                    val layerH = when (activeLayer.kind) {
                        LayerKind.IMAGE -> (activeLayer.bitmap?.height ?: doc.height) * currentScale * activeLayer.scale
                        LayerKind.SHAPE -> (activeLayer.shapeConfig?.height ?: 160f) * currentScale
                        LayerKind.TEXT -> (activeLayer.textConfig?.fontSize ?: 36f) * 2f * currentScale
                        LayerKind.ADJUSTMENT -> docDisplayH
                    }

                    drawRect(
                        color = StudioAccentBlue,
                        topLeft = Offset(layerScreenX, layerScreenY),
                        size = Size(layerW, layerH),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 3f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    )

                    // Corner handles
                    val handleSize = 14f
                    val corners = listOf(
                        Offset(layerScreenX, layerScreenY),
                        Offset(layerScreenX + layerW, layerScreenY),
                        Offset(layerScreenX, layerScreenY + layerH),
                        Offset(layerScreenX + layerW, layerScreenY + layerH)
                    )
                    corners.forEach { pos ->
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(pos.x - handleSize / 2, pos.y - handleSize / 2),
                            size = Size(handleSize, handleSize)
                        )
                        drawRect(
                            color = StudioAccentBlue,
                            topLeft = Offset(pos.x - handleSize / 2, pos.y - handleSize / 2),
                            size = Size(handleSize, handleSize),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                        )
                    }
                }
            }
        }

        // Eyedropper Color Preview Badge if active
        if (activeTool == ToolType.EYEDROPPER) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(StudioSurfaceHighlight)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Pick Color: Tap canvas",
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}
