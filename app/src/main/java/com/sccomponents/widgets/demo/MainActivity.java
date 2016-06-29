package com.sccomponents.widgets.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
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

        // Clear all default features from the gauge
        gauge.removeAllFeatures();

        // Take in mind that when you tagged a feature after this feature inherit the principal
        // characteristic of the identifier.
        // For example in the case of the BASE_IDENTIFIER the feature notchs (always) will be
        // settle as the color and stroke size settle for the base (in xml or via code).

        // Create the base notchs.
        ScNotchs base = (ScNotchs) gauge.addFeature(ScNotchs.class);
        base.setTag(ScGauge.BASE_IDENTIFIER);
        base.setCount(40);
        base.setPosition(ScNotchs.NotchPositions.INSIDE);

        // Note that I will create two progress because to one will add the blur and to the other
        // will be add the emboss effect.

        // Create the progress notchs.
        ScNotchs progress = (ScNotchs) gauge.addFeature(ScNotchs.class);
        progress.setTag(ScGauge.PROGRESS_IDENTIFIER);
        progress.setCount(40);
        progress.setPosition(ScNotchs.NotchPositions.INSIDE);
        progress.setColors(
                Color.parseColor("#0BA60A"),
                Color.parseColor("#FEF301"),
                Color.parseColor("#EA0C01")
        );

        // Set the value
        gauge.setHighValue(90);

        // Each time I will change the value I must write it inside the counter text.
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float lowValue, float highValue) {
                // Write the value
                counter.setText((int) highValue + "%");
            }
        });

        // Before draw
        gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
            @Override
            public void onBeforeDrawCopy(ScCopier.CopyInfo info) {
                // Do nothing
            }

            @Override
            public void onBeforeDrawNotch(ScNotchs.NotchInfo info) {
                // Set the length of the notch
                info.source.setLength(gauge.dipToPixel(info.index + 5));
            }

            @Override
            public void onBeforeDrawPointer(ScPointer.PointerInfo info) {
                // Do nothing
            }

            @Override
            public void onBeforeDrawToken(ScWriter.TokenInfo info) {
                // Do nothing
            }
        });
    }

}

