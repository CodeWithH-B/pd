package com.example.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.engine.ImageProcessor
import com.example.model.AdjustmentParams
import com.example.model.AdjustmentType
import com.example.model.CropRatio
import com.example.model.FilterParams
import com.example.model.FilterType
import com.example.model.HistoryEntry
import com.example.model.Layer
import com.example.model.LayerBlendMode
import com.example.model.LayerKind
import com.example.model.ProjectDocument
import com.example.model.ShapeConfig
import com.example.model.ShapeType
import com.example.model.TextConfig
import com.example.model.ToolType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID
import kotlin.math.max

data class PhotoCraftUiState(
    val document: ProjectDocument,
    val activeTool: ToolType = ToolType.BRUSH,
    // Brush
    val brushColor: Long = 0xFF2F80ED,
    val brushSize: Float = 18f,
    val brushOpacity: Float = 1.0f,
    // Shape
    val activeShapeConfig: ShapeConfig = ShapeConfig(),
    // Text
    val activeTextConfig: TextConfig = TextConfig(),
    // Crop
    val activeCropRatio: CropRatio = CropRatio.FREE,
    // Eyedropper
    val pickedColor: Long = 0xFF2F80ED,
    // Zoom & pan
    val zoomLevel: Float = 1.0f,
    val panOffsetX: Float = 0f,
    val panOffsetY: Float = 0f,
    // History
    val historyList: List<HistoryEntry> = emptyList(),
    val historyIndex: Int = -1,
    // Dialog visibility
    val isLayersSheetOpen: Boolean = false,
    val isAdjustDialogVisible: Boolean = false,
    val isFilterDialogVisible: Boolean = false,
    val isColorPickerVisible: Boolean = false,
    val isExportDialogVisible: Boolean = false,
    val isHistoryDialogVisible: Boolean = false,
    val isNewDocDialogVisible: Boolean = false,
    val colorPickerTarget: ColorPickerTarget = ColorPickerTarget.BRUSH,
    val statusMessage: String? = null,
    val isExporting: Boolean = false
)

enum class ColorPickerTarget {
    BRUSH,
    SHAPE_FILL,
    SHAPE_STROKE,
    TEXT
}

class PhotoCraftViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<PhotoCraftUiState> = _uiState.asStateFlow()

    // Interactive drawing stroke path
    private var currentStrokePath = Path()
    private var lastX = 0f
    private var lastY = 0f

    init {
        loadPresetSample("wave")
    }

    private fun createInitialState(): PhotoCraftUiState {
        val defaultDoc = ProjectDocument(
            id = UUID.randomUUID().toString(),
            title = "Untitled-1",
            width = 1080,
            height = 1080,
            layers = emptyList(),
            activeLayerId = ""
        )
        return PhotoCraftUiState(document = defaultDoc)
    }

    fun loadPresetSample(preset: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val context = getApplication<Application>()
            val resId = when (preset) {
                "wave" -> R.drawable.sample_wave_1794067622166
                "sunrise" -> R.drawable.sample_sunrise_1794067638460
                else -> null
            }

            val doc = if (resId != null) {
                val opts = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                val bmp = BitmapFactory.decodeResource(context.resources, resId, opts)
                val baseLayer = Layer(
                    id = "layer_base",
                    name = if (preset == "wave") "The Great Wave" else "Impression Sunrise",
                    kind = LayerKind.IMAGE,
                    bitmap = bmp.copy(Bitmap.Config.ARGB_8888, true)
                )

                val badgeLayer = Layer(
                    id = "layer_badge",
                    name = "Lotus Badge",
                    kind = LayerKind.SHAPE,
                    shapeConfig = ShapeConfig(
                        type = ShapeType.ROUNDED_RECT,
                        fillColor = 0x881E2028,
                        strokeColor = 0xFF00C0FF,
                        strokeWidth = 3f,
                        width = 440f,
                        height = 100f,
                        cornerRadius = 50f
                    ),
                    offsetY = 380f
                )

                val textLayer = Layer(
                    id = "layer_text",
                    name = "Title Type",
                    kind = LayerKind.TEXT,
                    textConfig = TextConfig(
                        text = if (preset == "wave") "PHOTOCRAFT • WAVE" else "PHOTOCRAFT • SUNRISE",
                        textColor = 0xFFFFFFFF,
                        fontSize = 28f,
                        isBold = true
                    ),
                    offsetY = 380f
                )

                ProjectDocument(
                    id = UUID.randomUUID().toString(),
                    title = if (preset == "wave") "Great Wave.psd" else "Impression Sunrise.psd",
                    width = bmp.width,
                    height = bmp.height,
                    layers = listOf(baseLayer, badgeLayer, textLayer),
                    activeLayerId = baseLayer.id
                )
            } else {
                // Blank canvas
                val blankBmp = Bitmap.createBitmap(1080, 1080, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(blankBmp)
                canvas.drawColor(Color.WHITE)
                val baseLayer = Layer(
                    id = "layer_background",
                    name = "Background",
                    kind = LayerKind.IMAGE,
                    bitmap = blankBmp
                )
                ProjectDocument(
                    id = UUID.randomUUID().toString(),
                    title = "New Canvas.psd",
                    width = 1080,
                    height = 1080,
                    layers = listOf(baseLayer),
                    activeLayerId = baseLayer.id
                )
            }

            withContext(Dispatchers.Main) {
                _uiState.update { state ->
                    val historyEntry = HistoryEntry(
                        id = UUID.randomUUID().toString(),
                        actionName = "Open ${doc.title}",
                        documentSnapshot = doc
                    )
                    state.copy(
                        document = doc,
                        historyList = listOf(historyEntry),
                        historyIndex = 0,
                        zoomLevel = 1.0f,
                        panOffsetX = 0f,
                        panOffsetY = 0f,
                        statusMessage = "Loaded ${doc.title}"
                    )
                }
            }
        }
    }

    fun loadFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) {
                        val baseLayer = Layer(
                            id = UUID.randomUUID().toString(),
                            name = "Imported Image",
                            kind = LayerKind.IMAGE,
                            bitmap = bmp.copy(Bitmap.Config.ARGB_8888, true)
                        )
                        val doc = ProjectDocument(
                            id = UUID.randomUUID().toString(),
                            title = "Imported-${System.currentTimeMillis() % 1000}.psd",
                            width = bmp.width,
                            height = bmp.height,
                            layers = listOf(baseLayer),
                            activeLayerId = baseLayer.id
                        )
                        withContext(Dispatchers.Main) {
                            pushHistoryState("Import Image", doc)
                            _uiState.update {
                                it.copy(
                                    document = doc,
                                    zoomLevel = 1.0f,
                                    statusMessage = "Opened image (${doc.width}x${doc.height})"
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Tool selection
    fun selectTool(tool: ToolType) {
        _uiState.update { it.copy(activeTool = tool) }
    }

    // Brush settings
    fun updateBrushColor(color: Long) {
        _uiState.update { it.copy(brushColor = color) }
    }

    fun updateBrushSize(size: Float) {
        _uiState.update { it.copy(brushSize = size) }
    }

    fun updateBrushOpacity(opacity: Float) {
        _uiState.update { it.copy(brushOpacity = opacity) }
    }

    // Interactive Drawing on active layer bitmap
    fun onDrawStart(docX: Float, docY: Float) {
        val activeLayer = getActiveLayer() ?: return
        if (activeLayer.isLocked || !activeLayer.isVisible || activeLayer.kind != LayerKind.IMAGE) return

        currentStrokePath.reset()
        currentStrokePath.moveTo(docX - activeLayer.offsetX, docY - activeLayer.offsetY)
        lastX = docX
        lastY = docY
    }

    fun onDrawMove(docX: Float, docY: Float) {
        val state = _uiState.value
        val activeLayer = getActiveLayer() ?: return
        if (activeLayer.isLocked || !activeLayer.isVisible || activeLayer.kind != LayerKind.IMAGE) return
        val bmp = activeLayer.bitmap ?: return

        val localX = docX - activeLayer.offsetX
        val localY = docY - activeLayer.offsetY

        currentStrokePath.quadTo(
            lastX - activeLayer.offsetX,
            lastY - activeLayer.offsetY,
            (localX + lastX - activeLayer.offsetX) / 2f,
            (localY + lastY - activeLayer.offsetY) / 2f
        )
        lastX = docX
        lastY = docY

        // Draw onto bitmap
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            strokeWidth = state.brushSize
        }

        if (state.activeTool == ToolType.ERASER) {
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        } else {
            paint.color = state.brushColor.toInt()
            paint.alpha = (state.brushOpacity * 255).toInt()
        }

        canvas.drawPath(currentStrokePath, paint)
        _uiState.update { it.copy(document = it.document.copy()) } // trigger recomposition
    }

    fun onDrawEnd() {
        val activeLayer = getActiveLayer() ?: return
        if (activeLayer.isLocked || !activeLayer.isVisible || activeLayer.kind != LayerKind.IMAGE) return

        pushHistoryState(
            if (_uiState.value.activeTool == ToolType.ERASER) "Eraser Stroke" else "Brush Stroke",
            _uiState.value.document
        )
    }

    // Move / Transform active layer
    fun translateActiveLayer(deltaX: Float, deltaY: Float) {
        val activeLayer = getActiveLayer() ?: return
        if (activeLayer.isLocked) return

        val updatedLayers = _uiState.value.document.layers.map { layer ->
            if (layer.id == activeLayer.id) {
                layer.copy(
                    offsetX = layer.offsetX + deltaX,
                    offsetY = layer.offsetY + deltaY
                )
            } else layer
        }
        _uiState.update {
            it.copy(document = it.document.copy(layers = updatedLayers))
        }
    }

    fun finishTransform() {
        pushHistoryState("Transform Layer", _uiState.value.document)
    }

    // Color picker
    fun sampleColorAt(docX: Int, docY: Int) {
        val doc = _uiState.value.document
        if (docX < 0 || docX >= doc.width || docY < 0 || docY >= doc.height) return

        viewModelScope.launch(Dispatchers.Default) {
            val composite = ImageProcessor.renderComposite(doc)
            val pixel = composite.getPixel(docX.coerceIn(0, doc.width - 1), docY.coerceIn(0, doc.height - 1))
            val colorLong = (pixel.toLong() and 0xFFFFFFFFL)
            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        pickedColor = colorLong,
                        brushColor = colorLong,
                        statusMessage = "Picked color #${java.lang.Long.toHexString(colorLong).uppercase()}"
                    )
                }
            }
        }
    }

    // Layer management
    fun addEmptyLayer() {
        val doc = _uiState.value.document
        val newBmp = Bitmap.createBitmap(doc.width, doc.height, Bitmap.Config.ARGB_8888)
        val newLayer = Layer(
            id = UUID.randomUUID().toString(),
            name = "Layer ${doc.layers.size + 1}",
            kind = LayerKind.IMAGE,
            bitmap = newBmp
        )
        val newLayers = doc.layers + newLayer
        val newDoc = doc.copy(layers = newLayers, activeLayerId = newLayer.id)
        pushHistoryState("New Layer", newDoc)
        _uiState.update { it.copy(document = newDoc, statusMessage = "Created ${newLayer.name}") }
    }

    fun addShapeLayer(type: ShapeType) {
        val doc = _uiState.value.document
        val state = _uiState.value
        val shapeConfig = state.activeShapeConfig.copy(type = type)
        val newLayer = Layer(
            id = UUID.randomUUID().toString(),
            name = "${type.label} ${doc.layers.size + 1}",
            kind = LayerKind.SHAPE,
            shapeConfig = shapeConfig
        )
        val newDoc = doc.copy(layers = doc.layers + newLayer, activeLayerId = newLayer.id)
        pushHistoryState("Add ${type.label}", newDoc)
        _uiState.update { it.copy(document = newDoc, statusMessage = "Added ${newLayer.name}") }
    }

    fun addTextLayer(text: String) {
        val doc = _uiState.value.document
        val state = _uiState.value
        val textConfig = state.activeTextConfig.copy(text = text)
        val newLayer = Layer(
            id = UUID.randomUUID().toString(),
            name = "Type \"${text.take(12)}\"",
            kind = LayerKind.TEXT,
            textConfig = textConfig
        )
        val newDoc = doc.copy(layers = doc.layers + newLayer, activeLayerId = newLayer.id)
        pushHistoryState("Add Text Layer", newDoc)
        _uiState.update { it.copy(document = newDoc, statusMessage = "Added Text Layer") }
    }

    fun addAdjustmentLayer(type: AdjustmentType) {
        val doc = _uiState.value.document
        val adjParams = AdjustmentParams(type = type)
        val newLayer = Layer(
            id = UUID.randomUUID().toString(),
            name = "${type.label} Adj",
            kind = LayerKind.ADJUSTMENT,
            adjustmentParams = adjParams
        )
        val newDoc = doc.copy(layers = doc.layers + newLayer, activeLayerId = newLayer.id)
        pushHistoryState("New ${type.label} Layer", newDoc)
        _uiState.update {
            it.copy(
                document = newDoc,
                isAdjustDialogVisible = true,
                statusMessage = "Added ${newLayer.name}"
            )
        }
    }

    fun duplicateActiveLayer() {
        val doc = _uiState.value.document
        val active = getActiveLayer() ?: return
        val copyBmp = active.bitmap?.copy(Bitmap.Config.ARGB_8888, true)
        val newLayer = active.copy(
            id = UUID.randomUUID().toString(),
            name = "${active.name} copy",
            bitmap = copyBmp,
            offsetX = active.offsetX + 20f,
            offsetY = active.offsetY + 20f
        )
        val index = doc.layers.indexOfFirst { it.id == active.id }
        val newLayers = doc.layers.toMutableList().apply { add(index + 1, newLayer) }
        val newDoc = doc.copy(layers = newLayers, activeLayerId = newLayer.id)
        pushHistoryState("Duplicate ${active.name}", newDoc)
        _uiState.update { it.copy(document = newDoc, statusMessage = "Duplicated ${active.name}") }
    }

    fun deleteActiveLayer() {
        val doc = _uiState.value.document
        if (doc.layers.size <= 1) {
            _uiState.update { it.copy(statusMessage = "Cannot delete only remaining layer") }
            return
        }
        val active = getActiveLayer() ?: return
        val newLayers = doc.layers.filter { it.id != active.id }
        val nextActiveId = newLayers.lastOrNull()?.id ?: ""
        val newDoc = doc.copy(layers = newLayers, activeLayerId = nextActiveId)
        pushHistoryState("Delete ${active.name}", newDoc)
        _uiState.update { it.copy(document = newDoc, statusMessage = "Deleted ${active.name}") }
    }

    fun moveActiveLayerUp() {
        val doc = _uiState.value.document
        val active = getActiveLayer() ?: return
        val index = doc.layers.indexOfFirst { it.id == active.id }
        if (index < doc.layers.lastIndex) {
            val newLayers = doc.layers.toMutableList().apply {
                val item = removeAt(index)
                add(index + 1, item)
            }
            val newDoc = doc.copy(layers = newLayers)
            pushHistoryState("Raise Layer", newDoc)
            _uiState.update { it.copy(document = newDoc) }
        }
    }

    fun moveActiveLayerDown() {
        val doc = _uiState.value.document
        val active = getActiveLayer() ?: return
        val index = doc.layers.indexOfFirst { it.id == active.id }
        if (index > 0) {
            val newLayers = doc.layers.toMutableList().apply {
                val item = removeAt(index)
                add(index - 1, item)
            }
            val newDoc = doc.copy(layers = newLayers)
            pushHistoryState("Lower Layer", newDoc)
            _uiState.update { it.copy(document = newDoc) }
        }
    }

    fun mergeDown() {
        val doc = _uiState.value.document
        val active = getActiveLayer() ?: return
        val index = doc.layers.indexOfFirst { it.id == active.id }
        if (index <= 0) return

        viewModelScope.launch(Dispatchers.Default) {
            val lower = doc.layers[index - 1]
            // Merge active and lower into a single bitmap
            val subDoc = doc.copy(layers = listOf(lower, active))
            val mergedBmp = ImageProcessor.renderComposite(subDoc)
            val mergedLayer = Layer(
                id = lower.id,
                name = "${lower.name} merged",
                kind = LayerKind.IMAGE,
                bitmap = mergedBmp
            )
            val newLayers = doc.layers.toMutableList().apply {
                removeAt(index)
                set(index - 1, mergedLayer)
            }
            val newDoc = doc.copy(layers = newLayers, activeLayerId = mergedLayer.id)
            withContext(Dispatchers.Main) {
                pushHistoryState("Merge Down", newDoc)
                _uiState.update { it.copy(document = newDoc, statusMessage = "Merged layers") }
            }
        }
    }

    fun flattenImage() {
        val doc = _uiState.value.document
        if (doc.layers.size <= 1) return

        viewModelScope.launch(Dispatchers.Default) {
            val composite = ImageProcessor.renderComposite(doc)
            val flattened = Layer(
                id = UUID.randomUUID().toString(),
                name = "Background",
                kind = LayerKind.IMAGE,
                bitmap = composite
            )
            val newDoc = doc.copy(layers = listOf(flattened), activeLayerId = flattened.id)
            withContext(Dispatchers.Main) {
                pushHistoryState("Flatten Image", newDoc)
                _uiState.update { it.copy(document = newDoc, statusMessage = "Flattened document") }
            }
        }
    }

    fun setActiveLayer(id: String) {
        _uiState.update { it.copy(document = it.document.copy(activeLayerId = id)) }
    }

    fun toggleLayerVisibility(id: String) {
        val doc = _uiState.value.document
        val updated = doc.layers.map { if (it.id == id) it.copy(isVisible = !it.isVisible) else it }
        _uiState.update { it.copy(document = doc.copy(layers = updated)) }
    }

    fun toggleLayerLock(id: String) {
        val doc = _uiState.value.document
        val updated = doc.layers.map { if (it.id == id) it.copy(isLocked = !it.isLocked) else it }
        _uiState.update { it.copy(document = doc.copy(layers = updated)) }
    }

    fun setLayerOpacity(id: String, opacity: Float) {
        val doc = _uiState.value.document
        val updated = doc.layers.map { if (it.id == id) it.copy(opacity = opacity) else it }
        _uiState.update { it.copy(document = doc.copy(layers = updated)) }
    }

    fun setLayerBlendMode(id: String, mode: LayerBlendMode) {
        val doc = _uiState.value.document
        val updated = doc.layers.map { if (it.id == id) it.copy(blendMode = mode) else it }
        _uiState.update { it.copy(document = doc.copy(layers = updated)) }
    }

    // Adjustment updating
    fun updateActiveLayerAdjustment(params: AdjustmentParams) {
        val doc = _uiState.value.document
        val active = getActiveLayer() ?: return
        val updated = doc.layers.map {
            if (it.id == active.id) it.copy(adjustmentParams = params) else it
        }
        _uiState.update { it.copy(document = doc.copy(layers = updated)) }
    }

    fun commitAdjustment() {
        pushHistoryState("Adjust ${getActiveLayer()?.name ?: "Layer"}", _uiState.value.document)
        _uiState.update { it.copy(isAdjustDialogVisible = false) }
    }

    // Filter applying
    fun applyFilterToActiveLayer(filter: FilterParams) {
        val doc = _uiState.value.document
        val active = getActiveLayer() ?: return
        if (active.bitmap == null) return

        viewModelScope.launch(Dispatchers.Default) {
            val filteredBmp = ImageProcessor.applyFilterToBitmap(active.bitmap, filter)
            val updated = doc.layers.map {
                if (it.id == active.id) it.copy(bitmap = filteredBmp, filterParams = filter) else it
            }
            val newDoc = doc.copy(layers = updated)
            withContext(Dispatchers.Main) {
                pushHistoryState("Filter ${filter.type.label}", newDoc)
                _uiState.update {
                    it.copy(
                        document = newDoc,
                        isFilterDialogVisible = false,
                        statusMessage = "Applied ${filter.type.label}"
                    )
                }
            }
        }
    }

    // Crop Canvas
    fun applyCrop(ratio: CropRatio) {
        val doc = _uiState.value.document
        val targetRatio = ratio.ratio ?: return

        viewModelScope.launch(Dispatchers.Default) {
            var newW = doc.width
            var newH = (newW / targetRatio).toInt()
            if (newH > doc.height) {
                newH = doc.height
                newW = (newH * targetRatio).toInt()
            }

            val cropLeft = (doc.width - newW) / 2
            val cropTop = (doc.height - newH) / 2

            val updatedLayers = doc.layers.map { layer ->
                if (layer.bitmap != null) {
                    val croppedBmp = Bitmap.createBitmap(layer.bitmap, cropLeft.coerceIn(0, doc.width - newW), cropTop.coerceIn(0, doc.height - newH), newW, newH)
                    layer.copy(bitmap = croppedBmp)
                } else {
                    layer.copy(offsetX = layer.offsetX - cropLeft, offsetY = layer.offsetY - cropTop)
                }
            }

            val newDoc = doc.copy(width = newW, height = newH, layers = updatedLayers)
            withContext(Dispatchers.Main) {
                pushHistoryState("Crop (${ratio.label})", newDoc)
                _uiState.update {
                    it.copy(
                        document = newDoc,
                        statusMessage = "Cropped to ${ratio.label} ($newW x $newH)"
                    )
                }
            }
        }
    }

    // Undo / Redo
    fun undo() {
        val state = _uiState.value
        if (state.historyIndex > 0) {
            val nextIndex = state.historyIndex - 1
            val entry = state.historyList[nextIndex]
            _uiState.update {
                it.copy(
                    document = entry.documentSnapshot,
                    historyIndex = nextIndex,
                    statusMessage = "Undo: ${entry.actionName}"
                )
            }
        }
    }

    fun redo() {
        val state = _uiState.value
        if (state.historyIndex < state.historyList.lastIndex) {
            val nextIndex = state.historyIndex + 1
            val entry = state.historyList[nextIndex]
            _uiState.update {
                it.copy(
                    document = entry.documentSnapshot,
                    historyIndex = nextIndex,
                    statusMessage = "Redo: ${entry.actionName}"
                )
            }
        }
    }

    fun jumpToHistory(index: Int) {
        val state = _uiState.value
        if (index in state.historyList.indices) {
            val entry = state.historyList[index]
            _uiState.update {
                it.copy(
                    document = entry.documentSnapshot,
                    historyIndex = index,
                    statusMessage = "Restored: ${entry.actionName}",
                    isHistoryDialogVisible = false
                )
            }
        }
    }

    private fun pushHistoryState(action: String, doc: ProjectDocument) {
        val state = _uiState.value
        val truncated = state.historyList.take(state.historyIndex + 1)
        val entry = HistoryEntry(
            id = UUID.randomUUID().toString(),
            actionName = action,
            documentSnapshot = doc
        )
        val newList = (truncated + entry).takeLast(30)
        _uiState.update {
            it.copy(
                historyList = newList,
                historyIndex = newList.lastIndex
            )
        }
    }

    // Zoom and pan
    fun updateZoom(scaleMultiplier: Float) {
        _uiState.update {
            it.copy(zoomLevel = (it.zoomLevel * scaleMultiplier).coerceIn(0.2f, 5.0f))
        }
    }

    fun resetZoom() {
        _uiState.update { it.copy(zoomLevel = 1.0f, panOffsetX = 0f, panOffsetY = 0f) }
    }

    fun panCanvas(dx: Float, dy: Float) {
        _uiState.update {
            it.copy(
                panOffsetX = it.panOffsetX + dx,
                panOffsetY = it.panOffsetY + dy
            )
        }
    }

    // Exporting
    fun exportImage(format: String, quality: Int, shareAfterSave: Boolean) {
        _uiState.update { it.copy(isExporting = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val doc = _uiState.value.document
            val composite = ImageProcessor.renderComposite(doc)

            val compressFormat = when (format.uppercase()) {
                "PNG" -> Bitmap.CompressFormat.PNG
                "WEBP" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
                else -> Bitmap.CompressFormat.JPEG
            }
            val ext = when (format.uppercase()) {
                "PNG" -> "png"
                "WEBP" -> "webp"
                else -> "jpg"
            }
            val filename = "PhotoCraft_${System.currentTimeMillis()}.$ext"

            var savedUri: Uri? = null

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/$ext")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/PhotoCraft")
                    }
                    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            composite.compress(compressFormat, quality, out)
                        }
                        savedUri = uri
                    }
                } else {
                    val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "PhotoCraft")
                    if (!dir.exists()) dir.mkdirs()
                    val file = File(dir, filename)
                    FileOutputStream(file).use { out ->
                        composite.compress(compressFormat, quality, out)
                    }
                    savedUri = Uri.fromFile(file)
                }

                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            isExportDialogVisible = false,
                            statusMessage = "Exported $filename to Pictures/PhotoCraft"
                        )
                    }

                    if (shareAfterSave && savedUri != null) {
                        shareImageUri(context, savedUri)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            statusMessage = "Export failed: ${e.message}"
                        )
                    }
                }
            }
        }
    }

    private fun shareImageUri(context: Context, uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share with PhotoCraft").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    // Dialog toggles
    fun setLayersSheetOpen(open: Boolean) = _uiState.update { it.copy(isLayersSheetOpen = open) }
    fun setAdjustDialogVisible(visible: Boolean) = _uiState.update { it.copy(isAdjustDialogVisible = visible) }
    fun setFilterDialogVisible(visible: Boolean) = _uiState.update { it.copy(isFilterDialogVisible = visible) }
    fun setColorPickerVisible(visible: Boolean, target: ColorPickerTarget = ColorPickerTarget.BRUSH) =
        _uiState.update { it.copy(isColorPickerVisible = visible, colorPickerTarget = target) }
    fun setExportDialogVisible(visible: Boolean) = _uiState.update { it.copy(isExportDialogVisible = visible) }
    fun setHistoryDialogVisible(visible: Boolean) = _uiState.update { it.copy(isHistoryDialogVisible = visible) }
    fun setNewDocDialogVisible(visible: Boolean) = _uiState.update { it.copy(isNewDocDialogVisible = visible) }
    fun clearStatusMessage() = _uiState.update { it.copy(statusMessage = null) }

    fun getActiveLayer(): Layer? {
        val doc = _uiState.value.document
        return doc.layers.find { it.id == doc.activeLayerId } ?: doc.layers.lastOrNull()
    }
}
