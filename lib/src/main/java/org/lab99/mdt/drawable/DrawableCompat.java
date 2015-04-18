package org.lab99.mdt.drawable;

import android.graphics.drawable.Drawable;
import android.os.Build;

import java.lang.reflect.Field;

/**
 * Compatibility class for Drawable
 */
public final class DrawableCompat extends android.support.v4.graphics.drawable.DrawableCompat {

    public static Drawable.Callback getCallback(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return drawable.getCallback();
        } else {
            return getCallbackGingerbread(drawable);
        }
    }

    private static Drawable.Callback getCallbackGingerbread(Drawable drawable) {
        try {
            Field f = drawable.getClass().getDeclaredField("mCallback");
            f.setAccessible(true);
            return (Drawable.Callback) f.get(drawable);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

}
