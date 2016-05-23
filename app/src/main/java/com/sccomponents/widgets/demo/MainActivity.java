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
import com.sccomponents.widgets.ScSeekBar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the seek bar
        final ScSeekBar seekBar = (ScSeekBar) this.findViewById(R.id.seekBar);
        assert seekBar != null;

        // Rounded cap
        seekBar.setStrokesCap(Paint.Cap.ROUND);
        // Set the value to 80% take as reference a range of 0, 100.
        seekBar.setValue(80, 0, 100);

        // Event
        seekBar.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float degrees) {
                // Get the text control and write the value
                TextView counter = (TextView) MainActivity.this.findViewById(R.id.counter);
                assert counter != null;
                int value = (int) seekBar.translateAngleToValue(degrees, 0, 100);
                counter.setText(value + "%");
            }
        });
    }
}
