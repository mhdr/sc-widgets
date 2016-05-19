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
        notchs.getPainter().setStrokeCap(Paint.Cap.ROUND);
        notchs.setOnDrawListener(new ScNotchs.OnDrawListener() {
            @Override
            public void onDrawNotch(ScNotchs.NotchInfo info) {
                // Calculate a custom angle
                info.angle = info.angle * 2 - 180;
                // Distance and length
                info.distanceFromBorder = info.index * 4;
                info.length = notchs.getNotchs() - info.index;
            }
        });

    }
}
