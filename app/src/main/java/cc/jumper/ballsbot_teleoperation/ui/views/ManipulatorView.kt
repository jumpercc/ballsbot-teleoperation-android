package cc.jumper.ballsbot_teleoperation.ui.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import cc.jumper.ballsbot_teleoperation.BotSize
import cc.jumper.ballsbot_teleoperation.ManipulatorState
import cc.jumper.ballsbot_teleoperation.R

private const val RANGE_LIMIT = 0.75
private const val TEXT_SIZE = 40f
private const val STROKE_WIDTH = 8f
private const val MANUPULATOR_COORDS_DIVIDER = 1000f

enum class ManipulatorProjection(val caption: String) {
    XY("XY"),
    XZ("XZ");
}

class ManipulatorView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0
    private var projection = ManipulatorProjection.XY

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap
    private lateinit var currentBotSize: BotSize
    private lateinit var manipulatorState: ManipulatorState

    private val backgroundColor =
        ResourcesCompat.getColor(resources, R.color.manipulator_background, null)
    private val botColor = ResourcesCompat.getColor(resources, R.color.manipulator_bot, null)
    private val handColor = ResourcesCompat.getColor(resources, R.color.manipulator_hand, null)
    private val textColor = ResourcesCompat.getColor(resources, R.color.manipulator_text, null)

    private val botPaint = Paint().apply {
        color = botColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
        strokeWidth = STROKE_WIDTH
    }

    private val handPaint = Paint().apply {
        color = handColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
        strokeWidth = STROKE_WIDTH
    }

    private val textPaint = Paint().apply {
        color = textColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = TEXT_SIZE
        typeface = Typeface.create("", Typeface.BOLD)
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

    fun setProjection(newProjection: ManipulatorProjection) {
        projection = newProjection
    }

    fun updatePose(botSize: BotSize, manipulator: ManipulatorState) {
        currentBotSize = botSize
        manipulatorState = manipulator
        redrawExtraBitmap()
    }

    private fun transformPoint(source: PointF): PointF {
        return PointF(source.x, -source.y)
    }

    private fun redrawExtraBitmap() {
        if (!::extraCanvas.isInitialized) {
            reinitExtra()
        }
        extraCanvas.drawColor(backgroundColor)

        val metersPerPixel = 2.0 * RANGE_LIMIT / canvasWidth
        val xShift = canvasWidth / 2.0f
        val yShift = canvasHeight / 2.0f

        extraCanvas.drawText(projection.caption, TEXT_SIZE, TEXT_SIZE, textPaint)

        if (::currentBotSize.isInitialized) {
            if (projection == ManipulatorProjection.XY) {
                val startPoint = transformPoint(
                    PointF(
                        (currentBotSize.x / metersPerPixel).toFloat(),
                        (currentBotSize.y / metersPerPixel).toFloat(),
                    )
                )
                val endPoint = transformPoint(
                    PointF(
                        ((currentBotSize.x + currentBotSize.w) / metersPerPixel).toFloat(),
                        ((currentBotSize.y + currentBotSize.h) / metersPerPixel).toFloat(),
                    )
                )
                extraCanvas.drawRect(
                    startPoint.x + xShift, startPoint.y + yShift,
                    endPoint.x + xShift, endPoint.y + yShift,
                    botPaint
                )
            }

            val secondIndex = if (projection == ManipulatorProjection.XY) {
                1
            } else {
                2
            }
            var currentPoint = PointF(0f, 0f)
            manipulatorState.current_pose.points.forEach {
                val aPoint = transformPoint(
                    PointF(
                        (it[0] / MANUPULATOR_COORDS_DIVIDER / metersPerPixel).toFloat(),
                        (it[secondIndex] / MANUPULATOR_COORDS_DIVIDER / metersPerPixel).toFloat(),
                    )
                )
                extraCanvas.drawLine(
                    currentPoint.x + xShift, currentPoint.y + yShift,
                    aPoint.x + xShift, aPoint.y + yShift,
                    handPaint
                )
                currentPoint = aPoint
            }
            manipulatorState.current_pose.claw_points.forEach {
                val aPoint = transformPoint(
                    PointF(
                        (it[0] / MANUPULATOR_COORDS_DIVIDER / metersPerPixel).toFloat(),
                        (it[secondIndex] / MANUPULATOR_COORDS_DIVIDER / metersPerPixel).toFloat(),
                    )
                )
                extraCanvas.drawLine(
                    currentPoint.x + xShift, currentPoint.y + yShift,
                    aPoint.x + xShift, aPoint.y + yShift,
                    handPaint
                )
            }
        }

        invalidate()
    }
}