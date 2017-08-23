package com.hado.calendar

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import java.util.*

class MainActivity : AppCompatActivity() {

    @BindView(R.id.rv_calendar)
    lateinit var rvCalendar: RecyclerView

    lateinit var adapter: WeeksAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        val currentWeek = TimeUtils.getWeekSinceJulianDay(CalendarMonthView.MONDAY)
        val weekParams = HashMap<String, Int>()
        weekParams.put(WeeksAdapter.WEEK_PARAMS_CURRENT_WEEK, currentWeek)
        weekParams.put(WeeksAdapter.WEEK_PARAMS_WEEK_START, CalendarMonthView.MONDAY)
        weekParams.put(WeeksAdapter.WEEK_PARAMS_TEXT_SIZE, 10)
        weekParams.put(WeeksAdapter.WEEK_PARAMS_EVENT_SHOW_NUMBER, 5)
        adapter = WeeksAdapter(weekParams)
        rvCalendar.adapter = adapter
        rvCalendar.scrollToPosition(currentWeek)
    }

    @OnClick(R.id.btn_check)
    fun check() {
        val layoutManager = rvCalendar.layoutManager as LinearLayoutManager
        val position = layoutManager.findFirstCompletelyVisibleItemPosition()
        val view = layoutManager.findViewByPosition(position)
        println("Top ${view.top}")
    }
}
