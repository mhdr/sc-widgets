package com.sccomponents.widgets.demo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.sccomponents.widgets.ScWriter;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        // Dimensions
        int padding = 30;
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

        // Draw the path only for have a reference
        Paint temp = new Paint();
        temp.setStyle(Paint.Style.STROKE);
        temp.setStrokeWidth(2);
        canvas.drawPath(path, temp);

        // Create the tokens
        int count = 14;
        String[] tokens = new String[count];
        for (int index = 0; index < count; index ++) {
            tokens[index] = (index < 9 ? "0" : "") + (index + 1);
        }

        // Feature
        ScWriter writer = new ScWriter (path);
        writer.setTokens(tokens);
        writer.setUnbend(true);
        writer.setLastTokenOnEnd(true);
        writer.setColors(Color.RED, Color.BLUE, Color.GREEN, Color.CYAN);
        writer.setOnDrawListener(new ScWriter.OnDrawListener() {
            @Override
            public void onBeforeDrawToken(ScWriter.TokenInfo info) {
                info.angle -= 90;
                info.offset = new PointF(5, 10);
            }
        });
        writer.draw(canvas);

        // Add the bitmap to the container
        imageContainer.setImageBitmap(bitmap);
    }

}

