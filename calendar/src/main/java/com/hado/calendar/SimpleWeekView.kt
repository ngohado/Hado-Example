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
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.view.MotionEvent
import android.view.View
import java.security.InvalidParameterException
import java.util.*


class SimpleWeekView(context: Context) : View(context) {

    val calendar: Calendar = Calendar.getInstance()

    var selectDayListener: ((week: Int, date: Date) -> Unit)? = null

    // affects the padding on the sides of this view
    var mPadding = 0

    var mSelectedDayRect = Rect()
    var mCurrentDayRect = Rect()
    var mSelectedDayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var mCurrentDayPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var mDayNumberPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    var mSeparatorVerticalPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var mSeparatorHorizontalPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var mSeparatorMonthPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var mSelectedDayLine: Drawable

    // Cache the number strings so we don't have to recompute them each time
    lateinit var mDayNumbers: Array<String>

    // The position week of this view
    var mWeek = -1
    // The number of current week, that mean NOW
    var mCurrentWeek = -1
    //The margin distance top and left between day's number and day's bounder
    var mDayNumberMargin = DEFAULT_DAY_NUMBER_MARGIN
    // Quick reference to the width of this view, matches parent
    var mWidth: Int = 0
    var mCellWidth: Int = 0
    // The height this view should draw at in pixels, set by height param
    var mHeight: Int = DEFAULT_HEIGHT
    // If this view contains the selected day
    var mHasSelectedDay: Boolean = false
    // Which day is selected [0-6] or -1 if no day is selected
    var mSelectedDay: Int = DEFAULT_SELECTED_DAY
    // Which day is today [0-6] or -1 if no day is today
    var mToday: Int = DEFAULT_SELECTED_DAY
    // Which day of the week to start on [0-6]
    var mWeekStart: Int = DEFAULT_WEEK_START
    // The number of days + a spot for week number if it is displayed
    var mNumCells: Int = DEFAULT_NUM_DAYS

    var mSeparatorMonthPath: Path = Path()

    var mBGColor: Int = 0
    var mSelectedWeekBGColor: Int = 0
    var mFutureDayColor: Int = 0
    var mPastDayColor: Int = 0
    var mDaySeparatorColor: Int = 0
    var mTodayOutlineColor: Int = 0
    var mWeekNumColor: Int = 0

    init {
        //Use to apply effect line dashed
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        mBGColor = ContextCompat.getColor(context, R.color.month_bgcolor)
        mSelectedWeekBGColor = ContextCompat.getColor(context, R.color.month_selected_week_bgcolor)
        mFutureDayColor = ContextCompat.getColor(context, R.color.month_mini_day_number)
        mPastDayColor = ContextCompat.getColor(context, R.color.month_other_month_day_number)
        mDaySeparatorColor = ContextCompat.getColor(context, R.color.month_grid_lines)
        mTodayOutlineColor = ContextCompat.getColor(context, R.color.mini_month_today_outline_color)
        mWeekNumColor = ContextCompat.getColor(context, R.color.month_week_num_color)
        mSelectedDayLine = ContextCompat.getDrawable(context, R.drawable.dayline_minical_holo_light)

        if (mScale == 0f) {
            mScale = context.resources.displayMetrics.density
            if (mScale != 1f) {
                DEFAULT_HEIGHT *= mScale.toInt()
                MIN_HEIGHT *= mScale.toInt()
                MINI_DAY_NUMBER_TEXT_SIZE *= mScale.toInt()
                MINI_TODAY_OUTLINE_WIDTH *= mScale.toInt()
                DAY_SEPARATOR_WIDTH *= mScale.toInt()
            }
        }

        // Sets up any standard paints that will be used
        initView()
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
        // We keep the current value for any params not present
        if (params.containsKey(VIEW_PARAMS_HEIGHT)) {
            mHeight = params[VIEW_PARAMS_HEIGHT]!!
            if (mHeight < MIN_HEIGHT) {
                mHeight = MIN_HEIGHT
            }
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
        if (!params.containsKey(VIEW_PARAMS_WEEK)) {
            throw InvalidParameterException("You must specify the week number for this view")
        }
        mWeek = params[VIEW_PARAMS_WEEK]!!

        if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
            mWeekStart = params[VIEW_PARAMS_WEEK_START]!!
        }
        val daysOfWeek = TimeUtils.getDaysOfWeek(mWeek, mCurrentWeek, mWeekStart, calendar)

        mToday = -1
        val calendar = Calendar.getInstance()
        for (i in 0..mNumCells - 1) {
            if (calendar.time.isSameDay(calendar, daysOfWeek[i])) {
                mToday = i
            }

            mDayNumbers[i] = TimeUtils.getDateNumber(calendar, daysOfWeek[i])
        }
        updateSelectionPositions()
        updateCurrentDayPositions()
        updateSeparatorMonth()
    }

    /**
     * Sets up the text and style properties for painting. Override this if you
     * want to use a different paint.
     */
    fun initView() {
        mSelectedDayPaint.strokeWidth = MINI_TODAY_OUTLINE_WIDTH.toFloat()
        mSelectedDayPaint.style = Style.STROKE
        mSelectedDayPaint.color = mTodayOutlineColor

        mCurrentDayPaint.style = Style.FILL
        mCurrentDayPaint.color = Color.YELLOW

        mSeparatorVerticalPaint.style = Style.STROKE
        mSeparatorVerticalPaint.strokeWidth = DAY_SEPARATOR_WIDTH.toFloat()
        mSeparatorVerticalPaint.pathEffect = DashPathEffect(floatArrayOf(3f, 5f), 0f)
        mSeparatorVerticalPaint.color = Color.parseColor("#cccccc")

        mSeparatorHorizontalPaint.style = Style.STROKE
        mSeparatorHorizontalPaint.strokeWidth = DAY_SEPARATOR_WIDTH.toFloat()
        mSeparatorHorizontalPaint.color = Color.GRAY

        mSeparatorMonthPaint.style = Style.STROKE
        mSeparatorMonthPaint.strokeWidth = DAY_SEPARATOR_WIDTH.toFloat() * 2
        mSeparatorMonthPaint.color = Color.BLACK

        mDayNumberPaint.isFakeBoldText = true
        mDayNumberPaint.textSize = MINI_DAY_NUMBER_TEXT_SIZE.toFloat()
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
    fun getDayFromLocation(x: Float): Date? {
        val dayStart = mPadding
        if (x < dayStart || x > mWidth - mPadding) {
            return null
        }

        // Selection is (x - start) / (pixels/day) == (x -s) * day / pixels
        val dayPosition = ((x - dayStart) * mNumCells / (mWidth - dayStart - mPadding)).toInt()
        return TimeUtils.getDaysOfWeek(mWeek, mCurrentWeek, mWeekStart, calendar)[dayPosition]
    }

    override fun onDraw(canvas: Canvas) {
        drawCurrentDayBackground(canvas)
        drawDayNumber(canvas)
        drawSelectedDaySeparator(canvas)
    }


    fun drawDayNumber(canvas: Canvas) {
        for (i in 0..mNumCells - 1) {
            //define color of past day and future day
            when {
                mWeek < mCurrentWeek -> mDayNumberPaint.color = mPastDayColor
                mWeek > mCurrentWeek -> mDayNumberPaint.color = mFutureDayColor
                mWeek == mCurrentWeek && i >= mToday -> mDayNumberPaint.color = mFutureDayColor
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
                mWeek == mCurrentWeek && i >= mToday -> mDayNumberPaint.alpha = 255
                else -> mDayNumberPaint.alpha = 255 / 2 //else that mean: this view is current week and the day at "i" index is past day
            }

            val y = Math.abs(mDayNumberPaint.ascent()) + mDayNumberMargin
            val x = i * mCellWidth + mDayNumberMargin
            canvas.drawText(mDayNumbers[i], x.toFloat(), y, mDayNumberPaint)

            if (i == 0) continue

            canvas.drawLine((i * mCellWidth).toFloat(), 0f, (i * (mCellWidth)).toFloat(), mHeight.toFloat(), mSeparatorVerticalPaint)
        }
        canvas.drawPath(mSeparatorMonthPath, mSeparatorMonthPaint)
        canvas.drawLine(0f, mHeight.toFloat(), width.toFloat(), mHeight.toFloat(), mSeparatorHorizontalPaint)
    }

    fun drawCurrentDayBackground(canvas: Canvas) {
        if (mToday != -1) {
            canvas.drawRect(mCurrentDayRect, mCurrentDayPaint)
        }
    }

    fun drawSelectedDaySeparator(canvas: Canvas) {
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
    fun updateSeparatorMonth() {
        mSeparatorMonthPath.reset()
        val halfStrokeWidth = mSeparatorMonthPaint.strokeWidth / 2
        //because the text of first day of month is eg: "Aug 1", the length is always larger than 2
        val firstDayOfMonthNumber: Int =  mDayNumbers.indexOfFirst { dayNumberString ->
            dayNumberString.length > 2
        }

        //if current week is'nt contain a fist day of month then return
        if (firstDayOfMonthNumber == -1) return

        mSeparatorMonthPath.moveTo(0f, if (firstDayOfMonthNumber == 0) halfStrokeWidth else (mHeight - halfStrokeWidth))
        for (i in 0..mNumCells - 1)
            when {
                i < firstDayOfMonthNumber -> mSeparatorMonthPath.lineTo((i + 1) * mCellWidth.toFloat(), mHeight - halfStrokeWidth)
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
    fun updateSelectionPositions() {
        if (mHasSelectedDay) {
            var selectedPosition = mSelectedDay - mWeekStart
            if (selectedPosition < 0) {
                selectedPosition += 7
            }
            val selectedLeft = selectedPosition * (mWidth - mPadding * 2) / mNumCells + mPadding
            val selectedRight = (selectedPosition + 1) * (mWidth - mPadding * 2) / mNumCells + mPadding
            mSelectedDayRect.top = 1
            mSelectedDayRect.bottom = mHeight - 1
            mSelectedDayRect.left = selectedLeft + 1
            mSelectedDayRect.right = selectedRight - 1
        }
    }

    /**
     * This calculates the positions for the current day lines.
     */
    fun updateCurrentDayPositions() {
        if (mToday != -1) {
            var currentDayPosition = mToday
            if (currentDayPosition < 0) {
                currentDayPosition += 7
            }
            val currentDayLeft = currentDayPosition * (mWidth - mPadding * 2) / mNumCells + mPadding
            val currentDayRight = (currentDayPosition + 1) * (mWidth - mPadding * 2) / mNumCells + mPadding
            mCurrentDayRect.top = 1
            mCurrentDayRect.bottom = mHeight - 1
            mCurrentDayRect.left = currentDayLeft + 1
            mCurrentDayRect.right = currentDayRight - 1
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(View.MeasureSpec.getSize(widthMeasureSpec), mHeight)
    }

    var clickDownTime: Long = 0L

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

    companion object {
        val CLICK_TIME_OUT = 500
        /**
         * This sets the height of this week in pixels
         */
        val VIEW_PARAMS_HEIGHT = "height"

        val VIEW_PARAMS_WEEK = "week"

        val VIEW_PARAMS_CURRENT_WEEK = "current_week"

        /**
         * This sets one of the days in this view as selected [Time.SUNDAY]
         * through [Time.SATURDAY].
         */
        val VIEW_PARAMS_SELECTED_DAY = "selected_day"

        /**
         * Which day the week should start on. [Time.SUNDAY] through
         * [Time.SATURDAY].
         */
        val VIEW_PARAMS_WEEK_START = "week_start"

        var DEFAULT_HEIGHT = 32
        var MIN_HEIGHT = 10
        val DEFAULT_SELECTED_DAY = -1
        val DEFAULT_WEEK_START = Calendar.SUNDAY - 1
        val DEFAULT_NUM_DAYS = 7

        var DEFAULT_DAY_NUMBER_MARGIN = 10

        var DAY_SEPARATOR_WIDTH = 1

        var MINI_DAY_NUMBER_TEXT_SIZE = 10
        var MINI_TODAY_OUTLINE_WIDTH = 2

        // used for scaling to the device density
        var mScale = 0f
    }
}