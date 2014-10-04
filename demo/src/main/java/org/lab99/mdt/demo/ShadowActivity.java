package org.lab99.mdt.demo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import org.lab99.mdt.widget.EffectDrawable;


public class ShadowActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shadow);

        View bg1 = findViewById(R.id.bg1);
        EffectDrawable background = EffectDrawable.apply(bg1);
        if (background.getShadowSelf().getDepth() == 0) {
            background.getShadowSelf().setDepth(1);
        }
    }
}
