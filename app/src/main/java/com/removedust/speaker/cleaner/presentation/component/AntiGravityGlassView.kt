package com.removedust.speaker.cleaner.presentation.component

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Outline
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.removedust.speaker.cleaner.R
import eightbitlab.com.blurview.BlurView
import eightbitlab.com.blurview.RenderScriptBlur


/**
 * A reusable UI component that achieves a glassmorphism effect.
 * Encapsulates the Dimezis BlurView setup, custom shadow/border background
 * matching the design parameters exactly (Frost: 4, Corner Radius: 8, Shadow X: -1, Y: 4, Blur: 16).
 * Dynamically draws a diagonal linear gradient border stroke to achieve
 * the Figma highlight effect (where the top-right and bottom-left corners appear faded/blurry).
 */
class AntiGravityGlassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val blurView: BlurView
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    var isGlassBorderVisible: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    // Frost setting from design image is 4
    var blurRadius: Float = 4f
        set(value) {
            field = value
            if (isAttachedToWindow) {
                blurView.setBlurRadius(value)
                blurView.invalidate()
            }
        }

    // Corner radius from design image is 8
    var cornerRadiusDp: Float = 8f
        set(value) {
            field = value
            updateOutlines()
            invalidate()
        }

    init {
        // 1. Enable onDraw execution for FrameLayout
        setWillNotDraw(false)

        // 2. Set the background containing the Shadow (X: -1, Y: 4)
        background = ContextCompat.getDrawable(context, R.drawable.bg_glass_card)

        // 3. Create and add the BlurView programmatically
        blurView = BlurView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        }
        addView(blurView)

        updateOutlines()
        
        // Crucial: false so that custom soft shadow in the background is not clipped
        clipToOutline = false 
    }

    private fun updateOutlines() {
        val density = resources.displayMetrics.density
        val radiusPx = cornerRadiusDp * density

        // Outline provider for the parent layout to support rounded outlines
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radiusPx)
            }
        }

        // Set rounded outline for the BlurView itself so the blurred content is clipped
        blurView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radiusPx)
            }
        }
        blurView.clipToOutline = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val density = resources.displayMetrics.density
        val strokeWidthPx = 1.5f * density
        borderPaint.strokeWidth = strokeWidthPx
        
        // Linear gradient from top-left (0,0) to bottom-right (w,h).
        // Keeps the top and left borders solid white (light-facing edges),
        // and fades out the right and bottom borders completely to transparent (shadow/refraction edges).
        val gradient = LinearGradient(
            0f, 0f,
            w.toFloat() * 1.5f, h.toFloat(),
            intArrayOf(
                Color.WHITE,
                Color.WHITE,
                Color.WHITE,
                Color.WHITE
            ),
            floatArrayOf(
                0.0f,
                0f,
                0f,
                0.5f
            ),
            Shader.TileMode.CLAMP
        )
        borderPaint.shader = gradient
        updateOutlines()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isGlassBorderVisible) return

        val density = resources.displayMetrics.density
        val radiusPx = cornerRadiusDp * density
        val strokeWidthPx = borderPaint.strokeWidth
        
        // Position the stroke inside the boundaries
        val rect = RectF(
            strokeWidthPx / 2f, 
            strokeWidthPx / 2f, 
            width.toFloat() - strokeWidthPx / 2f, 
            height.toFloat() - strokeWidthPx / 2f
        )
        canvas.drawRoundRect(rect, radiusPx, radiusPx, borderPaint)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setupBlur()
    }

    private fun setupBlur() {
        val activity = context as? Activity ?: return
        val decorView = activity.window.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
        
        // Explicitly load the app's light background gradient to resolve the dark window background clear bug.
        val clearDrawable = ContextCompat.getDrawable(context, R.drawable.bg_app_gradient) ?: decorView.background

        // Setup the blur view with the root view
        blurView.setupWith(rootView, RenderScriptBlur(context))
            .setFrameClearDrawable(clearDrawable)
            .setBlurRadius(blurRadius)
    }

    /**
     * Helper to configure the view specifically for tab toggles.
     * Manages background states (selected vs. unselected).
     */
    fun setGlassTabState(selected: Boolean) {
        cornerRadiusDp = 8f // Tab items use 8dp corner radius based on Figma specs
        
        if (selected) {
            background = ContextCompat.getDrawable(context, R.drawable.bg_glass_tab_selected)
            blurView.visibility = View.INVISIBLE // Hide blur since it's solid blue
            isGlassBorderVisible = false
        } else {
            background = ContextCompat.getDrawable(context, R.drawable.bg_glass_tab_unselected)
            blurView.visibility = View.VISIBLE // Show blur for glassmorphic unselected state
            isGlassBorderVisible = true
        }
    }
}
