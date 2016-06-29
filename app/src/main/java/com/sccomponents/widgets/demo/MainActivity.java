package com.sccomponents.widgets.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.sccomponents.widgets.ScWidget;
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

        // Create a drawable
        final Bitmap indicator = BitmapFactory.decodeResource(this.getResources(), R.drawable.indicator);

        // Set the values.
        gauge.setHighValue(14, -10, 30);
        gauge.setPathTouchThreshold(40);

        // Set colors of the base
        final ScFeature base = gauge.findFeature(ScGauge.BASE_IDENTIFIER);
        base.setColors(
                Color.parseColor("#15B7FF"), Color.parseColor("#15B7FF"),
                Color.parseColor("#98CA06"), Color.parseColor("#98CA06"),
                Color.parseColor("#98CA06"), Color.parseColor("#98CA06"),
                Color.parseColor("#98CA06"),
                Color.parseColor("#DC1E10")
        );
        base.setFillingColors(ScFeature.ColorsMode.SOLID);

        // Notchs
        ScNotchs notchs = (ScNotchs) gauge.findFeature(ScGauge.NOTCHS_IDENTIFIER);
        notchs.setPosition(ScNotchs.NotchPositions.INSIDE);

        // Writer
        String[] tokens = new String[9];
        for (int index = 0; index < 9; index++) {
            tokens[index] = Integer.toString(index * 5 - 10);
        }

        ScWriter writer = (ScWriter) gauge.findFeature(ScGauge.WRITER_IDENTIFIER);
        writer.setTokens(tokens);
        writer.setLastTokenOnEnd(true);
        writer.setPosition(ScWriter.TokenPositions.INSIDE);

        // Each time I will change the value I must write it inside the counter text.
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float lowValue, float highValue) {
                // Write the value
                highValue = ScGauge.percentageToValue(highValue, -10, 30);
                float round = (Math.round(highValue * 10.0f)) / 10.0f;
                counter.setText(Float.toString(round) + "°");
            }
        });

        gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
            @Override
            public void onBeforeDrawCopy(ScCopier.CopyInfo info) {
                // Check if is the progress
                if (info.source.getTag() == ScGauge.PROGRESS_IDENTIFIER) {
                    // Scale and adjust the offset
                    info.scale = new PointF(1.1f, 1.1f);
                    info.offset = new PointF(-28, -28);

                    // Adjust the color
                    int color = base.getGradientColor(gauge.getHighValue(), 100);
                    info.source.getPainter().setColor(color);
                }
            }

            @Override
            public void onBeforeDrawNotch(ScNotchs.NotchInfo info) {
                // Move the offset
                info.offset = -info.length / 2;

                // Check for module highlight the notchs than have module 5 and 10
                if (info.index % 10 == 0) {
                    info.size = 6;
                } else if (info.index % 5 != 0) {
                    info.length -= 5;
                }
            }

            @Override
            public void onBeforeDrawPointer(ScPointer.PointerInfo info) {
                // Check if the pointer if the high pointer
                if (info.source.getTag() == ScGauge.HIGH_POINTER_IDENTIFIER) {
                    // Adjust the offset
                    info.offset.x = -indicator.getWidth() / 2;
                    info.offset.y = -indicator.getHeight() / 2 - gauge.getStrokeSize();
                    // Assign the bitmap to the pointer info structure
                    info.bitmap = indicator;
                }
            }

            @Override
            public void onBeforeDrawToken(ScWriter.TokenInfo info) {
                // Center on the notchs
                Rect bounds = new Rect();
                info.source.getPainter().getTextBounds(info.text, 0, info.text.length(), bounds);
                info.offset.x = - (bounds.width() / 2) - 1;
            }
        });
    }

}

