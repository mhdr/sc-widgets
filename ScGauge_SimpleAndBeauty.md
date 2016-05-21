# ScGauge - Simple and beauty
Some examples or go back to the class (documentation)[ScGauge.md].

### Flat 1

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/2.jpg"> 
```xml
    <FrameLayout
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:background="#f5f5f5"
        android:padding="10dp"
        >

        <com.sccomponents.widgets.ScGauge
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:id="@+id/gauge"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            sc:scc_angle_start="135"
            sc:scc_angle_sweep="270"
            sc:scc_progress_color="#ffff00ff"
            sc:scc_progress_size="10dp"
            sc:scc_stroke_color="@color/accent_material_light"
            sc:scc_stroke_size="10dp"/>

        <TextView
            android:id="@+id/counter"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="bottom|center_horizontal"
            android:layout_marginBottom="5dp"
            android:text="0%"
            android:textColor="@color/accent_material_light"
            android:textSize="42dp"/>

    </FrameLayout>
```

```java
        // Get the gauge
        final ScGauge gauge = (ScGauge) this.findViewById(R.id.gauge);
        assert gauge != null;

        // Hide the notchs.
        // You can obtain the same result setting the notchs number to zero.
        gauge.show(true, false, true);
        // Rounded cap
        gauge.setStrokesCap(Paint.Cap.ROUND);
        // Set the value to 80% take as reference a range of 0, 100.
        gauge.setValue(80, 0, 100);

        // Event
        gauge.setOnEventListener(new ScGauge.OnEventListener() {
            @Override
            public void onValueChange(float degrees) {
                // Get the text control
                TextView counter = (TextView) MainActivity.this.findViewById(R.id.counter);
                assert counter != null;

                // Note that I used translateAngleToValue but in this case is nonsense.
                // In this case I could use getValue(0, 100) for have the same result.
                // This is right if you are using the animator for move the value because the
                // degrees changes over time gradually instead the getValue methods return always
                // the final value to reach.
                int value = (int) gauge.translateAngleToValue(degrees, 0, 100);
                counter.setText(value + "%");
            }
        });
```


### Flat 2

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/3.jpg"> 
```xml
    <FrameLayout
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:background="#f5f5f5"
        android:padding="10dp" >

        <com.sccomponents.widgets.ScGauge
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:id="@+id/gauge"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:padding="10dp"
            sc:scc_angle_start="-90"
            sc:scc_progress_color="#288636"
            sc:scc_progress_size="10dp"
            sc:scc_stroke_color="#585258"
            sc:scc_stroke_size="2dp" />

        <TextView
            android:id="@+id/counter"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:text="0%"
            android:textColor="#585258"
            android:textSize="42dp"/>

    </FrameLayout>
```

```java
        // Get the gauge
        final ScGauge gauge = (ScGauge) this.findViewById(R.id.gauge);
        assert gauge != null;

        // Hide the notchs.
        // You can obtain the same result setting the notchs number to zero.
        gauge.show(true, false, true);
        // He set the value to 80% take as reference a range of 0, 100.
        gauge.setValue(40, 0, 100);

        // Event
        ...
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