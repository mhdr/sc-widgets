package com.sccomponents.widgets.demo;

import android.graphics.Color;
import android.graphics.Paint;
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

        // Set the last token on the end of path
        final ScWriter writer = (ScWriter) gauge.findFeature(ScGauge.WRITER_IDENTIFIER);
        writer.setLastTokenOnEnd(true);

        // Set the value
        gauge.setHighValue(25);

        // Before draw
        gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
            @Override
            public void onBeforeDrawCopy(ScCopier.CopyInfo info) {
                // NOP
            }

            @Override
            public void onBeforeDrawNotch(ScNotches.NotchInfo info) {
                // The notch length
                info.length = gauge.dipToPixel(info.index % 4 == 0 ? 20 : 10);
            }

            @Override
            public void onBeforeDrawPointer(ScPointer.PointerInfo info) {
                // NOP
            }

            @Override
            public void onBeforeDrawToken(ScWriter.TokenInfo info) {
                // Get the text bounds
                Rect bounds = new Rect();
                info.source.getPainter().getTextBounds(info.text, 0, info.text.length(), bounds);

                // Zero angle
                info.angle = 0.0f;
                info.offset.x = -50 - bounds.width();
                info.offset.y = bounds.height() / 2;
            }
        });
    }

}

