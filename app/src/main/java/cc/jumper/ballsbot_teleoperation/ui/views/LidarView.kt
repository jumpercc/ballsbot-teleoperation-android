package cc.jumper.ballsbot_teleoperation.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import cc.jumper.ballsbot_teleoperation.BotSize
import cc.jumper.ballsbot_teleoperation.R
import kotlin.math.min

private const val RANGE_LIMIT = 3.0
private const val POINT_RADIUS = 2f

class LidarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var canvasWidth: Int = 0
    private var canvasHeight: Int = 0
    private lateinit var currentBotSize: BotSize
    private lateinit var currentCloud: List<List<Double>>

    private lateinit var extraCanvas: Canvas
    private lateinit var extraBitmap: Bitmap

    private val backgroundColor =
        ResourcesCompat.getColor(resources, R.color.lidar_background, null)
    private val botColor = ResourcesCompat.getColor(resources, R.color.lidar_self, null)
    private val cloudColor = ResourcesCompat.getColor(resources, R.color.lidar_cloud, null)

    private val botPaint = Paint().apply {
        color = botColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
    }

    private val cloudPaint = Paint().apply {
        color = cloudColor
        isAntiAlias = true
        isDither = true
        style = Paint.Style.FILL
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

    fun updateCloud(botSize: BotSize, cloud: List<List<Double>>) {
        currentBotSize = botSize
        currentCloud = cloud
        redrawExtraBitmap()
    }

    private fun transformPoint(source: PointF): PointF {
        return PointF(-source.y, -source.x)
    }

    private fun redrawExtraBitmap() {
        if (!::extraCanvas.isInitialized) {
            reinitExtra()
        }
        extraCanvas.drawColor(backgroundColor)

        val minDimension = min(canvasWidth, canvasHeight)
        val metersPerPixel = 2.0 * RANGE_LIMIT / minDimension
        val xShift = canvasWidth / 2.0f
        val yShift = canvasHeight / 2.0f

        if (::currentBotSize.isInitialized) {
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

            currentCloud.forEach {
                val aPoint = transformPoint(
                    PointF(
                        (it[0] / metersPerPixel).toFloat(),
                        (it[1] / metersPerPixel).toFloat()
                    )
                )
                if (-xShift <= aPoint.x && aPoint.x <= xShift && -yShift <= aPoint.y && aPoint.y <= yShift) {
                    extraCanvas.drawCircle(
                        aPoint.x + xShift, aPoint.y + yShift,
                        POINT_RADIUS,
                        cloudPaint
                    )
                }
            }
        }

        invalidate()
    }
}