package com.sccomponents.widgets.demo;

import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.sccomponents.widgets.ScArc;
import com.sccomponents.widgets.ScGauge;
import com.sccomponents.widgets.ScNotchs;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the gauge
        final ScGauge gauge = (ScGauge) this.findViewById(R.id.gauge);
        assert gauge != null;
        // Set the value to 60% take as reference a range of 0, 100.
        gauge.setValue(100, 0, 100);

        // Set the color gradient on the progress arc
        ScArc progress = gauge.getArcs()[2];
        progress.setStrokeColors(Color.GREEN, Color.RED);

        // Event
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float degrees) {
            }
        });

    }
}
