# ScDrawer

This is a small and fundamental class to design the future components using the "path following" method.
The duty of this class is divided in two main: draw on view the path and provide to add some "features" to the drawing system.
Whereas the "features" are independent from this class and will be only added the draw system following some user defined setting important to understand.

There are two ways to draw an arc on this component.
- **DRAW**: will simply draw the arc on the component canvas using the proper methods.
- **STRETCH**: before draw in the basic mode (as above) and after stretch the canvas.
<br />
This methods of draw will stretch also the stroke to creating a good effect.
<br />

Also you can decide witch dimension want to fill: none, both dimensions, vertical or horizontal.
This for give to the user many combinations to render the arc on the drawing area.
<br />

For better understand the mode to use this options will propose you **some examples** at the end of this guide.
<br />

## ScDrawer class details
This class extend the [ScWidget](..\sc-widget\ScWidget.md) class.
This is an abstract class and cannot be instantiate directly but only inherited from another class.
Note that this class no have path properties exposed to modify it directly but you need to override the `createPath()` method.
If you decide to expose some property for manage the path you can use the **protected** property named `mPath`.

> **NOTE**
> In this version the class not implement any methods to auto-size the path within the drawing area.
> For example the area calculation not consider the width of the stroke so if the stroke size is noticeable and the component padding zero could be a clipping drawing.
> This can be simply solved playing with the component padding.


#### Public methods

- **void addFeature(ScFeature feature)**<br />
**ScFeature addFeature(Class<?> classRef)**<br />
Add one feature to this drawer.
The second overload instantiate a new object from the class reference passed.
The passed class reference must implement the ScFeature interface.

- **boolean removeFeature(ScFeature feature)**<br />
Remove a feature from this drawer.

- **void removeAllFeatures()**<br />
Remove all feature from this drawer.

- **List<ScFeature> findFeatures(Class<?> classRef, String tag)**<br />
Find all features that corresponds to a class and tag reference.
If the class reference is null the class will be not consider. 
Same behavior for the tag param.

- **Paint getPainter()**<br />
Get the arc painter.


#### Getter and Setter

- **get/setStrokeSize**  -> `float` value, default `3dp`<br />
The value must be passed in pixel.

- **get/setStrokeColor**  -> `int` value, default `Color.BLACK`<br />
The stroke color of painter.

- **get/setMaxWidth**  -> `int` value, default `Int.MAX_VALUE`<br />
The value must be passed in pixel.

- **get/setMaxHeight**  -> `int` value, default `Int.MAX_VALUE`<br />
The value must be passed in pixel.

- **get/setFillingArea**  -> `FillingArea` value, default `FillingArea.BOTH`<br />
Possibly values by enum: `NONE`, `BOTH`, `HORIZONTAL`, `VERTICAL`
This indicate what kind of dimension will filled.

- **get/setFillingMode**  -> `FillingMode` value, default `FillingMode.DRAW`<br />
Possibly values by enum: `DRAW`, `STRETCH`<br />
Please look above for a short explain of this feature.


---
####### XML Properties
```xml
    <declare-styleable name="ScComponents">
        <attr name="scc_stroke_size" format="dimension" />
        <attr name="scc_stroke_color" format="color" />
        <attr name="scc_fill_area" format="enum" />
        <attr name="scc_fill_mode" format="enum" />
        <attr name="scc_fill_colors" format="enum" />
        <attr name="scc_max_width" format="dimension" />
        <attr name="scc_max_height" format="dimension" />
    </declare-styleable>
```


####### Understanding the canvas and area filling

![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-drawer/1.jpg)
![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-drawer/2.jpg)

![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-drawer/3.jpg)
![image](https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-drawer/4.jpg)


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