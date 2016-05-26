package com.sccomponents.widgets.demo;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.sccomponents.widgets.ScArc;
import com.sccomponents.widgets.ScDrawer;
import com.sccomponents.widgets.ScGauge;

import java.text.Format;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RectF rect = new RectF(0, 0, 300, 300);
        Path path = new Path();
        path.addArc(rect, 180f, 180f);

        ScDrawer drawer = (ScDrawer) this.findViewById(R.id.drawer);
        drawer.setPath(path);
    }

}

