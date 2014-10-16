package org.lab99.mdt.drawable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * Ripple Drawable of Material Design Ripple effect
 */
public class RippleDrawable extends Drawable {
    private final static int DEFAULT_MIN_RADIUS = 30;
    private float mMinRadius;
    private float mMaxRadius;
    private Bitmap mBitmapRipple;
    private Bitmap mBitmapBackground;
    private RippleState mState;
    private float mRadius;

    public RippleDrawable() {
        this(null);

        setRippleEnabled(false);
        setOverlayEnabled(false);

        mState.mOnStateChangedListener = new RippleStateChanger(this);
        mMinRadius = DEFAULT_MIN_RADIUS;
    }

    RippleDrawable(RippleState state) {
        mState = new RippleState(state);
    }

    public float getRippleProgress() {
        return mState.mRippleProgress;
    }

    public void setRippleProgress(float progress) {
        //System.out.println("setRippleProgress(" + progress + ")");
        mState.mRippleProgress = progress;
        mRadius = calculateRadius();

        //  Call 'invalidateSelf()' will trigger 'View.invalidate()', so this will redrawn later.
        invalidateSelf();
    }

    private float calculateRadius() {
        return mState.mRippleProgress * (mMaxRadius - mMinRadius) + mMinRadius;
    }

    public boolean isEnabled() {
        return mState.mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mState.mEnabled = enabled;
        invalidateSelf();
    }

    public float getOverlayAlpha() {
        return mState.mOverlayAlpha;
    }

    public void setOverlayAlpha(float alpha) {
        //System.out.println("setOverlayAlpha(" + alpha + ")");
        mState.mOverlayAlpha = alpha;
        invalidateSelf();
    }

    public OnStateChangedListener getOnStateChangedListener() {
        return mState.mOnStateChangedListener;
    }

    public void setOnStateChangedListener(OnStateChangedListener listener) {
        mState.mOnStateChangedListener = listener;
    }

    public PointF getRippleCenter() {
        return mState.mRippleCenter;
    }

    public RippleDrawable setRippleCenter(float x, float y) {
        mState.mRippleCenter.set(x, y);
        mMaxRadius = calculateMaxRadius();
        invalidateSelf();
        return this;
    }

    public int getRippleColor() {
        return mState.mRippleColor;
    }

    public RippleDrawable setRippleColor(int color) {
        mState.mRippleColor = color;
        invalidateSelf();
        return this;
    }

    public int getOverlayColor() {
        return mState.mOverlayColor;
    }

    public RippleDrawable setOverlayColor(int color) {
        mState.mOverlayColor = color;
        mState.mOverlayAlpha = 1;
        invalidateSelf();
        return this;
    }

    boolean isRippleEnabled() {
        return mState.mRippleEnabled;
    }

    RippleDrawable setRippleEnabled(boolean value) {
        mState.mRippleEnabled = value;
        invalidateSelf();
        return this;
    }

    boolean isOverlayEnabled() {
        return mState.mOverlayEnabled;
    }

    RippleDrawable setOverlayEnabled(boolean value) {
        mState.mOverlayEnabled = value;
        invalidateSelf();
        return this;
    }

    /**
     * @param alpha Range is [0,1)
     * @return
     */
    public RippleDrawable setAlpha(float alpha) {
        mState.mAlpha = alpha;
        invalidateSelf();
        return this;
    }

    public Drawer getMaskDrawer() {
        return mState.mMaskDrawer;
    }

    public void setMaskDrawer(Drawer drawer) {
        mState.mMaskDrawer = drawer;
    }

    public TouchTracker getTouchTracker() {
        return mState.mTouchTracker;
    }

    public void setTouchTracker(TouchTracker tracker) {
        mState.mTouchTracker = tracker;
    }

    @Override
    public void draw(Canvas canvas) {
        if (isEnabled() && !getBounds().isEmpty()) {
            if (getMaskDrawer() != null && mRadius >= DEFAULT_MIN_RADIUS) {
                //  Clear bitmap
                mBitmapBackground.eraseColor(Color.TRANSPARENT);
                mBitmapRipple.eraseColor(Color.TRANSPARENT);

                //  Because we need PorterDuff.Mode.SRC_ATOP later, the canvas has to be empty
                //  So, we cannot use given canvas, which contains other stuff already.
                Canvas c = new Canvas(mBitmapBackground);
                //  draw background
                getMaskDrawer().draw(c);

                //  Render Ripple Layer
                //  Ripple has to be drawn on a Bitmap layer, otherwise later Mode.SRC_ATOP will be wrong
                Canvas cr = new Canvas(mBitmapRipple);
                //  render ripple
                drawRipple(cr);

                //  draw ripple layer on background
                c.drawBitmap(mBitmapRipple, 0, 0, mState.mOverlayPaint);

                //  draw everything on given canvas
                canvas.drawBitmap(mBitmapBackground, 0, 0, null);
            } else {
                //  draw ripple directly
                drawRipple(canvas);
            }
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mState.mOverlayPaint.setColorFilter(cf);
        mState.mRipplePaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        if (!bounds.isEmpty()) {
            createBitmaps();
        }
        setRippleCenter(bounds.width() / 2, bounds.height() / 2);
        mMaxRadius = calculateMaxRadius();
    }

    @Override
    public void setAlpha(int alpha) {
        mState.mAlpha = alpha / 255f;
    }

    @Override
    public boolean isStateful() {
        return true;
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
    public ConstantState getConstantState() {
        return mState;
    }

    private float calculateMaxRadius() {
        float w = getBounds().width();
        float h = getBounds().height();

        PointF c = mState.mRippleCenter;

        if (c.x == 0 && c.y == 0) {
            return (float) Math.sqrt(w * w + h * h);
        } else {
            float l1 = PointF.length(c.x, c.y);
            float l2 = PointF.length(c.x, h - c.y);
            float l3 = PointF.length(w - c.x, c.y);
            float l4 = PointF.length(w - c.x, h - c.y);
            return Math.max(Math.max(l1, l2), Math.max(l3, l4));
        }
    }

    private void createBitmaps() {
        Rect bound = getBounds();
        mBitmapRipple = Bitmap.createBitmap(bound.width(), bound.height(), Bitmap.Config.ARGB_8888);
        mBitmapBackground = Bitmap.createBitmap(bound.width(), bound.height(), Bitmap.Config.ARGB_8888);
    }

    private void drawRipple(Canvas canvas) {
        //  draw overlay layer
        if (mState.mOverlayEnabled) {
            //  calculate the correct overlay color with current alpha
            int c = Color.argb(
                    (int) (Color.alpha(mState.mOverlayColor) * mState.mOverlayAlpha * mState.mAlpha),
                    Color.red(mState.mOverlayColor),
                    Color.green(mState.mOverlayColor),
                    Color.blue(mState.mOverlayColor)
            );
            canvas.drawColor(c);
        }
        //  draw ripple layer
        if (mState.mRippleEnabled) {
            mState.mRipplePaint.setColor(mState.mRippleColor);
            mState.mRipplePaint.setAlpha((int) (Color.alpha(mState.mRippleColor) * mState.mAlpha));
            canvas.drawCircle(mState.mRippleCenter.x, mState.mRippleCenter.y, mRadius, mState.mRipplePaint);
        }
    }

    static class RippleState extends ConstantState {
        //  General
        boolean mEnabled;
        float mAlpha;
        //  Ripple
        boolean mRippleEnabled;
        PointF mRippleCenter;
        int mRippleColor;
        float mRippleProgress;
        Paint mRipplePaint;
        //  Overlay
        boolean mOverlayEnabled;
        float mOverlayAlpha;
        int mOverlayColor;
        Paint mOverlayPaint;
        //  Handler
        Drawer mMaskDrawer;
        TouchTracker mTouchTracker;
        OnStateChangedListener mOnStateChangedListener;

        RippleState(RippleState orig) {
            if (orig != null) {
                initWithState(orig);
            } else {
                initWithoutState();
            }
        }

        private void initWithState(RippleState orig) {
            //  General
            mEnabled = orig.mEnabled;
            mAlpha = orig.mAlpha;
            //  Ripple
            mRippleEnabled = orig.mRippleEnabled;
            mRippleCenter = new PointF(orig.mRippleCenter.x, orig.mRippleCenter.y);
            mRippleColor = orig.mRippleColor;
            mRippleProgress = orig.mRippleProgress;
            mRipplePaint = new Paint(orig.mRipplePaint);
            //  Overlay
            mOverlayEnabled = orig.mOverlayEnabled;
            mOverlayAlpha = orig.mOverlayAlpha;
            mOverlayColor = orig.mOverlayColor;
            mOverlayPaint = new Paint(orig.mOverlayPaint);
            //  Handler
            mMaskDrawer = orig.mMaskDrawer;
            mTouchTracker = orig.mTouchTracker;
            mOnStateChangedListener = orig.mOnStateChangedListener;
        }

        private void initWithoutState() {
            //  General
            mEnabled = true;
            mAlpha = 1;
            //  Ripple
            mRippleEnabled = true;
            mRippleCenter = new PointF();
            mRippleColor = 0x40cccccc;  //  mdt_button_pressed_dark
            mRippleProgress = 0;
            mRipplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            //  Overlay
            mOverlayEnabled = true;
            mOverlayAlpha = 0;
            mOverlayColor = 0x26cccccc; //  mdt_button_hover_dark
            mOverlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mOverlayPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
        }

        @Override
        public Drawable newDrawable() {
            return new RippleDrawable(this);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }

    }
}
