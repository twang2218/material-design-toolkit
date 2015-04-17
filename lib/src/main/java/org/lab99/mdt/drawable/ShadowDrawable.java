package org.lab99.mdt.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v8.renderscript.RenderScript;

import org.lab99.mdt.utils.Utils;

public class ShadowDrawable extends Drawable {
    //  Max Depth
    private final static float DEPTH_MAX = 5;

    //  variables
    private ShadowState mState;

    public ShadowDrawable() {
        this(null);
        mState.mOnStateChangedListener = new ShadowStateChanger(this);
    }

    ShadowDrawable(ShadowState state) {
        mState = new ShadowState(state);
    }

    public void destroy() {
        mState.destroy();
    }

    @Override
    public void draw(Canvas canvas) {
        mState.mRender.draw(canvas, mState.mShadow);
    }

    @Override
    public int getAlpha() {
        return mState.mShadow.mAlpha;
    }

    @Override
    public void setAlpha(int alpha) {
        mState.mShadow.mAlpha = alpha;
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
        return 0;
    }

    //  Getters & Setters

    public void setRenderScript(RenderScript renderScript) {
        mState.setRenderScript(renderScript);
        invalidateSelf();
    }

    public void setMaskDrawer(Drawer drawer) {
        mState.mShadow.mMaskDrawer = drawer;
        invalidateSelf();
    }

    public float getDepth() {
        return mState.mShadow.mDepth;
    }

    public void setDepth(float depth) {
        float d;
        if (depth < 0) {
            d = 0;
        } else if (depth > DEPTH_MAX) {
            d = DEPTH_MAX;
        } else {
            d = depth;
        }

        if (d != getDepth()) {
            mState.mShadow.mDepth = d;
        }
        invalidateSelf();
    }

    public float getRotation() {
        return mState.mShadow.mRotation;
    }

    public void setRotation(float rotation) {
        mState.mShadow.mRotation = rotation;
        invalidateSelf();
    }

    public OnStateChangedListener getOnStateChangedListener() {
        return mState.mOnStateChangedListener;
    }

    public void setOnStateChangedListener(OnStateChangedListener listener) {
        mState.mOnStateChangedListener = listener;
    }

    @Override
    public boolean setState(int[] stateSet) {
        boolean changed = false;
        if (getOnStateChangedListener() != null) {
            changed = getOnStateChangedListener().onStateChange(getState(), stateSet);
        }

        return super.setState(stateSet) || changed;
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        return getOnStateChangedListener() != null && getOnStateChangedListener().onStateChange(getState(), stateSet);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mState.mShadow.mBounds = bounds;
        invalidateSelf();
    }

    public void setContext(Context context) {
        try {
            mState.mRender.setRenderScript(RenderScript.create(context));
        } catch (Throwable error) {
            error.printStackTrace();
        }
        Utils.init(context);
    }

    @Override
    public ConstantState getConstantState() {
        return mState;
    }

    static class Shadow {
        //  Offset function calculate from offset set {1.5, 3, 10, 14, 19}
        //  Material Design Guideline > Layout > Dimensionality > Shadows
        //  offset(depth) = 0.57 * depth^2 + 0.91 * depth;
        private final static float SHADOW_OFFSET_A = 0.57f;
        private final static float SHADOW_OFFSET_B = 0.91f;
        //  blur_radius(offset) = 1.5 * offset;
        private final static float SHADOW_BLUR_RADIUS_OFFSET_FACTOR = 1.5f;

        //  properties
        float mDepth;
        Rect mBounds;
        float mRotation;
        int mAlpha;
        //  handler
        Drawer mMaskDrawer;

        public Shadow() {
            mBounds = new Rect();
            mAlpha = 255;
        }

        public Shadow(Shadow o) {
            mDepth = o.mDepth;
            mBounds = new Rect(o.mBounds);
            mRotation = o.mRotation;
            mAlpha = o.mAlpha;
            mMaskDrawer = o.mMaskDrawer;
        }

        public float getShadowBlurRadius() {
            float offset = SHADOW_OFFSET_A * (mDepth * mDepth) + SHADOW_OFFSET_B * mDepth;
            return offset * SHADOW_BLUR_RADIUS_OFFSET_FACTOR;
        }

        public float getShadowOffset() {
            //  offset = (a * depth * depth) + (b * depth)
            float offset = SHADOW_OFFSET_A * (mDepth * mDepth) + SHADOW_OFFSET_B * mDepth;
            return Utils.getPixelFromDip(offset);
        }
    }


    //  State
    private static class ShadowState extends ConstantState {
        //  Shadow
        Shadow mShadow;
        //  Render
        ShadowRender mRender;
        //  Handler
        OnStateChangedListener mOnStateChangedListener;

        ShadowState(ShadowState orig) {
            if (orig != null)
                initWithState(orig);
            else
                initWithoutState();
        }

        private void initWithState(ShadowState orig) {
            //  shadow
            mShadow = new Shadow(orig.mShadow);
            //  render
            mRender = new ShadowRender(orig.mRender);
            //  Handler
            mOnStateChangedListener = orig.mOnStateChangedListener;
        }

        private void initWithoutState() {
            mShadow = new Shadow();
            mRender = new ShadowRender((RenderScript) null);
            //  everything else is either 0 or null
        }

        public void destroy() {
            if (mRender != null) {
                mRender.destroy();
            }
        }

        //  Getters & Setters

        public void setRenderScript(RenderScript renderScript) {
            if (renderScript != null) {
                if (mRender == null) {
                    mRender = new ShadowRender(renderScript);
                } else {
                    mRender.setRenderScript(renderScript);
                }
            }
        }

        @Override
        public Drawable newDrawable() {
            return new ShadowDrawable(this);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }
    }
}
