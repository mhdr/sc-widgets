package com.sccomponents.widgets.demo;

import android.animation.ArgbEvaluator;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sccomponents.widgets.ScNotchs;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ScNotchs notchs = (ScNotchs) this.findViewById(R.id.notchs);
        assert notchs != null;
        notchs.setOnDrawListener(new ScNotchs.OnDrawListener() {
            @Override
            public float onDrawNotch(Paint painter, float angle, int count) {
                // ATTENTION!
                // In this exercise you will note that I never used the class setter to set the
                // stroke size, notchs length or the stroke color.
                // This because call the standard setter doing a component invalidate that
                // would call again this method going into an infinite loop.

                // Hold the starting notch length
                float length = notchs.getNotchsLength();

                // Emphasis every 4 notchs
                if (count % 4 == 0) {
                    // Change the stroke size and the length
                    painter.setStrokeWidth(painter.getStrokeWidth() * 3);
                    length *= 3;
                }
                // Emphasis every 2 notchs
                else if (count % 2 == 0) {
                    // Change the stroke size and the length
                    painter.setStrokeWidth(painter.getStrokeWidth() * 2);
                    length *= 2;
                }
                // No emphasis
                else {
                    // Set the default stroke size and not change the length
                    painter.setStrokeWidth(notchs.dipToPixel(ScNotchs.DEFAULT_STROKE_SIZE));
                }

                // Change the color
                float fraction = (float) count / (float) notchs.getNotchs();
                int color = (Integer) new ArgbEvaluator().evaluate(fraction, 0xffff0000, 0xff0000ff);
                painter.setColor(color);

                // return the new length
                return length;
            }
        });
    }
}
