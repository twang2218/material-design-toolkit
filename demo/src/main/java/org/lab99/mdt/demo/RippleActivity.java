package org.lab99.mdt.demo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import org.lab99.mdt.widget.EffectDrawable;


public class RippleActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ripple);

        addRipple();
    }

    private void addRipple() {
        View text = findViewById(R.id.text);
        //new RippleView(text);
        EffectDrawable.apply(text);
//        EffectDrawable background = new EffectDrawable(text.getBackground());
//        ViewCompat.setBackground(text, background);
//        text.setOnTouchListener(background.getTouchTracker());

        View textTransparent = findViewById(R.id.text_transparent);
        EffectDrawable.apply(textTransparent);

        View button = findViewById(R.id.button1);
//        new RippleView(button);
        EffectDrawable.apply(button);

        View buttonInside = findViewById(R.id.button_inside);
        View arena = findViewById(R.id.arena);

        EffectDrawable arena_background = EffectDrawable.apply(buttonInside, arena);
        arena_background.getRipple().setRippleColor(getResources().getColor(R.color.mdt_red_500));

//        RippleAnimation animator = new RippleAnimation(arena)
//                .setRippleColor(getResources().getColor(R.color.mdt_red_500))
//                .setEnableFocusedAnimation(true);
//        animator.setTouchView(buttonInside);
    }

}
