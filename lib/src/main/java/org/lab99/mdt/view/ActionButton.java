package org.lab99.mdt.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import org.lab99.mdt.R;
import org.lab99.mdt.utils.DrawableCompat;

public class ActionButton extends Button {
    private float mSize;
    private Drawable mIcon;
    private float mIconSize;

    public ActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public ActionButton(Context context) {
        this(context, null, 0);
    }

    public ActionButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    //  initializer

    @Override
    protected void initAttributes(Context context, AttributeSet attrs) {
        super.initAttributes(context, attrs);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionButton);
            if (a == null) {
                return;
            }

            try {
                setSize(a.getDimension(R.styleable.ActionButton_size, getDefaultSize()));
                setIcon(a.getDrawable(R.styleable.ActionButton_iconDrawable));
                setIconSize(a.getDimension(R.styleable.ActionButton_iconSize, getDefaultIconSize()));
            } finally {
                a.recycle();
            }
        }
    }

    //  Overrides

    @Override
    protected Drawable getBackgroundFromColorDrawable(ColorDrawable colorDrawable) {
        //  create background with given color
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(DrawableCompat.getColor(colorDrawable));
        return drawable;
    }

    //  http://stackoverflow.com/a/12267248/3554436
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = (int) getSize() + getPaddingLeft() + getPaddingRight();
        int desiredHeight = (int) getSize() + getPaddingTop() + getPaddingBottom();

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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawIcon(canvas);
    }

    //  Getters / Setters

    public float getSize() {
        return mSize;
    }

    public void setSize(float size) {
        mSize = size;
        postInvalidate();
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
        postInvalidate();
    }

    public float getIconSize() {
        return mIconSize;
    }

    public void setIconSize(float size) {
        mIconSize = size;
        postInvalidate();
    }

    //  Default

    protected float getDefaultSize() {
        return getContext().getResources().getDimension(R.dimen.mdt_fab_large_size);
    }

    protected float getDefaultIconSize() {
        return getContext().getResources().getDimension(R.dimen.mdt_icon_size);
    }

    protected void drawIcon(Canvas canvas) {
        if (mIcon != null) {
            //  calculate bounds
            float half_size = getIconSize() / 2f;

            float cw = getWidth() - getPaddingLeft() - getPaddingRight();
            float ch = getHeight() - getPaddingTop() - getPaddingBottom();

            float cx = (cw / 2) + getPaddingLeft();
            float cy = (ch / 2) + getPaddingTop();

            mIcon.setBounds((int) (cx - half_size), (int) (cy - half_size), (int) (cx + half_size), (int) (cy + half_size));

            mIcon.draw(canvas);
        }
    }


}
