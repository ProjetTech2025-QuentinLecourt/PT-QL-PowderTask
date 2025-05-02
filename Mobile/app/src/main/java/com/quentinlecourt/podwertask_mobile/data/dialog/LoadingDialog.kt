package com.quentinlecourt.podwertask_mobile.data.dialog

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.lifecycle.LifecycleOwner
import com.quentinlecourt.podwertask_mobile.R
import kotlinx.coroutines.delay
import androidx.lifecycle.lifecycleScope
import com.quentinlecourt.podwertask_mobile.data.model.StatusType
import kotlinx.coroutines.launch

class LoadingDialog(context: Context, private val lifecycleOwner: LifecycleOwner) :
    Dialog(context, R.style.LoadingDialogTheme) {

    private val progressBar: ProgressBar
    private val ivStatus: ImageView
    private val tvLoadingMessage: TextView

    init {
        setContentView(R.layout.loading_dialog)
        setCancelable(false)

        progressBar = findViewById(R.id.progressBar)
        ivStatus = findViewById(R.id.ivStatus)
        tvLoadingMessage = findViewById(R.id.tv_loading_message)

        window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    fun setMessage(message: String) {
        tvLoadingMessage.text = message
    }

    fun setStatus(
        status: StatusType,
        message: String = status.defaultMessage,
        rightNow: Boolean = false
    ) {
        tvLoadingMessage.text = message

        if (rightNow) {
            progressBar.visibility = View.INVISIBLE
            show()
        }

        crossfadeToIcon(status.iconRes)

        lifecycleOwner.lifecycleScope.launch {
            delay(2000)
            dismiss()
        }
    }

    private fun crossfadeToIcon(@DrawableRes iconRes: Int) {
        // Animation de fondu entre ProgressBar et icône
        ivStatus.setImageResource(iconRes)

        val fadeOut = AlphaAnimation(1f, 0f).apply {
            duration = 500
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationEnd(animation: Animation?) {
                    progressBar.visibility = View.INVISIBLE
                }

                override fun onAnimationStart(animation: Animation?) {}
                override fun onAnimationRepeat(animation: Animation?) {}
            })
        }

        val fadeIn = AlphaAnimation(0f, 1f).apply {
            duration = 500
        }

        progressBar.startAnimation(fadeOut)
        ivStatus.startAnimation(fadeIn)
        ivStatus.visibility = View.VISIBLE
    }

    fun animateProgressColor(@ColorInt targetColor: Int) {
        val animator = ValueAnimator.ofArgb(
            progressBar.indeterminateTintList?.defaultColor ?: R.color.bleu_gestion_t_61,
            targetColor
        )
        animator.duration = 1000
        animator.addUpdateListener {
            progressBar.indeterminateTintList = ColorStateList.valueOf(it.animatedValue as Int)
        }
        animator.start()
    }
}