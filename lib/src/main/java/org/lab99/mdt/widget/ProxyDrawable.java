package org.lab99.mdt.widget;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;

import org.lab99.mdt.utils.DrawableCompat;

import java.lang.ref.WeakReference;

/**
 * A drawable will act as a proxy
 */
public class ProxyDrawable extends Drawable implements Drawable.Callback {
    private ProxyState mState;


    public ProxyDrawable(Drawable original) {
        this(original, null);
    }

    ProxyDrawable(@NonNull Drawable original, ProxyState state) {
        this(state, null);
        mState.setOriginal(original);
        super.setBounds(original.getBounds());
    }

    ProxyDrawable(ProxyState state, Resources res) {
        mState = createConstantState(state, this, res);
    }

    //  Overrides of Drawable

    @Override
    public void draw(Canvas canvas) {
        getOriginal().draw(canvas);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        getOriginal().setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return getOriginal().getOpacity();
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        getOriginal().setBounds(left, top, right, bottom);
        super.setBounds(left, top, right, bottom);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public int getAlpha() {
        return getOriginal().getAlpha();
    }

    @Override
    public void setAlpha(int alpha) {
        getOriginal().setAlpha(alpha);
    }

    @Override
    public void invalidateSelf() {
        getOriginal().invalidateSelf();
    }

    @Override
    public boolean isStateful() {
        return getOriginal().isStateful();
    }

    @Override
    public boolean setState(int[] stateSet) {
        return getOriginal().setState(stateSet);
    }

    @Override
    public int[] getState() {
        return getOriginal().getState();
    }

    @Override
    public int getIntrinsicWidth() {
        return getOriginal().getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return getOriginal().getIntrinsicHeight();
    }

    @Override
    public int getMinimumWidth() {
        return getOriginal().getMinimumWidth();
    }

    @Override
    public int getMinimumHeight() {
        return getOriginal().getMinimumHeight();
    }

    @Override
    public ConstantState getConstantState() {
        return mState;
    }

    //  Overrides of Drawable.Callback

    @Override
    public void invalidateDrawable(Drawable who) {
        Callback callback = DrawableCompat.getCallback(this);
        if (who == getOriginal() && callback != null) {
            callback.invalidateDrawable(this);
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        Callback callback = DrawableCompat.getCallback(this);
        if (who == getOriginal() && callback != null) {
            callback.scheduleDrawable(this, what, when);
        }
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        Callback callback = DrawableCompat.getCallback(this);
        if (who == getOriginal() && callback != null) {
            callback.unscheduleDrawable(this, what);
        }
    }

    //  Getters/Setters

    public Drawable getOriginal() {
        ProxyState state = (ProxyState) getConstantState();
        if (state.getOriginal() != null) {
            return state.getOriginal();
        } else {
            return null;
        }

    }

    public void setOriginal(@NonNull Drawable drawable) {
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
                mOriginal = orig.mOriginal.getConstantState().newDrawable(res);
            }
            mCallback = new WeakReference<Callback>(callback);
            if (mOriginal != null) {
                mOriginal.setCallback(mCallback.get());
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
            mOriginal = prepare(mOriginal, original);
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

        protected Drawable prepare(Drawable src, Drawable target) {
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
