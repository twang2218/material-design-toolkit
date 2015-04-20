package org.lab99.mdt.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import org.lab99.mdt.R;
import org.lab99.mdt.drawable.ColorDrawableCompat;
import org.lab99.mdt.utils.Utils;

/**
 * The material design Button widget, which is basically as same function as the stock button, but
 * materialized.
 */
public class Button extends Paper {
    public Button(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Button(Context context) {
        this(context, null, 0);
    }

    public Button(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    //  initializer

    @Override
    protected void initAttributes(Context context, AttributeSet attrs) {
        super.initAttributes(context, attrs);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Button);
            if (a == null) {
                return;
            }

            try {
                setClickable(a.getBoolean(R.styleable.Button_android_clickable, true));
                setFocusable(a.getBoolean(R.styleable.Button_android_focusable, true));
            } finally {
                a.recycle();
            }
        }
    }

    //  Override

    @Override
    protected boolean isDefaultRippleOnTouchEnabled() {
        return true;
    }


    //  Getters & Setters

    /**
     * Override the {@link Paper#setBackground} to have special treatment of {@link ColorDrawable}.
     * <p/>
     * As the paper should be round cornered, a {@link GradientDrawable} will be created for this
     * coloured paper as the background if the given Drawable is a {@link ColorDrawable}.
     *
     * @param background The new drawable for the background.
     */
    @Override
    public void setBackground(Drawable background) {
        if (background instanceof ColorDrawable) {
            //  Convert color drawable to the default shape with the specified color
            background = getBackgroundFromColorDrawable((ColorDrawable) background);
        }
        super.setBackground(background);
    }

    protected Drawable getBackgroundFromColorDrawable(ColorDrawable colorDrawable) {
        //  create background with given color
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(Utils.getPixelFromDip(getContext(), 2));
        drawable.setColor(ColorDrawableCompat.getColor(colorDrawable));
        return drawable;
    }
}
