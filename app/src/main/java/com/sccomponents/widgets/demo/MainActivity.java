package com.sccomponents.widgets.demo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.sccomponents.widgets.ScCopier;
import com.sccomponents.widgets.ScNotchs;


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

        // Feature
        ScCopier copier = new ScCopier(path);
        copier.getPainter().setStrokeWidth(8);
        copier.setOnDrawListener(new ScCopier.OnDrawListener() {
            @Override
            public void onBeforeDrawCopy(ScCopier.CopyInfo info) {
                info.scale = new PointF(0.5f, 1.0f);
                info.offset = new PointF(125.0f, 0.0f);
                info.rotate = -45;
            }
        });
        copier.draw(canvas);

        // Add the bitmap to the container
        imageContainer.setImageBitmap(bitmap);
    }

}

