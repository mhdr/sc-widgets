# ScNotchs
Create a feature that draw a series of notchs following the base path.

You can define the line characteristic by setting the inner painter.
Many other characteristics can be change before drawing the single notch linking the dedicated listener.

This class inherit all its properties from the [ScFeature](../sc-feature/ScFeature.md) so please take a look to the related documentation.
<br />
<br />

#### Public Methods

- **float snapToNotchs(float value)**<br />
Round the value near the closed notch.

- **void setDividePathInContours(boolean value)**<br />
By default the class will draw the n notchs on each contours that compose the current path.<br />
If settle on false the class will consider the path as a unique path.

- **void setOnDrawListener(OnDrawListener listener)**<br />
Link the listener.
<br />
<br />

#### Getter and Setter

- **get/setCount**  -> `int` value, default `0`<br />
Set or get the notchs count.

- **get/setLength**  -> `float` value, default `0`<br />
Set or get the notchs length.<br />
The value must be passed in pixel.

- **get/setType**  -> `NotchTypes` value, default `NotchTypes.LINE`<br />
Possibly values by enum: `LINE`, `CIRCLE`, `CIRCLE_FILLED`<br />
Set or get the notchs type

- **get/setPosition**  -> `NotchPositions` value, default `NotchPositions.MIDDLE`<br />
Possibly values by enum: `INSIDE`, `MIDDLE`, `OUTSIDE`<br />
Set or get the notchs alignment respect the path.
<br />
<br />

#### Interfaces

- **OnDrawListener**<br />
**void onBeforeDrawNotch(NotchInfo info)**<br />
Called before draw the single notch.<br />
Note that changing the `info` properties you will change the current notch drawing.<br />
NotchInfo properties list: `size`, `length`, `color`, `index`, `offset`, `distanceFromStart`, `visible`, `type`, `align`.
<br />
<br />

---
####### Let's play

- **Common examples xml**
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


<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-notchs/1.jpg" align="right" />
- **Create a bezier line path and the notchs on it.**
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


<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-notchs/2.jpg" align="right" />
- **Circle contour type**
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

<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-notchs/3.jpg" align="right" />
- **Circle filled type**
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


<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-notchs/4.jpg" align="right" />
- **Play with colors and notchs info structure**
```java
    ...
    // Create a line path
    Path path = new Path();
    path.moveTo(drawArea.left, drawArea.centerY());
    path.lineTo(drawArea.right, drawArea.centerY());

    // Feature
    ScNotchs notchs = new ScNotchs(path);
    notchs.getPainter().setStrokeWidth(4);
    notchs.setColors(Color.GREEN, Color.YELLOW, Color.RED);
    notchs.setCount(24);
    notchs.setLength(30);
    notchs.setOnDrawListener(new ScNotchs.OnDrawListener() {
        @Override
        public void onBeforeDrawNotch(ScNotchs.NotchInfo info) {
            // Set the length and the position
            float ratio = info.index / info.source.getCount();
            info.length = info.source.getLength() * ratio + 10;
            info.offset = - (info.length / 2) * (1 - ratio);
            info.align = info.index % 2 == 0 ?
                ScNotchs.NotchPositions.INSIDE: ScNotchs.NotchPositions.OUTSIDE;
        }
    });
    notchs.draw(canvas);
    ...
```


<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-notchs/5.jpg" align="right" />
- **Complex path and play with the points distance from path start**
```java
    // Dimensions
    int padding = 24;
    Rect drawArea = new Rect(padding, padding, 500 - padding, 500 - padding);

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

    // Create a whirlpool
    Path path = new Path();
    path.moveTo(drawArea.centerX(), drawArea.centerY());

    for (int degrees = 0; degrees < 360 * 2; degrees ++) {
        float radiant = (float) Math.toRadians(degrees);
        float multiplier = degrees / 4;
        path.lineTo(
                drawArea.centerX() + (float) Math.cos(radiant) * multiplier,
                drawArea.centerY() + (float) Math.sin(radiant) * multiplier
        );
    }

    // Feature
    ScNotchs notchs = new ScNotchs(path);
    notchs.setCount(30);
    notchs.setType(ScNotchs.NotchTypes.CIRCLE_FILLED);
    notchs.setOnDrawListener(new ScNotchs.OnDrawListener() {
        @Override
        public void onBeforeDrawNotch(ScNotchs.NotchInfo info) {
            // Set the length
            info.length = info.index;
            // Calculate a new distance and the relative point on the path
            float distance = info.distance * (info.index / info.source.getCount());
            info.point = info.source.getPoint(distance);
        }
    });
    notchs.draw(canvas);

    // Add the bitmap to the container
    imageContainer.setImageBitmap(bitmap);
```


<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-copier/6.jpg" align="right" />
- **Multiple contours path managing**
```java
    ...
    // Create path with two contours
    Path path = new Path();
    path.moveTo(drawArea.left, drawArea.bottom);
    path.quadTo(drawArea.left, drawArea.top, drawArea.centerX(), drawArea.top);

    path.moveTo(drawArea.right, drawArea.top);
    path.quadTo(drawArea.right, drawArea.bottom, drawArea.centerX(), drawArea.bottom);

    // Feature
    ScNotchs notchs = new ScNotchs(path);
    notchs.setColors(Color.LTGRAY, Color.BLACK);
    notchs.getPainter().setStrokeWidth(4);
    notchs.setCount(10);
    notchs.setPosition(ScNotchs.NotchPositions.INSIDE);
    notchs.setDividePathInContours(false);
    notchs.setOnDrawListener(new ScNotchs.OnDrawListener() {
        @Override
        public void onBeforeDrawNotch(ScNotchs.NotchInfo info) {
            info.length = 10 + info.index * 5;
            info.size = 3 + info.index;
        }
    });
    notchs.draw(canvas);
    ...
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