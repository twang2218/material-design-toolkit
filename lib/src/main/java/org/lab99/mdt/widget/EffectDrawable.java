package org.lab99.mdt.widget;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public class EffectDrawable extends ProxyDrawable implements Drawable.Callback {
    //  0 - Self Shadow
    //  1 - Original Background
    //  2 - Ripple
    //  3 - Child Shadow

    public EffectDrawable(@NonNull Drawable original) {
        this(original, null);
    }

    EffectDrawable(@NonNull Drawable original, EffectState state) {
        super(original, state);
    }

    EffectDrawable(EffectState state, Resources res) {
        super(state, res);
    }

    @Override
    protected ProxyState createConstantState(ProxyState orig, Callback callback, Resources res) {
        return new EffectState((EffectState) orig, callback, res);
    }

    static class EffectState extends ProxyState {
        private Drawable mShadowSelf;
        private Drawable mRipple;
        private Drawable mShadowChild;

        EffectState(EffectState orig, Callback callback, Resources res) {
            super(orig, callback, res);

            if (orig != null) {
                mShadowSelf = orig.mShadowSelf.getConstantState().newDrawable(res);
                mRipple = orig.mRipple.getConstantState().newDrawable(res);
                mShadowChild = orig.mShadowChild.getConstantState().newDrawable(res);
            }
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new EffectDrawable(this, res);
        }

        @Override
        public Drawable newDrawable() {
            return new EffectDrawable(this, null);
        }

        public Drawable getShadowSelf() {
            return mShadowSelf;
        }

        public void setShadowSelf(Drawable shadow) {
            mShadowSelf = prepare(mShadowSelf, shadow);
        }

        public Drawable getRipple() {
            return mRipple;
        }

        public void setRipple(Drawable ripple) {
            mRipple = prepare(mRipple, ripple);
        }

        public void setShadowChild(Drawable shadow) {
            mShadowChild = prepare(mShadowChild, shadow);
        }

        public Drawable getmShadowChild() {
            return mShadowChild;
        }
    }
}
