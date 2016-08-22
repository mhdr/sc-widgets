package com.sccomponents.widgets.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.sccomponents.widgets.ScArcGauge;
import com.sccomponents.widgets.ScCopier;
import com.sccomponents.widgets.ScDrawer;
import com.sccomponents.widgets.ScGauge;
import com.sccomponents.widgets.ScLinearGauge;
import com.sccomponents.widgets.ScNotches;
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
        final ScLinearGauge gauge = (ScLinearGauge) this.findViewById(R.id.line);
        assert gauge != null;

        // Create a drawable
        final Bitmap indicator = BitmapFactory.decodeResource(this.getResources(), R.drawable.indicator);

        // Set the values.
        gauge.setHighValue(75);
        gauge.setPathTouchThreshold(40);

        // Event
        gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
            @Override
            public void onBeforeDrawCopy(ScCopier.CopyInfo info) {
                // NOP
            }

            @Override
            public void onBeforeDrawNotch(ScNotches.NotchInfo info) {
                // Calculate the length
                float min = 10.0f;
                float max = 40.0f;
                float current = min + (max - min) * (info.index / (float) gauge.getNotches());

                // Apply
                info.length = gauge.dipToPixel(current);
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
                // Set angle and text
                info.angle = 0.0f;
                info.text = Math.round(gauge.getHighValue()) + "%";

                // Set the position
                float distance = info.source.getDistance(gauge.getHighValue());
                info.offset.x = 20;
                info.offset.y = -distance + 20;
            }
        });
    }

}

