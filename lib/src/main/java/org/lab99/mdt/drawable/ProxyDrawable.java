package org.lab99.mdt.drawable;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;

import org.lab99.mdt.utils.DrawableCompat;

import java.lang.ref.WeakReference;

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
        if (original != null && !original.getBounds().isEmpty()) {
            super.setBounds(original.getBounds());
        }
    }

    ProxyDrawable(ProxyState state, Resources res) {
        mState = createConstantState(state, this, res);
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
    public void invalidateSelf() {
        if (getOriginal() != null)
            getOriginal().invalidateSelf();
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
        Callback callback = DrawableCompat.getCallback(this);
        if (mState.verifyDrawable(who) && callback != null) {
            callback.invalidateDrawable(this);
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        Callback callback = DrawableCompat.getCallback(this);
        if (mState.verifyDrawable(who) && callback != null) {
            callback.scheduleDrawable(this, what, when);
        }
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        Callback callback = DrawableCompat.getCallback(this);
        if (mState.verifyDrawable(who) && callback != null) {
            callback.unscheduleDrawable(this, what);
        }
    }

    //  Getters/Setters

    public Drawable getOriginal() {
        ProxyState state = (ProxyState) getConstantState();
        return state.getOriginal();
    }

    public void setOriginal(Drawable drawable) {
        ProxyState state = (ProxyState) getConstantState();
        if (state.getOriginal() != null) {
            state.setOriginal(drawable);
        }
    }

    protected ProxyState createConstantState(ProxyState orig, Callback callback, Resources res) {
        return new ProxyState(orig, callback, res);
    }

    //  ProxyState

    static class ProxyState extends Drawable.ConstantState {
        private WeakReference<Callback> mCallback;
        private Drawable mOriginal;

        ProxyState(ProxyState orig, Callback callback, Resources res) {
            if (orig != null) {
                initWithState(orig, res);
            } else {
                initWithoutState(res);
            }
            setCallback(callback);
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
            mOriginal = prepareCallback(mOriginal, original);
        }

        protected Callback getCallback() {
            return mCallback.get();
        }

        protected void setCallback(Callback callback) {
            mCallback = new WeakReference<Callback>(callback);
            if (mOriginal != null) {
                mOriginal.setCallback(mCallback.get());
            }
        }

        protected void initWithState(ProxyState orig, Resources res) {
            mOriginal = orig.mOriginal.getConstantState().newDrawable(res);
        }

        protected void initWithoutState(Resources res) {

        }

        protected boolean verifyDrawable(Drawable who) {
            return who == getOriginal();
        }

        protected Drawable prepareCallback(Drawable src, Drawable target) {
            if (src == target) {
                return target;
            }

            //  remove original drawable callback
            if (src != null) {
                src.setCallback(null);
            }
            //  get a copy of new drawable
            if (target != null) {
                Drawable prepared = target.getConstantState().newDrawable();
                //  set callback
                prepared.setCallback(mCallback.get());

                return prepared;
            } else {
                return null;
            }
        }
    }
}
