package org.lab99.mdt.widget;

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
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

/**
 * Ripple Drawable to simulate Material Design Ripple effect
 */
class RippleDrawable extends Drawable {
    private final static int DEFAULT_MIN_RADIUS = 30;
    private final static float MAX_RADIUS_FACTOR = 1f;
    private Drawable mOriginalDrawable;
    private PointF mCenter;
    private float mRadius;
    private float mMinRadius;
    private float mMaxRadius;
    private float mRippleProgress;
    private float mOverlayAlpha;
    private int mOverlayColor;
    private int mRippleColor;
    private float mGeneralAlpha;
    private Paint mPaintRipple = new Paint();
    private Paint mPaintOverlay = new Paint();
    private Bitmap mBitmapRipple;
    private Bitmap mBitmapBackground;
    private boolean mEnableRipple = true;
    private boolean mEnableOverlay = true;

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (v.isPressed()) {
                mCenter.set(event.getRawX(), event.getRawY());
            }
            return false;
        }
    };

    public RippleDrawable(Drawable drawable) {
        mPaintRipple.setAntiAlias(true);
        mPaintOverlay.setAntiAlias(true);
        mPaintOverlay.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        mOriginalDrawable = drawable;
        mMinRadius = DEFAULT_MIN_RADIUS;
        mGeneralAlpha = 1;
        if (mOriginalDrawable != null) {
            setBounds(mOriginalDrawable.getBounds());
        }
        mCenter = new PointF();
        mRippleProgress = 0;
    }

    public float getRippleProgress() {
        return mRippleProgress;
    }

    public void setRippleProgress(float progress) {
        //System.out.println("setRippleProgress(" + progress + ")");
        mRippleProgress = progress;
        mRadius = mRippleProgress * (mMaxRadius - mMinRadius) + mMinRadius;

        //  Call 'invalidateSelf()' will trigger 'View.invalidate()', so this will redrawn later.
        invalidateSelf();
    }

    public float getOverlayAlpha() {
        return mOverlayAlpha;
    }

    public void setOverlayAlpha(float alpha) {
        //System.out.println("setOverlayAlpha(" + alpha + ")");
        mOverlayAlpha = alpha;
        invalidateSelf();
    }

    public RippleDrawable setRippleCenter(float x, float y) {
        mCenter.set(x, y);
        invalidateSelf();
        return this;
    }

    public RippleDrawable setRippleColor(int color) {
        mRippleColor = color;
        invalidateSelf();
        return this;
    }

    public RippleDrawable setOverlayColor(int color) {
        mOverlayColor = color;
        mOverlayAlpha = 1;
        invalidateSelf();
        return this;
    }

    public RippleDrawable setEnableRipple(boolean value) {
        mEnableRipple = value;
        invalidateSelf();
        return this;
    }

    public RippleDrawable setEnableOverlay(boolean value) {
        mEnableOverlay = value;
        invalidateSelf();
        return this;
    }

    public RippleDrawable setGeneralAlpha(float alpha) {
        mGeneralAlpha = alpha;
        invalidateSelf();
        return this;
    }

    public RippleDrawable setOriginalDrawable(Drawable drawable) {
        mOriginalDrawable = drawable;
        mOriginalDrawable.setBounds(getBounds());
        if (mBitmapBackground == null && !getBounds().isEmpty()) {
            createBitmaps();
        }
        return this;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mOriginalDrawable != null && !getBounds().isEmpty()) {
            if (mOriginalDrawable instanceof ColorDrawable) {
                //  For ColorDrawable, there no need to do Mode.SRC_ATOP, so just draw one on another;
                mOriginalDrawable.draw(canvas);
                drawRipple(canvas);
            } else {
                //  Because we need PorterDuff.Mode.SRC_ATOP later, the canvas has to be empty
                //  So, we cannot use given canvas, which contains other stuff already.
                Canvas c = new Canvas(mBitmapBackground);
                c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                //  draw background
                mOriginalDrawable.draw(c);

                //  Render Ripple Layer
                //  Ripple has to be drawn on a Bitmap layer, otherwise later Mode.SRC_ATOP will be wrong
                Canvas cr = new Canvas(mBitmapRipple);
                cr.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                //  render ripple
                drawRipple(cr);

                //  draw ripple layer on background
                c.drawBitmap(mBitmapRipple, 0, 0, mPaintOverlay);

                //  draw everything on given canvas
                canvas.drawBitmap(mBitmapBackground, 0, 0, null);
            }
        } else {
            //  draw ripple directly
            drawRipple(canvas);
        }

    }

    @Override
    public void setAlpha(int alpha) {
        if (mOriginalDrawable != null) {
            mOriginalDrawable.setAlpha(alpha);
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (mOriginalDrawable != null) {
            mOriginalDrawable.setColorFilter(cf);
        }
    }

    @Override
    public int getOpacity() {
        if (mOriginalDrawable != null) {
            return mOriginalDrawable.getOpacity();
        } else {
            return PixelFormat.TRANSPARENT;
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        if (mOriginalDrawable != null) {
            mOriginalDrawable.setBounds(bounds);
            if (!bounds.isEmpty()) {
                createBitmaps();
            }
        }
        calculateMaxRadius(bounds.left, bounds.top, bounds.right, bounds.bottom);
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    public boolean setState(int[] stateSet) {
        boolean ret = false;
        if (mOriginalDrawable != null) {
            ret = mOriginalDrawable.setState(stateSet);
        }

        boolean prev_pressed = contains(getState(), android.R.attr.state_pressed);
        boolean next_pressed = contains(stateSet, android.R.attr.state_pressed);

        if (!prev_pressed && next_pressed) {
            System.err.println("setState(): Pressed");
        } else if (prev_pressed && !next_pressed) {
            System.err.println("setState(): Released");
        }


        return ret || super.setState(stateSet);
    }

    private boolean contains(int[] array, int value) {
        for (int v : array) {
            if (v == value)
                return true;
        }
        return false;
    }

    private String convertStatesToString(int[] stateSet) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int state : stateSet) {
            switch (state) {
                case android.R.attr.state_pressed:
                    sb.append(" pressed, ");
                    break;
                case android.R.attr.state_activated:
                    sb.append(" activated, ");
                    break;
                case android.R.attr.state_focused:
                    sb.append(" focused, ");
                    break;
                case android.R.attr.state_enabled:
                    sb.append(" enabled, ");
                    break;
                case android.R.attr.state_selected:
                    sb.append(" selected, ");
                    break;
                case android.R.attr.state_hovered:
                    sb.append(" hovered, ");
                    break;
                case android.R.attr.state_accelerated:
                    sb.append(" accelerated, ");
                    break;
                case android.R.attr.state_window_focused:
                    sb.append(" window_focused, ");
                    break;

                default:
                    sb.append(" " + state + ", ");
                    break;
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private void calculateMaxRadius(int left, int top, int right, int bottom) {
        mMaxRadius = MAX_RADIUS_FACTOR * Math.max(right - left, bottom - top);
    }

    private void createBitmaps() {
        Rect bound = getBounds();
        mBitmapRipple = Bitmap.createBitmap(bound.width(), bound.height(), Bitmap.Config.ARGB_8888);
        mBitmapBackground = Bitmap.createBitmap(bound.width(), bound.height(), Bitmap.Config.ARGB_8888);
    }

    private void drawRipple(Canvas canvas) {
        //  draw overlay layer
        if (mEnableOverlay) {
            //  calculate the correct overlay color with current alpha
            int c = Color.argb(
                    (int) (Color.alpha(mOverlayColor) * mOverlayAlpha * mGeneralAlpha),
                    Color.red(mOverlayColor),
                    Color.green(mOverlayColor),
                    Color.blue(mOverlayColor)
            );
            canvas.drawColor(c);
        }
        //  draw ripple layer
        if (mEnableRipple) {
            mPaintRipple.setColor(mRippleColor);
            mPaintRipple.setAlpha((int) (Color.alpha(mRippleColor) * mGeneralAlpha));
            canvas.drawCircle(mCenter.x, mCenter.y, mRadius, mPaintRipple);
        }
    }
}
