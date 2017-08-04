package com.hado.calendar

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import java.util.*

class MainActivity : AppCompatActivity() {

    @BindView(R.id.rv_calendar)
    lateinit var rvCalendar: RecyclerView

    lateinit var adapter: WeeksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        val currentWeek = TimeUtils.getWeekSinceJulianDay()
        val weekParams = HashMap<String, Int>()
        weekParams.put(WeeksAdapter.WEEK_PARAMS_SHOW_WEEK, currentWeek)
        weekParams.put(WeeksAdapter.WEEK_PARAMS_WEEK_START, 0)
        adapter = WeeksAdapter(this, weekParams)
        rvCalendar.adapter = adapter
    }
}
