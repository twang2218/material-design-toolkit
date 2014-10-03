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

class ShadowRender {
    //  Scale before blur
    //  shadow_scale(depth) = 2 * depth;
    private final static float SHADOW_SCALE_DEPTH_FACTOR = 2f;
    //  Shadow Depth Threshold
    private final static float SHADOW_DEPTH_THRESHOLD = 0.5f;
    //  Alpha for Top(Key) Shadow
    private final static float SHADOW_ALPHA_TOP = 0.25f;
    //  Alpha for Bottom(Ambient) Shadow
    private final static float SHADOW_ALPHA_BOTTOM = 0.22f;

    RenderScript mRenderScript;
    private Cache mCache;
    private Paint mShadowPaint;

    public ShadowRender(RenderScript renderScript) {
        mRenderScript = renderScript;
        mCache = new Cache();

        mShadowPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    }

    public ShadowRender(ShadowRender o) {
        mRenderScript = o.mRenderScript;
        mCache = o.mCache;
        mShadowPaint = o.mShadowPaint;
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

    public void draw(Canvas canvas, ShadowDrawable.Shadow shadow) {
        if (shadow.mDepth > SHADOW_DEPTH_THRESHOLD && shadow.mMaskDrawer != null) {
//            long begin = System.currentTimeMillis();

            int width = shadow.mBounds.width();
            int height = shadow.mBounds.height();

            float scaleX = getShadowScale(shadow.mDepth, width);
            float scaleY = getShadowScale(shadow.mDepth, height);

            //  get data arena
            Data data = allocateBitmap((int) (width / scaleX), (int) (height / scaleY));

            if (data != null) {
                //      Scaling for better performance
                data.bitmap.eraseColor(Color.TRANSPARENT);
                mShadowPaint.setAlpha(255);
                Canvas cs = new Canvas(data.bitmap);
                cs.scale(1 / scaleX, 1 / scaleY);

                //  draw background
                shadow.mMaskDrawer.draw(cs);

                //  make it black
                cs.drawColor(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

                //  make it blur (RenderScript)
                //  In EditMode, just draw the scaled background to show the idea, since RS is not supported in IDE.
                if (mRenderScript != null) {
                    data.allocation.copyFrom(data.bitmap);
                    data.filter.setInput(data.allocation);
                    data.filter.setRadius(shadow.getShadowBlurRadius() / scaleX);
                    data.filter.forEach(data.allocation);
                    data.allocation.copyTo(data.bitmap);
                }

                //      calculate offset, handling rotation;
                double rotation_alpha = Math.toRadians(shadow.mRotation);
                float offset = shadow.getShadowOffset();
                float x = (float) Math.sin(rotation_alpha) * offset;
                float y = (float) Math.cos(rotation_alpha) * offset;

                //  draw shadow on canvas
                canvas.save();
                canvas.scale(scaleX, scaleY);
                //      draw Bottom Shadow
                mShadowPaint.setAlpha((int) (SHADOW_ALPHA_BOTTOM * shadow.mAlpha));
                canvas.drawBitmap(data.bitmap, 0, 0, mShadowPaint);
                //      draw Top Shadow
                mShadowPaint.setAlpha((int) (SHADOW_ALPHA_TOP * shadow.mAlpha));
                canvas.drawBitmap(data.bitmap, x / scaleX, y / scaleY, mShadowPaint);

                canvas.restore();
            }

//            System.out.println("drawShadow(): " + (System.currentTimeMillis() - begin) + " ms (depth:" + mDepth + ")");
        }
    }

    //  Getters & Setters

    /**
     * The shadow bitmap should be aligned to 8, otherwise, some warning/error will raised
     * See also: http://stackoverflow.com/a/25064929/3554436
     *
     * @param origin_scale the original scale, will be used to calculate the aligned scale.
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

    private float getShadowScale(float depth, int length) {
        float scale = depth * SHADOW_SCALE_DEPTH_FACTOR;
        return alignScale(scale, length);
    }

    public void setRenderScript(RenderScript renderScript) {
        mRenderScript = renderScript;
    }

    //  private
    private Data allocateBitmap(int scaled_width, int scaled_height) {
        //  calculate scaled width/height
        if (scaled_width > 0 && scaled_height > 0) {
            Point size = new Point(scaled_width, scaled_height);
            //      check cache
            Data data = mCache.get(size);
            if (data != null) {
                //  found cached data, use it.
                return data;
            } else {
                //  create the new
                data = new Data();
                data.bitmap = Bitmap.createBitmap(scaled_width, scaled_height, Bitmap.Config.ARGB_8888);
                if (mRenderScript != null) {
                    data.allocation = Allocation.createFromBitmap(mRenderScript, data.bitmap);
                    data.filter = ScriptIntrinsicBlur.create(mRenderScript, data.allocation.getElement());
                }
                //  save to cache
                mCache.put(size, data);
                return data;
            }
        }

        return null;
    }

    /**
     * Internal structure, which is used in Cache
     */
    private static class Data {
        public Bitmap bitmap;
        public Allocation allocation;
        public ScriptIntrinsicBlur filter;
    }

    /**
     * LruCache for Render
     */
    private static class Cache extends LruCache<Point, Data> {
        //  Cache Size
        private final static int DEFAULT_CACHE_SIZE = 5;

        public Cache() {
            this(DEFAULT_CACHE_SIZE);
        }

        public Cache(int maxSize) {
            super(maxSize);
        }

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
