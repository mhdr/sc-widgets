# ScNotchs
This components create a notchs arc inscribed inside a rectangle area.<br />
Considering that this component inherit from the [ScArc](ScArc) component please take a look to the related documentation before use it.


## ScNotchs class details
This class extend the [ScArc](ScArc) class.

> **KNOWED ISSUES**
> When the arc is stretched have some visual issues about the notchs direction.
> This problem will be fixed in the new version is possible.
> About this and other any help will be **appreciated**.

#### Public methods

- **setOnDrawListener(OnDrawListener listener)**<br />
Link to the draw listener.


#### Getter and Setter
- **get/setNotchs**  -> float value, default <code>0</code><br />
The number of sector where the arc will be divided.
Note that if the arc is NOT closed you will see one more notch that will represent the starting one.
- **get/setNotchsLength**  -> float value, default is double of stroke size<br />
The notchs line length.


#### Interfaces

```java
    public interface OnDrawListener {
        // Return the new length of notch
        float onDrawNotch(Paint painter, float angle, int count);
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

```java
        final ScNotchs notchs = (ScNotchs) this.findViewById(R.id.notchs);
        assert notchs != null;
        notchs.setOnDrawListener(new ScNotchs.OnDrawListener() {
            @Override
            public float onDrawNotch(Paint painter, float angle, int count) {
                // ATTENTION!
                // In this exercise you will note that I never used the class setter to set the
                // stroke size, notchs length or the stroke color.
                // This because call the standard setter doing a component invalidate that
                // would call again this method going into an infinite loop.

                // Hold the starting notch length
                float length = notchs.getNotchsLength();

                // Emphasis every 4 notchs
                if (count % 4 == 0) {
                    // Change the stroke size and the length
                    painter.setStrokeWidth(painter.getStrokeWidth() * 3);
                    length *= 3;
                }
                // Emphasis every 2 notchs
                else if (count % 2 == 0) {
                    // Change the stroke size and the length
                    painter.setStrokeWidth(painter.getStrokeWidth() * 2);
                    length *= 2;
                }
                // No emphasis
                else {
                    // Set the default stroke size and not change the length
                    painter.setStrokeWidth(notchs.dipToPixel(ScNotchs.DEFAULT_STROKE_SIZE));
                }

                // Change the color
                float fraction = (float) count / (float) notchs.getNotchs();
                int color = (Integer) new ArgbEvaluator().evaluate(fraction, 0xffff0000, 0xff0000ff);
                painter.setColor(color);

                // return the new length
                return length;
            }
        });
```

### Custom coloring and notchs emphasis

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scnotchs/3.jpg"> 
```xml
    <com.sccomponents.widgets.ScNotchs
        xmlns:sc="http://schemas.android.com/apk/res-auto"
        android:id="@+id/notchs"
        android:layout_width="200dp"
        android:layout_height="wrap_content"
        android:background="#cccccc"
        android:padding="10dp"
        sc:scc_angle_sweep="270"
        sc:scc_notchs="9"
        sc:scc_notchs_length="1dp"
        sc:scc_stroke_size="12dp"/>
```

```java
        ScNotchs notchs = (ScNotchs) this.findViewById(R.id.notchs);
        assert notchs != null;
        notchs.getPainter().setStrokeCap(Paint.Cap.ROUND);
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