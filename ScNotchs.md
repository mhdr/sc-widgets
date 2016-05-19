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

---
####### Examples

<table>
    <tr>
        <td>**CODE**</td>
        <td>**RESULT**</td>
    <tr>
</table>




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