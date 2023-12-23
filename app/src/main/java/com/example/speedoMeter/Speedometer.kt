package com.example.speedoMeter

import android.content.Context
import android.graphics.*
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
    private val speedColor = Color.parseColor("#24ECCE")
    private val defaultColor = Color.parseColor("#656565")
    private var currentSpeed = 0
    private val maxSpeed = 180
    var lineSweepAngle = 0f
    var currentNumberOfLines = 0

    private val speedometerPaint = createDesignPaint(20f, defaultColor, Paint.Style.STROKE)
    private val needlePaint = createDesignPaint(8f, defaultColor, Paint.Style.STROKE)

    init {
        needlePaint.style = Paint.Style.FILL
    }

    fun setSpeed(speed: Int) {
        val clampedSpeed = speed.coerceIn(0, 180)

        CoroutineScope(Dispatchers.Main).launch {
            val range = if (currentSpeed <= clampedSpeed) currentSpeed..clampedSpeed else currentSpeed downTo clampedSpeed

            for (i in range) {
                currentSpeed = i
                invalidate()
                delay(15)
            }

            currentSpeed = clampedSpeed
            invalidate()
        }
    }

    private fun calculateNumberOfLines(speed: Int) = minOf((speed / 4) + 1, 46)

    private fun calculateSweepAngleForArc(speed: Int) = min((speed.toFloat() / maxSpeed) * 260f, 260f)

    private fun calculateSweepAngleForLines(speed: Int) = calculateSweepAngleNow(if (speed >= 180) 180 else speed)

    private fun calculateSweepAngleNow(speed: Int): Float {
        val sweepAngle = 260f
        return (if (speed % 4 == 0) speed else speed - (speed % 4)).toFloat() / maxSpeed * sweepAngle
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
        val text = currentSpeed.toString()
        val mphText = "mph"
        val arcSweepAngle = calculateSweepAngleForArc(currentSpeed)
        val newNUmberOfLines = calculateNumberOfLines(currentSpeed)
        val yPosMph = height / 2f + 50f
        val interval = 20
        val numberOfLines = 46
        var lineLength = 60f
        lineSweepAngle = calculateSweepAngleForLines(currentSpeed)

        canvas.drawColor(Color.BLACK)
        drawArc(speedometerPaint, startAngle, sweepAngle, increasedRadius)
        drawArc(createDesignPaint(20f, speedColor, Paint.Style.STROKE), startAngle, arcSweepAngle, radius)
        canvas.drawCircle(width / 2f, height / 2f, radius / 2.5f, speedometerPaint)
        canvas.drawText(mphText, width / 2f, yPosMph, createTextPaint(45f, Color.WHITE, Paint.Align.CENTER))
        canvas.drawText(text, width / 2f, height / 2f, createTextPaint(120f, Color.WHITE, Paint.Align.CENTER))

        for (i in 0..180 step interval) {
            val angle = startAngle + ((i).toFloat() / 180f) * sweepAngle
            val xPos = width / 2f + (increasedRadius / 1.75f) * cos(Math.toRadians(angle.toDouble())).toFloat()
            val yPos = height / 2f + (increasedRadius / 1.75f) * sin(Math.toRadians(angle.toDouble())).toFloat()

            canvas.drawText(i.toString(), xPos, yPos, createTextPaint(50f, Color.WHITE, Paint.Align.CENTER, Typeface.create(Typeface.SERIF, Typeface.ITALIC)))
        }

        drawNumberLines(startAngle, numberOfLines, sweepAngle, lineLength, increasedRadius, createDesignPaint(8f, defaultColor, Paint.Style.STROKE))
        drawNumberLines(startAngle, newNUmberOfLines, lineSweepAngle, lineLength, increasedRadius, createDesignPaint(8f, speedColor, Paint.Style.STROKE))

        currentNumberOfLines = min(currentNumberOfLines, newNUmberOfLines - 1)
    }

    private fun drawNumberLines(startAngle: Float, numberOfLines: Int, sweepAngle: Float, lineLength: Float, radius: Float, paint: Paint) {
        for (i in 0 until numberOfLines) {
            val angle = (startAngle + i.toFloat() / (numberOfLines - 1) * sweepAngle).toDouble()
            val newLineLength = lineLength + if (i % 5 == 0 || i == 0) 45 else 0

            val lineStartX = width / 2f + (radius - 30f) * cos(toRadians(angle)).toFloat()
            val lineStartY = height / 2f + (radius - 30f) * sin(toRadians(angle)).toFloat()

            val lineEndX = width / 2f + (radius - newLineLength) * cos(toRadians(angle)).toFloat()
            val lineEndY = height / 2f + (radius - newLineLength) * sin(toRadians(angle)).toFloat()

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
