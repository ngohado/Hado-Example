package com.hado.calendar

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup

/**
 * Created by DoanNH on 8/2/2017.
 */
class CalendarMonthAdapter : RecyclerView.Adapter<WeekViewHolder>() {




    override fun onBindViewHolder(holder: WeekViewHolder?, position: Int) {

    }

    override fun getItemCount(): Int = 3500

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): WeekViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.item_week, parent, false)
        return WeekViewHolder(view)
    }

}