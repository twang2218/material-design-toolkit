package org.lab99.mdt.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.lab99.mdt.R;
import org.lab99.mdt.drawable.MessengerDrawable;
import org.lab99.mdt.drawable.PaperDrawable;

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

    public static PaperDrawable apply(View view) {
        return apply(view, view);
    }

    /**
     * @param touch_view  touch view will trigger the state change event;
     * @param ripple_view the ripple view has to be able to receive 'onTouch' event
     * @return
     */
    public static PaperDrawable apply(View touch_view, View ripple_view) {
        Drawable original = touch_view.getBackground();

        if (original instanceof PaperDrawable) {
            //  don't swap the background if it's already PaperDrawable already.
            return (PaperDrawable) original;
        } else {
            //  create new warp drawable for the old drawable
            PaperDrawable background = new PaperDrawable(ripple_view.getBackground(), touch_view.getContext());
            ViewCompat.setBackground(ripple_view, background);
            //  attach touch tracker
            ripple_view.setOnTouchListener(background.getTouchTracker());

            if (touch_view != ripple_view) {
                //  link the messenger for passing state message to 'background'
                MessengerDrawable messenger = new MessengerDrawable(touch_view.getBackground(), background);
                ViewCompat.setBackground(touch_view, messenger);
                //  force ripple_view not 'clickable', so it will not trigger the state change event.
                ripple_view.setClickable(false);
            }
            return background;
        }
    }

    //  initializer

    protected void init(Context context, AttributeSet attrs) {
        initAttributes(context, attrs);
    }

    protected void initAttributes(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Paper);
            if (a == null) {
                return;
            }

            try {
                setDepth(a.getFloat(R.styleable.Paper_depth, getDefaultDepth()));
                setRippleEnabled(a.getBoolean(R.styleable.Paper_rippleEnabled, isDefaultRippleEnabled()));
                setRippleOnTouchEnabled(a.getBoolean(R.styleable.Paper_rippleOnTouchEnabled, isDefaultRippleOnTouchEnabled()));
                setOriginalBackground(a.getDrawable(R.styleable.Paper_android_background));
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

    //  Getters & Setters

    public PaperDrawable getPaperBackground() {
        Drawable background = getBackground();

        if (background instanceof PaperDrawable) {
            return (PaperDrawable) background;
        } else {
            //  create the PaperDrawable to wrap the original one, if it's not already PaperDrawable.
            PaperDrawable drawable = new PaperDrawable(getBackground(), getContext());
            ViewCompat.setBackground(this, drawable);
            return drawable;
        }
    }

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

    public Drawable getOriginalBackground() {
        return getPaperBackground().getOriginal();
    }

    public void setOriginalBackground(Drawable background) {
        getPaperBackground().setOriginal(background);
    }

    public float getDepth() {
        return getPaperBackground().getDepth();
    }

    public void setDepth(float depth) {
        getPaperBackground().setDepth(depth);
        postInvalidate();
    }

    //  default value
    protected float getDefaultDepth() {
        return 0;
    }

    protected boolean isDefaultRippleEnabled() {
        return true;
    }

    protected boolean isDefaultRippleOnTouchEnabled() {
        return false;
    }
}
