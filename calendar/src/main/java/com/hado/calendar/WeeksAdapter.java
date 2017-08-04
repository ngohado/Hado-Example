package com.hado.calendar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.view.ViewGroup;
import android.widget.AbsListView;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by DoanNH on 8/2/2017.
 */

public class WeeksAdapter extends RecyclerView.Adapter<SimpleWeekViewHolder> {
    /**
     * Which month should be in focus currently.
     */
    public static final String WEEK_PARAMS_FOCUS_MONTH = "focus_month";
    /**
     * Whether the week number should be shown. Non-zero to show them.
     */
    public static final String WEEK_PARAMS_SHOW_WEEK = "week_numbers";
    /**
     * Which day the week should start on. {@link Time#SUNDAY} through
     * {@link Time#SATURDAY}.
     */
    public static final String WEEK_PARAMS_WEEK_START = "week_start";
    /**
     * The Julian day to highlight as selected.
     */
    public static final String WEEK_PARAMS_JULIAN_DAY = "selected_day";


    private static final int WEEK_COUNT = 3497;

    private static int DEFAULT_MONTH_FOCUS = 0;

    private static float mScale = 0;

    private Context mContext;

    // The day to highlight as selected
    private Date mSelectedDay;

    // The week since 1970 that the selected day is in
    private int mSelectedWeek;

    // When the week starts; numbered like Time.<WEEKDAY> (e.g. SUNDAY=0).
    private int mFirstDayOfWeek;

    private int mFocusMonth = DEFAULT_MONTH_FOCUS;


    public WeeksAdapter(Context context, HashMap<String, Integer> params) {
        mContext = context;
        mSelectedDay = Calendar.getInstance().getTime();
        updateParams(params);
    }

    @Override
    public SimpleWeekViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SimpleWeekView view = new SimpleWeekView(parent.getContext());
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(
                AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(params);
        view.setClickable(true);
        return new SimpleWeekViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleWeekViewHolder holder, int position) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mSelectedDay);

        HashMap<String, Integer> drawingParams = holder.getDrawingParams();

        int selectedDay = -1;

        if (mSelectedWeek == position) {
            selectedDay = calendar.get(Calendar.DAY_OF_WEEK);
        }

        drawingParams.put(SimpleWeekView.VIEW_PARAMS_HEIGHT, 250);
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_SELECTED_DAY, selectedDay);
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_WEEK_START, mFirstDayOfWeek);
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_WEEK, position);
        drawingParams.put(SimpleWeekView.VIEW_PARAMS_FOCUS_MONTH, mFocusMonth);

        holder.setDrawingParams(drawingParams, calendar.getTimeZone().getDisplayName());
        holder.invalidate();
    }

    @Override
    public int getItemCount() {
        return WEEK_COUNT;
    }

    public void updateParams(HashMap<String, Integer> params) {
        if (params == null) {
            return;
        }

        if (params.containsKey(WEEK_PARAMS_FOCUS_MONTH)) {
            mFocusMonth = params.get(WEEK_PARAMS_FOCUS_MONTH);
        }
        if (params.containsKey(WEEK_PARAMS_WEEK_START)) {
            mFirstDayOfWeek = params.get(WEEK_PARAMS_WEEK_START);
        }
        if (params.containsKey(WEEK_PARAMS_SHOW_WEEK)) {
            mSelectedWeek = params.get(WEEK_PARAMS_SHOW_WEEK);
        }

        refresh();
    }

    private void refresh() {
        notifyDataSetChanged();
    }
}
