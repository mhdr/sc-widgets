package com.sccomponents.widgets.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.sccomponents.widgets.ScGauge;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ScGauge gauge = (ScGauge) MainActivity.this.findViewById(R.id.gauge);
        final Button button = (Button) this.findViewById(R.id.button);
        final TextView value = (TextView) this.findViewById(R.id.txtValue);

        assert button != null;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                assert gauge != null;
                gauge.setValue(2, 0, 10);
            }
        });

        assert gauge != null;
        gauge.getValueAnimator().setDuration(1000);
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float degrees) {
                assert value != null;
                value.setText("" + degrees);
            }
        });
    }
}
