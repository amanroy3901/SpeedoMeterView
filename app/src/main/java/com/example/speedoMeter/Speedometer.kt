package com.example.speedoMeter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Math.min
import java.lang.Math.toRadians
import kotlin.math.cos
import kotlin.math.sin


class SpeedometerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private lateinit var canvas: Canvas
    private val radius = 450f
    private var speedColor = Color.parseColor("#24ECCE")
    private var defaultColor = Color.parseColor("#656565")
    private var currentSpeed = 0
    private var minSpeed: Int
    private var maxSpeed: Int
    private var interval: Int
    var lineSweepAngle = 0f
    var lineValueDifference: Int
    var currentNumberOfLines = 0
    var meterType: MeterType


    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SpeedometerView)
        defaultColor =
            a.getColor(R.styleable.SpeedometerView_defaultColor, Color.parseColor("#656565"))
        speedColor = a.getColor(R.styleable.SpeedometerView_speedColor, Color.parseColor("#24ECCE"))
        minSpeed = a.getInteger(R.styleable.SpeedometerView_minSpeed, 0)
        maxSpeed = a.getInteger(R.styleable.SpeedometerView_maxSpeed, 180)
        interval = a.getInteger(R.styleable.SpeedometerView_interval, 20)
        val mType = a.getInt(R.styleable.SpeedometerView_meterType, 0)
        a.recycle()
        lineValueDifference = (maxSpeed - minSpeed) / 45
        if (mType == 0) {
            meterType = MeterType.Meter_SpeedoMeter
        } else {
            meterType = MeterType.Meter_RotoMeter
        }
    }

    fun setSpeed(speed: Int) {
        val clampedSpeed = speed.coerceIn(minSpeed, maxSpeed)

        CoroutineScope(Dispatchers.Main).launch {
            val range =
                if (currentSpeed <= clampedSpeed) currentSpeed..clampedSpeed else currentSpeed downTo clampedSpeed

            if (meterType == MeterType.Meter_SpeedoMeter) {
                for (i in range) {
                    currentSpeed = i
                    invalidate()
                    delay(15)
                }
            } else {
                for (i in range step 25) {
                    currentSpeed = i
                    invalidate()
                    delay(15)
                }
            }


            currentSpeed = clampedSpeed
            invalidate()
        }
    }

    private fun calculateNumberOfLines(speed: Int) = minOf((speed / lineValueDifference) + 1, 46)

    private fun calculateSweepAngleForArc(speed: Int) =
        min((speed.toFloat() / maxSpeed) * 260f, 260f)

    private fun calculateSweepAngleForLines(speed: Int) =
        calculateSweepAngleNow(if (speed >= maxSpeed) maxSpeed else speed)

    private fun calculateSweepAngleNow(speed: Int): Float {
        val sweepAngle = 260f
        return (if (speed % lineValueDifference == 0) speed else speed - (speed % lineValueDifference)).toFloat() / maxSpeed * sweepAngle
    }


    private fun createTextPaint(
        textSize: Float, color: Int, align: Paint.Align? = null, typeface: Typeface? = null
    ): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize
            this.color = color
            align?.let { this.textAlign = align }
            typeface?.let { this.typeface = it }
        }
    }

    private fun createDesignPaint(strokeWidth: Float, color: Int, style: Paint.Style): Paint {
        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.strokeWidth = strokeWidth
            this.color = color
            this.style = style
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        this.canvas = canvas

        val startAngle = 140f
        val sweepAngle = 260f
        val increasedRadius = 450f
        var speedText : String
        val arcSweepAngle = calculateSweepAngleForArc(currentSpeed)
        val newNUmberOfLines = calculateNumberOfLines(currentSpeed)
        val yPosMph = height / 2f + 50f
        val numberOfLines = 46
        var unitText: String
        var meterSpeedTextSize : Float
        var lineLength = 60f
        lineSweepAngle = calculateSweepAngleForLines(currentSpeed)
        val speedometerPaint = createDesignPaint(20f, defaultColor, Paint.Style.STROKE)

        if (meterType == MeterType.Meter_SpeedoMeter) {
            unitText = "mph"
            speedText = currentSpeed.toString()
            meterSpeedTextSize = 50f
        } else {
            unitText = "rpm"
            speedText = (currentSpeed / 1000).toString()
            meterSpeedTextSize = 80f
        }
        canvas.drawColor(Color.BLACK)
        drawArc(speedometerPaint, startAngle, sweepAngle, increasedRadius)
        drawArc(
            createDesignPaint(20f, speedColor, Paint.Style.STROKE),
            startAngle,
            arcSweepAngle,
            radius
        )
        canvas.drawCircle(width / 2f, height / 2f, radius / 2.5f, speedometerPaint)
        canvas.drawText(
            unitText,
            width / 2f,
            yPosMph,
            createTextPaint(45f, Color.WHITE, Paint.Align.CENTER)
        )
        canvas.drawText(
            speedText,
            width / 2f,
            height / 2f,
            createTextPaint(120f, Color.WHITE, Paint.Align.CENTER)
        )

        for (i in minSpeed..maxSpeed step interval) {
            val angle = startAngle + ((i).toFloat() / maxSpeed) * sweepAngle
            val xPos =
                width / 2f + (increasedRadius / 1.75f) * cos(toRadians(angle.toDouble())).toFloat()
            val yPos =
                height / 2f + (increasedRadius / 1.75f) * sin(toRadians(angle.toDouble())).toFloat()

            var speedText = if(meterType == MeterType.Meter_RotoMeter) i/1000 else i
            canvas.drawText(
                speedText.toString(),
                xPos,
                yPos,
                createTextPaint(
                    meterSpeedTextSize,
                    Color.WHITE,
                    Paint.Align.CENTER,
                    Typeface.create(Typeface.SERIF, Typeface.ITALIC)
                )
            )
        }

        drawNumberLines(
            startAngle,
            numberOfLines,
            sweepAngle,
            lineLength,
            increasedRadius,
            createDesignPaint(8f, defaultColor, Paint.Style.STROKE)
        )
        drawNumberLines(
            startAngle,
            newNUmberOfLines.toInt(),
            lineSweepAngle,
            lineLength,
            increasedRadius,
            createDesignPaint(8f, speedColor, Paint.Style.STROKE)
        )

        currentNumberOfLines = min(currentNumberOfLines, (newNUmberOfLines - 1).toInt())
    }

    /**
     * Draws lines on the speedometer arc based on the specified parameters.
     *
     * @param startAngle Starting angle of the arc in degrees.
     * @param numberOfLines Number of lines to be drawn on the arc.
     * @param sweepAngle Sweep angle of the arc in degrees.
     * @param lineLength Length of the lines to be drawn.
     * @param radius Radius of the speedometer arc.
     * @param paint Paint object to define the visual properties of the lines.
     */
    private fun drawNumberLines(
        startAngle: Float,
        numberOfLines: Int,
        sweepAngle: Float,
        lineLength: Float,
        radius: Float,
        paint: Paint
    ) {
        for (i in 0 until numberOfLines) {
            val angle = (startAngle + i.toFloat() / (numberOfLines - 1) * sweepAngle).toDouble()

            // Adjust line length based on multiples of 5
            val newLineLength = lineLength + if (i % 5 == 0 || i == 0) 45 else 0

            // Calculate starting point of the line
            val lineStartX = width / 2f + (radius - 30f) * cos(toRadians(angle)).toFloat()
            val lineStartY = height / 2f + (radius - 30f) * sin(toRadians(angle)).toFloat()

            // Calculate ending point of the line
            val lineEndX = width / 2f + (radius - newLineLength) * cos(toRadians(angle)).toFloat()
            val lineEndY = height / 2f + (radius - newLineLength) * sin(toRadians(angle)).toFloat()

            // Draw the line on the canvas using the specified Paint object
            canvas.drawLine(lineStartX, lineStartY, lineEndX, lineEndY, paint)
        }
    }


    private fun drawArc(paint: Paint, startAngle: Float, sweepAngle: Float, radius: Float) {
        canvas.drawArc(
            width / 2f - radius,
            height / 2f - radius,
            width / 2f + radius,
            height / 2f + radius,
            startAngle,
            sweepAngle,
            false,
            paint
        )
    }

}

enum class MeterType {
    Meter_SpeedoMeter,
    Meter_RotoMeter
}
