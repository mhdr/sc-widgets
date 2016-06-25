package com.sccomponents.widgets.demo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.sccomponents.widgets.ScCopier;
import com.sccomponents.widgets.ScNotchs;
import com.sccomponents.widgets.ScPointer;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        // Dimensions
        int padding = 24;
        final Rect drawArea = new Rect(padding, padding, 500 - padding, 300 - padding);

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
        path.moveTo(drawArea.left, drawArea.top);
        path.quadTo(drawArea.centerX(), drawArea.top, drawArea.centerX(), drawArea.centerY());
        path.quadTo(drawArea.centerX(), drawArea.bottom, drawArea.right, drawArea.bottom);

        // Draw the path on for have a reference
        Paint temp = new Paint();
        temp.setStyle(Paint.Style.STROKE);
        temp.setStrokeWidth(2);
        canvas.drawPath(path, temp);

        // Preload the bitmap
        final Bitmap custom = BitmapFactory.decodeResource(this.getResources(), R.drawable.arrow);

        // Feature
        ScPointer pointer = new ScPointer(path);
        pointer.setOnDrawListener(new ScPointer.OnDrawListener() {
            @Override
            public void onBeforeDrawPointer(ScPointer.PointerInfo info) {
                info.bitmap = custom;
                info.offset = new PointF(-16, -16);
                // Uncomment the following line if you not want bitmap rotation
                // info.angle = 0;
            }
        });

        for (int position = 0; position <= 100; position = position + 10) {
            pointer.setPosition(position);
            pointer.draw(canvas);
        }

        // Add the bitmap to the container
        imageContainer.setImageBitmap(bitmap);
    }

}

