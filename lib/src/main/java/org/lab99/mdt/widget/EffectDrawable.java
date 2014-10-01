package org.lab99.mdt.widget;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import org.lab99.mdt.utils.ViewCompat;

public class EffectDrawable extends ProxyDrawable implements Drawable.Callback {
    //  0 - Self Shadow
    //  1 - Original Background
    //  2 - Ripple
    //  3 - Child Shadow

    public EffectDrawable(Drawable original) {
        this(original, null);
    }

    EffectDrawable(Drawable original, EffectState state) {
        super(original, state);
    }

    EffectDrawable(EffectState state, Resources res) {
        super(state, res);
    }

    public static EffectDrawable apply(View view) {
        return apply(view, view);
    }

    /**
     * @param touch_view  touch view will trigger the state change event;
     * @param ripple_view the ripple view has to be able to receive 'onTouch' event
     * @return
     */
    public static EffectDrawable apply(View touch_view, View ripple_view) {
        //  create new warp drawable for the old drawable
        EffectDrawable background = new EffectDrawable(ripple_view.getBackground());
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

    @Override
    protected ProxyState createConstantState(ProxyState orig, Callback callback, Resources res) {
        return new EffectState((EffectState) orig, callback, res);
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

        getRipple().setState(stateSet);
        getShadowSelf().setState(stateSet);

        return ret;
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);

        getRipple().setBounds(left, top, right, bottom);
        getShadowSelf().setBounds(left, top, right, bottom);
    }

    public RippleDrawable getRipple() {
        return ((EffectState) getConstantState()).getRipple();
    }

    public Drawable getShadowSelf() {
        return ((EffectState) getConstantState()).getShadowSelf();
    }

    public Drawable getShadowChild() {
        return ((EffectState) getConstantState()).getShadowChild();
    }

    public TouchTracker getTouchTracker() {
        return ((EffectState) getConstantState()).getTouchTracker();
    }

    static class EffectState extends ProxyState {
        private Drawable mShadowSelf;
        private RippleDrawable mRipple;
        private Drawable mShadowChild;
        private TouchTracker mTouchTracker;

        EffectState(EffectState orig, Callback callback, Resources res) {
            super(orig, callback, res);

        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new EffectDrawable(this, res);
        }

        @Override
        public Drawable newDrawable() {
            return new EffectDrawable(this, null);
        }

        @Override
        protected void initWithState(ProxyState orig, Resources res) {
            super.initWithState(orig, res);
            EffectState state = (EffectState) orig;
            mShadowSelf = state.getShadowSelf().getConstantState().newDrawable(res);
            mRipple = (RippleDrawable) state.getRipple().getConstantState().newDrawable(res);
            mShadowChild = state.getShadowChild().getConstantState().newDrawable(res);
            setTouchTracker(state.mTouchTracker);
        }

        @Override
        protected void initWithoutState(Resources res) {
            super.initWithoutState(res);
            mRipple = new RippleDrawable();
            //  TODO: to be remove
            mShadowSelf = new ColorDrawable(Color.TRANSPARENT);
            mShadowChild = new ColorDrawable(Color.TRANSPARENT);

            setTouchTracker(new TouchTracker());
        }

        @Override
        protected void setCallback(Callback callback) {
            super.setCallback(callback);
            mRipple.setCallback(callback);
            mShadowSelf.setCallback(callback);
            mShadowChild.setCallback(callback);
        }

        @Override
        protected boolean verifyDrawable(Drawable who) {
            return super.verifyDrawable(who)
                    || who == getRipple()
                    || who == getShadowSelf()
                    || who == getShadowChild();
        }

        @Override
        public void setOriginal(Drawable original) {
            super.setOriginal(original);
            if (original != null) {
                mRipple.setMaskDrawer(new Drawer() {
                    @Override
                    public void draw(Canvas canvas) {
                        if (getOriginal() != null) {
                            getOriginal().draw(canvas);
                        }
                    }
                });
            } else {
                mRipple.setMaskDrawer(null);
            }
        }

        public Drawable getShadowSelf() {
            return mShadowSelf;
        }

        public void setShadowSelf(Drawable shadow) {
            mShadowSelf = prepare(mShadowSelf, shadow);
        }

        public RippleDrawable getRipple() {
            return mRipple;
        }

        public void setRipple(RippleDrawable ripple) {
            mRipple = (RippleDrawable) prepare(mRipple, ripple);
        }

        public Drawable getShadowChild() {
            return mShadowChild;
        }

        public void setShadowChild(Drawable shadow) {
            mShadowChild = prepare(mShadowChild, shadow);
        }

        public TouchTracker getTouchTracker() {
            return mTouchTracker;
        }

        public void setTouchTracker(TouchTracker tracker) {
            mTouchTracker = tracker;
            mRipple.setTouchTracker(mTouchTracker);
        }
    }
}
