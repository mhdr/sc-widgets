package com.sccomponents.widgets.demo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sccomponents.widgets.ScArcGauge;
import com.sccomponents.widgets.ScCopier;
import com.sccomponents.widgets.ScGauge;
import com.sccomponents.widgets.ScNotches;
import com.sccomponents.widgets.ScPointer;
import com.sccomponents.widgets.ScWriter;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        // Start and end line width
        final float startWidth = 10;
        final float endWidth = 120;

        // Notches count
        final int notchesCount = 750;

        // Find the components
        final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
        assert gauge != null;

        // Clear all default features from the gauge
        gauge.removeAllFeatures();

        // Create the base notches.
        ScNotches base = (ScNotches) gauge.addFeature(ScNotches.class);
        base.setTag("MY_BASE");
        base.setColors(
                Color.parseColor("#5C6567"),
                Color.parseColor("#5C6567"),
                Color.parseColor("#5C6567"),
                Color.parseColor("#BBBBBD"),
                Color.parseColor("#5C6567")
        );
        base.setCount(notchesCount);
        base.getPainter().setStrokeWidth(2);

        // Create the filler.
        ScNotches filler = (ScNotches) gauge.addFeature(ScNotches.class);
        filler.setTag("MY_FILLER");
        filler.setCount(notchesCount);
        filler.getPainter().setStrokeWidth(2);
        filler.setColors(Color.parseColor("#222222"));

        // Create the progress.
        ScNotches progress = (ScNotches) gauge.addFeature(ScNotches.class);
        progress.setTag(ScGauge.PROGRESS_IDENTIFIER);
        progress.setCount(notchesCount);

        // Set the value
        gauge.setHighValue(90);

        // Events
        gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
            @Override
            public void onBeforeDrawCopy(ScCopier.CopyInfo copyInfo) {

            }

            @Override
            public void onBeforeDrawNotch(ScNotches.NotchInfo notchInfo) {
                // Calculate the base length
                float fraction = notchInfo.index / (float) notchesCount;
                float length = startWidth + (fraction * (endWidth - startWidth));

                // Reduce the length by the case
                if (notchInfo.source.getTag().equals("MY_FILLER")) {
                    float maxMargin = 12.0f;
                    length -= gauge.dipToPixel(maxMargin * fraction);
                }

                if (notchInfo.source.getTag().equals(ScGauge.PROGRESS_IDENTIFIER)) {
                    float maxMargin = 16.0f;
                    length -= gauge.dipToPixel(maxMargin * fraction);
                }

                notchInfo.length = length;
            }

            @Override
            public void onBeforeDrawPointer(ScPointer.PointerInfo pointerInfo) {

            }

            @Override
            public void onBeforeDrawToken(ScWriter.TokenInfo tokenInfo) {

            }
        });
    }

}

