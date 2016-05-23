package com.sccomponents.widgets.demo;

import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

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

        // Events
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float degrees) {
                // Get the text control and write the value
                TextView counter = (TextView) MainActivity.this.findViewById(R.id.counter);
                assert counter != null;
                counter.setText((int) gauge.getValue(0, 100) + "%");
            }
        });

    }
}
