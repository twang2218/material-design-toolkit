package org.lab99.mdt.drawable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;

/**
 * Compat class for ColorDrawable
 */
public final class ColorDrawableCompat {
    public static int getColor(ColorDrawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return drawable.getColor();
        } else {
            return getColorGingerbread(drawable);
        }
    }

    private static int getColorGingerbread(ColorDrawable drawable) {
        final Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        drawable.draw(canvas);
        return bitmap.getPixel(0, 0);
    }
}
