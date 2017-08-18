package com.hado.loadingplayground

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

/**
 * Created by DoanNH on 18-Aug-17.
 */
class MyTextView : View {

    lateinit var paintBackground: Paint
    lateinit var paintText: TextPaint
    lateinit var rect: Rect
    lateinit var staticLayout: StaticLayout

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        val test = "Tại Mỹ thì nhà mạng Sprint và Best Buy đã bắt đầu nhận đặt trước từ hôm nay đối với chiếc smartphone Essential Phone do cha đẻ Android là Andy Rubin phát triển. Quan trọng hơn, ông còn đưa ra một vài thông tin liên quan tới chính sách hỗ trợ sản phẩm và các phụ kiện đi kèm "
        paintBackground = Paint()
        paintBackground.color = Color.GRAY
        paintText = TextPaint()
        paintText.textSize = 24f
        paintText.color = Color.WHITE
        rect = Rect(20, 20, 500, 400)
        staticLayout = StaticLayout(test, paintText, 440, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, false)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(rect, paintBackground)
        canvas.save()
        canvas.translate(40f, 50f)
        staticLayout.draw(canvas)
    }
}