package org.lab99.mdt.view;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

/**
 * Compatibility class for View
 */
public class ViewCompat extends android.support.v4.view.ViewCompat {
    /**
     * Set the background of a {@link View}.
     * <p/>
     * Pre-Jelly Bean, the method name is {@link View#setBackgroundDrawable(Drawable)}, however,
     * it's deprecated and changed to {@link View#setBackground(Drawable)}.
     *
     * @param view     The view which will be set a background.
     * @param drawable The background {@link Drawable}.
     * @see View#setBackground(Drawable)
     * @see View#setBackgroundDrawable(Drawable)
     */
    @SuppressWarnings("deprecation")
    public static void setBackground(View view, Drawable drawable) {
        //  Keep the original padding, otherwise they will be lost
        Rect padding = new Rect(view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
        //  Set the background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
        //  Restore the padding
        view.setPadding(padding.left, padding.top, padding.right, padding.bottom);
    }
}
