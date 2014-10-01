package org.lab99.mdt.utils;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

/**
 * Compatibility class for View
 */
public final class ViewCompat extends android.support.v4.view.ViewCompat {
    @SuppressWarnings("deprecation")
    public static void setViewBackground(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }

    }
}
