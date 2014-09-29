package org.lab99.mdt.demo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

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
        new RippleView(text);

        View textTransparent = findViewById(R.id.text_transparent);
        new RippleView(textTransparent);

        View button = findViewById(R.id.button);
        new RippleView(button);

        View buttonInside = findViewById(R.id.button_inside);
        View arena = findViewById(R.id.arena);
        RippleAnimation animator = new RippleAnimation(arena)
                .setRippleColor(getResources().getColor(R.color.mdt_red_500))
                .setEnableFocusedAnimation(true);
        animator.setTouchView(buttonInside);
    }

}
