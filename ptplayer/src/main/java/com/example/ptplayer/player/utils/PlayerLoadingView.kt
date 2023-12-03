package com.example.ptplayer.player.utils

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import com.example.ptplayer.databinding.LayoutLoadingBinding

class LoadingView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    private val binding: LayoutLoadingBinding =
        LayoutLoadingBinding.inflate(LayoutInflater.from(context), this, true)

  /*  fun setLoadingTitle(string: String) {
        with(binding) {
            tvMessage.isVisible = string.isNotBlank()
            if (string.isBlank()) {
                return
            }
            tvMessage.text = string
        }
    }*/
}