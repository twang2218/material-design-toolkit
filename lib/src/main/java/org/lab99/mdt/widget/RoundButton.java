package org.lab99.mdt.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.nineoldandroids.animation.ObjectAnimator;

import org.lab99.mdt.R;

public class RoundButton extends View implements Shadow.BackgroundDrawer {
    //  Lift on touch
    //  http://www.google.com/design/spec/animation/responsive-interaction.html#responsive-interaction-ink-reactions
    private final static float TOUCH_LIFT_DEPTH = 2;
    private final static int[] DEFAULT_PADDING_ID = {
            R.dimen.md_shadow_padding_depth_1,
            R.dimen.md_shadow_padding_depth_2,
            R.dimen.md_shadow_padding_depth_3,
            R.dimen.md_shadow_padding_depth_4,
            R.dimen.md_shadow_padding_depth_5
    };

    //  properties
    protected Drawable mIcon;
    protected int mColor = Color.WHITE;
    protected boolean mShowShadow = true;
    protected float mDepth = 0;
    protected float mWidgetWidth;
    protected float mWidgetHeight;
    //  variables
    private Shadow mShadow;
    private Paint mPlatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float mCenterX;
    private float mCenterY;
    private float mRadius;
    private float mIconSize;
    private ViewCompat mViewCompat = new ViewCompat();
    //      animation
    private boolean mPressed = false;
    private ObjectAnimator mShadowAnimator;


    public RoundButton(Context context) {
        super(context);
        init(context, null);
    }

    public RoundButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoundButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    //  initializers
    protected void init(Context context, AttributeSet attrs) {
        initViews(context);
        initAttributes(context, attrs);
    }

    protected void initViews(Context context) {
    }

    protected void initAttributes(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundButton);
            if (a == null) {
                return;
            }

            try {
                Resources res = context.getResources();
                setIcon(a.getDrawable(R.styleable.RoundButton_android_icon));
                setShowShadow(a.getBoolean(R.styleable.RoundButton_shadow, mShowShadow));
                setColor(a.getColor(R.styleable.RoundButton_backgroundColor, mColor));
                setIconSize(a.getDimension(R.styleable.RoundButton_iconSize, res.getDimension(R.dimen.md_icon_size)));
                float default_fab_size = res.getDimension(R.dimen.md_fab_large_size);
                setWidgetWidth(a.getDimension(R.styleable.RoundButton_widgetWidth, default_fab_size));
                setWidgetHeight(a.getDimension(R.styleable.RoundButton_widgetHeight, default_fab_size));
                setDepth(a.getFloat(R.styleable.RoundButton_depth, 0));

                if (!a.hasValue(R.styleable.RoundButton_android_padding)) {
                    //  set default padding based on depth
                    int depth = (int) getDepth();
                    //  touch will lift, so need more space for shadow
                    if (isClickable()) {
                        depth += TOUCH_LIFT_DEPTH;
                    }
                    int padding;
                    if (depth >= 1 && depth <= 5) {
                        padding = res.getDimensionPixelOffset(DEFAULT_PADDING_ID[depth - 1]);
                    } else if (depth < 1) {
                        padding = 0;
                    } else {
                        padding = res.getDimensionPixelOffset(DEFAULT_PADDING_ID[4]);
                    }
                    setPadding(padding, padding, padding, padding);
                }
            } finally {
                a.recycle();
            }
        }

    }

    //  override
    @Override
    protected void onDraw(Canvas canvas) {
        //  draw shadow
        if (!isInEditMode()) {
            //  cannot use RenderScript in EditMode.
            mShadow.draw(canvas);
        }

        //  draw background
        drawBackground(canvas);

        //  draw icon
        drawIcon(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            mShadow = new Shadow(this, this);
            mShadow.setRotation(mViewCompat.getRotation(this));
            mShadow.setDepth(mDepth);
            mShadowAnimator = ObjectAnimator.ofFloat(mShadow, "depth", getDepth() + 1);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        mShadow.destroy();
        mShadow = null;
        super.onDetachedFromWindow();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public synchronized void setRotation(float rotation) {
        super.setRotation(rotation);
        if (mShadow != null) {
            mShadow.setRotation(rotation);
        }
        postInvalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float cw = getWidth() - getPaddingLeft() - getPaddingRight();
        float ch = getHeight() - getPaddingTop() - getPaddingBottom();

        mCenterX = (cw / 2) + getPaddingLeft();
        mCenterY = (ch / 2) + getPaddingTop();
        mRadius = Math.min(getWidgetWidth(), getWidgetHeight()) / 2f;

        if (mShowShadow && mShadow != null) {
            mShadow.setSize(w, h);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (isClickable()) {
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {

                //  firing animation
                boolean is_pressed_now = (action == MotionEvent.ACTION_DOWN);
                if (mPressed != is_pressed_now) {
                    //  pressed state changed
                    if (is_pressed_now) {
                        //  just pressed
                        animatePressed();
                    } else {
                        //  just release
                        animateRelease();
                    }
                    mPressed = is_pressed_now;
                }
            } else if (action == MotionEvent.ACTION_CANCEL) {
                animateRelease();
            }
        }

        return super.onTouchEvent(event);
    }

    //  http://stackoverflow.com/a/12267248/3554436
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = (int) getWidgetWidth() + getPaddingLeft() + getPaddingRight();
        int desiredHeight = (int) getWidgetHeight() + getPaddingTop() + getPaddingBottom();

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }

        //MUST CALL THIS
        setMeasuredDimension(width, height);
    }

    @Override
    public void drawBackground(Canvas canvas) {
        canvas.drawCircle(mCenterX, mCenterY, mRadius, mPlatePaint);
//        RectF bound = new RectF(mCenterX - mRadius, mCenterY - mRadius, mCenterX + mRadius, mCenterY + mRadius);
//        float round_radius = Utils.getPixelFromDip(getContext(), 2);
//        canvas.drawRoundRect(bound, round_radius, round_radius, mPlatePaint);
    }

    //  getters / setters
    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable drawable) {
        mIcon = drawable;
        postInvalidate();
    }

    public boolean getShowShadow() {
        return mShowShadow;
    }

    public void setShowShadow(boolean value) {
        mShowShadow = value;
        postInvalidate();
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
        mPlatePaint.setColor(mColor);
        postInvalidate();
    }

    public float getIconSize() {
        return mIconSize;
    }

    public void setIconSize(float size) {
        mIconSize = size;
        postInvalidate();
    }

    public float getDepth() {
        return mDepth;
    }

    public void setDepth(float depth) {
        mDepth = depth;
        if (mShadow != null) {
            mShadow.setDepth(mDepth);
        }
        postInvalidate();
    }

    public float getWidgetWidth() {
        return mWidgetWidth;
    }

    public void setWidgetWidth(float width) {
        mWidgetWidth = width;
        postInvalidate();
    }

    public float getWidgetHeight() {
        return mWidgetHeight;
    }

    public void setWidgetHeight(float height) {
        mWidgetHeight = height;
        postInvalidate();
    }

    private void drawIcon(Canvas canvas) {
        if (mIcon != null) {
            float half_size = mIconSize / 2f;
            mIcon.setBounds((int) (mCenterX - half_size), (int) (mCenterY - half_size), (int) (mCenterX + half_size), (int) (mCenterY + half_size));
            mIcon.draw(canvas);
        }
    }

    public void animatePressed() {
        if (mShadowAnimator.isRunning()) {
            mShadowAnimator.cancel();
        }

        mShadowAnimator.setFloatValues(getDepth() + TOUCH_LIFT_DEPTH);
        mShadowAnimator.setDuration(getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));
        mShadowAnimator.start();
    }

    public void animateRelease() {
        if (mShadowAnimator.isRunning()) {
            mShadowAnimator.cancel();
        }

        mShadowAnimator.setFloatValues(getDepth());
        mShadowAnimator.setDuration(getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));
        mShadowAnimator.start();
    }

}
