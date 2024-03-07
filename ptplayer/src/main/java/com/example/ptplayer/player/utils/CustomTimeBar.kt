package com.example.ptplayer.player.utils
import android.content.Context
import android.util.AttributeSet
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.DefaultTimeBar
@UnstableApi
class CustomTimeBar : DefaultTimeBar {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: android.graphics.Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        val newHeight = if (focused) 10.dpToPx(context) else 2.dpToPx(context)
        layoutParams.height = newHeight
        requestLayout()
    }


}

fun Int.dpToPx(context: Context): Int {
    val density = context.resources.displayMetrics.density
    return (this * density).toInt()
}
