package org.lab99.mdt.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.lab99.mdt.demo.R.layout.activity_main);

        //  link
        link(R.id.button_color, ColorActivity.class);
        link(R.id.button_ripple, RippleActivity.class);
        link(R.id.button_shadow, ShadowActivity.class);
    }

    private void link(int rid, final Class<?> activity) {
        findViewById(rid).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, activity));
            }
        });
    }
}
