package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.model.AdjustmentParams
import com.example.model.AdjustmentType
import com.example.model.FilterParams
import com.example.model.FilterType
import com.example.model.Layer
import com.example.model.LayerBlendMode
import com.example.model.LayerKind
import com.example.model.ProjectDocument
import com.example.model.ShapeConfig
import com.example.model.ShapeType
import com.example.model.TextConfig
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object ImageProcessor {

    /**
     * Creates a composite bitmap combining all visible layers according to their order,
     * opacity, blend modes, transforms, vector shapes, texts, and adjustment layers.
     */
    fun renderComposite(document: ProjectDocument): Bitmap {
        val width = max(1, document.width)
        val height = max(1, document.height)
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Checkerboard / canvas background is transparent in output bitmap
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        for (layer in document.layers) {
            if (!layer.isVisible) continue

            paint.alpha = (layer.opacity.coerceIn(0f, 1f) * 255).toInt()
            paint.xfermode = getPorterDuffMode(layer.blendMode)

            canvas.save()
            canvas.translate(layer.offsetX, layer.offsetY)
            if (layer.scale != 1.0f || layer.rotation != 0f) {
                canvas.rotate(layer.rotation, width / 2f, height / 2f)
                canvas.scale(layer.scale, layer.scale, width / 2f, height / 2f)
            }

            when (layer.kind) {
                LayerKind.IMAGE -> {
                    layer.bitmap?.let { bmp ->
                        val layerPaint = Paint(paint)
                        applyFiltersToPaint(layer.filterParams, layer.adjustmentParams, layerPaint, width, height)
                        canvas.drawBitmap(bmp, 0f, 0f, layerPaint)
                    }
                }
                LayerKind.SHAPE -> {
                    layer.shapeConfig?.let { shape ->
                        drawShape(canvas, shape, paint)
                    }
                }
                LayerKind.TEXT -> {
                    layer.textConfig?.let { text ->
                        drawText(canvas, text, paint)
                    }
                }
                LayerKind.ADJUSTMENT -> {
                    // Global adjustment layer effect applied across canvas
                    layer.adjustmentParams?.let { adj ->
                        applyAdjustmentOverCanvas(canvas, adj, width, height, layer.opacity)
                    }
                }
            }

            canvas.restore()
        }

        return result
    }

    private fun getPorterDuffMode(blendMode: LayerBlendMode): PorterDuffXfermode {
        val mode = when (blendMode) {
            LayerBlendMode.NORMAL -> PorterDuff.Mode.SRC_OVER
            LayerBlendMode.MULTIPLY -> PorterDuff.Mode.MULTIPLY
            LayerBlendMode.SCREEN -> PorterDuff.Mode.SCREEN
            LayerBlendMode.OVERLAY -> PorterDuff.Mode.OVERLAY
            LayerBlendMode.DARKEN -> PorterDuff.Mode.DARKEN
            LayerBlendMode.LIGHTEN -> PorterDuff.Mode.LIGHTEN
            LayerBlendMode.COLOR_DODGE -> PorterDuff.Mode.SCREEN
            LayerBlendMode.COLOR_BURN -> PorterDuff.Mode.MULTIPLY
            LayerBlendMode.HARD_LIGHT -> PorterDuff.Mode.OVERLAY
            LayerBlendMode.DIFFERENCE -> PorterDuff.Mode.XOR
        }
        return PorterDuffXfermode(mode)
    }

    fun applyFiltersToPaint(
        filter: FilterParams,
        adj: AdjustmentParams?,
        paint: Paint,
        width: Int,
        height: Int
    ) {
        val cm = ColorMatrix()

        if (adj != null) {
            applyAdjustmentToMatrix(adj, cm)
        }

        when (filter.type) {
            FilterType.SEPIA -> {
                val sepiaMatrix = ColorMatrix(
                    floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 0f,
                        0.349f, 0.686f, 0.168f, 0f, 0f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(sepiaMatrix)
            }
            FilterType.POSTERIZE -> {
                // Color matrix tweak for high contrast / stepping look
                val contrastMatrix = ColorMatrix()
                val scale = 1.4f
                val translate = (-0.2f) * 255f
                contrastMatrix.set(
                    floatArrayOf(
                        scale, 0f, 0f, 0f, translate,
                        0f, scale, 0f, 0f, translate,
                        0f, 0f, scale, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(contrastMatrix)
            }
            else -> {}
        }

        paint.colorFilter = ColorMatrixColorFilter(cm)
    }

    private fun applyAdjustmentToMatrix(adj: AdjustmentParams, cm: ColorMatrix) {
        when (adj.type) {
            AdjustmentType.BRIGHTNESS_CONTRAST -> {
                val contrast = (adj.contrast / 100f) + 1f
                val brightness = adj.brightness * 1.5f
                val t = (1.0f - contrast) * 128f + brightness
                val matrix = ColorMatrix(
                    floatArrayOf(
                        contrast, 0f, 0f, 0f, t,
                        0f, contrast, 0f, 0f, t,
                        0f, 0f, contrast, 0f, t,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(matrix)
            }
            AdjustmentType.HUE_SATURATION -> {
                val sat = ((adj.saturation + 100f) / 100f).coerceAtLeast(0f)
                val satMatrix = ColorMatrix()
                satMatrix.setSaturation(sat)
                cm.postConcat(satMatrix)

                if (adj.hue != 0f) {
                    val hueMatrix = ColorMatrix()
                    // Rotate hue in RGB plane
                    hueMatrix.setRotate(0, adj.hue)
                    hueMatrix.setRotate(1, adj.hue)
                    hueMatrix.setRotate(2, adj.hue)
                    cm.postConcat(hueMatrix)
                }
            }
            AdjustmentType.LEVELS -> {
                val range = max(1f, adj.whitePoint - adj.blackPoint)
                val scale = 255f / range
                val offset = -adj.blackPoint * scale
                val matrix = ColorMatrix(
                    floatArrayOf(
                        scale, 0f, 0f, 0f, offset,
                        0f, scale, 0f, 0f, offset,
                        0f, 0f, scale, 0f, offset,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(matrix)
            }
            AdjustmentType.VIBRANCE -> {
                // Vibrance saturates cooler tones without over-saturating skin tones
                val vib = ((adj.vibrance + 100f) / 100f).coerceAtLeast(0f)
                val vibMatrix = ColorMatrix()
                vibMatrix.setSaturation(vib)
                cm.postConcat(vibMatrix)
            }
            AdjustmentType.BLACK_AND_WHITE -> {
                val bwMatrix = ColorMatrix()
                bwMatrix.setSaturation(0f)
                cm.postConcat(bwMatrix)
            }
            AdjustmentType.INVERT -> {
                val invertMatrix = ColorMatrix(
                    floatArrayOf(
                        -1f, 0f, 0f, 0f, 255f,
                        0f, -1f, 0f, 0f, 255f,
                        0f, 0f, -1f, 0f, 255f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(invertMatrix)
            }
            AdjustmentType.COLOR_BALANCE -> {
                val rScale = 1f + (adj.redShift / 100f)
                val gScale = 1f + (adj.greenShift / 100f)
                val bScale = 1f + (adj.blueShift / 100f)
                val balanceMatrix = ColorMatrix(
                    floatArrayOf(
                        rScale, 0f, 0f, 0f, 0f,
                        0f, gScale, 0f, 0f, 0f,
                        0f, 0f, bScale, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(balanceMatrix)
            }
        }
    }

    private fun applyAdjustmentOverCanvas(
        canvas: Canvas,
        adj: AdjustmentParams,
        width: Int,
        height: Int,
        layerOpacity: Float
    ) {
        val cm = ColorMatrix()
        applyAdjustmentToMatrix(adj, cm)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)
        paint.alpha = (layerOpacity * 255).toInt()
        val rect = Rect(0, 0, width, height)
        // Draw non-destructive color matrix tint over composite
        canvas.drawRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), paint)
    }

    private fun drawShape(canvas: Canvas, shape: ShapeConfig, basePaint: Paint) {
        val fillPaint = Paint(basePaint).apply {
            style = Paint.Style.FILL
            color = shape.fillColor.toInt()
        }
        val strokePaint = Paint(basePaint).apply {
            style = Paint.Style.STROKE
            color = shape.strokeColor.toInt()
            strokeWidth = shape.strokeWidth
        }

        val cx = canvas.width / 2f
        val cy = canvas.height / 2f
        val halfW = shape.width / 2f
        val halfH = shape.height / 2f
        val rect = RectF(cx - halfW, cy - halfH, cx + halfW, cy + halfH)

        when (shape.type) {
            ShapeType.RECTANGLE -> {
                canvas.drawRect(rect, fillPaint)
                if (shape.strokeWidth > 0) canvas.drawRect(rect, strokePaint)
            }
            ShapeType.ROUNDED_RECT -> {
                canvas.drawRoundRect(rect, shape.cornerRadius, shape.cornerRadius, fillPaint)
                if (shape.strokeWidth > 0) canvas.drawRoundRect(rect, shape.cornerRadius, shape.cornerRadius, strokePaint)
            }
            ShapeType.ELLIPSE -> {
                canvas.drawOval(rect, fillPaint)
                if (shape.strokeWidth > 0) canvas.drawOval(rect, strokePaint)
            }
            ShapeType.LINE -> {
                strokePaint.strokeWidth = max(2f, shape.strokeWidth)
                canvas.drawLine(rect.left, rect.centerY(), rect.right, rect.centerY(), strokePaint)
            }
            ShapeType.STAR -> {
                val path = createStarPath(cx, cy, 5, halfW, halfW * 0.45f)
                canvas.drawPath(path, fillPaint)
                if (shape.strokeWidth > 0) canvas.drawPath(path, strokePaint)
            }
        }
    }

    private fun createStarPath(cx: Float, cy: Float, points: Int, outerR: Float, innerR: Float): Path {
        val path = Path()
        val step = Math.PI / points
        var angle = -Math.PI / 2.0
        path.moveTo((cx + outerR * cos(angle)).toFloat(), (cy + outerR * sin(angle)).toFloat())

        for (i in 0 until points) {
            angle += step
            path.lineTo((cx + innerR * cos(angle)).toFloat(), (cy + innerR * sin(angle)).toFloat())
            angle += step
            path.lineTo((cx + outerR * cos(angle)).toFloat(), (cy + outerR * sin(angle)).toFloat())
        }
        path.close()
        return path
    }

    private fun drawText(canvas: Canvas, text: TextConfig, basePaint: Paint) {
        val textPaint = Paint(basePaint).apply {
            color = text.textColor.toInt()
            textSize = text.fontSize
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isUnderlineText = text.isUnderline
            val style = when {
                text.isBold && text.isItalic -> Typeface.BOLD_ITALIC
                text.isBold -> Typeface.BOLD
                text.isItalic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }
            typeface = Typeface.create(Typeface.SANS_SERIF, style)
        }

        val cx = canvas.width / 2f
        val cy = canvas.height / 2f - ((textPaint.descent() + textPaint.ascent()) / 2f)
        canvas.drawText(text.text, cx, cy, textPaint)
    }

    /**
     * Applies a destructive image filter (like Blur, Sharpen, Pixelate, Vignette) to an existing bitmap
     * producing a new raster bitmap for editing or exporting.
     */
    fun applyFilterToBitmap(src: Bitmap, filter: FilterParams): Bitmap {
        return when (filter.type) {
            FilterType.NONE -> src
            FilterType.GAUSSIAN_BLUR -> fastBoxBlur(src, filter.radius.toInt().coerceIn(1, 40))
            FilterType.SHARPEN -> sharpenBitmap(src, filter.intensity / 50f)
            FilterType.PIXELATE -> pixelateBitmap(src, max(2, (filter.radius * 0.75f).toInt()))
            FilterType.VIGNETTE -> applyVignette(src, filter.intensity / 100f)
            FilterType.SEPIA, FilterType.POSTERIZE -> {
                val output = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(output)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                applyFiltersToPaint(filter, null, paint, src.width, src.height)
                canvas.drawBitmap(src, 0f, 0f, paint)
                output
            }
            FilterType.EMBOSS -> embossBitmap(src)
        }
    }

    /**
     * Fast stack blur / box blur algorithm.
     */
    fun fastBoxBlur(src: Bitmap, radius: Int): Bitmap {
        val w = src.width
        val h = src.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val pix = IntArray(w * h)
        src.getPixels(pix, 0, w, 0, 0, w, h)

        val r = radius.coerceIn(1, 50)
        val div = 2 * r + 1

        // Horizontal pass
        val temp = IntArray(w * h)
        for (y in 0 until h) {
            var rSum = 0
            var gSum = 0
            var bSum = 0
            var aSum = 0

            for (i in -r..r) {
                val x = i.coerceIn(0, w - 1)
                val p = pix[y * w + x]
                aSum += (p shr 24) and 0xFF
                rSum += (p shr 16) and 0xFF
                gSum += (p shr 8) and 0xFF
                bSum += p and 0xFF
            }

            for (x in 0 until w) {
                temp[y * w + x] = ((aSum / div) shl 24) or
                        ((rSum / div) shl 16) or
                        ((gSum / div) shl 8) or
                        (bSum / div)

                val x1 = (x - r).coerceIn(0, w - 1)
                val x2 = (x + r + 1).coerceIn(0, w - 1)
                val p1 = pix[y * w + x1]
                val p2 = pix[y * w + x2]

                aSum += ((p2 shr 24) and 0xFF) - ((p1 shr 24) and 0xFF)
                rSum += ((p2 shr 16) and 0xFF) - ((p1 shr 16) and 0xFF)
                gSum += ((p2 shr 8) and 0xFF) - ((p1 shr 8) and 0xFF)
                bSum += (p2 and 0xFF) - (p1 and 0xFF)
            }
        }

        // Vertical pass
        for (x in 0 until w) {
            var rSum = 0
            var gSum = 0
            var bSum = 0
            var aSum = 0

            for (i in -r..r) {
                val y = i.coerceIn(0, h - 1)
                val p = temp[y * w + x]
                aSum += (p shr 24) and 0xFF
                rSum += (p shr 16) and 0xFF
                gSum += (p shr 8) and 0xFF
                bSum += p and 0xFF
            }

            for (y in 0 until h) {
                pix[y * w + x] = ((aSum / div) shl 24) or
                        ((rSum / div) shl 16) or
                        ((gSum / div) shl 8) or
                        (bSum / div)

                val y1 = (y - r).coerceIn(0, h - 1)
                val y2 = (y + r + 1).coerceIn(0, h - 1)
                val p1 = temp[y1 * w + x]
                val p2 = temp[y2 * w + x]

                aSum += ((p2 shr 24) and 0xFF) - ((p1 shr 24) and 0xFF)
                rSum += ((p2 shr 16) and 0xFF) - ((p1 shr 16) and 0xFF)
                gSum += ((p2 shr 8) and 0xFF) - ((p1 shr 8) and 0xFF)
                bSum += (p2 and 0xFF) - (p1 and 0xFF)
            }
        }

        output.setPixels(pix, 0, w, 0, 0, w, h)
        return output
    }

    private fun pixelateBitmap(src: Bitmap, blockSize: Int): Bitmap {
        val size = max(2, blockSize)
        val w = src.width
        val h = src.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val small = Bitmap.createScaledBitmap(src, max(1, w / size), max(1, h / size), false)
        val canvas = Canvas(output)
        val paint = Paint().apply { isFilterBitmap = false }
        canvas.drawBitmap(small, null, Rect(0, 0, w, h), paint)
        return output
    }

    private fun sharpenBitmap(src: Bitmap, amount: Float): Bitmap {
        val blurred = fastBoxBlur(src, 2)
        val w = src.width
        val h = src.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val srcPix = IntArray(w * h)
        val blurPix = IntArray(w * h)
        src.getPixels(srcPix, 0, w, 0, 0, w, h)
        blurred.getPixels(blurPix, 0, w, 0, 0, w, h)

        val outPix = IntArray(w * h)
        val factor = 1f + amount
        for (i in 0 until w * h) {
            val sp = srcPix[i]
            val bp = blurPix[i]
            val a = (sp shr 24) and 0xFF
            val sr = (sp shr 16) and 0xFF
            val sg = (sp shr 8) and 0xFF
            val sb = sp and 0xFF
            val br = (bp shr 16) and 0xFF
            val bg = (bp shr 8) and 0xFF
            val bb = bp and 0xFF

            val r = (sr + factor * (sr - br)).toInt().coerceIn(0, 255)
            val g = (sg + factor * (sg - bg)).toInt().coerceIn(0, 255)
            val b = (sb + factor * (sb - bb)).toInt().coerceIn(0, 255)

            outPix[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
        }
        output.setPixels(outPix, 0, w, 0, 0, w, h)
        return output
    }

    private fun applyVignette(src: Bitmap, intensity: Float): Bitmap {
        val w = src.width
        val h = src.height
        val output = src.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)

        val cx = w / 2f
        val cy = h / 2f
        val radius = max(w, h) / 1.2f

        val colors = intArrayOf(Color.TRANSPARENT, Color.argb((intensity * 230).toInt().coerceIn(0, 255), 0, 0, 0))
        val stops = floatArrayOf(0.4f, 1.0f)
        val shader = RadialGradient(cx, cy, radius, colors, stops, Shader.TileMode.CLAMP)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.shader = shader
        }
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)
        return output
    }

    private fun embossBitmap(src: Bitmap): Bitmap {
        val w = src.width
        val h = src.height
        val output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val srcPix = IntArray(w * h)
        src.getPixels(srcPix, 0, w, 0, 0, w, h)
        val outPix = IntArray(w * h)

        for (y in 1 until h - 1) {
            for (x in 1 until w - 1) {
                val p1 = srcPix[(y - 1) * w + (x - 1)]
                val p2 = srcPix[(y + 1) * w + (x + 1)]

                val r1 = (p1 shr 16) and 0xFF
                val g1 = (p1 shr 8) and 0xFF
                val b1 = p1 and 0xFF

                val r2 = (p2 shr 16) and 0xFF
                val g2 = (p2 shr 8) and 0xFF
                val b2 = p2 and 0xFF

                val v = (128 + ((r1 + g1 + b1) - (r2 + g2 + b2)) / 2).coerceIn(0, 255)
                outPix[y * w + x] = (0xFF shl 24) or (v shl 16) or (v shl 8) or v
            }
        }
        output.setPixels(outPix, 0, w, 0, 0, w, h)
        return output
    }
}
