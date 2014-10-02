package org.lab99.mdt.widget;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.support.v4.util.LruCache;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.View;

import org.lab99.mdt.utils.Utils;

public class Shadow {
    //  Offset function calculate from offset set {1.5, 3, 10, 14, 19}
    //  Material Design Guideline > Layout > Dimensionality > Shadows
    //  offset(depth) = 0.57 * depth^2 + 0.91 * depth;
    private final static float SHADOW_OFFSET_A = 0.57f;
    private final static float SHADOW_OFFSET_B = 0.91f;
    //  blur_radius(offset) = 1.5 * offset;
    private final static float SHADOW_BLUR_RADIUS_OFFSET_FACTOR = 1.5f;
    //  Alpha for Top(Key) Shadow
    private final static int SHADOW_ALPHA_TOP = (int) (0.25 * 255);
    //  Alpha for Bottom(Ambient) Shadow
    private final static int SHADOW_ALPHA_BOTTOM = (int) (0.22 * 255);
    //  Scale before blur
    //  shadow_scale(depth) = 2 * depth;
    private final static float SHADOW_SCALE_DEPTH_FACTOR = 2f;
    private float mShadowScaleX = SHADOW_SCALE_DEPTH_FACTOR;
    private float mShadowScaleY = SHADOW_SCALE_DEPTH_FACTOR;
    //  Shadow Depth Threshold
    private final static float SHADOW_DEPTH_THRESHOLD = 0.5f;
    //  Max Depth
    private final static float DEPTH_MAX = 5;
    //  properties
    protected float mDepth = 0;
    protected int mWidth;
    protected int mHeight;
    protected float mRotation = 0;
    protected BackgroundDrawer mBackgroundDrawer = null;
    //  variables
    private View mView;
    private Paint mShadowPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private float mShadowOffset;
    private float mShadowBlurRadius;
    private Bitmap mBitmapShadow;
    private Allocation mAllocationShadow;
    private RenderScript mRenderScript;
    private ScriptIntrinsicBlur mBlurFilter;
    private Cache mCache = new Cache();

    public Shadow(View view, BackgroundDrawer drawer) {
        mView = view;
        mBackgroundDrawer = drawer;
        try {
            mRenderScript = RenderScript.create(mView.getContext());
        } catch (Error ex) {
            ex.printStackTrace();
        }
    }

    public void destroy() {
        if (mRenderScript != null) {
            mRenderScript.destroy();
            mRenderScript = null;
        }
        if (mCache != null) {
            mCache.evictAll();
        }
    }

    public float getDepth() {
        return mDepth;
    }

    public void setDepth(float depth) {
        if (depth < 0) {
            mDepth = 0;
        } else {
            mDepth = depth;
        }

        if (mDepth >= SHADOW_DEPTH_THRESHOLD && mDepth <= DEPTH_MAX) {
            //  offset = (a * depth * depth) + (b * depth)
            float offset = SHADOW_OFFSET_A * (mDepth * mDepth) + SHADOW_OFFSET_B * mDepth;
            mShadowOffset = Utils.getPixelFromDip(mView.getContext(), offset);
            mShadowBlurRadius = offset * SHADOW_BLUR_RADIUS_OFFSET_FACTOR;
            float new_scale = (int) (mDepth * SHADOW_SCALE_DEPTH_FACTOR);
            setShadowScaleX(new_scale);
            setShadowScaleY(new_scale);
            //  try allocate bitmap
            allocateBitmap();
        }

        mView.postInvalidate();
    }

    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;

        //  realign scale
        setShadowScaleX(mShadowScaleX);
        setShadowScaleY(mShadowScaleY);
        //  try allocate bitmap
        allocateBitmap();
    }

    public float getRotation() {
        return mRotation;
    }

    public void setRotation(float rotation) {
        mRotation = rotation;
    }

    public BackgroundDrawer getBackgroundDrawer() {
        return mBackgroundDrawer;
    }

    public void setBackgroundDrawer(BackgroundDrawer drawer) {
        mBackgroundDrawer = drawer;
    }

    public void draw(Canvas canvas) {
        if (mDepth > SHADOW_DEPTH_THRESHOLD && mBitmapShadow != null && mBackgroundDrawer != null) {
//            long begin = System.currentTimeMillis();

            //      Scaling for better performance
            mBitmapShadow.eraseColor(Color.TRANSPARENT);
            mShadowPaint.setAlpha(255);
            Canvas cs = new Canvas(mBitmapShadow);
            cs.scale(1 / mShadowScaleX, 1 / mShadowScaleY);

            //  draw background
            mBackgroundDrawer.drawBackground(cs);

            //  make it black
            cs.drawColor(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

            //  make it blur (RenderScript)
            //  In EditMode, just draw the scaled background to show the idea, since RS is not supported in IDE.
            if (mRenderScript != null) {
                mAllocationShadow.copyFrom(mBitmapShadow);
                mBlurFilter.setInput(mAllocationShadow);
                mBlurFilter.setRadius(mShadowBlurRadius / mShadowScaleX);
                mBlurFilter.forEach(mAllocationShadow);
                mAllocationShadow.copyTo(mBitmapShadow);
            }
            //      calculate offset, handling rotation;
            double rotation_alpha = Math.toRadians(getRotation());
            float x = (float) Math.sin(rotation_alpha) * mShadowOffset;
            float y = (float) Math.cos(rotation_alpha) * mShadowOffset;

            //  draw shadow on canvas
            canvas.save();
            canvas.scale(mShadowScaleX, mShadowScaleY);
            //      draw Bottom Shadow
            mShadowPaint.setAlpha(SHADOW_ALPHA_BOTTOM);
            canvas.drawBitmap(mBitmapShadow, 0, 0, mShadowPaint);
            //      draw Top Shadow
            mShadowPaint.setAlpha(SHADOW_ALPHA_TOP);
            canvas.drawBitmap(mBitmapShadow, x / mShadowScaleX, y / mShadowScaleY, mShadowPaint);

            canvas.restore();

//            System.out.println("drawShadow(): " + (System.currentTimeMillis() - begin) + " ms (depth:" + mDepth + ")");
        }
    }

    /**
     * The shadow bitmap should be aligned to 8, otherwise, some warning/error will raised
     * See also: http://stackoverflow.com/a/25064929/3554436
     *
     * @param origin_scale
     * @param length       the width or height we need to align the scale
     * @return aligned scale
     */
    private float alignScale(float origin_scale, int length) {
        if (length > 0) {
            //  the scale should near 2^exp
            int exp = (int) (Math.log(origin_scale) / Math.log(2));
            if (exp < 1) {
                exp = 1;
            }
            //  scale = 2^exp
            float scale = 2 << (exp - 1);

            //  align scale
            int scaled_length = (int) (length / scale);
            scaled_length -= scaled_length % 8;
            scale = length / (float) scaled_length;

            return scale;
        } else {
            return origin_scale;
        }
    }

    private void setShadowScaleX(float scale) {
        mShadowScaleX = alignScale(scale, mWidth);
    }

    private void setShadowScaleY(float scale) {
        mShadowScaleY = alignScale(scale, mHeight);
    }

    private void allocateBitmap() {
        //  calculate scaled width/height
        int scaled_width = (int) (mWidth / mShadowScaleX);
        int scaled_height = (int) (mHeight / mShadowScaleY);

        if (scaled_width > 0 && scaled_height > 0) {
            //  check whether the dimension changed
            if (mBitmapShadow == null || scaled_width != mBitmapShadow.getWidth() || scaled_height != mBitmapShadow.getHeight()) {
                Point size = new Point(scaled_width, scaled_height);
                //      check cache
                Data data = mCache.get(size);
                if (data != null) {
                    //  found cached data, use it.
                    mBitmapShadow = data.bitmap;
                    mAllocationShadow = data.allocation;
                    mBlurFilter = data.filter;
                } else {
                    //  create the new
                    mBitmapShadow = Bitmap.createBitmap(scaled_width, scaled_height, Bitmap.Config.ARGB_8888);
                    if (mRenderScript != null) {
                        mAllocationShadow = Allocation.createFromBitmap(mRenderScript, mBitmapShadow);
                        mBlurFilter = ScriptIntrinsicBlur.create(mRenderScript, mAllocationShadow.getElement());
                    }
                    //  save to cache
                    mCache.put(size, new Data(mBitmapShadow, mAllocationShadow, mBlurFilter));
                }
            }
        }
    }

    public interface BackgroundDrawer {
        public void drawBackground(Canvas canvas);
    }

    /**
     * Internal structure, which is used in Cache
     */
    private static class Data {
        public Bitmap bitmap;
        public Allocation allocation;
        public ScriptIntrinsicBlur filter;

        public Data(Bitmap bitmap, Allocation allocation, ScriptIntrinsicBlur filter) {
            this.bitmap = bitmap;
            this.allocation = allocation;
            this.filter = filter;
        }
    }

    private class Cache extends LruCache<Point, Data> {
        //  Cache Size
        private final static int DEFAULT_CACHE_SIZE = 5;

        public Cache() {
            this(DEFAULT_CACHE_SIZE);
        }

        public Cache(int maxSize) {
            super(maxSize);
        }

        /**
         * Release the resources.
         *
         * @param evicted
         * @param key
         * @param oldValue
         * @param newValue
         */
        @Override
        protected void entryRemoved(boolean evicted, Point key, Data oldValue, Data newValue) {
            super.entryRemoved(evicted, key, oldValue, newValue);

            if (oldValue != null) {
                if (oldValue.bitmap != null)
                    oldValue.bitmap.recycle();
                if (oldValue.allocation != null)
                    oldValue.allocation.destroy();
                if (oldValue.filter != null)
                    oldValue.filter.destroy();
            }
        }
    }
}
