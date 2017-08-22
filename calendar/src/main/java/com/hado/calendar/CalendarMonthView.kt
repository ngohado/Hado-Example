/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hado.calendar

import android.content.Context
import android.graphics.*
import android.graphics.Paint.Style
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.view.MotionEvent
import android.view.View
import java.security.InvalidParameterException
import java.util.*


class CalendarMonthView(context: Context) : View(context) {
    var selectDayListener: ((week: Int, date: Date) -> Unit)? = null

    private var mSelectedDayRect = Rect()
    private var mCurrentDayRect = Rect()
    private var mSelectedDayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mCurrentDayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mDayNumberPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var mDashedSeparatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mSeparatorMonthPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Cache the number strings so we don't have to recompute them each time
    private lateinit var mDayNumbers: Array<String>

    // The position week of this view
    private var mWeek = -1
    // The number of current week, that mean NOW
    private var mCurrentWeek = -1
    // Quick reference to the width of this view, matches parent
    private var mWidth: Int = 0
    private var mCellWidth: Int = 0
    // The height this view should draw at in pixels, set by height param
    private var mHeight: Int = DEFAULT_HEIGHT
    // If this view contains the selected day
    private var mHasSelectedDay: Boolean = false
    // Which day is selected [0-6] or -1 if no day is selected
    private var mSelectedDay: Int = DEFAULT_SELECTED_DAY
    // Which day is today [0-6] or -1 if no day is today
    private var mTodayPosition: Int = DEFAULT_SELECTED_DAY
    // Which day of the week to start on [0-6]
    private var mWeekStart: Int = DEFAULT_WEEK_START
    // The number of days + a spot for week number if it is displayed
    private var mNumCells: Int = 7

    private var padding: Int = DEFAULT_PADDING
        get() = (field * mScale).toInt()

    private var paddingEvent: Int = DEFAULT_EVENT_PADDING
        get() = (field * mScale).toInt()

    private var eventShowNumber: Int = DEFAULT_EVENT_SHOW_NUMBER

    private var textHeight: Int = 0
    private var textSize: Float = DEFAULT_TEXT_SIZE
        get() = field * mScale

    private var mSeparatorMonthPath: Path = Path()

    private var mNormalDayColor: Int = 0
    private var mTodayColor: Int = 0
    private var mSaturdayColor: Int = 0
    private var mSundayColor: Int = 0
    private var mSelectedDayColor: Int = 0
    private var mScale = 0f

    init {
        //Use to apply effect line dashed
        setLayerType(View.LAYER_TYPE_SOFTWARE, mSelectedDayPaint)
        mTodayColor = ContextCompat.getColor(context, R.color.today_number)
        mNormalDayColor = ContextCompat.getColor(context, R.color.normal_day_number)
        mSaturdayColor = ContextCompat.getColor(context, R.color.saturday_number)
        mSundayColor = ContextCompat.getColor(context, R.color.sunday_number)
        mSelectedDayColor = ContextCompat.getColor(context, R.color.selected_day)

        mScale = context.resources.displayMetrics.density
    }

    fun setWeekParams(params: HashMap<String, Int>) {
        if (!params.containsKey(VIEW_PARAMS_WEEK)) {
            throw InvalidParameterException("You must specify the week number for this view")
        }

        if (params.containsKey(VIEW_PARAMS_CURRENT_WEEK)) {
            mCurrentWeek = params[VIEW_PARAMS_CURRENT_WEEK]!!
        }

        if (params.containsKey(VIEW_PARAMS_SELECTED_DAY)) {
            mSelectedDay = params[VIEW_PARAMS_SELECTED_DAY]!!
        }
        mHasSelectedDay = mSelectedDay != -1

        // Allocate space for caching the day numbers and focus values
        mDayNumbers = Array(mNumCells, { "" })
        if (params.containsKey(VIEW_PARAMS_WEEK)) {
            mWeek = params[VIEW_PARAMS_WEEK]!!
        } else {
            throw InvalidParameterException("You must specify the week number for this view")
        }

        if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
            mWeekStart = params[VIEW_PARAMS_WEEK_START]!!
        }
        val daysOfWeek: ArrayList<Date> = TimeUtils.getDaysOfWeek(mWeek, mCurrentWeek, mWeekStart)
        for (i in 0 until daysOfWeek.size) {
            mDayNumbers[i] = TimeUtils.getDateNumber(Calendar.getInstance(), daysOfWeek[i])
        }

        mTodayPosition = -1
        if (mWeek == mCurrentWeek) {
            mTodayPosition = getDayPosition(params[VIEW_PARAMS_TODAY_NUMBER]!!)
        }

        if (params.containsKey(VIEW_PARAMS_EVENT_SHOW_NUMBER)) {
            eventShowNumber = params[VIEW_PARAMS_EVENT_SHOW_NUMBER]!!
        }

        if (params.containsKey(VIEW_PARAMS_TEXT_SIZE)) {
            textSize = params[VIEW_PARAMS_TEXT_SIZE]!!.toFloat()
        }

        textHeight = getTextHeight(textSize)

        mHeight = calculateHeight(eventShowNumber, textHeight)

        initView()
        updateSelectionPositions()
        updateCurrentDayPositions()
        updateSeparatorMonth()
    }

    private fun calculateHeight(showNumber: Int, tHeight: Int): Int {
        val h = padding * 3.5f + tHeight + showNumber * (tHeight + 2 * paddingEvent)
        return h.toInt()
    }

    /**
     * Sets up the text and style properties for painting. Override this if you
     * want to use a different paint.
     */
    private fun initView() {
        mSelectedDayPaint.strokeWidth = 1f * mScale
        mSelectedDayPaint.style = Style.STROKE
        mSelectedDayPaint.color = mSelectedDayColor

        mCurrentDayPaint.style = Style.FILL
        mCurrentDayPaint.color = Color.parseColor("#FFFEC4")

        mDashedSeparatorPaint.style = Style.STROKE
        mDashedSeparatorPaint.strokeWidth = 1f * mScale
        mDashedSeparatorPaint.pathEffect = DashPathEffect(floatArrayOf(7f, 7f), 0f)
        mDashedSeparatorPaint.color = Color.parseColor("#cccccc")

        mSeparatorMonthPaint.style = Style.STROKE
        mSeparatorMonthPaint.strokeWidth = 1f * mScale
        mSeparatorMonthPaint.color = Color.parseColor("#5C5953")

        mDayNumberPaint.textSize = textSize
        mDayNumberPaint.style = Style.FILL
    }

    /**
     * Calculates the day that the given x position is in, accounting for week
     * number. Returns a Time referencing that day or null if
     * @param x The x position of the touch event
     * @return A Date object for the tapped day or null if the position wasn't
     * * in a day
     */
    private fun getDayFromLocation(x: Float): Date? {
        // Selection is (x) / (pixels/day) == (x -s) * day / pixels
        val dayPosition = (x * mNumCells / mWidth).toInt()
        return TimeUtils.getDaysOfWeek(mWeek, mCurrentWeek, mWeekStart)[dayPosition]
    }

    override fun onDraw(canvas: Canvas) {
        drawCurrentDayBackground(canvas)
        drawDayNumber(canvas)
        drawSelectedDaySeparator(canvas)
    }


    private fun drawDayNumber(canvas: Canvas) {
        for (position in 0 until mNumCells) {
            mDayNumberPaint.color = mNormalDayColor

            //define color of weekend day number
            when (mWeekStart) {
                SUNDAY -> { //start with sunday
                    if (position == mNumCells - 1) mDayNumberPaint.color = mSaturdayColor
                    else if (position == 0) mDayNumberPaint.color = mSundayColor
                }

                MONDAY -> { //start with monday
                    if (position == mNumCells - 2) mDayNumberPaint.color = mSaturdayColor
                    else if (position == mNumCells - 1) mDayNumberPaint.color = mSundayColor
                }
            }

            //define color of today number
            if (position == mTodayPosition) mDayNumberPaint.color = mTodayColor

            //define alpha of paint depend on past day and future day
            when {
                mWeek < mCurrentWeek -> mDayNumberPaint.alpha = 255 / 2
                mWeek > mCurrentWeek -> mDayNumberPaint.alpha = 255
                mWeek == mCurrentWeek && position >= mTodayPosition -> mDayNumberPaint.alpha = 255
                else -> mDayNumberPaint.alpha = 255 / 2 //else that mean: this view is current week and the day at "i" index is past day
            }

            val y = textHeight + padding * 1.5f
            val x = position * mCellWidth + padding
            canvas.drawText(mDayNumbers[position], x.toFloat(), y, mDayNumberPaint)

            if (position > 0) { //draw vertical separator between two days
                canvas.drawLine(position * mCellWidth.toFloat(), 0f, position * mCellWidth.toFloat(), mHeight.toFloat(), mDashedSeparatorPaint)
            }
        }

        if (!mSeparatorMonthPath.isEmpty) { //draw separator between two months
            canvas.drawPath(mSeparatorMonthPath, mSeparatorMonthPaint)
        }

        //draw horizontal separator between two weeks
        canvas.drawLine(0f, mHeight.toFloat(), mWidth.toFloat(), mHeight.toFloat(), mDashedSeparatorPaint)
    }

    private fun drawCurrentDayBackground(canvas: Canvas) {
        if (mTodayPosition != -1) {
            canvas.drawRect(mCurrentDayRect, mCurrentDayPaint)
        }
    }

    private fun drawSelectedDaySeparator(canvas: Canvas) {
        if (mHasSelectedDay) {
            canvas.drawRect(mSelectedDayRect, mSelectedDayPaint)
        }
    }

    override fun onSizeChanged(width: Int, h: Int, oldWidth: Int, oldHeight: Int) {
        if (mWidth == width) return

        mWidth = width
        mCellWidth = mWidth / mNumCells
        updateSelectionPositions()
        updateCurrentDayPositions()
        updateSeparatorMonth()
    }

    /**
     * This method update path of separator between two month
     */
    private fun updateSeparatorMonth() {
        mSeparatorMonthPath.reset()
        val halfStrokeWidth = mSeparatorMonthPaint.strokeWidth / 2
        //because the text of first day of month is eg: "Aug 1", the length is always larger than 2
        val firstDayOfMonthNumber: Int = mDayNumbers.indexOfFirst { dayNumberString ->
            dayNumberString.length > 2
        }

        //if current week is'nt contain a fist day of month then return
        if (firstDayOfMonthNumber == -1) return

        mSeparatorMonthPath.moveTo(0f, if (firstDayOfMonthNumber == 0) halfStrokeWidth else (mHeight - halfStrokeWidth * 2))
        for (position in 0 until mNumCells)
            when {
                position < firstDayOfMonthNumber -> mSeparatorMonthPath.lineTo((position + 1) * mCellWidth.toFloat(), mHeight - halfStrokeWidth * 2)
                position > firstDayOfMonthNumber -> mSeparatorMonthPath.lineTo((position + 1) * mCellWidth.toFloat(), halfStrokeWidth)
                else -> { // i is first day of month number
                    mSeparatorMonthPath.lineTo(position * mCellWidth.toFloat(), halfStrokeWidth)
                    mSeparatorMonthPath.lineTo((position + 1) * mCellWidth.toFloat(), halfStrokeWidth)
                }
            }
    }

    /**
     * This calculates the positions for the selected day lines.
     */
    private fun updateSelectionPositions() {
        if (mHasSelectedDay) {
            val selectedPosition = getDayPosition(mSelectedDay)
            val selectedLeft = selectedPosition * mCellWidth
            val selectedRight = (selectedPosition + 1) * mCellWidth
            mSelectedDayRect.top = mSelectedDayPaint.strokeWidth.toInt()
            mSelectedDayRect.bottom = mHeight - mSelectedDayPaint.strokeWidth.toInt()
            mSelectedDayRect.left = selectedLeft
            mSelectedDayRect.right = selectedRight
        }
    }

    /**
     * This calculates the positions for the current day lines.
     */
    private fun updateCurrentDayPositions() {
        if (mTodayPosition != -1) {
            val currentDayLeft = mTodayPosition * mCellWidth
            val currentDayRight = (mTodayPosition + 1) * mCellWidth
            mCurrentDayRect.top = 0
            mCurrentDayRect.bottom = mHeight
            mCurrentDayRect.left = currentDayLeft
            mCurrentDayRect.right = currentDayRight
        }
    }

    private fun getDayPosition(dayNumber: Int): Int {
        var position = dayNumber - mWeekStart
        if (position < 0) position += 7
        return position
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), mHeight)
    }

    private var clickDownTime: Long = 0L
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                clickDownTime = System.currentTimeMillis()
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (System.currentTimeMillis() - clickDownTime < CLICK_TIME_OUT) {
                    val dateSelected = getDayFromLocation(event.rawX)
                    if (dateSelected != null) {
                        selectDayListener?.invoke(mWeek, dateSelected)
                        val calendar = Calendar.getInstance()
                        calendar.firstDayOfWeek = if (mWeekStart == SUNDAY) Calendar.SUNDAY else Calendar.MONDAY
                        calendar.time = dateSelected
                        mHasSelectedDay = true
                        mSelectedDay = calendar.get(Calendar.DAY_OF_WEEK) - 1
                        updateSelectionPositions()
                        invalidate()
                    }
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    fun setSeparateDayColor(color: Int) {
        this.mSeparatorMonthPaint.color = color
    }

    companion object {
        val CLICK_TIME_OUT = 500

        val VIEW_PARAMS_WEEK = "week"

        val VIEW_PARAMS_CURRENT_WEEK = "current_week"

        val VIEW_PARAMS_TODAY_NUMBER = "today_number"

        val VIEW_PARAMS_SELECTED_DAY = "selected_day"

        val VIEW_PARAMS_WEEK_START = "week_start"

        val VIEW_PARAMS_EVENT_SHOW_NUMBER = "even_show_number"

        val VIEW_PARAMS_TEXT_SIZE = "text_size"

        val DEFAULT_EVENT_SHOW_NUMBER = 5

        val DEFAULT_HEIGHT = 32

        val DEFAULT_SELECTED_DAY = -1

        val DEFAULT_WEEK_START = Calendar.SUNDAY - 1

        val DEFAULT_PADDING = 5

        val DEFAULT_EVENT_PADDING = 2

        val DEFAULT_TEXT_SIZE = 8f

        val SUNDAY = 0

        val MONDAY = 1

        fun getTextHeight(size: Float): Int {
            val paint = Paint()
            paint.textSize = size
            val textBound = Rect()
            paint.getTextBounds("99", 0, 2, textBound)
            return textBound.height()
        }
    }
}