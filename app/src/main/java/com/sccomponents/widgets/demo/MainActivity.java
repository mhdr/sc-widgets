package com.sccomponents.widgets.demo;

import android.animation.Animator;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.sccomponents.widgets.ScArcGauge;
import com.sccomponents.widgets.ScCopier;
import com.sccomponents.widgets.ScFeature;
import com.sccomponents.widgets.ScGauge;
import com.sccomponents.widgets.ScNotches;
import com.sccomponents.widgets.ScPointer;
import com.sccomponents.widgets.ScWriter;

import java.util.Random;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        // Find the components
        final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
        assert gauge != null;

        // Path touch initialization
        gauge.setRecognizePathTouch(true);
        gauge.setPathTouchThreshold(30);

        // Get the pointer
        final ImageView pointer = (ImageView) findViewById(R.id.indicator);
        assert pointer != null;
        pointer.setRotation(180);

        // Just setting the duration of the animation in milliseconds
        Animator animator = gauge.getHighValueAnimator();
        animator.setDuration(500);

        gauge.setHighValue(60);

        // Each time I will change the value I must write it inside the counter text.
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float lowValue, float highValue) {
                // Write the value
                float angle = gauge.percentageToAngle(highValue);
                pointer.setRotation(angle);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        Random rnd = new Random();

        // Find the components
        final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
        assert gauge != null;
        gauge.setHighValue(rnd.nextInt(100));

        return true;
    }

}

