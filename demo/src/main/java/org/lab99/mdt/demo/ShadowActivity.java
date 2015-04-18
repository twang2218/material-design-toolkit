package org.lab99.mdt.demo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import org.lab99.mdt.drawable.PaperDrawable;
import org.lab99.mdt.view.Paper;


public class ShadowActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shadow);

        PaperDrawable background;

        View bg1 = findViewById(R.id.paper1);
        background = Paper.apply(bg1);
        background.setDepth(2);
        background.setRippleOnTouchEnabled(true);

        View bg2 = findViewById(R.id.paper2);
        background = Paper.apply(bg2);
        background.setDepth(2);
        background.setRippleOnTouchEnabled(false);

        View bg3 = findViewById(R.id.paper3);
        background = Paper.apply(bg3);
        background.setDepth(2);
        background.setRippleOnTouchEnabled(true);
    }
}
