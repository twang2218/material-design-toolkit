package org.lab99.mdt.drawable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;


public class PaperDrawable extends ProxyDrawable implements Drawable.Callback {
    Context mContext;

    public PaperDrawable(Context context) {
        this(null, null, context);
    }

    public PaperDrawable(Drawable original, Context context) {
        this(original, null, context);
    }

    PaperDrawable(Drawable original, PaperState state, Context context) {
        super(original, state);
        setContext(context);
    }

    PaperDrawable(PaperState state, Resources res) {
        super(state, res);
    }

    @Override
    protected ProxyState createConstantState(ProxyState orig, Resources res) {
        return new PaperState((PaperState) orig, res);
    }

    @Override
    public void draw(Canvas canvas) {
        getShadowSelf().draw(canvas);
        super.draw(canvas);
        getRipple().draw(canvas);
        getShadowChild().draw(canvas);
    }

    @Override
    public boolean setState(int[] stateSet) {
        boolean ret = super.setState(stateSet);

        getShadowSelf().setState(stateSet);
        getRipple().setState(stateSet);

        return ret;
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        getShadowSelf().setBounds(bounds);
        getRipple().setBounds(bounds);
    }

    private PaperState getPaperState() {
        return ((PaperState) getConstantState());
    }

    public ShadowDrawable getShadowSelf() {
        return getPaperState().mShadowSelf;
    }

    public RippleDrawable getRipple() {
        return getPaperState().mRipple;
    }

    public Drawable getShadowChild() {
        return getPaperState().mShadowChildren;
    }

    public TouchTracker getTouchTracker() {
        return getPaperState().mTouchTracker;
    }

    public boolean isRippleEnabled() {
        return getRipple().isEnabled();
    }

    public void setRippleEnabled(boolean enabled) {
        getRipple().setEnabled(enabled);
    }

    public boolean isRippleOnTouchEnabled() {
        return getRipple().getOnStateChangedListener().isEnabled();
    }

    public void setRippleOnTouchEnabled(boolean enabled) {
        getRipple().getOnStateChangedListener().setEnabled(enabled);
    }

    public float getDepth() {
        return getShadowSelf().getDepth();
    }

    public void setDepth(float depth) {
        getShadowSelf().setDepth(depth);
    }

    public float getRotation() {
        return getShadowSelf().getRotation();
    }

    public void setRotation(float rotation) {
        getShadowSelf().setRotation(rotation);
    }

    public void setContext(Context context) {
        mContext = context;
        getShadowSelf().setContext(mContext);
    }

    /* Ripple */

    public void setRippleColor(int color) {
        getRipple().setRippleColor(color);
    }

    static class PaperState extends ProxyState {
        //  0 - Self Shadow
        //  1 - Original Background
        //  2 - Ripple
        //  3 - Children Shadow

        ShadowDrawable mShadowSelf;
        RippleDrawable mRipple;
        Drawable mShadowChildren;
        TouchTracker mTouchTracker;

        PaperState(PaperState orig, Resources res) {
            super(orig, res);
        }

        @Override
        public Drawable newDrawable() {
            return new PaperDrawable(this, null);
        }

        @Override
        protected void initWithState(ProxyState orig, Resources res) {
            super.initWithState(orig, res);
            PaperState state = (PaperState) orig;
            mShadowSelf = (ShadowDrawable) state.mShadowSelf.getConstantState().newDrawable(res);
            mRipple = (RippleDrawable) state.mRipple.getConstantState().newDrawable(res);
            mShadowChildren = state.mShadowChildren.getConstantState().newDrawable(res);
            setTouchTracker(state.mTouchTracker);
        }

        @Override
        protected void initWithoutState(Resources res) {
            super.initWithoutState(res);
            mShadowSelf = new ShadowDrawable();
            mRipple = new RippleDrawable();
            //  TODO: to be remove
            mShadowChildren = new ColorDrawable(Color.TRANSPARENT);
            setTouchTracker(new TouchTracker());
        }

        @Override
        protected void setCallback(Callback callback) {
            super.setCallback(callback);
            mShadowSelf.setCallback(callback);
            mRipple.setCallback(callback);
            mShadowChildren.setCallback(callback);
        }

        @Override
        protected boolean verifyDrawable(Drawable who) {
            return super.verifyDrawable(who)
                    || who == mShadowSelf
                    || who == mRipple
                    || who == mShadowChildren;
        }

        @Override
        public void setOriginal(Drawable original) {
            super.setOriginal(original);
            if (original != null) {
                Drawer masker = new Drawer() {
                    @Override
                    public void draw(Canvas canvas) {
                        if (getOriginal() != null) {
                            getOriginal().draw(canvas);
                        }
                    }
                };
                mShadowSelf.setMaskDrawer(masker);
                mRipple.setMaskDrawer(masker);
            } else {
                //  clear the mask as original is null
                mShadowSelf.setMaskDrawer(null);
                mRipple.setMaskDrawer(null);
            }
        }


        public void setShadowSelf(ShadowDrawable shadow) {
            if (shadow != null) {
                mShadowSelf = (ShadowDrawable) shadow.getConstantState().newDrawable();
            }
        }

        public void setRipple(RippleDrawable ripple) {
            if (ripple != null) {
                mRipple = (RippleDrawable) ripple.getConstantState().newDrawable();
            }
        }

        public void setShadowChild(Drawable shadow) {
            if (shadow != null) {
                mShadowChildren = shadow.getConstantState().newDrawable();
            }
        }

        public void setTouchTracker(TouchTracker tracker) {
            mTouchTracker = tracker;
            mRipple.setTouchTracker(mTouchTracker);
        }
    }
}
