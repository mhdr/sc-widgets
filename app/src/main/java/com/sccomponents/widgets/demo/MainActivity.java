package com.sccomponents.widgets.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.sccomponents.widgets.ScGauge;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the indicator image
        final ImageView indicator = (ImageView) this.findViewById(R.id.indicator);
        assert indicator != null;
        // Set the center pivot for a right rotation
        indicator.setPivotX(35f);
        indicator.setPivotY(35f);

        // Get the gauge
        final ScGauge gauge = (ScGauge) this.findViewById(R.id.gauge);
        assert gauge != null;
        // Set the value to 75% take as reference a range of 0, 100.
        gauge.setValue(75, 0, 100);

        // Event
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float degrees) {
                // Transform the relative angle in an absolute one
                indicator.setRotation(degrees - gauge.getAngleStart());
            }
        });
    }
}
