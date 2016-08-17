# ScLinearGauge examples
Following some example of the [ScLinearGauge](../sc-lineargauge/ScLinearGauge.md) application.

Is simple to understand that inheriting from the [ScGauge](../sc-gauge/ScGauge.md) class the possibilities are infinite.
So this example as been only a demonstration of the most used configurations.

> **ATTENTION**<br />
> Please keep in mind that you can enable the user input (`setInputEnabled`) to allow the user to drive the gauge values.<br />
> Also you can use the animator (`getHighValueAnimator` or `getLowValueAnimator`) to animate the value changing.

<br />
<br />

---
####### Example 1

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-lineargauge/f-01.jpg"> 
```xml
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#ECECEA"
        android:padding="10dp"
        android:orientation="vertical">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Progress"
            android:textColor="#5B5B5A"
            android:textSize="24dp" />

        <com.sccomponents.widgets.ScLinearGauge 
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:id="@+id/line"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:paddingBottom="10dp"
            android:paddingTop="10dp"
            sc:scc_bottom="0"
            sc:scc_progress_colors="#3589F6"
            sc:scc_progress_size="6dp"
            sc:scc_stroke_color="#ABCDED"
            sc:scc_stroke_size="6dp"
            sc:scc_top="0"
            sc:scc_value="75" />

    </LinearLayout>
```

---
####### Example 2

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-lineargauge/f-02.jpg"> 
```xml
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="#ECECEA"
        android:padding="10dp"
        android:orientation="vertical">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:id="@+id/counter"
            android:textColor="#5B5B5A"
            android:textSize="24dp" />

        <com.sccomponents.widgets.ScLinearGauge
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:id="@+id/line"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:paddingBottom="20dp"
            android:paddingTop="20dp"
            sc:scc_bottom="0"
            sc:scc_progress_colors="#3589F6"
            sc:scc_progress_size="6dp"
            sc:scc_stroke_color="#ABCDED"
            sc:scc_stroke_size="6dp"
            sc:scc_top="0"
            sc:scc_pointer_radius="10dp"
            sc:scc_pointer_colors="#3589F6"
            sc:scc_path_touchable="true" />

    </LinearLayout>
```

```java
    // Find the components
    final ScLinearGauge gauge = (ScLinearGauge) this.findViewById(R.id.line);
    assert gauge != null;

    final TextView counter = (TextView) this.findViewById(R.id.counter);
    assert counter != null;

    // Set the value
    gauge.setHighValue(75);

    // Each time I will change the value I must write it inside the counter text.
    gauge.setOnEventListener(new ScGauge.OnEventListener() {
        @Override
        public void onValueChange(float lowValue, float highValue) {
            counter.setText("Value: " + (int) highValue + "%");
        }
    });
```
<br />
<br />

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