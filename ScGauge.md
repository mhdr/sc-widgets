# ScGauge
A fusion of [ScArc](ScArc.md) and [ScNotchs](ScNotchs.md) components, this create a gauge with many possibilities of customize.<br />
The class has been designed to starting simple and user friendly but provides all needed functions to customize it in every parts.<br />

Follow in this page you will find some examples for better understand the potentiality of this component.<br />
But is simple to understand that you can use for create gauges, progress bars and, extending this class, seek bars ([ScSeekBar](ScSeekBar.md)).


## ScGauge class details
This class extend the [ScWidget](ScWidget.md) class.<br />
The component if create using of [ScArc](ScArc.md) and [ScNotchs](ScNotchs.md) components so please take a look about the documentation relative to each component.

The default configuration have:
- One [ScArc](ScArc.md) for the base arc
- One [ScNotchs](ScNotchs.md) for create the notchs
- One [ScArc](ScArc.md) for the progress arc

In this documentation will see that the configuration can be changed.<br />
Take in mind that this object consider the "progress" is always the last one.


#### Public methods

- **ScArc getBaseArc()**<br />
Get the base arc.

- **ScArc getNotchsArc()**<br />
Get the notchs arc.<br />
Note that the notchs will be returned ad a ScArc because in the advanced use of the gauge it could be an ScArc, so you need to cast it for use as ScNotchs.

- **ScArc getProgressArc()**<br />
Get the progress arc.

- **void setStrokesCap(Paint.Cap cap)**<br />
Set stroke cap of painter for all components inside the gauge.<br />
Default value is BUTT from the [ScArc](ScArc.md) settings.

- **void setCanvasFilling(ScArc.FillingArea area, ScArc.FillingMode mode)**<br />
The canvas filling setting for all components inside the gauge.<br />
For the filling possibilities please refer to the [ScArc](ScArc.md) documentation.

- **void show(boolean baseArc, boolean notchsArc, boolean progressArc)**<br />
Set the components visibility.

- **float translateAngleToValue(float angle, float startRange, float endRange)**<br />
Translate the angle in a value within the passed range of values.

- **Animator getValueAnimator()**<br />
Get the value animator.<br />
Note that the initial value duration of the animation is zero equal to "no animation".

- **void changeComponentsConfiguration(boolean baseArcToNotchs, boolean notchsArcToArc, boolean progressArcToNotchs)**<br />
Change the components configuration.<br />
This method is only for advanced use of ScGauge and use it improperly can be cause of component malfunction.<br />
Changing the component type mean create a new one and lost old information like visibility and cap stroke style.<br />
So, if you did some change about inner properties before call this method, you must remember to apply again these settings.

- **void setNotchsStyle(ScNotchs.NotchsTypes value)**<br />
Set the notchs style for all ScNotchs components inside this component.
For the style possibilities please refer to the [ScNotchs](ScNotchs.md) documentation.

- **void setDrawNotchsForLast(boolean value)**<br />
Draw the notchs for the last in the drawing method.<br />
The default sequence of drawing is base arc, notchs arc and progress arc for last.


#### Getter and Setter

- **get/setAngleStart**  -> float value, default <code>0</code><br />
The start angle in degrees.<br />
This method have effect on all the arcs inside the component.

- **get/setAngleSweep**  -> float value, default <code>360</code><br />
The sweep angle (in degrees) is the delta value between the start angle and the end angle.
This method have effect on all the arcs inside the component.

- **get/setStrokeSize**  -> float value, default <code>3dp</code><br />
The value must be passed in pixel.<br />
This method have on the base and the notchs component.

- **get/setStrokeColor**  -> int value, default <code>Color.BLACK</code><br />
The base stroke color.<br />
This method have on the base and the notchs component.

- **get/setProgressSize**  -> float value, default <code>1dp</code><br />
The value must be passed in pixel.

- **get/setProgressColor**  -> int value, default <code>Color.GRAY</code><br />
The base stroke color.

- **get/setNotchsSize**  -> float value, default <code>3dp</code><br />
The value must be passed in pixel.<br />
This method have on the notchs component.

- **get/setNotchsColor**  -> int value, default <code>Color.BLACK</code><br />
The notchs stroke color.<br />
This method have on the notchs component.

- **get/setNotchs**  -> float value, default <code>0</code><br />
The number of sector where the arc will be divided.<br />
This method have on all the notchs inside component.<br />
Note that if the arc is NOT closed you will see one more notch that will represent the starting one.

- **get/setNotchsLength**  -> float value, default is double of stroke size<br />
The notchs line length.

- **get/setSnapToNotchs**  -> boolean value, default <code>false</code><br />
Round the progress value to the near notch degrees.

- **get/setValue**  -> float value, default <code>0</code><br />
The current progress value.<br />
IMPORTANT! This getter/setter have a overload where you can pass a range of float values and the methods translate the reference value to the angle in degrees and call the base methods.



---
####### XML using

<img align="right" src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/1.jpg"> 
```xml
    <com.sccomponents.widgets.ScGauge
        xmlns:sc="http://schemas.android.com/apk/res-auto"
        android:id="@+id/notchs"
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:background="#cccccc"
        android:padding="10dp"
        sc:scc_angle_sweep="270"
        sc:scc_stroke_size="6dp"
        sc:scc_progress_size="3dp"
        sc:scc_value="135"
        sc:scc_notchs="12" />
```

####### XML Properties
```xml
    <declare-styleable name="ScComponents">
        <attr name="scc_angle_start" format="float" />
        <attr name="scc_angle_sweep" format="float" />
        <attr name="scc_stroke_size" format="dimension" />
        <attr name="scc_stroke_color" format="color" />
        <attr name="scc_progress_size" format="dimension" />
        <attr name="scc_progress_color" format="color" />
        <attr name="scc_value" format="float" />
        <attr name="scc_notchs_size" format="dimension" />
        <attr name="scc_notchs_color" format="color" />
        <attr name="scc_notchs" format="integer" />
        <attr name="scc_notchs_length" format="dimension" />
    </declare-styleable>
```


## Let's play

> **DON'T FORGET**<br />
> You can use the <code>getAnimator()</code> to animate the progress value.<br />
> Is enough to set the animation duration to a value over zero for see the value changing using an interpolator.<br />

> **FUNNY**<br />
> Can be funny to create new examples.<br />
> So if you created new one please send me the picture and the xml/java code and I'll insert in.


### [Simple and beauty](ScGauge_SimpleAndBeauty.md)
Press on above link to see the code application.

![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/2.jpg)
![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/3.jpg)
![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/4.jpg)
![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/5.jpg)


### [Notchs](ScGauge_Notchs.md)
Press on above link to see the code application.

![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/6.jpg)
![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/7.jpg)
![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/8.jpg)
![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/9.jpg)


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