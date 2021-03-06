package com.hado.calendar

import android.support.v7.widget.RecyclerView
import android.text.format.Time
import android.view.ViewGroup
import android.widget.AbsListView
import java.security.InvalidParameterException
import java.util.*

/**
 * Created by DoanNH on 8/2/2017.
 */

class WeeksAdapter(params: HashMap<String, Int>) : RecyclerView.Adapter<SimpleWeekViewHolder>() {
    val calendar: Calendar = Calendar.getInstance()

    // The day to highlight as selected
    private var mSelectedDate: Date
    private var mTodayNumber: Int

    // The week since 1970 that the selected day is in
    private var mSelectedWeek: Int = 0
    private var mCurrentWeek: Int = 0

    //0: Sunday, 1: Monday
    private var mFirstDayOfWeek: Int = 0

    init {
        updateParams(params)
        mSelectedDate = calendar.time
        mTodayNumber = calendar.get(Calendar.DAY_OF_WEEK) - 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleWeekViewHolder {
        val view = SimpleWeekView(parent.context)
        val params = AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT)
        view.layoutParams = params
        view.isClickable = true
        view.selectDayListener = this::selectDate
        return SimpleWeekViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimpleWeekViewHolder, position: Int) {
        val drawingParams = holder.drawingParams

        var selectedDay = -1

        if (mSelectedWeek == position) {
            calendar.time = mSelectedDate
            selectedDay = calendar.get(Calendar.DAY_OF_WEEK) - 1
        }

        drawingParams.put(SimpleWeekView.VIEW_PARAMS_WEEK, position)
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_CURRENT_WEEK, mCurrentWeek)
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_WEEK_START, mFirstDayOfWeek)
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_SELECTED_DAY, selectedDay)
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_TODAY_NUMBER, mTodayNumber)
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_TEXT_SIZE, 15)
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_EVEN_SHOW_NUMBER, 10)

        holder.setDrawingParams(drawingParams, calendar.timeZone.displayName)
    }

    private fun selectDate(week: Int, date: Date) {
        val weekSelectedOld = mSelectedWeek
        mSelectedWeek = week
        mSelectedDate = date
        if (weekSelectedOld != week) {
            notifyItemChanged(weekSelectedOld)
        }
    }

    override fun getItemCount(): Int {
        return WEEK_COUNT
    }

    fun updateParams(params: HashMap<String, Int>?) {
        if (params == null) {
            return
        }

        if (params.containsKey(WEEK_PARAMS_CURRENT_WEEK)) {
            mCurrentWeek = params[WEEK_PARAMS_CURRENT_WEEK]!!
        } else {
            throw InvalidParameterException("You must specify the current week number")
        }

        if (params.containsKey(WEEK_PARAMS_WEEK_START)) {
            mFirstDayOfWeek = params[WEEK_PARAMS_WEEK_START]!!
        }

        mSelectedWeek = if (params.containsKey(WEEK_PARAMS_SHOW_WEEK)) params[WEEK_PARAMS_SHOW_WEEK]!! else mCurrentWeek
    }

    private fun refresh() {
        notifyDataSetChanged()
    }

    companion object {
        /**
         * Whether the week number should be shown. Non-zero to show them.
         */
        val WEEK_PARAMS_SHOW_WEEK = "week_numbers"
        /**
         * Which day the week should start on. [Time.SUNDAY] through
         * [Time.SATURDAY].
         */
        val WEEK_PARAMS_WEEK_START = "week_start"

        /**
         * The current week number
         */
        val WEEK_PARAMS_CURRENT_WEEK = "current_week"
        /**
         * The Julian day to highlight as selected.
         */
        val WEEK_PARAMS_JULIAN_DAY = "selected_day"


        private val WEEK_COUNT = 3497

    }
}
