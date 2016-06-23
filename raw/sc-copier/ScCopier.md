# ScCopier

Create a feature that draw a line copy of the given path.
You can define the line characteristic by setting the inner painter

This class inherit all its properties from the [ScFeature](..\sc-feature\ScFeature.md) so please take a look to the related documentation.
The class is a basic class and not expose only one proprietary method and all the methods inherited from [ScFeature](..\sc-feature\ScFeature.md).

#### Public Methods

- **void setOnDrawListener(OnDrawListener listener)**<br />
Link the listener.


#### Interfaces

- **OnDrawListener**<br />
**void onBeforeDrawCopy(CopyInfo info)**<br />
Called before draw the path copy.
Note that changing the `info` properties you will change the copy drawing.
Properties list: `scale`, `offset`.


---
####### Let's play

<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-copier/1.jpg" align="right" />
Create a bezier line and colorize it.
```java
    // Dimensions
    int width = 300;
    int height = 200;
    int middleStroke = 6;

    // Get the main layout
    ImageView imageContainer = (ImageView) this.findViewById(R.id.image);
    assert imageContainer != null;

    // Create the path building a bezier curve from the left-top to the right-bottom angles of
    // the drawing area.
    Path path = new Path();
    path.moveTo(0, middleStroke);
    path.quadTo(width / 2, middleStroke, width / 2, height / 2);
    path.quadTo(width / 2, height - middleStroke, width, height - middleStroke);

    // Create a bitmap and link a canvas
    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    canvas.drawColor(Color.parseColor("#f5f5f5"));

    // Feature
    ScCopier copier = new ScCopier(path);
    copier.getPainter().setStrokeWidth(middleStroke * 2);
    copier.setColors(Color.RED, Color.GREEN, Color.BLUE);
    copier.setFillingColors(ScFeature.ShaderMode.SOLID);
    copier.draw(canvas);

    // Add the bitmap to the container
    imageContainer.setImageBitmap(bitmap);
```

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