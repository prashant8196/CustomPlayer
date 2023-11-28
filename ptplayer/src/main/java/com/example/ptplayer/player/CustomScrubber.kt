package com.example.ptplayer.player

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("ClickableViewAccessibility")
class CustomScrubber(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(
        context as AppCompatActivity, attrs, 0
    )

    private val scrubberPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var progress = 0f
    private var duration = 0L

    var onScrubListener: ((Float) -> Unit)? = null

    init {
        scrubberPaint.color = Color.YELLOW
        textPaint.color = Color.WHITE
        textPaint.textSize = 30f
        setOnTouchListener { _, event ->
            handleTouchEvent(event)
            true
        }
    }

    private fun handleTouchEvent(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // Calculate the progress based on the touch position
                val touchX = event.x.coerceIn(0f, width.toFloat())
                val progress = touchX / width

                setProgress(progress, duration)
                onScrubListener?.invoke(progress)
            }
        }
    }

    fun setProgress(progress: Float, duration: Long) {
        this.progress = progress
        this.duration = duration
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the scrubber line
        val scrubberHeight = height / 2
        canvas.drawLine(0f, scrubberHeight.toFloat(), width.toFloat(), scrubberHeight.toFloat(), scrubberPaint)

        // Draw the progress indicator
        val indicatorX = progress * width
        canvas.drawCircle(indicatorX, scrubberHeight.toFloat(), 10f, scrubberPaint)

        // Draw the current duration text
        val currentTime = (progress * duration).toLong()
        val text = formatDuration(currentTime)
        val textWidth = textPaint.measureText(text)
        val textX = indicatorX - textWidth / 2
        val textY = scrubberHeight.toFloat() - 20f // Adjust vertical position as needed
        canvas.drawText(text, textX, textY, textPaint)
    }

    private fun formatDuration(duration: Long): String {
        val minutes = duration / 60000
        val seconds = (duration % 60000) / 1000
        return String.format("%02d:%02d", minutes, seconds)
    }
}
