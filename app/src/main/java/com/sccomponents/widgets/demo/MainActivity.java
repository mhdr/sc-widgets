package com.sccomponents.widgets.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sccomponents.widgets.ScCopier;
import com.sccomponents.widgets.ScDrawer;
import com.sccomponents.widgets.ScFeature;
import com.sccomponents.widgets.ScNotchs;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        // Dimensions
        int padding = 24;
        Rect drawArea = new Rect(padding, padding, 500 - padding, 300 - padding);

        // Get the main layout
        ImageView imageContainer = (ImageView) this.findViewById(R.id.image);
        assert imageContainer != null;

        // Create a bitmap and link a canvas
        Bitmap bitmap = Bitmap.createBitmap(
                drawArea.width() + padding * 2, drawArea.height() + padding * 2,
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.parseColor("#f5f5f5"));

        // Create the path building a bezier curve from the left-top to the right-bottom angles of
        // the drawing area.
        Path path = new Path();
        path.moveTo(drawArea.left, drawArea.centerY());
        path.lineTo(drawArea.right, drawArea.centerY());

        // Feature
        ScNotchs notchs = new ScNotchs(path);
        notchs.getPainter().setStrokeWidth(4);
        notchs.setCount(24);
        notchs.setLength(30);
        notchs.setOnDrawListener(new ScNotchs.OnDrawListener() {
            @Override
            public void onBeforeDrawNotch(ScNotchs.NotchInfo info) {
                // Check th module
                if (info.index % 2 == 0)
                    info.align = ScNotchs.NotchPositions.INSIDE;
                else
                    info.align = ScNotchs.NotchPositions.OUTSIDE;

                // Calc the percentage
                float ratio = info.index / info.source.getCount();

                // Set the length by the position
                info.length = info.source.getLength() * ratio + 10;
                info.offset = - (info.length / 2) * (1 - ratio);
            }
        });
        notchs.draw(canvas);

        // Add the bitmap to the container
        imageContainer.setImageBitmap(bitmap);
    }

}

