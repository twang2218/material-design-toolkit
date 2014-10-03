package org.lab99.mdt.widget;

import com.nineoldandroids.animation.ObjectAnimator;

import java.lang.ref.WeakReference;

class ShadowStateChanger extends StateChanger {
    //  Lift on touch
    //  http://www.google.com/design/spec/animation/responsive-interaction.html#responsive-interaction-ink-reactions
    private final static float TOUCH_LIFT_DEPTH = 2;
    private final static long DEFAULT_DURATION = 150;

    private WeakReference<ShadowDrawable> mShadow;
    private ObjectAnimator mShadowAnimator;

    ShadowStateChanger(ShadowDrawable shadowDrawable) {
        mShadow = new WeakReference<ShadowDrawable>(shadowDrawable);
        mShadowAnimator = ObjectAnimator.ofFloat(getShadow(), "depth", getShadow().getDepth() + 1);
        mShadowAnimator.setDuration(DEFAULT_DURATION);
    }

    private ShadowDrawable getShadow() {
        return mShadow.get();
    }

    @Override
    public void onPressed() {
        if (isRunning()) {
            cancel();
        }

        mShadowAnimator.setFloatValues(getShadow().getDepth() + TOUCH_LIFT_DEPTH);
        mShadowAnimator.start();
    }

    @Override
    public void onReleased() {
        if (isRunning()) {
            cancel();
        }

        mShadowAnimator.setFloatValues(getShadow().getDepth());
        mShadowAnimator.start();
    }

    @Override
    public void onFocused() {

    }

    @Override
    public boolean isRunning() {
        return mShadowAnimator.isRunning();
    }

    @Override
    public void cancel() {
        if (isRunning()) {
            mShadowAnimator.cancel();
        }
    }

    public void setDuration(long duration) {
        mShadowAnimator.setDuration(duration);
    }
}
