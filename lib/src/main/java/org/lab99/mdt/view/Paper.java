package org.lab99.mdt.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import org.lab99.mdt.R;
import org.lab99.mdt.drawable.PaperDrawable;
import org.lab99.mdt.utils.ViewCompat;

public class Paper extends TextView {

    public Paper(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public Paper(Context context) {
        this(context, null, 0);
    }

    public Paper(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    //  initializer

    protected void init(Context context, AttributeSet attrs) {
        initViews(context);
        initAttributes(context, attrs);
    }

    protected void initViews(Context context) {
        ViewCompat.setBackground(this, new PaperDrawable(super.getBackground(), context));
    }

    protected void initAttributes(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Paper);
            if (a == null) {
                return;
            }

            try {
                setDepth(a.getFloat(R.styleable.Paper_depth, 0));
                setRippleEnabled(a.getBoolean(R.styleable.Paper_rippleEnabled, true));
                setRippleOnTouchEnabled(a.getBoolean(R.styleable.Paper_rippleOnTouchEnabled, false));
//                setBackground(a.getDrawable(R.styleable.Paper_android_background));
            } finally {
                a.recycle();
            }
        }
    }

    //  Override

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void setRotation(float rotation) {
        super.setRotation(rotation);
        getPaperBackground().setRotation(rotation);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getPaperBackground().getTouchTracker().onTouch(this, event);
        return super.onTouchEvent(event);
    }

    @Override
    public Drawable getBackground() {
        return getPaperBackground().getOriginal();
    }

    @Override
    public void setBackground(Drawable background) {
        setBackgroundDrawable(background);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setBackgroundDrawable(Drawable background) {
        getPaperBackground().setOriginal(background);
        postInvalidate();
    }

    //  Getters & Setters

    public boolean isRippleEnabled() {
        return getPaperBackground().isRippleEnabled();
    }

    public void setRippleEnabled(boolean enabled) {
        getPaperBackground().setRippleEnabled(enabled);
        postInvalidate();
    }

    public boolean isRippleOnTouchEnabled() {
        return getPaperBackground().isRippleOnTouchEnabled();
    }

    public void setRippleOnTouchEnabled(boolean enabled) {
        getPaperBackground().setRippleOnTouchEnabled(enabled);
    }

    @SuppressWarnings("deprecation")
    protected PaperDrawable getPaperBackground() {
        Drawable drawable = super.getBackground();
        if (drawable == null || !(drawable instanceof PaperDrawable)) {
            //  if the background is not the right one, then create one
            PaperDrawable paperDrawable = new PaperDrawable(drawable, getContext());
            super.setBackgroundDrawable(paperDrawable);
        }

        return ((PaperDrawable) super.getBackground());
    }

    public float getDepth() {
        return getPaperBackground().getDepth();
    }

    public void setDepth(float depth) {
        getPaperBackground().setDepth(depth);
        postInvalidate();
    }


}
