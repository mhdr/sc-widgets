package com.sccomponents.widgets.demo;

import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.sccomponents.widgets.ScArcGauge;
import com.sccomponents.widgets.ScCopier;
import com.sccomponents.widgets.ScFeature;
import com.sccomponents.widgets.ScGauge;
import com.sccomponents.widgets.ScNotchs;
import com.sccomponents.widgets.ScPointer;
import com.sccomponents.widgets.ScWriter;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        // Find the components
        final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
        assert gauge != null;

        final TextView counter = (TextView) this.findViewById(R.id.counter);
        assert counter != null;

        // Bring on top
        gauge.bringOnTop(ScGauge.BASE_IDENTIFIER);
        gauge.bringOnTop(ScGauge.NOTCHS_IDENTIFIER);

        // Set the value
        gauge.setHighValue(320, 0, 320);

        // Each time I will change the value I must write it inside the counter text.
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float lowValue, float highValue) {
                // Write the value
                int value = (int) ScGauge.percentageToValue(highValue, 0, 320);
                counter.setText(Integer.toString(value));
            }
        });

        // Before draw
        gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
            @Override
            public void onBeforeDrawCopy(ScCopier.CopyInfo info) {
                // Check for the progress
                if (info.source.getTag() == ScGauge.PROGRESS_IDENTIFIER) {
                    // Scale
                    info.scale = new PointF(0.95f, 0.95f);
                    info.offset = new PointF(14.0f, 14.0f);
                }
            }

            @Override
            public void onBeforeDrawNotch(ScNotchs.NotchInfo info) {
                // Set the length of the notch
                info.length = info.index == 0 || info.index == info.source.getCount() ?
                        gauge.dipToPixel(15) : gauge.dipToPixel(5);
                info.color = info.index > 6 ? Color.RED : Color.WHITE;
            }

            @Override
            public void onBeforeDrawPointer(ScPointer.PointerInfo info) {
                // Do nothing
            }

            @Override
            public void onBeforeDrawToken(ScWriter.TokenInfo info) {
                // Get the text bounds
                Rect bounds = new Rect();
                info.source.getPainter().getTextBounds(info.text, 0, info.text.length(), bounds);

                // Hide the first
                info.visible = info.index != 0;

                // Reset the angle and the offset
                info.angle = 0.0f;
                info.offset.y = bounds.height() / 2 + 5;
                info.offset.x = -bounds.width() * ((float) info.index / (float) info.source.getTokens().length);
            }
        });
    }

}

