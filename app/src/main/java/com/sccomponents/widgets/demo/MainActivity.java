package com.sccomponents.widgets.demo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sccomponents.widgets.ScCopier;
import com.sccomponents.widgets.ScDrawer;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Init
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        // Get the main layout
        LinearLayout main = (LinearLayout) this.findViewById(R.id.main);
        assert main != null;

        // Create the custom class
        Line line = new Line(this);
        line.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300));
        line.setBackgroundColor(Color.LTGRAY);
        line.setPadding(20, 20, 20, 20);
        line.setStrokeSize(0);

        // Feature
        ScCopier copier = (ScCopier) line.addFeature(ScCopier.class);
        copier.setColors(Color.RED, Color.GREEN, Color.BLUE);
        copier.getPainter().setStrokeWidth(14);
        //copier.setFillingColors(ScFeature.ShaderMode.SOLID);

        // Add to view
        main.addView(line);
    }

    public class Line extends ScDrawer {

        // Constructor
        public Line(Context context) {
            super(context);
        }

        /**
         * Create the path to draw.
         * This method need to draw something on the canvas. Note that the ScDrawer class not expose
         * other methods or public properties to manage the path.
         * To work on path you can use the protected properties: mPath and mPathMeasurer.
         *
         * @param maxWidth  the horizontal boundaries
         * @param maxHeight the vertical boundaries
         * @return return the new path
         */
        @Override
        protected Path createPath(int maxWidth, int maxHeight) {
            // Will be build a bezier curve from the left-top to the right-bottom angles of the
            // drawing area.
            Path path = new Path();
            path.quadTo(maxWidth / 2, 0.0f, maxWidth / 2, maxHeight / 2);
            path.quadTo(maxWidth / 2, maxHeight, maxWidth, maxHeight);

            // Return the new path
            return path;
        }
    }

}

