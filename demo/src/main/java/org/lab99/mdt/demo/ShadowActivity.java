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

        View button1 = findViewById(R.id.bg1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EffectDrawable background = EffectDrawable.apply(v);
                background.getShadowSelf().setDepth(2);
            }
        });
    }
}
