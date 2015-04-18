package org.lab99.mdt.demo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import org.lab99.mdt.drawable.PaperDrawable;


public class RippleActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ripple);

        addRipple();
    }

    private void addRipple() {
        View text = findViewById(R.id.text);
        PaperDrawable.apply(text);

        View textTransparent = findViewById(R.id.text_transparent);
        PaperDrawable text_background = PaperDrawable.apply(textTransparent);
        text_background.setDepth(2);

        View button = findViewById(R.id.button1);
        PaperDrawable.apply(button);

        View buttonInside = findViewById(R.id.button_inside);
        View arena = findViewById(R.id.arena);

        PaperDrawable arena_background = PaperDrawable.apply(buttonInside, arena);
        arena_background.setRippleColor(getResources().getColor(R.color.material_red_500));
    }

}
