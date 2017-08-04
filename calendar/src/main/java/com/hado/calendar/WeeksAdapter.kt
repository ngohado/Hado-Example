package com.hado.calendar

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.format.Time
import android.view.ViewGroup
import android.widget.AbsListView
import java.security.InvalidParameterException
import java.util.*

/**
 * Created by DoanNH on 8/2/2017.
 */

class WeeksAdapter(private val mContext: Context, params: HashMap<String, Int>) : RecyclerView.Adapter<SimpleWeekViewHolder>() {
    val calendar: Calendar = Calendar.getInstance()

    // The day to highlight as selected
    val mSelectedDay: Date = Calendar.getInstance().time

    // The week since 1970 that the selected day is in
    var mSelectedWeek: Int = 0
    var mCurrentWeek: Int = 0

    //0: Sunday, 1: Monday
    private var mFirstDayOfWeek: Int = 0

    private var mFocusMonth = DEFAULT_MONTH_FOCUS


    init {
        calendar.firstDayOfWeek = if (mFirstDayOfWeek == 0) Calendar.SUNDAY else Calendar.MONDAY
        calendar.time = mSelectedDay
        updateParams(params)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleWeekViewHolder {
        val view = SimpleWeekView(parent.context)
        val params = AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT)
        view.layoutParams = params
        view.isClickable = true
        return SimpleWeekViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimpleWeekViewHolder, position: Int) {
        val drawingParams = holder.drawingParams

        var selectedDay = -1

        if (mSelectedWeek == position) {
            selectedDay = calendar.get(Calendar.DAY_OF_WEEK) - 1
        }

        drawingParams.put(SimpleWeekView.VIEW_PARAMS_HEIGHT, 250)
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_SELECTED_DAY, selectedDay)
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_WEEK_START, mFirstDayOfWeek)
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_WEEK, position)
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_CURRENT_WEEK, mCurrentWeek)
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_FOCUS_MONTH, mFocusMonth)

        holder.setDrawingParams(drawingParams, calendar.timeZone.displayName)
        holder.invalidate()
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

        if (params.containsKey(WEEK_PARAMS_FOCUS_MONTH)) {
            mFocusMonth = params[WEEK_PARAMS_FOCUS_MONTH]!!
        }

        if (params.containsKey(WEEK_PARAMS_WEEK_START)) {
            mFirstDayOfWeek = params[WEEK_PARAMS_WEEK_START]!!
        }

        if (params.containsKey(WEEK_PARAMS_SHOW_WEEK)) {
            mSelectedWeek = params[WEEK_PARAMS_SHOW_WEEK]!!
        } else {
            mSelectedWeek = mCurrentWeek
        }

        refresh()
    }

    private fun refresh() {
        notifyDataSetChanged()
    }

    companion object {
        /**
         * Which month should be in focus currently.
         */
        val WEEK_PARAMS_FOCUS_MONTH = "focus_month"
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

        private val DEFAULT_MONTH_FOCUS = 0

        private val mScale = 0f
    }
}
