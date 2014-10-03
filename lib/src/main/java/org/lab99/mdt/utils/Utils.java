package org.lab99.mdt.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

public final class Utils {
    private static Context sContext;
    private static DisplayMetrics sMetrics;
    private static WindowManager sWindowManger;

    static {
        sMetrics = Resources.getSystem().getDisplayMetrics();
    }

    public static void init(Context context) {
        sContext = context;
        sMetrics = sContext.getResources().getDisplayMetrics();
        sWindowManger = (WindowManager) sContext.getSystemService(Context.WINDOW_SERVICE);
    }

    @SuppressWarnings("deprecation")
    public static Point getScreenSize() {
        Display display = sWindowManger.getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
        } else {
            size.x = display.getWidth();
            size.y = display.getHeight();
        }
        return size;
    }

    public static Point getScreenSize(Context context) {
        if (context != sContext) {
            init(context);
        }

        return getScreenSize();
    }

    public static float getPixelFromDip(float dip) {
        if (sMetrics != null && sMetrics.density > 0) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, sMetrics);
        } else {
            //  On some devices which 'ro.sf.lcd_density' is missing, then 'sMetrics' is invalid.
            //  In such case, just assume it's 'xhdpi', better then nothing or exception.
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.density = 2;
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, metrics);
        }
    }

    public static float getPixelFromDip(Context context, float dip) {
        if (context != sContext) {
            init(context);
        }

        return getPixelFromDip(dip);
    }
}
