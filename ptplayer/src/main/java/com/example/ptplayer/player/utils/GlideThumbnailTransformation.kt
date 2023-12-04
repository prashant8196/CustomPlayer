package com.example.ptplayer.player.utils

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.math.ceil


private const val MAX_COLUMNS = 10
private const val THUMBNAILS_EACH = 5000
class GlideThumbnailTransformation(position: Long, maxLine: Int) : BitmapTransformation() {
    private var MAXLINES = 6
    private val x: Int
    private val y: Int

    fun math(f: Float): Int {
        val c = (f + 0.5f).toInt()
        val n = f + 0.5f
        return if ((n - c) % 2 == 0f) f.toInt() else c
    }
    init {

        MAXLINES = if (maxLine > 0) {
            val maxDivideBy5 = maxLine / 5.toDouble()
            val divideBy10 = maxDivideBy5 / 10.toDouble()
            ceil(divideBy10).toInt()
        } else {
            6
        }

        val square = position.toInt().div(THUMBNAILS_EACH)
        x = square % MAX_COLUMNS
        y = square / MAX_COLUMNS
    }

    override fun transform(
        pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int
    ): Bitmap {
        val width = toTransform.width / MAX_COLUMNS
        val height = toTransform.height / MAXLINES

        return Bitmap.createBitmap(toTransform, x * width, y * height, width, height);
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        val data: ByteArray = ByteBuffer.allocate(8).putInt(x).putInt(y).array()
        messageDigest.update(data)
    }

    override fun hashCode(): Int {
        return (x.toString() + y.toString()).hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is GlideThumbnailTransformation) return false
        return (other.x == x) && (other.y == y)
    }
}