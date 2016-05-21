package com.sccomponents.widgets.demo;

import android.graphics.BlurMaskFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.sccomponents.widgets.ScArc;
import com.sccomponents.widgets.ScGauge;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the gauge
        final ScGauge gauge = (ScGauge) this.findViewById(R.id.gauge);
        assert gauge != null;

        // Hide the notchs.
        // You can obtain the same result setting the notchs number to zero.
        gauge.show(true, false, true);
        // Rounded cap
        gauge.setStrokesCap(Paint.Cap.ROUND);
        // He set the value to 80% take as reference a range of 0, 100.
        gauge.setValue(80, 0, 100);

        // Event
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float degrees) {
                // Get the text control
                TextView counter = (TextView) MainActivity.this.findViewById(R.id.counter);
                assert counter != null;

                // Note that I used translateAngleToValue but in this case is nonsense.
                // In this case I could use getValue(0, 100) for have the same result.
                // This is right if you are using the animator for move the value because the
                // degrees changes over time gradually instead the getValue methods return always
                // the final value to reach.
                int value = (int) gauge.translateAngleToValue(degrees, 0, 100);
                counter.setText(value + "%");
            }
        });
    }
}
