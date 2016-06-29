# ScGauge
Manage a generic gauge.
 
This class is studied to be an "helper class" to facilitate the user to create a gauge.
The path is generic and must be defined in a inherited class.

This class start with a standard configuration of features as follow:
- **One copier** (inherited from the [ScCopier](../sc-drawer/ScCopier.md))<br />
The base drawing following the path defined in the owner without changes. 
- **One notchs** manager (inherited from the [ScNotchs](../sc-notchs/ScNotchs.md))<br />
To create the _notchs_ that will following the path defined in the owner. 
- **One writer** manager (inherited from the [ScWriter](../sc-writer/ScWriter.md))<br />
To write _text_ tokens on the path defined in the owner.
- **One copier** (inherited from the [ScCopier](../sc-copier/ScCopier.md))<br />
To create the _progress_ effect. 
- **Two pointers** (inherited from the [ScPointer](../sc-pointer/ScPointer.md))<br />
In case of _input_ enabled can define the low and high value by the user interaction on the pointers.
 
In this class are exposed many methods to drive the most used properties from the code or directly from the XML.
To manage the features will recognized from the class type and its tag so changing, for example, the color of notchs you will change the color of all notchs tagged.
This is useful when you have a custom features configuration that use one more of feature per type. 
All the custom features added without a defined tag should be managed by the user by himself.

## ScGauge class details
This class extend the [ScDrawer](..\sc-drawer\ScDrawer.md) class.
This is an abstract class and cannot be instantiate directly but only inherited from another class.
Note that this class no have path properties exposed to modify it directly but you need to override the `createPath()` method.
If you decide to expose some property for manage the path you can use the **protected** property named `mPath`.


#### Constants

- **static final String BASE_IDENTIFIER**
- **static final String NOTCHS_IDENTIFIER**
- **static final String WRITER_IDENTIFIER**
- **static final String PROGRESS_IDENTIFIER**
- **static final String HIGH_POINTER_IDENTIFIER**
- **static final String LOW_POINTER_IDENTIFIER**


#### Public methods

- **Animator getHighValueAnimator()**<br />
Get the high value animator.
Note that the initial value duration of the animation is zero equal to "no animation".

- **Animator getLowValueAnimator()**<br />
Get the low value animator.
Note that the initial value duration of the animation is zero equal to "no animation".

- **void setOnEventListener(OnEventListener listener)**<br />
Link the listener.

- **void setOnDrawListener(OnDrawListener listener)**<br />
Link the listener.


#### Getter and Setter

- **get/setStrokeSize**  -> `float` value, default `3dp`<br />
The value must be passed in pixel.

- **get/setStrokeColor**  -> `int` value, default `Color.BLACK`<br />
The stroke color of painter.

- **get/setProgressSize**  -> `float` value, default `1dp`<br />
Define the stroke width of the progress.
The value must be passed in pixel.

- **get/setProgressColor**  -> `int` value, default `Color.BLACK`<br />
The color of progress stroke.

- **get/setHighValue**  -> `float` value, default `0`<br />
Set the current progress high value in percentage from the path start or respect a range of values.

- **get/setLowValue**  -> `float` value, default `0`<br />
Set the current progress low value in percentage from the path start or respect a range of values.

- **get/setNotchsSize**  -> `float` value, default `3dp`<br />
Define the notch stroke width.
The value must be passed in pixel.

- **get/setNotchsColor**  -> `int` value, default `Color.BLACK`<br />
The color of notchs stroke.

- **get/setNotchs**  -> `int` value, default `0`<br />
The number of the notchs.

- **get/setNotchsLength**  -> `float` value, default `0`<br />
Define the notchs line length.

- **get/setSnapToNotchs**  -> `boolean` value, default `false`<br />
Define if the progress values (low and high) will be rounded to the closed notch.

- **get/setTextTokens**  -> `String[]` value, default `null`<br />
Set the text token to write on the path.

- **get/setTextSize**  -> `float` value, default `16dp`<br />
Set the text size.
The value must be passed in pixel.

- **get/setTextColor**  -> `int` value, default `Color.BLACK`<br />
The color of text tokens.

- **get/setPointerRadius**  -> `float` value, default `0`<br />
The radius of the pointers.
The value must be passed in pixel.

- **get/setPointersColor**  -> `int` value, default `Color.BLACK`<br />
The pointers color.

- **get/setPointerHaloWidth**  -> `float` value, default `10dp`<br />
The pointers halo width.
The value must be passed in pixel.
Note that the halo will draw half out the pointer and half inside.


#### OnPathTouchListener

**OnEventListener**
- **void onValueChange(float lowValue, float highValue)**<br />
Called when the high or the low value changed.


**OnDrawListener**
- **void onBeforeDrawCopy(ScCopier.CopyInfo info)**<br />
Called before draw the path copy.

- **onBeforeDrawNotch(ScNotchs.NotchInfo info)**<br />
Called before draw the single notch.

- **onBeforeDrawPointer(ScPointer.PointerInfo info)**<br />
Called before draw the pointer.
If the method set the bitmap inside the info object the default drawing will be bypassed and the new bitmap will be draw on the canvas following the other setting.

- **onBeforeDrawToken(ScWriter.TokenInfo info)**<br />
Called before draw the single text token.


---
####### XML Properties
```xml
    <declare-styleable name="ScComponents">
        ...
        <attr name="scc_stroke_size" format="dimension" />
        <attr name="scc_stroke_color" format="color" />
        <attr name="scc_progress_size" format="dimension" />
        <attr name="scc_progress_color" format="color" />
        <attr name="scc_value" format="float" />
        <attr name="scc_notchs_size" format="dimension" />
        <attr name="scc_notchs_color" format="color" />
        <attr name="scc_notchs" format="integer" />
        <attr name="scc_notchs_length" format="float" />
        <attr name="scc_snap_to_notchs" format="boolean" />
        <attr name="scc_text_size" format="dimension" />
        <attr name="scc_text_color" format="color" />
        <attr name="scc_text_tokens" format="string" />
        <attr name="scc_pointer_radius" format="dimension" />
        <attr name="scc_pointer_color" format="color" />
        <attr name="scc_halo_size" format="dimension" />
    </declare-styleable>
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