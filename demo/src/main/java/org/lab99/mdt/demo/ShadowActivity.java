package org.lab99.mdt.demo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import org.lab99.mdt.drawable.PaperDrawable;


public class ShadowActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shadow);

        PaperDrawable background;
        View bg1 = findViewById(R.id.bg1);
        background = PaperDrawable.apply(bg1);
        background.setDepth(2);
        background.setRippleOnTouchEnabled(false);

        View bg2 = findViewById(R.id.bg2);
        background = PaperDrawable.apply(bg2);
        background.setDepth(2);
        background.setRippleOnTouchEnabled(false);

        View bg3 = findViewById(R.id.bg3);
        background = PaperDrawable.apply(bg3);
        background.setDepth(2);
        background.setRippleOnTouchEnabled(false);
    }
}
