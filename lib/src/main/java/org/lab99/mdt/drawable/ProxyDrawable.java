package org.lab99.mdt.drawable;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;

/**
 * A drawable will act as a proxy
 */
class ProxyDrawable extends Drawable implements Drawable.Callback {
    private ProxyState mState;


    public ProxyDrawable(Drawable original) {
        this(original, null);
    }

    ProxyDrawable(Drawable original, ProxyState state) {
        this(state, null);
        mState.setOriginal(original);
        mState.setCallback(this);

        if (original != null && !original.getBounds().isEmpty()) {
            super.setBounds(original.getBounds());
        }
    }

    ProxyDrawable(ProxyState state, Resources res) {
        mState = createConstantState(state, res);
    }

    //  Overrides of Drawable

    @Override
    public void draw(Canvas canvas) {
        if (getOriginal() != null)
            getOriginal().draw(canvas);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (getOriginal() != null)
            getOriginal().setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        if (getOriginal() != null)
            return getOriginal().getOpacity();
        else
            return PixelFormat.TRANSPARENT;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public int getAlpha() {
        if (getOriginal() != null)
            return getOriginal().getAlpha();
        else
            return super.getAlpha();
    }

    @Override
    public void setAlpha(int alpha) {
        if (getOriginal() != null)
            getOriginal().setAlpha(alpha);
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        if (getOriginal() != null)
            return getOriginal().setVisible(visible, restart);
        else
            return super.setVisible(visible, restart);
    }

    @Override
    public boolean isStateful() {
        return getOriginal() != null && getOriginal().isStateful();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        if (getOriginal() != null) {
            boolean changed = getOriginal().setState(state);
            onBoundsChange(getBounds());
            return changed;
        } else {
            return super.onStateChange(state);
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (getOriginal() != null)
            getOriginal().setBounds(bounds);
    }

    @Override
    public int getIntrinsicWidth() {
        if (getOriginal() != null)
            return getOriginal().getIntrinsicWidth();
        else
            return super.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        if (getOriginal() != null)
            return getOriginal().getIntrinsicHeight();
        else
            return super.getIntrinsicHeight();
    }

    @Override
    public int getMinimumWidth() {
        if (getOriginal() != null)
            return getOriginal().getMinimumWidth();
        else
            return super.getMinimumWidth();
    }

    @Override
    public int getMinimumHeight() {
        if (getOriginal() != null)
            return getOriginal().getMinimumHeight();
        else
            return super.getMinimumHeight();
    }

    @Override
    public ConstantState getConstantState() {
        return mState;
    }

    //  Overrides of Drawable.Callback

    @Override
    public void invalidateDrawable(Drawable who) {
        invalidateSelf();
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        scheduleSelf(what, when);
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        unscheduleSelf(what);
    }

    //  Getters/Setters

    public Drawable getOriginal() {
        ProxyState state = (ProxyState) getConstantState();
        return state.getOriginal();
    }

    public void setOriginal(Drawable drawable) {
        if (drawable != null) {
            drawable.setBounds(getBounds());
        }
        ProxyState state = (ProxyState) getConstantState();
        state.setOriginal(drawable);
        state.setCallback(this);
    }

    protected ProxyState createConstantState(ProxyState orig, Resources res) {
        return new ProxyState(orig, res);
    }

    //  ProxyState

    static class ProxyState extends Drawable.ConstantState {
        private Drawable mOriginal;

        ProxyState(ProxyState orig, Resources res) {
            if (orig != null) {
                initWithState(orig, res);
            } else {
                initWithoutState(res);
            }
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new ProxyDrawable(this, res);
        }

        @Override
        public Drawable newDrawable() {
            return new ProxyDrawable(this, null);
        }

        @Override
        public int getChangingConfigurations() {
            return mOriginal != null ? mOriginal.getChangingConfigurations() : 0;
        }

        public Drawable getOriginal() {
            return mOriginal;
        }

        public void setOriginal(Drawable original) {
            if (original != mOriginal) {
                mOriginal = original;
            }
        }

        protected void setCallback(Drawable.Callback callback) {
            if (mOriginal != null) {
                mOriginal.setCallback(callback);
            }
        }

        protected void initWithState(ProxyState orig, Resources res) {
            if (orig.mOriginal != null) {
                mOriginal = orig.mOriginal.getConstantState().newDrawable(res);
            }
        }

        protected void initWithoutState(Resources res) {

        }

        protected boolean verifyDrawable(Drawable who) {
            return who == getOriginal();
        }
    }
}
