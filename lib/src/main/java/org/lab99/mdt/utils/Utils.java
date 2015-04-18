package org.lab99.mdt.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public final class Utils {
    public static float getPixelFromDip(Context context, float dip) {
        DisplayMetrics metrics;
        if (context != null) {
            metrics = context.getResources().getDisplayMetrics();
        } else {
            //  On some devices which 'ro.sf.lcd_density' is missing, then 'sMetrics' is invalid.
            //  In such case, just assume it's 'xhdpi', better then nothing or exception.
            metrics = new DisplayMetrics();
            metrics.density = 2;
        }
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, metrics);
    }
}
