# ScGauge - Notchs
Some examples or go back to the class [documentation](ScGauge.md).


### Play with colors and filters

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/6.jpg"> 
```xml
    <FrameLayout
        android:layout_width="200dp"
        android:layout_height="wrap_content"
        android:background="#fff5f5f5"
        android:padding="10dp">

        <com.sccomponents.widgets.ScGauge
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:id="@+id/gauge"
            sc:scc_angle_start="180"
            sc:scc_angle_sweep="180"
            sc:scc_progress_size="30dp"
            sc:scc_progress_color="#000000"
            sc:scc_stroke_size="30dp"
            sc:scc_notchs_size="2dp"
            sc:scc_notchs_color="#ffffff"
            sc:scc_notchs="4"
            sc:scc_notchs_length="30dp" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="100%"
            android:id="@+id/counter"
            android:layout_gravity="bottom|center_horizontal"
            android:textSize="30dp" />

    </FrameLayout>
```

```java
        // Get the gauge
        final ScGauge gauge = (ScGauge) this.findViewById(R.id.gauge);
        assert gauge != null;
        // Set the value to 30% take as reference a range of 0, 100.
        gauge.setValue(30, 0, 100);

        // Set the color gradient on the progress arc
        gauge.getBaseArc().setStrokeColors(
                Color.parseColor("#EA3A3C"),
                Color.parseColor("#FDE401"),
                Color.parseColor("#55B20C"),
                Color.parseColor("#3FA8F9")
        );

        // As the mask filter not support the hardware acceleration I must set the layer type
        // to software.
        gauge.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        // Apply the filter on the painters
        EmbossMaskFilter filter = new EmbossMaskFilter(new float[]{0.0f, 1.0f, 0.5f}, 0.8f, 3.0f, 5.0f);
        gauge.getBaseArc().getPainter().setMaskFilter(filter);
        gauge.getProgressArc().getPainter().setMaskFilter(filter);

        // Events
        gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
            @Override
            public void onBeforeDraw(Paint baseArc, Paint notchsArc, Paint progressArc) {
                // Set the progress color by taking the color from the base arc gradient.
                // Note that I passed the current gauge value because if not the current gradient
                // color if relative at the base arc draw angle.
                int color = gauge.getBaseArc()
                        .getCurrentGradientColor(gauge.getValue());
                progressArc.setColor(color);
            }

            @Override
            public void onDrawNotch(ScNotchs.NotchInfo info) {
                // Hide the first and the last notchs
                info.visible = info.index > 0 && info.index < info.source.getNotchs() + 1;
            }
        });

        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float degrees) {
                // Get the text control and write the value
                TextView counter = (TextView) MainActivity.this.findViewById(R.id.counter);
                assert counter != null;
                counter.setText((int) gauge.getValue(0, 100) + "%");
            }
        });
```


### Changing the component configuration

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/7.jpg"> 
```xml
    <FrameLayout
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:background="#fff5f5f5">

        <com.sccomponents.widgets.ScGauge
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:id="@+id/gauge"
            android:padding="10dp"
            sc:scc_angle_start="180"
            sc:scc_angle_sweep="90"
            sc:scc_progress_size="3dp"
            sc:scc_notchs="32" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="100%"
            android:id="@+id/counter"
            android:layout_gravity="bottom|right"
            android:textSize="40dp"
            android:textColor="#000000"
            android:layout_margin="20dp"/>

    </FrameLayout>
```

```java
        // Get the gauge
        final ScGauge gauge = (ScGauge) this.findViewById(R.id.gauge);
        assert gauge != null;

        // Convert the progress arc to notchs arc and hide the base arc.
        // This method create a new object for this reason better do it for first operation.
        gauge.changeComponentsConfiguration(false, false, true);
        gauge.show(false, true, true);

        // Set the value to 60% take as reference a range of 0, 100.
        gauge.setValue(60, 0, 100);

        // Set the color gradient on the progress arc
        gauge.getProgressArc().setStrokeColors(
                Color.parseColor("#EA3A3C"),
                Color.parseColor("#FDE401"),
                Color.parseColor("#55B20C"),
                Color.parseColor("#3FA8F9")
        );

        // Events
        gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
            @Override
            public void onBeforeDraw(Paint baseArc, Paint notchsArc, Paint progressArc) {
                // Do nothing
            }

            @Override
            public void onDrawNotch(ScNotchs.NotchInfo info) {
                // Hide the first notch to prevent a visual color filling issue
                info.visible = info.index > 0;
                // Change the length of the notch by the position index
                info.length += info.index;
            }
        });

        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float degrees) {
                // Get the text control and write the value
                TextView counter = (TextView) MainActivity.this.findViewById(R.id.counter);
                assert counter != null;
                counter.setText((int) gauge.getValue(0, 100) + "%");
            }
        });
```


### Custom padding and notchs emphasis

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/8.jpg"> 
```xml
    <FrameLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:background="#fff5f5f5">

        <com.sccomponents.widgets.ScGauge
            android:id="@+id/gauge"
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:layout_width="200dp"
            android:layout_height="wrap_content"
            android:padding="10dp"
            sc:scc_angle_start="155"
            sc:scc_angle_sweep="230"
            sc:scc_stroke_size="5dp"
            sc:scc_progress_size="20dp"
            sc:scc_notchs="16" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="100%"
            android:id="@+id/counter"
            android:textSize="40dp"
            android:textColor="#000000"
            android:layout_gravity="center"
            android:layout_marginTop="20dp"/>

    </FrameLayout>
```

```java
        // Get the gauge
        final ScGauge gauge = (ScGauge) this.findViewById(R.id.gauge);
        assert gauge != null;

        // Set the value to 60% take as reference a range of 0, 100.
        gauge.setValue(60, 0, 100);
        // Draw the notchs arc for last
        gauge.setDrawNotchsForLast(true);

        // Set the color gradient on the progress arc
        gauge.getProgressArc().setStrokeColors(
                Color.parseColor("#EA3A3C"),
                Color.parseColor("#FDE401"),
                Color.parseColor("#55B20C"),
                Color.parseColor("#3FA8F9")
        );

        // Events
        gauge.setOnDrawListener(new ScGauge.OnDrawListener() {
            @Override
            public void onBeforeDraw(Paint baseArc, Paint notchsArc, Paint progressArc) {
                // Do nothing
            }

            @Override
            public void onDrawNotch(ScNotchs.NotchInfo info) {
                // Notch emphasis
                if (info.index % 4 == 0) {
                    info.length = gauge.getBaseArc().getStrokeSize() + gauge.getProgressArc().getStrokeSize();
                }
            }
        });

        gauge.setOnCustomPaddingListener(new ScGauge.OnCustomPaddingListener() {
            @Override
            public void onCustomPadding(Rect baseArc, Rect notchsArc, Rect progressArc) {
                // Move the progress inside.
                // Noted that this rect NOT contain an area but contain the four padding used
                // for draw the component.
                // Noted also that in the bottom setting I used a number for adjust a typical
                // visual issue about inscribed arcs.
                progressArc.left += gauge.getBaseArc().getStrokeSize();
                progressArc.right += gauge.getBaseArc().getStrokeSize();
                progressArc.top += gauge.getBaseArc().getStrokeSize();
                progressArc.bottom += gauge.getBaseArc().getStrokeSize() - 5;
            }
        });

        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float degrees) {
                // Get the text control and write the value
                TextView counter = (TextView) MainActivity.this.findViewById(R.id.counter);
                assert counter != null;
                counter.setText((int) gauge.getValue(0, 100) + "%");
            }
        });
```


# License
<pre>
 Copyright 2015 Samuele Carassai

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in  writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,  either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
</pre>