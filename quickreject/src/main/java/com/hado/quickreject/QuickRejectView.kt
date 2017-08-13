package com.hado.quickreject

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

/**
 * Created by Hado on 8/12/17.
 */
class QuickRejectView: View {
    var paintRect: Paint = Paint()
    lateinit var paintSmallRect: Paint

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        paintRect.color = Color.RED

        paintSmallRect = Paint()
        paintSmallRect.color = Color.GREEN
    }

    var i = 0

    override fun onDraw(canvas: Canvas) {

    }

}