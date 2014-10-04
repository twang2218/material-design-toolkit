package org.lab99.mdt.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import org.lab99.mdt.utils.ViewCompat;

public class PaperDrawable extends ProxyDrawable implements Drawable.Callback {
    public PaperDrawable(Drawable original, Context context) {
        this(original, null, context);
    }

    PaperDrawable(Drawable original, PaperState state, Context context) {
        super(original, state);
        ((PaperState) getConstantState()).setContext(context);
    }

    PaperDrawable(PaperState state, Resources res) {
        super(state, res);
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

    @Override
    protected ProxyState createConstantState(ProxyState orig, Callback callback, Resources res) {
        return new PaperState((PaperState) orig, callback, res);
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

    public ShadowDrawable getShadowSelf() {
        return ((PaperState) getConstantState()).mShadowSelf;
    }

    public RippleDrawable getRipple() {
        return ((PaperState) getConstantState()).mRipple;
    }

    public Drawable getShadowChild() {
        return ((PaperState) getConstantState()).mShadowChildren;
    }

    public TouchTracker getTouchTracker() {
        return ((PaperState) getConstantState()).mTouchTracker;
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

    static class PaperState extends ProxyState {
        //  0 - Self Shadow
        //  1 - Original Background
        //  2 - Ripple
        //  3 - Children Shadow

        ShadowDrawable mShadowSelf;
        RippleDrawable mRipple;
        Drawable mShadowChildren;
        TouchTracker mTouchTracker;
        Context mContext;

        PaperState(PaperState orig, Callback callback, Resources res) {
            super(orig, callback, res);
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
            mContext = state.mContext;
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

        public void setContext(Context context) {
            mContext = context;
            mShadowSelf.setContext(mContext);
        }

        public void setShadowSelf(ShadowDrawable shadow) {
            mShadowSelf = (ShadowDrawable) prepareCallback(mShadowSelf, shadow);
        }

        public void setRipple(RippleDrawable ripple) {
            mRipple = (RippleDrawable) prepareCallback(mRipple, ripple);
        }

        public void setShadowChild(Drawable shadow) {
            mShadowChildren = prepareCallback(mShadowChildren, shadow);
        }

        public void setTouchTracker(TouchTracker tracker) {
            mTouchTracker = tracker;
            mRipple.setTouchTracker(mTouchTracker);
        }
    }
}
