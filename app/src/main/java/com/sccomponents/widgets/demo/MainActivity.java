package com.sccomponents.widgets.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.sccomponents.widgets.ScArcGauge;
import com.sccomponents.widgets.ScCopier;
import com.sccomponents.widgets.ScFeature;
import com.sccomponents.widgets.ScGauge;
import com.sccomponents.widgets.ScNotchs;
import com.sccomponents.widgets.ScPointer;
import com.sccomponents.widgets.ScWriter;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        // Find the components
        final ScArcGauge gauge = (ScArcGauge) this.findViewById(R.id.gauge);
        assert gauge != null;

        final TextView txtLowValue = (TextView) this.findViewById(R.id.lowValue);
        assert txtLowValue != null;

        final TextView txtHighValue = (TextView) this.findViewById(R.id.highValue);
        assert txtHighValue != null;

        // Clear the pointers halo
        gauge.setPointerHaloWidth(0.0f);

        // Low pointer visibility
        ScFeature lowPointer = gauge.findFeature(ScGauge.LOW_POINTER_IDENTIFIER);
        lowPointer.setVisible(true);

        // Set the values.
        // Note that the low cannot be over the high so you must set always for second because
        // the initial high is, by default, equal to 0.
        gauge.setHighValue(59);
        gauge.setLowValue(12);

        // Each time I will change the value I must write it inside the counter text.
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float lowValue, float highValue) {
                txtLowValue.setText((int) lowValue + "°");
                txtHighValue.setText((int) highValue + "°");
            }
        });
    }

}

