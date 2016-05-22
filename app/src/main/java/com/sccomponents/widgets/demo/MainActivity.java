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

        // This method create a new object for this reason better do it for first operation.
        // As we cannot apply more than one mask effect to a single paint we need to have three
        // ScNotchs so we will convert the base arc to notchs and the progress arc too.
        // We'll use the base as usual, apply the blur on the notchs arc and apply the emboss on
        // the progress.
        gauge.changeComponentsConfiguration(true, false, true);

        // Now the base and progress arcs have inherited the color and the size from the notchs
        // setting so we need to change the base arc color programmatically.
        gauge.getBaseArc().setStrokeColor(Color.parseColor("#0D0B09"));

        // As the mask filter not support the hardware acceleration I must set the layer type
        // to software.
        gauge.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // Blur filter
        BlurMaskFilter blur = new BlurMaskFilter(5.0f, BlurMaskFilter.Blur.SOLID);
        gauge.getNotchsArc().getPainter().setMaskFilter(blur);

        // Emboss filter
        EmbossMaskFilter emboss = new EmbossMaskFilter(new float[]{0.0f, 1.0f, 0.5f}, 0.8f, 3.0f, 0.5f);
        gauge.getProgressArc().getPainter().setMaskFilter(emboss);

        // Set the value to 60% take as reference a range of 0, 100.
        gauge.setValue(60, 0, 100);

        // Events
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float degrees) {
                // Set the
                gauge.getNotchsArc().setAngleDraw(degrees);

                // Get the text control and write the value
                TextView counter = (TextView) MainActivity.this.findViewById(R.id.counter);
                assert counter != null;
                counter.setText((int) gauge.getValue(0, 100) + "%");
            }
        });

    }
}
