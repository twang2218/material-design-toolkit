package org.lab99.mdt.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.lab99.mdt.R;
import org.lab99.mdt.drawable.MessengerDrawable;
import org.lab99.mdt.drawable.PaperDrawable;

/**
 * The Paper widget implement the basic idea of the Material Design.
 * <p/>
 * To implement the shadow and ripple effect of material design, a special wrapper Drawable,
 * {@link PaperDrawable} is used as the view background.
 * Any background set through the setBackground() or "android:background" will be set as inner
 * background of the wrapper.
 */
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

    /**
     * Apply materials design on normal Android widget by wrapping the background with {@link PaperDrawable}.
     *
     * @param view the given normal android widget
     * @return The created {@link PaperDrawable} wrapper background will be returned.
     */
    public static PaperDrawable apply(View view) {
        return apply(view, view);
    }

    /**
     * Apply materials design on normal Android widget by wrapping the background with PaperDrawable.
     * This function is enable the separation of touch view and ripple effect display view,
     * so, one can touch a widget, and the ripple is shown on the top larger transparent layer.
     *
     * @param touch_view  touch view will trigger the state change event;
     * @param ripple_view the ripple view has to be able to receive 'onTouch' event
     * @return The created {@link PaperDrawable} wrapper background will be returned.
     */
    public static PaperDrawable apply(View touch_view, View ripple_view) {
        if (touch_view instanceof Paper) {
            return ((Paper) touch_view).getPaperBackground();
        }

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
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        getPaperBackground().getTouchTracker().onTouch(this, event);
        return super.onTouchEvent(event);
    }

    //  Getters & Setters

    /**
     * Get the {@link PaperDrawable} background.
     *
     * @return the {@link PaperDrawable} object of the background.
     */
    public PaperDrawable getPaperBackground() {
        Drawable background = super.getBackground();

        if (background instanceof PaperDrawable) {
            return (PaperDrawable) background;
        } else {
            //  create the PaperDrawable to wrap the original one, if it's not already PaperDrawable.
            PaperDrawable drawable = new PaperDrawable(background, getContext());
            ViewCompat.setBackground(this, drawable);
            return drawable;
        }
    }

    /**
     * Check whether the ripple effect is enabled on this view.
     *
     * @return Return true if the ripple effect is enabled.
     */
    public boolean isRippleEnabled() {
        return getPaperBackground().isRippleEnabled();
    }

    /**
     * Enable or disable the ripple effect of this view.
     *
     * @param enabled Set to true if want to enable the ripple effect.
     */
    public void setRippleEnabled(boolean enabled) {
        getPaperBackground().setRippleEnabled(enabled);
        postInvalidate();
    }

    /**
     * Check whether the ripple will be active via touch event.
     *
     * @return Return true if the ripple will be active by touch event.
     */
    public boolean isRippleOnTouchEnabled() {
        return getPaperBackground().isRippleOnTouchEnabled();
    }

    /**
     * Enable the ripple effect on the touch event.
     *
     * @param enabled Set to true if want to enable the ripple effect on the touch event.
     */
    public void setRippleOnTouchEnabled(boolean enabled) {
        getPaperBackground().setRippleOnTouchEnabled(enabled);
    }

    /**
     * Get the drawable of the user specified background, which is the inner Drawable of the wrapper
     * {@link PaperDrawable}.
     *
     * @return the inner drawable of the background.
     */
    @Override
    public Drawable getBackground() {
        return getPaperBackground().getOriginal();
    }

    /**
     * Set the inner background of the wrapper {@link PaperDrawable}, so the material design effect
     * will not be affected if the background is changed by the user.
     *
     * @param background The new drawable for the background.
     */
    @Override
    public void setBackground(Drawable background) {
        if (background instanceof PaperDrawable) {
            //  replace the background of PaperDrawable
            super.setBackground(background);
        } else {
            //  replace the inner original PaperDrawable
            getPaperBackground().setOriginal(background);
        }
    }

    /**
     * Get the shadow depth,
     * @return
     */
    public float getDepth() {
        return getPaperBackground().getDepth();
    }

    /**
     * Set the shadow depth.
     *
     * The depth is a relative depth to the view below, just the value for how the depth will be drawn.
     * It's not the absolute z-depth. So, if the setDepth(1) on the below view, and setDepth(2) on the
     * above view, the result shadow depth will still be <b>2</b>, rather than <b>1</b>.
     *
     * @param depth The depth value for the shadow.
     */

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
