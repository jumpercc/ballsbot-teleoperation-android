package cc.jumper.ballsbot_teleoperation.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import cc.jumper.ballsbot_teleoperation.Detection
import cc.jumper.ballsbot_teleoperation.R

private const val STROKE_WIDTH = 4f

class DetectionsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val drawableHtoWRatio = 240.0f / 320.0f

    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0

    private var currentDetections: List<Detection>? = null

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    private val detectionColor = ResourcesCompat.getColor(resources, R.color.detection_frame, null)

    private val detectionPaint = Paint().apply {
        color = detectionColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        canvasWidth = width
        canvasHeight = height
        reinitExtra()
        redrawExtraBitmap()
    }

    private fun reinitExtra() {
        if (::extraBitmap.isInitialized) extraBitmap.recycle()
        extraBitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        extraCanvas = Canvas(extraBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(extraBitmap, 0f, 0f, null)
    }

    fun updateDetections(detections: List<Detection>?) {
        currentDetections = detections
        redrawExtraBitmap()
    }

    private fun redrawExtraBitmap() {
        if (!::extraCanvas.isInitialized) {
            reinitExtra()
        }
        extraCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val minX: Float
        val minY: Float
        val maxX: Float
        val maxY: Float
        val width: Float
        val height: Float
        if (canvasHeight.toFloat() == drawableHtoWRatio * canvasWidth) {
            minX = 0.0f
            maxX = canvasWidth.toFloat()
            width = maxX
            minY = 0.0f
            maxY = canvasHeight.toFloat()
            height = maxY
        } else if (canvasHeight > drawableHtoWRatio * canvasWidth) {
            minX = 0.0f
            maxX = canvasWidth.toFloat()
            width = maxX
            height = drawableHtoWRatio * canvasWidth
            minY = (canvasHeight - height) / 2.0f
            maxY = minY + height
        } else {
            minY = 0.0f
            maxY = canvasHeight.toFloat()
            width = canvasHeight / drawableHtoWRatio
            minX = (canvasWidth - width) / 2.0f
            maxX = minX + width
            height = maxY
        }

        currentDetections?.forEach {
            extraCanvas.drawRect(
                minX + width * it.bottom_left[0].toFloat(),
                minY + height * (1.0f - it.top_right[1].toFloat()),
                minX + width * it.top_right[0].toFloat(),
                minY + height * (1.0f - it.bottom_left[1].toFloat()),
                detectionPaint
            )
        }
    }
}