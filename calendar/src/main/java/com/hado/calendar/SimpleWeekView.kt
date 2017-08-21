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


class SimpleWeekView(context: Context) : View(context) {
    var selectDayListener: ((week: Int, date: Date) -> Unit)? = null

    private var mSelectedDayRect = Rect()
    private var mCurrentDayRect = Rect()
    private var mSelectedDayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mCurrentDayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mDayNumberPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var mSeparatorVerticalPaint = Paint(Paint.ANTI_ALIAS_FLAG)
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

    private var mFutureDayColor: Int = 0
    private var mTodayColor: Int = 0
    private var mPastDayColor: Int = 0
    private var mSelectedDayColor: Int = 0
    private var mScale = 0f

    init {
        //Use to apply effect line dashed
        setLayerType(View.LAYER_TYPE_SOFTWARE, mSelectedDayPaint)
        mTodayColor = Color.parseColor("#F18D00")
        mFutureDayColor = ContextCompat.getColor(context, R.color.month_mini_day_number)
        mPastDayColor = ContextCompat.getColor(context, R.color.month_other_month_day_number)
        mSelectedDayColor = Color.parseColor("#5C5953")

        mScale = context.resources.displayMetrics.density
    }

    /**
     * Sets all the parameters for displaying this week. The only required
     * parameter is the week number. Other parameters have a default value and
     * will only update if a new value is included, except for focus month,
     * which will always default to no focus month if no value is passed in. See
     * [.VIEW_PARAMS_HEIGHT] for more info on parameters.
     * @param params A map of the new parameters, see
     * *               [.VIEW_PARAMS_HEIGHT]
     * *
     */
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

        if (params.containsKey(VIEW_PARAMS_EVEN_SHOW_NUMBER)) {
            eventShowNumber = params[VIEW_PARAMS_EVEN_SHOW_NUMBER]!!
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
        val h = padding * 3.5f + textHeight + showNumber * (tHeight + 2 * paddingEvent)
        return h.toInt()
    }

    /**
     * Sets up the text and style properties for painting. Override this if you
     * want to use a different paint.
     */
    private fun initView() {
        mSelectedDayPaint.strokeWidth = 4f
        mSelectedDayPaint.style = Style.STROKE
        mSelectedDayPaint.color = mSelectedDayColor

        mCurrentDayPaint.style = Style.FILL
        mCurrentDayPaint.color = Color.parseColor("#FFFEC4")

        mSeparatorVerticalPaint.style = Style.STROKE
        mSeparatorVerticalPaint.strokeWidth = 1f * mScale
        mSeparatorVerticalPaint.pathEffect = DashPathEffect(floatArrayOf(7f, 7f), 0f)
        mSeparatorVerticalPaint.color = Color.parseColor("#cccccc")

        mSeparatorMonthPaint.style = Style.STROKE
        mSeparatorMonthPaint.strokeWidth = 1 * mScale
        mSeparatorMonthPaint.color = Color.parseColor("#5C5953")

        mDayNumberPaint.textSize = textSize
        mDayNumberPaint.style = Style.FILL
    }

    /**
     * Calculates the day that the given x position is in, accounting for week
     * number. Returns a Time referencing that day or null if
     * @param x The x position of the touch event
     * *
     * @return A time object for the tapped day or null if the position wasn't
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
        for (i in 0 until mNumCells) {
            //define color of past day and future day
            when {
                mWeek < mCurrentWeek -> mDayNumberPaint.color = mPastDayColor
                mWeek > mCurrentWeek -> mDayNumberPaint.color = mFutureDayColor
                mWeek == mCurrentWeek && i >= mTodayPosition -> if (mTodayPosition == i) {
                    mDayNumberPaint.color = mTodayColor
                } else {
                    mDayNumberPaint.color = mFutureDayColor
                }
                else -> mDayNumberPaint.color = mPastDayColor //else that mean: this view is current week and the day at "i" index is past day
            }

            //define color of weekend day
            when (mWeekStart) {
                0 -> { //start with sunday
                    if (i == mNumCells - 1) mDayNumberPaint.color = Color.BLUE
                    if (i == 0) mDayNumberPaint.color = Color.RED
                }

                1 -> { //start with monday
                    if (i == mNumCells - 2) mDayNumberPaint.color = Color.BLUE
                    if (i == mNumCells - 1) mDayNumberPaint.color = Color.RED
                }
            }

            //define alpha of paint depend on past day and future day
            when {
                mWeek < mCurrentWeek -> mDayNumberPaint.alpha = 255 / 2
                mWeek > mCurrentWeek -> mDayNumberPaint.alpha = 255
                mWeek == mCurrentWeek && i >= mTodayPosition -> mDayNumberPaint.alpha = 255
                else -> mDayNumberPaint.alpha = 255 / 2 //else that mean: this view is current week and the day at "i" index is past day
            }

            val y = textHeight + padding * 1.5f
            val x = i * mCellWidth + padding
            canvas.drawText(mDayNumbers[i], x.toFloat(), y, mDayNumberPaint)

            if (i == 0) continue

            canvas.drawLine((i * mCellWidth).toFloat(), 0f, (i * (mCellWidth)).toFloat(), mHeight.toFloat(), mSeparatorVerticalPaint)
        }
        canvas.drawPath(mSeparatorMonthPath, mSeparatorMonthPaint)
        canvas.drawLine(0f, mHeight.toFloat(), width.toFloat(), mHeight.toFloat(), mSeparatorVerticalPaint)
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (mWidth == w) return

        mWidth = w
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
        for (i in 0 until mNumCells)
            when {
                i < firstDayOfMonthNumber -> mSeparatorMonthPath.lineTo((i + 1) * mCellWidth.toFloat(), mHeight - halfStrokeWidth * 2)
                i > firstDayOfMonthNumber -> mSeparatorMonthPath.lineTo((i + 1) * mCellWidth.toFloat(), halfStrokeWidth)
                else -> { // i is first day of month number
                    mSeparatorMonthPath.lineTo(i * mCellWidth.toFloat(), halfStrokeWidth)
                    mSeparatorMonthPath.lineTo((i + 1) * mCellWidth.toFloat(), halfStrokeWidth)
                }
            }
    }

    /**
     * This calculates the positions for the selected day lines.
     */
    private fun updateSelectionPositions() {
        if (mHasSelectedDay) {
            val selectedPosition = getDayPosition(mSelectedDay)
            val selectedLeft = selectedPosition * mWidth / mNumCells
            val selectedRight = (selectedPosition + 1) * mWidth / mNumCells
            mSelectedDayRect.top = 1
            mSelectedDayRect.bottom = mHeight - 1
            mSelectedDayRect.left = selectedLeft + 1
            mSelectedDayRect.right = selectedRight - 1
        }
    }

    /**
     * This calculates the positions for the current day lines.
     */
    private fun updateCurrentDayPositions() {
        if (mTodayPosition != -1) {
            var currentDayPosition = mTodayPosition
            if (currentDayPosition < 0) {
                currentDayPosition += 7
            }
            val currentDayLeft = currentDayPosition * mWidth / mNumCells
            val currentDayRight = (currentDayPosition + 1) * mWidth / mNumCells
            mCurrentDayRect.top = 1
            mCurrentDayRect.bottom = mHeight - 1
            mCurrentDayRect.left = currentDayLeft + 1
            mCurrentDayRect.right = currentDayRight - 1
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
                        calendar.firstDayOfWeek = if (mWeekStart == 0) Calendar.SUNDAY else Calendar.MONDAY
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

        val VIEW_PARAMS_EVEN_SHOW_NUMBER = "even_show_number"

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