package com.hado.calendar

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife

class MainActivity : AppCompatActivity() {

    @BindView(R.id.rv_calendar)
    lateinit var rvCalendar: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        val currentWeek = TimeUtils.getWeekSinceJulianDay()
        println(TimeUtils.getDaysOfWeek(currentWeek - 1, currentWeek))
    }
}
