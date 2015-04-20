package org.lab99.mdt.drawable;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

/**
 * Touch location tracker
 */
public class TouchTracker implements View.OnTouchListener {
    private PointF mLastTouch = new PointF();
    private PointF mLastTouchRaw = new PointF();

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mLastTouch.set(event.getX(), event.getY());
        mLastTouchRaw.set(event.getRawX(), event.getRawY());
        return false;
    }

    /**
     * Get the last touch location.
     *
     * @return Return the coordinates of the last touch location.
     */
    public PointF getLastTouch() {
        return mLastTouch;
    }

    /**
     * Get the last touch location in RAW value.
     * @return Return the Raw coordinates of the last touch location.
     */
    public PointF getLastTouchRaw() {
        return mLastTouchRaw;
    }
}
