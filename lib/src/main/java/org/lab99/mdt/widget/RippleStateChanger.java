package org.lab99.mdt.widget;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

import java.lang.ref.WeakReference;


class RippleStateChanger extends StateChanger {
    private final static float FOCUS_RANGE_LARGE = 0.30f;
    private final static float FOCUS_RANGE_SMALL = 0.25f;

    private WeakReference<RippleDrawable> mRipple;
    private ObjectAnimator mRippleAnimator;
    private ObjectAnimator mOverlayAnimator;
    private ObjectAnimator mAlphaAnimator;

    private boolean mEnableRippleEffect = true;
    private boolean mEnableOverlayEffect = true;

    RippleStateChanger(RippleDrawable rippleDrawable) {
        mRipple = new WeakReference<RippleDrawable>(rippleDrawable);
        mRippleAnimator = ObjectAnimator.ofFloat(getRipple(), "rippleProgress", 1f);
        mOverlayAnimator = ObjectAnimator.ofFloat(getRipple(), "overlayAlpha", 1f, 0f);
        mAlphaAnimator = ObjectAnimator.ofFloat(getRipple(), "alpha", 1f, 0f);
        mDuration = DEFAULT_DURATION;
    }

    @Override
    public boolean isRunning() {
        return mRippleAnimator.isRunning() || mOverlayAnimator.isRunning() || mAlphaAnimator.isRunning();
    }

    @Override
    public void cancel() {
        if (mRippleAnimator.isRunning())
            mRippleAnimator.cancel();

        if (mOverlayAnimator.isRunning())
            mOverlayAnimator.cancel();

        if (mAlphaAnimator.isRunning())
            mAlphaAnimator.cancel();
    }


    private RippleDrawable getRipple() {
        return mRipple.get();
    }


    @Override
    public void onPressed() {
        float current_ripple = 0;

        if (isRunning()) {
            cancel();
            current_ripple = (Float) mRippleAnimator.getAnimatedValue();
        }

        RippleDrawable ripple = getRipple();
        TouchTracker tracker = ripple.getTouchTracker();
        float x, y;
        if (tracker != null) {
            x = tracker.getLastTouch().x;
            y = tracker.getLastTouch().y;
        } else {
            x = ripple.getRippleCenter().x;
            y = ripple.getRippleCenter().y;
        }
        ripple.setRippleCenter(x, y);
        ripple.setAlpha(1f);
        ripple.setRippleEnabled(mEnableRippleEffect);
        ripple.setOverlayEnabled(mEnableOverlayEffect);
        ripple.setOverlayAlpha(1);

        if (getEnablePressedAnimation()) {
            //  Release Animation
            if (mEnableRippleEffect) {
                if (current_ripple < FOCUS_RANGE_LARGE) {
                    mRippleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                } else {
                    //  let the larger ripple go, and start another one
                    current_ripple = 0;
                    mRippleAnimator.setInterpolator(new DecelerateInterpolator());
                }
                mRippleAnimator.setDuration(mDuration);
                mRippleAnimator.setRepeatCount(0);
                mRippleAnimator.setFloatValues(current_ripple, 1);
                mRippleAnimator.start();
                if (mOverlayAnimator.isRunning()) {
                    mOverlayAnimator.cancel();
                }
            }
        } else {
            ripple.setRippleProgress(1);
        }

    }

    @Override
    public void onReleased() {
        if (getEnablePressedAnimation()) {
            mAlphaAnimator.setDuration(mDuration);
            mAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    RippleDrawable ripple = getRipple();

                    ripple.setAlpha(1f);
                    ripple.setRippleEnabled(false);
                    ripple.setOverlayEnabled(false);
                }
            });
            mAlphaAnimator.start();
            if (mOverlayAnimator.isRunning()) {
                mOverlayAnimator.cancel();
            }
            mOverlayAnimator.setDuration(mDuration);
            mOverlayAnimator.setFloatValues(0);
            mOverlayAnimator.start();
        } else {
            RippleDrawable ripple = getRipple();

            ripple.setAlpha(1f);
            ripple.setRippleEnabled(false);
            ripple.setOverlayEnabled(false);
        }
    }

    @Override
    public void onFocused() {
        if (isRunning()) {
            cancel();
        }

        RippleDrawable ripple = getRipple();
        ripple.setRippleCenter(ripple.getBounds().width() / 2, ripple.getBounds().height() / 2);
        ripple.setAlpha(1f);
        ripple.setRippleEnabled(mEnableRippleEffect);
        ripple.setOverlayEnabled(mEnableOverlayEffect);
        ripple.setOverlayAlpha(1);

        if (getEnableFocusedAnimation()) {
            //  Transit Animation
            //  Ripple
            if (mEnableRippleEffect) {
                final float begin, end;
                begin = FOCUS_RANGE_LARGE;
                end = FOCUS_RANGE_SMALL;
                mRippleAnimator.setDuration(mDuration);
                mRippleAnimator.setFloatValues(0, begin);
                mRippleAnimator.setInterpolator(new DecelerateInterpolator());
                mRippleAnimator.setRepeatCount(0);
                mRippleAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //  Holding Animation
                        mRippleAnimator.setDuration(mDuration);
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
        } else {
            ripple.setRippleProgress(FOCUS_RANGE_LARGE);
        }

    }

    public void setEnableRippleEffect(boolean enable) {
        mEnableRippleEffect = enable;
    }

    public void setEnableOverlayEffect(boolean enable) {
        mEnableOverlayEffect = enable;
    }


}
