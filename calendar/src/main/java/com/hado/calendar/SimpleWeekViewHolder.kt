package com.hado.calendar

import android.support.v7.widget.RecyclerView
import android.view.View
import java.util.*

/**
 * Created by DoanNH on 8/2/2017.
 */

class SimpleWeekViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var drawingParams = HashMap<String, Int>()
        private set

    fun setDrawingParams(drawingParams: HashMap<String, Int>, timezone: String) {
        this.drawingParams = drawingParams
        (itemView as CalendarMonthView).setWeekParams(drawingParams)
    }
}
