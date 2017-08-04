package com.hado.calendar;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.HashMap;

/**
 * Created by DoanNH on 8/2/2017.
 */

public class SimpleWeekViewHolder extends RecyclerView.ViewHolder {

    private HashMap<String, Integer> mDrawingParams = new HashMap<>();

    public SimpleWeekViewHolder(View itemView) {
        super(itemView);
    }

    public HashMap<String, Integer> getDrawingParams() {
        return mDrawingParams;
    }

    public void setDrawingParams(HashMap<String, Integer> drawingParams, String timezone) {
        this.mDrawingParams = drawingParams;
        ((SimpleWeekView) itemView).setWeekParams(drawingParams, timezone);
    }

    public void invalidate() {
        itemView.invalidate();
    }
}
