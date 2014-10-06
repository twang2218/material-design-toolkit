package org.lab99.mdt.drawable;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

import java.lang.ref.WeakReference;


class RippleStateChanger extends StateChanger {
    private final static float FOCUS_RANGE_LARGE = 0.7f;
    private final static float FOCUS_RANGE_SMALL = 0.5f;

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
        if (isRunning()) {
            cancel();
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
                mRippleAnimator.setInterpolator(new DecelerateInterpolator());
                mRippleAnimator.setDuration(getDuration());
                mRippleAnimator.setRepeatCount(0);
                mRippleAnimator.setFloatValues(0, 1);
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
            mAlphaAnimator.setDuration(getDuration());
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
            mOverlayAnimator.setDuration(getDuration());
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
                mRippleAnimator.setDuration(getDuration() * 2);
                mRippleAnimator.setFloatValues(0, begin);
                mRippleAnimator.setInterpolator(new DecelerateInterpolator());
                mRippleAnimator.setRepeatCount(0);
                mRippleAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        //  Holding Animation
                        mRippleAnimator.setDuration(getDuration() * 4);
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
