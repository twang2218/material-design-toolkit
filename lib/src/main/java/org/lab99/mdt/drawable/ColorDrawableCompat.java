package org.lab99.mdt.drawable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;

/**
 * Compat class for ColorDrawable
 */
public final class ColorDrawableCompat {
    /**
     * Get the color of {@link ColorDrawable}.
     * <p/>
     * The {@link ColorDrawable#getColor()} is only available post-Honeycomb,
     * so we have to hack it for the previous Android version.
     *
     * @param drawable The {@link ColorDrawable} object.
     * @return Return the color value of the {@link ColorDrawable}.
     * @see ColorDrawable#getColor()
     */
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
