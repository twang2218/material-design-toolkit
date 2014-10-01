package org.lab99.mdt.widget;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

import org.lab99.mdt.R;

public class RippleAnimation {
    private final static long DEFAULT_DURATION = 300;
    private final static float FOCUS_RANGE_LARGE = 0.30f;
    private final static float FOCUS_RANGE_SMALL = 0.25f;

    //  Ripple component
    private View mView;
    private Drawable mOriginal;
    private RippleDrawable mRipple;
    private long mDuration;
    //  Animation
    private boolean mEnableReleaseAnimation = true;
    private boolean mEnableFocusedAnimation = true;
    private boolean mEnablePressedAnimation = true;

    private ObjectAnimator mRippleAnimator;
    private ObjectAnimator mOverlayAnimator;
    private ObjectAnimator mAlphaAnimator;

    private boolean mEnableRippleEffect = true;
    private boolean mEnableOverlayEffect = true;

    //  for Touch event
    private boolean mPressed;
    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction() & MotionEvent.ACTION_MASK;
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
                final int[] mLocationOnScreen = new int[2];

                //  update ripple center
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    //  Honeycomb and later
                    mView.getLocationOnScreen(mLocationOnScreen);
                    float rX = event.getRawX() - mLocationOnScreen[0];
                    float rY = event.getRawY() - mLocationOnScreen[1];
                    setCenter(rX, rY);
                } else {
                    //  pre-Honeycomb
                    //  since we cannot get the view, then assume this drawable is the background of 'view'
                    setCenter(event.getX(), event.getY());
                }

                //  firing animation
                boolean is_pressed_now = (action == MotionEvent.ACTION_DOWN);
                if (mPressed != is_pressed_now) {
                    //  pressed state changed
                    if (is_pressed_now) {
                        //  just pressed
                        animatePressed();
                    } else {
                        //  just release
                        animateRelease();
                    }
                    mPressed = is_pressed_now;
                }
            } else if (action == MotionEvent.ACTION_CANCEL) {
                animateRelease();
            }

            //  If the view 'v' is clickable, return True will cause 'onClick' event being suppressed;
            //  If 'v' is NOT clickable, return False will cause 'ViewGroup' stop passing 'ACTION_UP' here.
            return !v.isClickable();
        }
    };

    private View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus)
                animateFocused();
            else
                animateRelease();
        }
    };

    /**
     * Creating RippleAnimator will replace the given 'view' background drawable with a wrapper RippleDrawable.
     *
     * @param view attached view
     */
    public RippleAnimation(View view) {
        if (view == null) {
            throw new IllegalArgumentException("The 'view' cannot be 'null'.");
        }

        mView = view;
        //  Replace the original Drawable with RippleDrawable
        mOriginal = view.getBackground();
        mRipple = new RippleDrawable();
        mRipple.setOverlayColor(view.getResources().getColor(R.color.mdt_button_hover_dark));
        mRipple.setRippleColor(view.getResources().getColor(R.color.mdt_button_pressed_dark));
        mRipple.setRippleEnabled(false);
        mRipple.setOverlayEnabled(false);
        mRipple.setRippleCenter(mView.getWidth() / 2, mView.getHeight() / 2);
        setViewBackground(mRipple);

        //  init animator
        mRippleAnimator = ObjectAnimator.ofFloat(mRipple, "rippleProgress", 1);
        mOverlayAnimator = ObjectAnimator.ofFloat(mRipple, "overlayAlpha", 1, 0);
        mAlphaAnimator = ObjectAnimator.ofInt(mRipple, "alpha", 255, 0);
        mPressed = false;
        setDuration(DEFAULT_DURATION);
    }

    public long getDuration() {
        return mDuration;
    }

    public RippleAnimation setDuration(long duration) {
        mDuration = duration;
        return this;
    }

    public RippleAnimation setEnableReleaseAnimation(boolean value) {
        mEnableReleaseAnimation = value;
        return this;
    }

    public RippleAnimation setEnableFocusedAnimation(boolean value) {
        mEnableFocusedAnimation = value;
        return this;
    }

    public RippleAnimation setEnablePressedAnimation(boolean value) {
        mEnablePressedAnimation = value;
        return this;
    }

    public RippleAnimation setRippleColor(int color) {
        mRipple.setRippleColor(color);
        return this;
    }

    public RippleAnimation setOverlayColor(int color) {
        mRipple.setOverlayColor(color);
        return this;
    }

    public RippleAnimation setCenter(float cx, float cy) {
        mRipple.setRippleCenter(cx, cy);
        return this;
    }

    public RippleAnimation setTouchView(View view) {
        view.setOnTouchListener(mOnTouchListener);
        view.setOnFocusChangeListener(mOnFocusChangeListener);
        return this;
    }

    public RippleAnimation setEnableRippleEffect(boolean enable) {
        mEnableRippleEffect = enable;
        return this;
    }

    public RippleAnimation setEnableOverlayEffect(boolean enable) {
        mEnableOverlayEffect = enable;
        return this;
    }

    public boolean isRunning() {
        return mRippleAnimator.isRunning() || mOverlayAnimator.isRunning() || mAlphaAnimator.isRunning();
    }

    public void cancel() {
        if (mRippleAnimator.isRunning())
            mRippleAnimator.cancel();

        if (mOverlayAnimator.isRunning())
            mOverlayAnimator.cancel();

        if (mAlphaAnimator.isRunning())
            mAlphaAnimator.cancel();
    }

    /**
     * Restore view's original Drawable
     */
    public void release() {
        setViewBackground(mOriginal);
    }

    @SuppressWarnings("deprecation")
    private void setViewBackground(Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mView.setBackground(drawable);
        } else {
            mView.setBackgroundDrawable(drawable);
        }

    }

    public void animateRelease() {
        if (mEnableReleaseAnimation) {
            mAlphaAnimator.setDuration(getDuration());
            mAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRipple.setAlpha(1f);
                    mRipple.setRippleEnabled(false);
                    mRipple.setOverlayEnabled(false);
                }
            });
            mAlphaAnimator.start();
            if (mOverlayAnimator.isRunning()) {
                mOverlayAnimator.clone();
            }
            mOverlayAnimator.setDuration(getDuration());
            mOverlayAnimator.setFloatValues(0);
            mOverlayAnimator.start();
        }
    }

    public void animateFocused() {
        if (mEnableFocusedAnimation) {
            if (isRunning()) {
                cancel();
            }

            mRipple.setRippleCenter(mView.getWidth() / 2, mView.getHeight() / 2);
            mRipple.setAlpha(1f);
            mRipple.setRippleEnabled(mEnableRippleEffect);
            mRipple.setOverlayEnabled(mEnableOverlayEffect);
            mRipple.setOverlayAlpha(1);

            //  Transit Animation
            //  Ripple
            if (mEnableRippleEffect) {
                final float begin, end;
                begin = FOCUS_RANGE_LARGE;
                end = FOCUS_RANGE_SMALL;
                mRippleAnimator.setDuration(getDuration());
                mRippleAnimator.setFloatValues(0, begin);
                mRippleAnimator.setInterpolator(new DecelerateInterpolator());
                mRippleAnimator.setRepeatCount(0);
                mRippleAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //  Holding Animation
                        mRippleAnimator.setDuration(getDuration());
                        mRippleAnimator.setFloatValues(begin, end);
                        mRippleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                        mRippleAnimator.setRepeatCount(ValueAnimator.INFINITE);
                        mRippleAnimator.setRepeatMode(ValueAnimator.REVERSE);
                        mRippleAnimator.start();
                        //  remove this listener
                        mRippleAnimator.removeListener(this);
                    }
                });
                mRippleAnimator.start();
            }
        }
    }

    public void animatePressed() {
        if (mEnablePressedAnimation) {
            float current_ripple = 0;

            if (isRunning()) {
                cancel();
                current_ripple = (Float) mRippleAnimator.getAnimatedValue();
            }

            mRipple.setAlpha(1f);
            mRipple.setRippleEnabled(mEnableRippleEffect);
            mRipple.setOverlayEnabled(mEnableOverlayEffect);
            mRipple.setOverlayAlpha(1);

            //  Release Animation
            if (mEnableRippleEffect) {
                if (current_ripple < FOCUS_RANGE_LARGE) {
                    mRippleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                } else {
                    //  let the larger ripple go, and start another one
                    current_ripple = 0;
                    mRippleAnimator.setInterpolator(new DecelerateInterpolator());
                }
                mRippleAnimator.setDuration(getDuration());
                mRippleAnimator.setRepeatCount(0);
                mRippleAnimator.setFloatValues(current_ripple, 1);
                mRippleAnimator.start();
                if (mOverlayAnimator.isRunning()) {
                    mOverlayAnimator.cancel();
                }
            }
        }
    }
}
