package org.lab99.mdt.widget;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

public class TieredDrawable extends LayerDrawable {
    private static final int TIER_INDEX_SELF_SHADOW = 0;
    private static final int TIER_INDEX_ORIGINAL = 1;
    private static final int TIER_INDEX_RIPPLE = 2;
    private static final int TIER_INDEX_CHILD_SHADOW = 3;
    private static final int TIER_SIZE = 4;

    public TieredDrawable(Drawable[] drawables) {
        super(drawables);
    }

    public static TieredDrawable create(Drawable original) {
        //  0 - Self Shadow
        //  1 - Original Background
        //  2 - Ripple
        //  3 - Child Shadow
        Drawable[] drawables = new Drawable[TIER_SIZE];
        Drawable empty = new ColorDrawable(Color.TRANSPARENT);
        drawables[TIER_INDEX_SELF_SHADOW] = empty;
        drawables[TIER_INDEX_ORIGINAL] = original;
        drawables[TIER_INDEX_RIPPLE] = empty;
        drawables[TIER_INDEX_CHILD_SHADOW] = empty;

        return new TieredDrawable(drawables);
    }
}
