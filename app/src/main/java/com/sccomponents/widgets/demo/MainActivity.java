package com.sccomponents.widgets.demo;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

        // Set the value to 60% take as reference a range of 0, 100.
        gauge.setValue(60, 0, 100);
        // Draw the notchs arc for last
        gauge.setDrawNotchsForLast(true);

        // Set the color gradient on the progress arc
        gauge.getProgressArc().setStrokeColors(
                Color.parseColor("#EA3A3C"),
                Color.parseColor("#FDE401"),
                Color.parseColor("#55B20C"),
                Color.parseColor("#3FA8F9")
        );

        // Events
        gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
            @Override
            public void onBeforeDraw(Paint baseArc, Paint notchsArc, Paint progressArc) {
                // Do nothing
            }

            @Override
            public void onDrawNotch(ScNotchs.NotchInfo info) {
                // Notch emphasis
                if (info.index % 4 == 0) {
                    info.length = gauge.getBaseArc().getStrokeSize() + gauge.getProgressArc().getStrokeSize();
                }
            }
        });

        gauge.setOnCustomPaddingListener(new ScGauge.OnCustomPaddingListener() {
            @Override
            public void onCustomPadding(Rect baseArc, Rect notchsArc, Rect progressArc) {
                // Move the progress inside.
                // Noted that this rect NOT contain an area but contain the four padding used
                // for draw the component.
                // Noted also that in the bottom setting I used a number for adjust a typical
                // visual issue about inscribed arcs.
                progressArc.left += gauge.getBaseArc().getStrokeSize();
                progressArc.right += gauge.getBaseArc().getStrokeSize();
                progressArc.top += gauge.getBaseArc().getStrokeSize();
                progressArc.bottom += gauge.getBaseArc().getStrokeSize() - 5;
            }
        });

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
