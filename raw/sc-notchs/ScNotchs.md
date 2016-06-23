# ScCopier

Create a feature that draw a series of notchs following the base path.
You can define the line characteristic by setting the inner painter.
Many other characteristics can be change before drawing the single notch linking the dedicated listener.

This class inherit all its properties from the [ScFeature](..\sc-feature\ScFeature.md) so please take a look to the related documentation.


#### Public Methods

- **float snapToNotchs(float value)**<br />
Round the value near the closed notch.

- **void setOnDrawListener(OnDrawListener listener)**<br />
Link the listener.


#### Getter and Setter

- **get/setCount**  -> `int` value, default `0`<br />
Set or get the notchs count.

- **get/setLength**  -> `float` value, default `0`<br />
Set or get the notchs length.
The value must be passed in pixel.

- **get/setType**  -> `NotchTypes` value, default `NotchTypes.LINE`<br />
Possibly values by enum: `LINE`, `CIRCLE`, `CIRCLE_FILLED`<br />
Set or get the notchs type

- **get/setPosition**  -> `NotchPositions` value, default `NotchPositions.MIDDLE`<br />
Possibly values by enum: `INSIDE`, `MIDDLE`, `OUTSIDE`<br />
Set or get the notchs alignment respect the path.


#### Interfaces

- **OnDrawListener**<br />
**void onBeforeDrawNotch(NotchInfo info)**<br />
Called before draw the single notch.
Note that changing the `info` properties you will change the current notch drawing.
NotchInfo properties list: `size`, `length`, `color`, `index`, `offset`, `distanceFromStart`, `visible`, `type`, `align`.


---
####### Let's play

Common examples xml
```xml
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:sc="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center_vertical|center_horizontal"
    android:orientation="vertical">

    <ImageView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:id="@+id/image" />

</LinearLayout>
```

<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-copier/1.jpg" align="right" />
Create a bezier line path and the notchs on it.
```java
    // Dimensions
    int padding = 24;
    Rect drawArea = new Rect(padding, padding, 500 - padding, 300 - padding);

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
    ScNotchs notchs = new ScNotchs(path);
    notchs.getPainter().setStrokeWidth(4);
    notchs.setCount(20);
    notchs.setLength(14);
    notchs.draw(canvas);

    // Add the bitmap to the container
    imageContainer.setImageBitmap(bitmap);
```

<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-copier/2.jpg" align="right" />
Circle type
```java
    ...
    // Feature
    ScNotchs notchs = new ScNotchs(path);
    notchs.getPainter().setStrokeWidth(2);
    notchs.setCount(20);
    notchs.setLength(14);
    notchs.setType(ScNotchs.NotchTypes.CIRCLE);
    notchs.draw(canvas);
    ...
```

<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-copier/3.jpg" align="right" />
Circle type
```java
    ...
    // Feature
    ScNotchs notchs = new ScNotchs(path);
    notchs.setCount(20);
    notchs.setLength(14);
    notchs.setType(ScNotchs.NotchTypes.CIRCLE_FILLED);
    notchs.draw(canvas);
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