package org.lab99.mdt.widget;

import android.content.res.Resources;
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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

import org.lab99.mdt.R;

import java.lang.ref.WeakReference;

/**
 * Ripple Drawable to simulate Material Design Ripple effect
 */
public class RippleDrawable extends Drawable {
    private final static int DEFAULT_MIN_RADIUS = 30;
    private final static float MAX_RADIUS_FACTOR = 1f;
    private PointF mCenter = new PointF();
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
    private IDrawer mMaskDrawer;
    private TouchTracker mTouchTracker;
    private OnStateChangedListener mOnStateChangedListener;

    public RippleDrawable(Resources res) {
        mPaintRipple.setAntiAlias(true);
        mPaintOverlay.setAntiAlias(true);
        mPaintOverlay.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        if (res != null) {
            setOverlayColor(res.getColor(R.color.mdt_button_hover_dark));
            setRippleColor(res.getColor(R.color.mdt_button_pressed_dark));
        }
        setEnableRipple(false);
        setEnableOverlay(false);

        mOnStateChangedListener = new RippleTransitAnimator(this);

        mMinRadius = DEFAULT_MIN_RADIUS;
        mGeneralAlpha = 1;
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

    public OnStateChangedListener getOnStateChangedListener() {
        return mOnStateChangedListener;
    }

    public void setOnStateChangedListener(OnStateChangedListener listener) {
        mOnStateChangedListener = listener;
    }

    public PointF getRippleCenter() {
        return mCenter;
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

    public IDrawer getMaskDrawer() {
        return mMaskDrawer;
    }

    public void setMaskDrawer(IDrawer drawer) {
        mMaskDrawer = drawer;
    }

    public TouchTracker getTouchTracker() {
        return mTouchTracker;
    }

    public void setTouchTracker(TouchTracker tracker) {
        mTouchTracker = tracker;
    }

    @Override
    public void draw(Canvas canvas) {
        if (!getBounds().isEmpty()) {
            if (getMaskDrawer() != null) {
                //  Because we need PorterDuff.Mode.SRC_ATOP later, the canvas has to be empty
                //  So, we cannot use given canvas, which contains other stuff already.
                Canvas c = new Canvas(mBitmapBackground);
                c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                //  draw background
                getMaskDrawer().draw(c);

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
            } else {
                //  draw ripple directly
                drawRipple(canvas);
            }
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaintOverlay.setColorFilter(cf);
        mPaintRipple.setColorFilter(cf);
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
        calculateMaxRadius(bounds.left, bounds.top, bounds.right, bounds.bottom);
        setRippleCenter(bounds.width() / 2, bounds.height() / 2);
    }

    @Override
    public void setAlpha(int alpha) {
        setGeneralAlpha(alpha / 255f);
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    @Override
    public boolean setState(int[] stateSet) {
        boolean prev_pressed = contains(getState(), android.R.attr.state_pressed);
        boolean next_pressed = contains(stateSet, android.R.attr.state_pressed);
        boolean prev_focused = contains(getState(), android.R.attr.state_focused);
        boolean next_focused = contains(stateSet, android.R.attr.state_focused);

        if (getOnStateChangedListener() != null) {
            if (prev_pressed != next_pressed) {
                //  pressed status changed
                if (next_pressed) {
                    System.err.println("setState(): Pressed");
                    getOnStateChangedListener().onPressed();
                } else {
                    System.err.println("setState(): Released");
                    getOnStateChangedListener().onReleased();
                }
            }
            if (prev_focused != next_focused) {
                //  focused status changed
                if (next_focused) {
                    System.err.println("setState(): Focused");
                    getOnStateChangedListener().onFocused();
                } else {
                    System.err.println("setState(): Unfocused");
                    getOnStateChangedListener().onReleased();
                }
            }
        }
        return super.setState(stateSet);
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

    static class RippleTransitAnimator implements OnStateChangedListener {
        private final static long DEFAULT_DURATION = 300;
        private final static float FOCUS_RANGE_LARGE = 0.30f;
        private final static float FOCUS_RANGE_SMALL = 0.25f;

        private WeakReference<RippleDrawable> mRipple;
        private ObjectAnimator mRippleAnimator;
        private ObjectAnimator mOverlayAnimator;
        private ObjectAnimator mAlphaAnimator;

        private boolean mEnableReleaseAnimation = true;
        private boolean mEnableFocusedAnimation = true;
        private boolean mEnablePressedAnimation = true;

        private boolean mEnableRippleEffect = true;
        private boolean mEnableOverlayEffect = true;

        private long mDuration;

        RippleTransitAnimator(RippleDrawable rippleDrawable) {
            mRipple = new WeakReference<RippleDrawable>(rippleDrawable);
            mRippleAnimator = ObjectAnimator.ofFloat(getRipple(), "rippleProgress", 1);
            mOverlayAnimator = ObjectAnimator.ofFloat(getRipple(), "overlayAlpha", 1, 0);
            mAlphaAnimator = ObjectAnimator.ofFloat(getRipple(), "generalAlpha", 1, 0);
            mDuration = DEFAULT_DURATION;
        }

        private RippleDrawable getRipple() {
            return mRipple.get();
        }

        public void setDuration(long duration) {
            mDuration = duration;
        }

        public void setEnableReleaseAnimation(boolean value) {
            mEnableReleaseAnimation = value;
        }

        public void setEnableFocusedAnimation(boolean value) {
            mEnableFocusedAnimation = value;
        }

        public void setEnablePressedAnimation(boolean value) {
            mEnablePressedAnimation = value;
        }


        public void setEnableRippleEffect(boolean enable) {
            mEnableRippleEffect = enable;
        }

        public void setEnableOverlayEffect(boolean enable) {
            mEnableOverlayEffect = enable;
        }

        public boolean isRunning() {
            return mRippleAnimator.isRunning() || mOverlayAnimator.isRunning() || mAlphaAnimator.isRunning();
        }

        public void cancel() {
            if (mRippleAnimator.isRunning())
                mRippleAnimator.cancel();

            if (mOverlayAnimator.isRunning())
                mOverlayAnimator.cancel();

            if (mAlphaAnimator.isRunning())
                mAlphaAnimator.cancel();
        }

        @Override
        public void onPressed() {
            if (mEnablePressedAnimation) {
                float current_ripple = 0;

                if (isRunning()) {
                    cancel();
                    current_ripple = (Float) mRippleAnimator.getAnimatedValue();
                }

                RippleDrawable ripple = getRipple();
                TouchTracker tracker = ripple.getTouchTracker();
                float x, y;
                if (tracker != null) {
                    x = tracker.getLastTouch().x;
                    y = tracker.getLastTouch().y;
                } else {
                    x = ripple.getRippleCenter().x;
                    y = ripple.getRippleCenter().y;
                }
                ripple.setRippleCenter(x, y);
                ripple.setGeneralAlpha(1);
                ripple.setEnableRipple(mEnableRippleEffect);
                ripple.setEnableOverlay(mEnableOverlayEffect);
                ripple.setOverlayAlpha(1);

                //  Release Animation
                if (mEnableRippleEffect) {
                    if (current_ripple < FOCUS_RANGE_LARGE) {
                        mRippleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                    } else {
                        //  let the larger ripple go, and start another one
                        current_ripple = 0;
                        mRippleAnimator.setInterpolator(new DecelerateInterpolator());
                    }
                    mRippleAnimator.setDuration(mDuration);
                    mRippleAnimator.setRepeatCount(0);
                    mRippleAnimator.setFloatValues(current_ripple, 1);
                    mRippleAnimator.start();
                    if (mOverlayAnimator.isRunning()) {
                        mOverlayAnimator.cancel();
                    }
                }
            }

        }

        @Override
        public void onReleased() {
            if (mEnableReleaseAnimation) {
                mAlphaAnimator.setDuration(mDuration);
                mAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        RippleDrawable ripple = getRipple();

                        ripple.setGeneralAlpha(1);
                        ripple.setEnableRipple(false);
                        ripple.setEnableOverlay(false);
                    }
                });
                mAlphaAnimator.start();
                if (mOverlayAnimator.isRunning()) {
                    mOverlayAnimator.clone();
                }
                mOverlayAnimator.setDuration(mDuration);
                mOverlayAnimator.setFloatValues(0);
                mOverlayAnimator.start();
            }
        }

        @Override
        public void onFocused() {
            if (mEnableFocusedAnimation) {
                if (isRunning()) {
                    cancel();
                }

                RippleDrawable ripple = getRipple();
                ripple.setRippleCenter(ripple.getBounds().width() / 2, ripple.getBounds().height() / 2);
                ripple.setGeneralAlpha(1);
                ripple.setEnableRipple(mEnableRippleEffect);
                ripple.setEnableOverlay(mEnableOverlayEffect);
                ripple.setOverlayAlpha(1);

                //  Transit Animation
                //  Ripple
                if (mEnableRippleEffect) {
                    final float begin, end;
                    begin = FOCUS_RANGE_LARGE;
                    end = FOCUS_RANGE_SMALL;
                    mRippleAnimator.setDuration(mDuration);
                    mRippleAnimator.setFloatValues(0, begin);
                    mRippleAnimator.setInterpolator(new DecelerateInterpolator());
                    mRippleAnimator.setRepeatCount(0);
                    mRippleAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            //  Holding Animation
                            mRippleAnimator.setDuration(mDuration);
                            mRippleAnimator.setFloatValues(begin, end);
                            mRippleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                            mRippleAnimator.setRepeatCount(ValueAnimator.INFINITE);
                            mRippleAnimator.setRepeatMode(ValueAnimator.REVERSE);
                            mRippleAnimator.start();
                            //  remove this listener
                            mRippleAnimator.removeListener(this);
                        }
                    });
                    mRippleAnimator.start();
                }
            }

        }
    }
}
