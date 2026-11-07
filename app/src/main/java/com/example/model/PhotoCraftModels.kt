package com.example.model

import android.graphics.Bitmap
import androidx.compose.ui.graphics.BlendMode

enum class LayerKind {
    IMAGE,
    SHAPE,
    TEXT,
    ADJUSTMENT
}

enum class LayerBlendMode(val label: String, val composeBlendMode: BlendMode) {
    NORMAL("Normal", BlendMode.SrcOver),
    MULTIPLY("Multiply", BlendMode.Multiply),
    SCREEN("Screen", BlendMode.Screen),
    OVERLAY("Overlay", BlendMode.Overlay),
    DARKEN("Darken", BlendMode.Darken),
    LIGHTEN("Lighten", BlendMode.Lighten),
    COLOR_DODGE("Color Dodge", BlendMode.ColorDodge),
    COLOR_BURN("Color Burn", BlendMode.ColorBurn),
    HARD_LIGHT("Hard Light", BlendMode.Hardlight),
    DIFFERENCE("Difference", BlendMode.Difference)
}

enum class ShapeType(val label: String) {
    RECTANGLE("Rectangle"),
    ELLIPSE("Ellipse"),
    LINE("Line"),
    STAR("Star"),
    ROUNDED_RECT("Rounded")
}

data class ShapeConfig(
    val type: ShapeType = ShapeType.RECTANGLE,
    val fillColor: Long = 0xFF2F80ED,
    val strokeColor: Long = 0xFFFFFFFF,
    val strokeWidth: Float = 4f,
    val width: Float = 240f,
    val height: Float = 160f,
    val cornerRadius: Float = 24f
)

data class TextConfig(
    val text: String = "PhotoCraft",
    val textColor: Long = 0xFFFFFFFF,
    val fontSize: Float = 36f,
    val isBold: Boolean = true,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false
)

enum class AdjustmentType(val label: String) {
    BRIGHTNESS_CONTRAST("Brightness / Contrast"),
    HUE_SATURATION("Hue / Saturation"),
    LEVELS("Levels"),
    VIBRANCE("Vibrance"),
    BLACK_AND_WHITE("Black & White"),
    COLOR_BALANCE("Color Balance"),
    INVERT("Invert")
}

data class AdjustmentParams(
    val type: AdjustmentType = AdjustmentType.BRIGHTNESS_CONTRAST,
    val brightness: Float = 0f, // -100 to 100
    val contrast: Float = 0f,   // -100 to 100
    val hue: Float = 0f,        // -180 to 180
    val saturation: Float = 0f, // -100 to 100
    val vibrance: Float = 0f,   // -100 to 100
    val blackPoint: Float = 0f, // 0 to 255
    val gamma: Float = 1.0f,    // 0.2 to 3.0
    val whitePoint: Float = 255f, // 0 to 255
    val redShift: Float = 0f,   // -100 to 100
    val greenShift: Float = 0f, // -100 to 100
    val blueShift: Float = 0f   // -100 to 100
)

enum class FilterType(val label: String) {
    NONE("None"),
    GAUSSIAN_BLUR("Gaussian Blur"),
    SHARPEN("Sharpen"),
    PIXELATE("Pixelate"),
    SEPIA("Sepia Tone"),
    VIGNETTE("Vignette"),
    POSTERIZE("Posterize"),
    EMBOSS("Emboss")
}

data class FilterParams(
    val type: FilterType = FilterType.NONE,
    val radius: Float = 10f, // 1 to 50
    val intensity: Float = 50f // 1 to 100
)

data class Layer(
    val id: String,
    val name: String,
    val kind: LayerKind,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val opacity: Float = 1.0f, // 0f .. 1f
    val blendMode: LayerBlendMode = LayerBlendMode.NORMAL,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    // Raster data
    val bitmap: Bitmap? = null,
    // Vector shape
    val shapeConfig: ShapeConfig? = null,
    // Vector text
    val textConfig: TextConfig? = null,
    // Adjustment properties
    val adjustmentParams: AdjustmentParams? = null,
    // Filter properties
    val filterParams: FilterParams = FilterParams()
)

data class ProjectDocument(
    val id: String,
    val title: String,
    val width: Int,
    val height: Int,
    val layers: List<Layer>,
    val activeLayerId: String
)

enum class ToolType(val label: String) {
    MOVE("Move"),
    BRUSH("Brush"),
    ERASER("Eraser"),
    SHAPE("Shapes"),
    TEXT("Text"),
    ADJUST("Adjust"),
    FILTER("Filters"),
    CROP("Crop"),
    EYEDROPPER("Picker")
}

enum class CropRatio(val label: String, val ratio: Float?) {
    FREE("Free", null),
    SQUARE("1:1", 1.0f),
    PHOTO_4_3("4:3", 4f / 3f),
    PHOTO_3_4("3:4", 3f / 4f),
    WIDE_16_9("16:9", 16f / 9f),
    STORY_9_16("9:16", 9f / 16f)
}

data class HistoryEntry(
    val id: String,
    val actionName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val documentSnapshot: ProjectDocument
)
