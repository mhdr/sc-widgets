# ScNotchs
This components create a notchs arc inscribed inside a rectangle area.<br />
Considering that this component inherit from the [ScArc](ScArc) component please take a look to the related documentation before use it.


## ScNotchs class details
This class extend the [ScArc](ScArc) class.

> **KNOWED ISSUES**<br />
> When the arc is stretched have some visual issues about the notchs direction.<br />
> This problem will be fixed in the new version is possible.<br />
> About this and other any help will be **appreciated**.<br />

#### Public methods

- **setOnDrawListener(OnDrawListener listener)**<br />
Link to the draw listener.


#### Getter and Setter
- **get/setNotchs**  -> float value, default <code>0</code><br />
The number of sector where the arc will be divided.
Note that if the arc is NOT closed you will see one more notch that will represent the starting one.
- **get/setNotchsLength**  -> float value, default is double of stroke size<br />
The notchs line length.
- **get/setNotchType**  -> NotchTypes value, default <code>NotchTypes.LINE</code><br />
Define the notch type: LINE, CIRCLE, CIRCLE_FILLED.


#### Interfaces

Changing the info values will be effect on to drive the draw on canvas.

```java
    public interface OnDrawListener {
       void onDrawNotch(NotchInfo info);
    }
```

---
####### XML using

Draw a circle as the right images

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scnotchs/1.jpg"> 
```xml
    <com.sccomponents.widgets.ScNotchs
        xmlns:sc="http://schemas.android.com/apk/res-auto"
        android:layout_width="200dp"
        android:layout_height="wrap_content"
        android:padding="10dp"
        sc:scc_notchs="10"
    />
```


####### XML Properties

Take a look to the [ScArc](ScArc) class documentation
```xml
    <declare-styleable name="ScComponents">
        ...
        ...
        <attr name="scc_notchs" format="integer" />
        <attr name="scc_notchs_length" format="dimension" />
    </declare-styleable>
```

## Let's play

### Custom coloring and notchs emphasis

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scnotchs/2.jpg"> 
```xml
    <com.sccomponents.widgets.ScNotchs
        xmlns:sc="http://schemas.android.com/apk/res-auto"
        android:id="@+id/notchs"
        android:layout_width="200dp"
        android:layout_height="wrap_content"
        android:background="#cccccc"
        android:padding="10dp"
        sc:scc_angle_start="-90"
        sc:scc_notchs="16"/>
```

<br />
<br />
<br />

```java
        final ScNotchs notchs = (ScNotchs) this.findViewById(R.id.notchs);
        assert notchs != null;
        notchs.setOnDrawListener(new ScNotchs.OnDrawListener() {
            @Override
            public void onDrawNotch(ScNotchs.NotchInfo info) {
                // ATTENTION!
                // In this exercise you will note that I never used the class setter to set the
                // stroke size, notchs length or the stroke color.
                // This because call the standard setter doing a component invalidate that
                // would call again this method going into an infinite loop.

                // Emphasis every 4 notchs
                if (info.index % 4 == 0) {
                    // Change the stroke size and the length
                    info.size *= 3;
                    info.length *= 3;
                }
                // Emphasis every 2 notchs
                else if (info.index % 2 == 0) {
                    // Change the stroke size and the length
                    info.size *= 2;
                    info.length *= 2;
                }

                // Change the color
                float fraction = (float) info.index / (float) notchs.getNotchs();
                info.color = (Integer) new ArgbEvaluator().evaluate(fraction, 0xffff0000, 0xff0000ff);
            }
        });
```

### Points and distance from border

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scnotchs/3.jpg"> 
```xml
    <com.sccomponents.widgets.ScNotchs
        xmlns:sc="http://schemas.android.com/apk/res-auto"
        android:id="@+id/notchs"
        android:layout_width="200dp"
        android:layout_height="wrap_content"
        android:background="#cccccc"
        sc:scc_notch_type="circle_filled"
        sc:scc_notchs="20"
        sc:scc_notchs_length="2dp"
        sc:scc_stroke_size="1dp" />
```

<br />
<br />

```java
        final ScNotchs notchs = (ScNotchs) this.findViewById(R.id.notchs);
        assert notchs != null;
        notchs.setOnDrawListener(new ScNotchs.OnDrawListener() {
            @Override
            public void onDrawNotch(ScNotchs.NotchInfo info) {
                // Adjust the distance from border
                float multiplier = (notchs.getWidth() / 2) / notchs.getNotchs();
                info.distanceFromBorder = (notchs.getNotchs() - info.index) * multiplier;

                // Adjust the dimension
                info.length = 1 + info.index / 5;
            }
        });
```

### Notchs position

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scnotchs/4.jpg"> 
```xml
    <com.sccomponents.widgets.ScNotchs
            xmlns:sc="http://schemas.android.com/apk/res-auto"
            android:id="@+id/notchs"
            android:layout_width="200dp"
            android:layout_height="200dp"
            android:background="#cccccc"
            android:padding="35dp"
            sc:scc_notchs="32" />
```

<br />
<br />
<br />
<br />

```java
        final ScNotchs notchs = (ScNotchs) this.findViewById(R.id.notchs);
        assert notchs != null;
        notchs.setOnDrawListener(new ScNotchs.OnDrawListener() {
            @Override
            public void onDrawNotch(ScNotchs.NotchInfo info) {
                // Adjust the length and the position
                info.length += info.index;
                info.distanceFromBorder = -info.length / 2;
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