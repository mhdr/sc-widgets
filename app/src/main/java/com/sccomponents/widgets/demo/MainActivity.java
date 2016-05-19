package com.sccomponents.widgets.demo;

import android.animation.ArgbEvaluator;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sccomponents.widgets.ScNotchs;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ScNotchs notchs = (ScNotchs) this.findViewById(R.id.notchs);
        assert notchs != null;
        notchs.setOnDrawListener(new ScNotchs.OnDrawListener() {
            @Override
            public void onDrawNotch(ScNotchs.NotchInfo info) {
                // Adjust the distance from border
                float multiplier = (notchs.getWidth() / 2) / notchs.getNotchs();
                info.distanceFromBorder = (notchs.getNotchs() - info.index) * multiplier;

                // Adjust the dimension
                info.length = 1 + info.index / 5;
            }
        });

    }
}
