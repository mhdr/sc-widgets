package com.sccomponents.widgets.demo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.sccomponents.widgets.ScNotchs;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        // Dimensions
        int padding = 24;
        Rect drawArea = new Rect(padding, padding, 500 - padding, 500 - padding);

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

        // Create path with two contours
        Path path = new Path();
        path.moveTo(drawArea.left, drawArea.bottom);
        path.quadTo(drawArea.left, drawArea.top, drawArea.centerX(), drawArea.top);

        path.moveTo(drawArea.right, drawArea.top);
        path.quadTo(drawArea.right, drawArea.bottom, drawArea.centerX(), drawArea.bottom);

        // Feature
        ScNotchs notchs = new ScNotchs(path);
        notchs.setColors(Color.LTGRAY, Color.BLACK);
        notchs.getPainter().setStrokeWidth(4);
        notchs.setCount(10);
        notchs.setLength(20);
        notchs.setPosition(ScNotchs.NotchPositions.INSIDE);
        notchs.setDividePathInContours(false);
        notchs.setOnDrawListener(new ScNotchs.OnDrawListener() {
            @Override
            public void onBeforeDrawNotch(ScNotchs.NotchInfo info) {
                info.length = 10 + info.index * 5;
                info.size = 3 + info.index;
            }
        });
        notchs.draw(canvas);

        // Add the bitmap to the container
        imageContainer.setImageBitmap(bitmap);
    }

}

