package org.lab99.mdt.drawable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

/**
 * A wrapper {@link Drawable} to implement the material design concept.
 * <p/>
 * The drawable contains several component:
 * <ul>
 * <li>An original {@link Drawable} for the normal background.</li>
 * <li>{@link RippleDrawable} for handling the ripple effect</li>
 * <li>{@link ShadowDrawable} for handling the shadow beneath the view</li>
 * </ul>
 */
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

    /**
     * Draw on the canvas.
     *
     * Draw the shadow first, and then draw the original drawable, then draw the ripple effect finally.
     *
     * @param canvas The given canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        getShadowSelf().draw(canvas);
        super.draw(canvas);
        getRipple().draw(canvas);
        getShadowChild().draw(canvas);
    }

    /**
     * Set the given state.
     *
     * @param stateSet the given states.
     * @return
     */
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

    ShadowDrawable getShadowSelf() {
        return getPaperState().mShadowSelf;
    }

    RippleDrawable getRipple() {
        return getPaperState().mRipple;
    }

    Drawable getShadowChild() {
        return getPaperState().mShadowChildren;
    }

    /**
     * Get the touch tracker, which is tracking the touch location.
     *
     * @return The {@link TouchTracker} object.
     */
    public TouchTracker getTouchTracker() {
        return getPaperState().mTouchTracker;
    }

    /**
     * Check whether the ripple effect is enabled or not.
     * @return Return true if the ripple is enabled.
     */
    public boolean isRippleEnabled() {
        return getRipple().isEnabled();
    }

    /**
     * Enable the ripple effect.
     * @param enabled Set to true if want to enable the ripple effect.
     */
    public void setRippleEnabled(boolean enabled) {
        getRipple().setEnabled(enabled);
    }

    /**
     * Check whether the ripple will be triggered by touch event.
     * @return Return true if the ripple will be triggered by touch event.
     */
    public boolean isRippleOnTouchEnabled() {
        return getRipple().getOnStateChangedListener().isEnabled();
    }

    /**
     * Enable the ability to trigger the ripple effect via touch event.
     * @param enabled Set to true if want to trigger the ripple effect by touch event.
     */
    public void setRippleOnTouchEnabled(boolean enabled) {
        getRipple().getOnStateChangedListener().setEnabled(enabled);
    }

    /**
     * Get the shadow depth
     * @return Return the shadow depth.
     */
    public float getDepth() {
        return getShadowSelf().getDepth();
    }

    /**
     * Set the shadow depth for drawing the shadow.
     *
     * @param depth The depth of shadow, the effective range is from 0 to 5.
     */
    public void setDepth(float depth) {
        getShadowSelf().setDepth(depth);
    }

    /**
     * Get the rotation value from the view.
     * @return the rotation value.
     */
    public float getRotation() {
        return getShadowSelf().getRotation();
    }

    /**
     * Set the rotation value of the view.
     *
     * The rotation of the attached view should be tracked, as the shadow should always be drawn under
     * the view, in another word, the shadow should always represent the widget depth as the light is
     * from the top, as the material design guideline said, no matter how the widget is rotated.
     *
     * @param rotation Set the same rotation value of the attached view.
     */
    public void setRotation(float rotation) {
        getShadowSelf().setRotation(rotation);
    }

    /**
     * Set {@link Context} object.
     * As {@link Context} object is required in {@link android.support.v8.renderscript.RenderScript} and {@link org.lab99.mdt.utils.Utils#getPixelFromDip(Context, float)}.
     * @param context The {@link Context} object.
     */
    public void setContext(Context context) {
        mContext = context;
        getShadowSelf().setContext(mContext);
    }

    /* Ripple */

    /**
     * Set ripple color.
     * @param color The ripple color.
     */
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
