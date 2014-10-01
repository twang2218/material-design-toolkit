package org.lab99.mdt.widget;

import android.graphics.PointF;
import android.view.MotionEvent;
import android.view.View;

/**
 * Touch location tracker
 */
class TouchTracker implements View.OnTouchListener {
    private PointF mLastTouch = new PointF();
    private PointF mLastTouchRaw = new PointF();

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mLastTouch.set(event.getX(), event.getY());
        mLastTouchRaw.set(event.getRawX(), event.getRawY());
        return false;
    }

    public PointF getLastTouch() {
        return mLastTouch;
    }

    public PointF getLastTouchRaw() {
        return mLastTouchRaw;
    }
}
