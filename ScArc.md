# ScArc
This components create an arc inscribed inside a rectangle area.<br />
The arc can be build starting from any angle and the length of the arc will be defined through a sweep angle value.
All the angle can be positive or negative and you can decide the filling area methods by many options.
An important feature of this component is the possibility to use the <code>wrap_content</code> for define the layout response.

There are two ways to draw an arc on this component.
- **DRAW**: will simply draw the arc on the component canvas using the proper methods.
- **STRETCH**: before draw in the basic mode (as above) and after stretch the canvas.<br />
This methods of draw will stretch also the stroke to creating a good effect.

Also you can decide witch dimension want to fill: none, both dimensions, vertical or horizontal.<br />
This for give to the user many combinations to render the arc on the drawing area.<br />
**LOOK** some images example at the end of this page.


## ScArc class details
This class extend the [ScWidget](ScWidget.md) class.
By default the arc create a closed circle: from 0° to 360°.<br />
Note that all angle is usually expressed in degrees and almost methods need to have an delta angle relative to the start angle.

#### Static methods

- **float normalizeAngle(float degrees)**<br />
Normalize a angle in degrees.
If the angle is over 360° will be normalized.<br />
This method work for negative and positive angle values.

- **boolean pointInsideCircle(float x, float y, float radius)**<br />
Check if point is inside a circle (Pitagora) supposed that the origin of the circle is 0, 0.

- **Point getPointFromAngle(float degrees, RectF area)**<br />
Find a point on the circumference inscribed in the passed area rectangle.<br />
This angle is intended to be a global angle and if not subdue to any restriction.


#### Public methods

- **Paint getPainter()**<br />
Get the arc painter.

- **Point getPointFromAngle(float degrees, float radiusAdjust)**<br />
**Point getPointFromAngle(float degrees)**<br />
Calc point position from relative angle in degrees.<br />
Note that the angle must be relative to the start angle defined by the component settings and not intended as a global angle.

- **float getAngleFromPoint(float x, float y)**<br />
Find the angle from position on the component.<br />
This method consider the angles limits settings and return a relative angle value within this limits.

- **boolean belongsToArc(float x, float y, float precision)**<br />
**boolean belongsToArc(float x, float y)**<br />
Check if a point belongs to the arc.

- **int getCurrentGradientColor()**<br />
**int getCurrentGradientColor(float angle)**<br />
Get the current gradient color by the current draw angle or a reference one.

#### Getter and Setter

- **get/setAngleStart**  -> float value, default <code>0</code><br />
The start angle in degrees.

- **get/setAngleSweep**  -> float value, default <code>360</code><br />
The sweep angle (in degrees) is the delta value between the start angle and the end angle.

- **get/setAngleDraw**  -> float value, default <code>360</code><br />
This angle (in degrees) is the really angle used to draw but when equal the sweep angle they stay sync.<br />
This property is useful to build a class that inherit this or play with the canvas space.<br />
Use this properties is same a drawing clip region for the arc.

- **get/setStrokeSize**  -> float value, default <code>3dp</code><br />
The value must be passed in pixel.

- **get/setStrokeColor**  -> int value, default <code>Color.BLACK</code><br />
Set the solid color of the stroke.

- **get/setStrokeColors**  -> int[] value, default <code>null</code><br />
Create a gradient color and apply it to the stroke.

- **get/setMaxWidth**  -> int value, default <code>Int.MAX_VALUE</code><br />
The value must be passed in pixel.

- **get/setMaxHeight**  -> int value, default <code>Int.MAX_VALUE</code><br />
The value must be passed in pixel.

- **get/setFillingArea**  -> FillingArea value, default <code>FillingArea.BOTH</code><br />
Possibly values by enum: NONE, BOTH, HORIZONTAL, VERTICAL

- **get/setFillingMode**  -> FillingMode value, default <code>FillingMode.DRAW</code><br />
Possibly values by enum: DRAW, STRETCH<br />
Please look above for a short explain of this feature.


---
####### XML using

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scarc/5.jpg"> 
```xml
    <com.sccomponents.widgets.ScArc
        android:layout_width="200dp"
        android:layout_height="wrap_content"
        android:padding="10dp"
    />
```

####### XML Properties
```xml
    <declare-styleable name="ScComponents">
        <attr name="scc_angle_start" format="float" />
        <attr name="scc_angle_sweep" format="float" />
        <attr name="scc_angle_draw" format="float" />
        <attr name="scc_stroke_size" format="dimension" />
        <attr name="scc_stroke_color" format="color" />
        <attr name="scc_fill_area" format="enum" />
        <attr name="scc_fill_mode" format="enum" />
        <attr name="scc_max_width" format="dimension" />
        <attr name="scc_max_height" format="dimension" />
    </declare-styleable>
```

####### Example images

![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/scarc/1.jpg)
![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/scarc/2.jpg)

![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/scarc/3.jpg)
![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/scarc/4.jpg)


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