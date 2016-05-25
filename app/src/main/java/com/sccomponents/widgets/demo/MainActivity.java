package com.sccomponents.widgets.demo;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.sccomponents.widgets.ScArc;
import com.sccomponents.widgets.ScGauge;

import java.text.Format;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the gauge
        final ScGauge gauge = (ScGauge) this.findViewById(R.id.gauge);
        assert gauge != null;

        gauge.setCanvasFilling(ScArc.FillingArea.NONE, ScArc.FillingMode.DRAW);

        // Set the colors on the base arc
        gauge.getBaseArc().setFillingColors(ScArc.FillingColors.SOLID);
        gauge.getBaseArc().setStrokeColors(
                Color.parseColor("#EA3A3C"),
                Color.parseColor("#FDE401"),
                Color.parseColor("#55B20C"),
                Color.parseColor("#3FA8F9")
        );

        // Set the value.
        gauge.setValue(-15, 0, -40);


        // Event
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float degrees) {
                // Get the text control
                TextView counter = (TextView) MainActivity.this.findViewById(R.id.counter);
                assert counter != null;

                // Set the value
                counter.setText("" + (int) gauge.getValue(0, -40));
            }
        });

    }

}

