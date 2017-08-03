package com.hado.aswitch

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.support.v4.view.GestureDetectorCompat
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

/**
 * Created by DoanNH on 8/3/2017.
 */
class SwitchButton : View {
    lateinit var bounderRect: RectF
    lateinit var bounderPaint: Paint
    lateinit var circlePaint: Paint
    var circleRadius: Float = 0f
    var rectRadius: Float = 0f
    lateinit var enablePoint: PointF
    lateinit var disablePoint: PointF
    lateinit var currentPoint: PointF

    var enable: Boolean = true

    lateinit var gestureDetector: GestureDetectorCompat


    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        gestureDetector = GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent?): Boolean {
                val start: Float = if (enable) enablePoint.x else disablePoint.x
                val end: Float = if (enable) disablePoint.x else enablePoint.x
                val animation = ValueAnimator.ofFloat(start, end)
                animation.duration = 300
                animation.addUpdateListener { animationValue ->
                    currentPoint.x = animationValue.animatedValue as Float
                    invalidate()
                }
                enable = !enable
                bounderPaint.color = if (enable) Color.GREEN else Color.GRAY
                animation.start()
                return true
            }
        })

    }


    fun init() {
        bounderRect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        rectRadius = height.toFloat() / 2
        circleRadius = (height.toFloat() / 2) - 4

        bounderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        bounderPaint.style = Paint.Style.FILL_AND_STROKE

        circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        circlePaint.style = Paint.Style.FILL
        circlePaint.color = Color.WHITE
        circlePaint.setShadowLayer(5.0f, 0.0f, 2.0f, Color.GRAY)

        enablePoint = PointF(width - rectRadius, rectRadius)
        disablePoint = PointF(rectRadius, rectRadius)

        if (enable) {
            bounderPaint.color = Color.GREEN
            currentPoint = PointF(enablePoint.x, enablePoint.y)
        } else {
            bounderPaint.color = Color.GRAY
            currentPoint = PointF(disablePoint.x, disablePoint.y)
        }
        setLayerType(LAYER_TYPE_SOFTWARE, circlePaint)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        init()
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.drawRoundRect(bounderRect, rectRadius, rectRadius, bounderPaint)
        canvas?.drawCircle(currentPoint.x, currentPoint.y, circleRadius, circlePaint)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)
    }


}
