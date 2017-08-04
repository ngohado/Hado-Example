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

package com.hado.calendar;

import android.app.Service;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;

import java.security.InvalidParameterException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;


public class SimpleWeekView extends View {
    public static final int MONDAY_BEFORE_JULIAN_EPOCH = Time.EPOCH_JULIAN_DAY - 3;
    private static StringBuilder mSB = new StringBuilder(50);
    private static Formatter mF = new Formatter(mSB, Locale.getDefault());

    /**
     * This sets the height of this week in pixels
     */
    public static final String VIEW_PARAMS_HEIGHT = "height";

    public static final String VIEW_PARAMS_WEEK = "week";
    /**
     * This sets one of the days in this view as selected {@link Time#SUNDAY}
     * through {@link Time#SATURDAY}.
     */
    public static final String VIEW_PARAMS_SELECTED_DAY = "selected_day";
    /**
     * Which day the week should start on. {@link Time#SUNDAY} through
     * {@link Time#SATURDAY}.
     */
    public static final String VIEW_PARAMS_WEEK_START = "week_start";

    /**
     * Which month is currently in focus, as defined by {@link Time#month}
     * [0-11].
     */
    public static final String VIEW_PARAMS_FOCUS_MONTH = "focus_month";
    /**
     * If this month should display week numbers. false if 0, true otherwise.
     */

    protected static int DEFAULT_HEIGHT = 32;
    protected static int MIN_HEIGHT = 10;
    protected static final int DEFAULT_SELECTED_DAY = -1;
    protected static final int DEFAULT_WEEK_START = Time.SUNDAY;
    protected static final int DEFAULT_NUM_DAYS = 7;
    protected static final int DEFAULT_FOCUS_MONTH = -1;

    protected static int DEFAULT_DAY_NUMBER_MARGIN = 10;

    protected static int DAY_SEPARATOR_WIDTH = 1;

    protected static int MINI_DAY_NUMBER_TEXT_SIZE = 10;
    protected static int MINI_TODAY_OUTLINE_WIDTH = 2;

    // used for scaling to the device density
    protected static float mScale = 0;

    // affects the padding on the sides of this view
    protected int mPadding = 0;

    protected Rect mSelectedDayRect = new Rect();
    protected Paint mSelectedDayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected Paint mMonthNumPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected Paint mSeparatorVerticalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected Paint mSeparatorHorizontalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    protected Drawable mSelectedDayLine;

    // Cache the number strings so we don't have to recompute them each time
    protected String[] mDayNumbers;
    // Quick lookup for checking which days are in the focus month
    protected boolean[] mFocusDay;
    // Quick lookup for checking which days are in an odd month (to set a different background)
    protected boolean[] mOddMonth;
    // The Julian day of the first day displayed by this item
    protected int mFirstJulianDay = -1;
    // The month of the first day in this week
    protected int mFirstMonth = -1;
    // The month of the last day in this week
    protected int mLastMonth = -1;
    // The position of this week, equivalent to weeks since the week of Jan 1st,
    // 1970
    protected int mWeek = -1;
    //The margin distance top and left between day's number and day's bounder
    protected int mDayNumberMargin = DEFAULT_DAY_NUMBER_MARGIN;
    // The height of day number, it's computed by MINI_DAY_NUMBER_TEXT_SIZE * scale
    protected int mDayNumberHeight;
    // Quick reference to the width of this view, matches parent
    protected int mWidth;
    // The height this view should draw at in pixels, set by height param
    protected int mHeight = DEFAULT_HEIGHT;
    // If this view contains the selected day
    protected boolean mHasSelectedDay = false;
    // If this view contains the today
    protected boolean mHasToday = false;
    // Which day is selected [0-6] or -1 if no day is selected
    protected int mSelectedDay = DEFAULT_SELECTED_DAY;
    // Which day is today [0-6] or -1 if no day is today
    protected int mToday = DEFAULT_SELECTED_DAY;
    // Which day of the week to start on [0-6]
    protected int mWeekStart = DEFAULT_WEEK_START;
    // The number of days + a spot for week number if it is displayed
    protected int mNumCells = DEFAULT_NUM_DAYS;
    // The left edge of the selected day
    protected int mSelectedLeft = -1;
    // The right edge of the selected day
    protected int mSelectedRight = -1;
    // The timezone to display times/dates in (used for determining when Today
    // is)
    protected String mTimeZone = Time.getCurrentTimezone();

    protected int mBGColor;
    protected int mSelectedWeekBGColor;
    protected int mFutureDayColor;
    protected int mPastDayColor;
    protected int mDaySeparatorColor;
    protected int mTodayOutlineColor;
    protected int mWeekNumColor;

    public SimpleWeekView(Context context) {
        super(context);

        //Use to apply effect line dashed
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        Resources res = context.getResources();

        mBGColor = res.getColor(R.color.month_bgcolor);
        mSelectedWeekBGColor = res.getColor(R.color.month_selected_week_bgcolor);
        mFutureDayColor = res.getColor(R.color.month_mini_day_number);
        mPastDayColor = res.getColor(R.color.month_other_month_day_number);
        mDaySeparatorColor = res.getColor(R.color.month_grid_lines);
        mTodayOutlineColor = res.getColor(R.color.mini_month_today_outline_color);
        mWeekNumColor = res.getColor(R.color.month_week_num_color);
        mSelectedDayLine = res.getDrawable(R.drawable.dayline_minical_holo_light);

        if (mScale == 0) {
            mScale = context.getResources().getDisplayMetrics().density;
            if (mScale != 1) {
                DEFAULT_HEIGHT *= mScale;
                MIN_HEIGHT *= mScale;
                MINI_DAY_NUMBER_TEXT_SIZE *= mScale;
                MINI_TODAY_OUTLINE_WIDTH *= mScale;
                DAY_SEPARATOR_WIDTH *= mScale;
            }
        }

        // Sets up any standard paints that will be used
        initView();
    }

    public static int getJulianMondayFromWeeksSinceEpoch(int week) {
        return MONDAY_BEFORE_JULIAN_EPOCH + week * 7;
    }

    /**
     * Sets all the parameters for displaying this week. The only required
     * parameter is the week number. Other parameters have a default value and
     * will only update if a new value is included, except for focus month,
     * which will always default to no focus month if no value is passed in. See
     * {@link #VIEW_PARAMS_HEIGHT} for more info on parameters.
     *
     * @param params A map of the new parameters, see
     *               {@link #VIEW_PARAMS_HEIGHT}
     * @param tz     The time zone this view should reference times in
     */
    public void setWeekParams(HashMap<String, Integer> params, String tz) {
        if (!params.containsKey(VIEW_PARAMS_WEEK)) {
            throw new InvalidParameterException("You must specify the week number for this view");
        }
        setTag(params);
        mTimeZone = tz;
        // We keep the current value for any params not present
        if (params.containsKey(VIEW_PARAMS_HEIGHT)) {
            mHeight = params.get(VIEW_PARAMS_HEIGHT);
            if (mHeight < MIN_HEIGHT) {
                mHeight = MIN_HEIGHT;
            }
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_DAY)) {
            mSelectedDay = params.get(VIEW_PARAMS_SELECTED_DAY);
        }
        mHasSelectedDay = mSelectedDay != -1;

        // Allocate space for caching the day numbers and focus values
        mDayNumbers = new String[mNumCells];
        mFocusDay = new boolean[mNumCells];
        mOddMonth = new boolean[mNumCells];
        mWeek = params.get(VIEW_PARAMS_WEEK);

        int julianMonday = getJulianMondayFromWeeksSinceEpoch(mWeek);
        Time time = new Time(tz);
        time.setJulianDay(julianMonday);

        if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
            mWeekStart = params.get(VIEW_PARAMS_WEEK_START);
        }

        // Now adjust our starting day based on the start day of the week
        // If the week is set to start on a Saturday the first week will be
        // Dec 27th 1969 -Jan 2nd, 1970
        if (time.weekDay != mWeekStart) {
            int diff = time.weekDay - mWeekStart;
            if (diff < 0) {
                diff += 7;
            }
            time.monthDay -= diff;
            time.normalize(true);
        }

        mFirstJulianDay = Time.getJulianDay(time.toMillis(true), time.gmtoff);
        mFirstMonth = time.month;

        // Figure out what day today is
        Time today = new Time(tz);
        today.setToNow();
        mHasToday = false;
        mToday = -1;

        int focusMonth = params.containsKey(VIEW_PARAMS_FOCUS_MONTH) ? params.get(
                VIEW_PARAMS_FOCUS_MONTH)
                : DEFAULT_FOCUS_MONTH;

        for (int i = 0; i < mNumCells; i++) {
            if (time.monthDay == 1) {
                mFirstMonth = time.month;
            }

            mOddMonth[i] = (time.month % 2) == 1;
            mFocusDay[i] = time.month == focusMonth;

            if (time.year == today.year && time.yearDay == today.yearDay) {
                mHasToday = true;
                mToday = i;
            }
            mDayNumbers[i] = Integer.toString(time.monthDay++);
            time.normalize(true);
        }
        // We do one extra add at the end of the loop, if that pushed us to a
        // new month undo it
        if (time.monthDay == 1) {
            time.monthDay--;
            time.normalize(true);
        }
        mLastMonth = time.month;

        updateSelectionPositions();
    }

    /**
     * Sets up the text and style properties for painting. Override this if you
     * want to use a different paint.
     */
    protected void initView() {
        mSelectedDayPaint.setFakeBoldText(false);
        mSelectedDayPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
        mSelectedDayPaint.setStyle(Style.FILL);

        mSeparatorVerticalPaint.setStyle(Style.STROKE);
        mSeparatorVerticalPaint.setStrokeWidth(DAY_SEPARATOR_WIDTH);
        mSeparatorVerticalPaint.setPathEffect(new DashPathEffect(new float[]{3, 5}, 0));
        mSeparatorVerticalPaint.setColor(Color.parseColor("#cccccc"));

        mSeparatorHorizontalPaint.setStyle(Style.STROKE);
        mSeparatorHorizontalPaint.setStrokeWidth(DAY_SEPARATOR_WIDTH);
        mSeparatorHorizontalPaint.setColor(Color.GRAY);

        mMonthNumPaint.setFakeBoldText(true);
        mMonthNumPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
        mMonthNumPaint.setStyle(Style.FILL);

        //use to compute the height of number text day
        Rect rect = new Rect();
        mMonthNumPaint.getTextBounds("12", 0, 2, rect);
        mDayNumberHeight = Math.abs(rect.top - rect.bottom);
    }

    /**
     * Returns the month of the first day in this week
     *
     * @return The month the first day of this view is in
     */
    public int getFirstMonth() {
        return mFirstMonth;
    }

    /**
     * Returns the month of the last day in this week
     *
     * @return The month the last day of this view is in
     */
    public int getLastMonth() {
        return mLastMonth;
    }

    /**
     * Returns the julian day of the first day in this view.
     *
     * @return The julian day of the first day in the view.
     */
    public int getFirstJulianDay() {
        return mFirstJulianDay;
    }

    /**
     * Calculates the day that the given x position is in, accounting for week
     * number. Returns a Time referencing that day or null if
     *
     * @param x The x position of the touch event
     * @return A time object for the tapped day or null if the position wasn't
     * in a day
     */
    public Time getDayFromLocation(float x) {
        int dayStart = mPadding;
        if (x < dayStart || x > mWidth - mPadding) {
            return null;
        }
        // Selection is (x - start) / (pixels/day) == (x -s) * day / pixels
        int dayPosition = (int) ((x - dayStart) * mNumCells / (mWidth - dayStart - mPadding));
        int day = mFirstJulianDay + dayPosition;

        Time time = new Time(mTimeZone);
        if (mWeek == 0) {
            // This week is weird...
            if (day < Time.EPOCH_JULIAN_DAY) {
                day++;
            } else if (day == Time.EPOCH_JULIAN_DAY) {
                time.set(1, 0, 1970);
                time.normalize(true);
                return time;
            }
        }

        time.setJulianDay(day);
        return time;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawDayNumber(canvas);
        drawDaySeparators(canvas);
    }

    /**
     * Draws the week and month day numbers for this week. Override this method
     * if you need different placement.
     *
     * @param canvas The canvas to draw on
     */
    protected void drawDayNumber(Canvas canvas) {
        mMonthNumPaint.setFakeBoldText(true);
        for (int i = 0; i < mNumCells; i++) {
            if (mHasToday && i >= mToday) {
                mMonthNumPaint.setColor(mFutureDayColor);
            } else {
                mMonthNumPaint.setColor(mPastDayColor);
            }

            int y = mDayNumberHeight + mDayNumberMargin;
            int x = i * (mWidth / mNumCells) + mDayNumberMargin;
            canvas.drawText(mDayNumbers[i], x, y, mMonthNumPaint);

            if (i == 0) continue;

            canvas.drawLine(i * (mWidth / mNumCells), 0, i * (mWidth / mNumCells), mHeight, mSeparatorVerticalPaint);
        }
        canvas.drawLine(0f, mHeight, getWidth(), mHeight, mSeparatorHorizontalPaint);
    }

    /**
     * Draws a horizontal line for separating the weeks. Override this method if
     * you want custom separators.
     *
     * @param canvas The canvas to draw on
     */
    protected void drawDaySeparators(Canvas canvas) {
        if (mHasSelectedDay) {
            mSelectedDayRect.top = 1;
            mSelectedDayRect.bottom = mHeight - 1;
            mSelectedDayRect.left = mSelectedLeft + 1;
            mSelectedDayRect.right = mSelectedRight - 1;
            mSelectedDayPaint.setStrokeWidth(MINI_TODAY_OUTLINE_WIDTH);
            mSelectedDayPaint.setStyle(Style.STROKE);
            mSelectedDayPaint.setColor(mTodayOutlineColor);
            canvas.drawRect(mSelectedDayRect, mSelectedDayPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        updateSelectionPositions();
    }

    /**
     * This calculates the positions for the selected day lines.
     */
    protected void updateSelectionPositions() {
        if (mHasSelectedDay) {
            int selectedPosition = mSelectedDay - mWeekStart;
            if (selectedPosition < 0) {
                selectedPosition += 7;
            }
            mSelectedLeft = selectedPosition * (mWidth - mPadding * 2) / mNumCells
                    + mPadding;
            mSelectedRight = (selectedPosition + 1) * (mWidth - mPadding * 2) / mNumCells
                    + mPadding;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mHeight);
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        Context context = getContext();
        // only send accessibility events if accessibility and exploration are
        // on.
        AccessibilityManager am = (AccessibilityManager) context
                .getSystemService(Service.ACCESSIBILITY_SERVICE);
        if (!am.isEnabled() || !am.isTouchExplorationEnabled()) {
            return super.onHoverEvent(event);
        }
        if (event.getAction() != MotionEvent.ACTION_HOVER_EXIT) {
            Time hover = getDayFromLocation(event.getX());
            if (hover != null
                    && (mLastHoverTime == null || Time.compare(hover, mLastHoverTime) != 0)) {
                Long millis = hover.toMillis(true);
                String date = formatDateRange(context, millis, millis,
                        DateUtils.FORMAT_SHOW_DATE);
                AccessibilityEvent accessEvent =
                        AccessibilityEvent.obtain(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
                accessEvent.getText().add(date);
                sendAccessibilityEventUnchecked(accessEvent);
                mLastHoverTime = hover;
            }
        }
        return true;
    }

    public String formatDateRange(Context context, long startMillis,
                                  long endMillis, int flags) {
        String date;
        String tz;
        if ((flags & DateUtils.FORMAT_UTC) != 0) {
            tz = Time.TIMEZONE_UTC;
        } else {
            tz = Time.getCurrentTimezone();
        }
        synchronized (mSB) {
            mSB.setLength(0);
            date = DateUtils.formatDateRange(context, mF, startMillis, endMillis, flags,
                    tz).toString();
        }
        return date;
    }

    Time mLastHoverTime = null;
}