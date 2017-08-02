package com.hado.calendar

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife

/**
 * Created by DoanNH on 8/2/2017.
 */
class WeekViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    @BindView(R.id.tv_mon)
    lateinit var tvMon: TextView

    @BindView(R.id.tv_tue)
    lateinit var tvTue: TextView

    @BindView(R.id.tv_wed)
    lateinit var tvWed: TextView

    @BindView(R.id.tv_thu)
    lateinit var tvThu: TextView

    @BindView(R.id.tv_fri)
    lateinit var tvFri: TextView

    @BindView(R.id.tv_sat)
    lateinit var tvSat: TextView

    @BindView(R.id.tv_sun)
    lateinit var tvSun: TextView


    init {
        ButterKnife.bind(this, view)
    }

    fun bindData(week: WeekData) {
        tvMon.text = week.dates[0].date.day.toString()
        tvTue.text = week.dates[1].date.day.toString()
        tvWed.text = week.dates[2].date.day.toString()
        tvThu.text = week.dates[3].date.day.toString()
        tvFri.text = week.dates[4].date.day.toString()
        tvSat.text = week.dates[5].date.day.toString()
        tvSun.text = week.dates[6].date.day.toString()
    }
}