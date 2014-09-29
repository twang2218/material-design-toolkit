package org.lab99.mdt.widget;


import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;

public class RippleView extends View {
    private View mView;
    private RippleAnimation mAnimation;

    public RippleView(View view) {
        super(view.getContext());
        mView = view;
        mAnimation = new RippleAnimation(mView)
                .setTouchView(mView);
    }

    public RippleAnimation getRippleAnimation() {
        return mAnimation;
    }

    @Override
    public void setOnClickListener(final OnClickListener l) {
        OnClickListener delayed_listener = new OnClickListener() {
            @Override
            public void onClick(final View v) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        l.onClick(v);
                    }
                }, mAnimation.getDuration());
            }
        };

        mView.setOnClickListener(delayed_listener);
    }

//    delegate

    @Override
    public void setBackground(Drawable background) {
        mAnimation.setDrawable(background);
    }
}
