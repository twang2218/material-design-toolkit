package org.lab99.mdt.demo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import org.lab99.mdt.utils.ViewCompat;
import org.lab99.mdt.widget.EffectDrawable;
import org.lab99.mdt.widget.RippleAnimation;
import org.lab99.mdt.widget.RippleView;


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
        EffectDrawable background = new EffectDrawable(text.getBackground());
        background.getRipple().setOverlayColor(getResources().getColor(org.lab99.mdt.R.color.mdt_button_hover_dark));
        background.getRipple().setRippleColor(getResources().getColor(org.lab99.mdt.R.color.mdt_button_pressed_dark));
        ViewCompat.setViewBackground(text, background);
        text.setOnTouchListener(background.getTouchTracker());

        View textTransparent = findViewById(R.id.text_transparent);
        new RippleView(textTransparent);

        View button = findViewById(R.id.button1);
//        new RippleView(button);
        EffectDrawable buttonBackground = new EffectDrawable(button.getBackground());
        ViewCompat.setViewBackground(button, buttonBackground);

        View buttonInside = findViewById(R.id.button_inside);
        View arena = findViewById(R.id.arena);
        RippleAnimation animator = new RippleAnimation(arena)
                .setRippleColor(getResources().getColor(R.color.mdt_red_500))
                .setEnableFocusedAnimation(true);
        animator.setTouchView(buttonInside);
    }

}
