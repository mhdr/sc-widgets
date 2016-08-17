package com.sccomponents.widgets.demo;

import android.graphics.Color;
import android.graphics.Paint;
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

        final TextView counter = (TextView) this.findViewById(R.id.counter);
        assert counter != null;

        // Set the value
        gauge.setHighValue(75);

        // Each time I will change the value I must write it inside the counter text.
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float lowValue, float highValue) {
                counter.setText("Value: " + (int) highValue + "%");
            }
        });

    }

}

