<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/2.jpg" height="100px" />
<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/3.jpg" height="100px" />
<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/4.jpg" height="100px" />
<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/5.jpg" height="100px" />
<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/11.jpg" height="100px" />
<br />
<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/6.jpg" height="100px" />
<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/7.jpg" height="100px" />
<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/8.jpg" height="100px" />
<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/9.jpg" height="100px" />
<img src="https://github.com/Paroca72/sc-widgets/blob/master/raw/scgauge/10.jpg" height="100px" />

# ScWidgets
This is a library of widgets

- **[ScWidget](ScWidget.md)**<br />
The base class for all widget component of this series.<br />
Contains just utility methods for facilitate the building of a component.

- **[ScArc](ScArc.md)**<br />
This components create an arc inscribed inside a rectangle area.<br />
The arc can be build staring from any angle and the length of the arc will be defined with an sweep angle.
All the angle can be positive or negative and you can decide the filling area methods by many options.
Please press on the link above to see some picture example of the possibility of this component.

- **[ScNotchs](ScNotchs.md)**<br />
This components create a notchs arc inscribed inside a rectangle area.<br />
Considering that this component inherit from the [ScArc](ScArc.md) component please take a look to the related documentation before use it.

- **[ScGauge](ScGauge.md)**<br />
A fusion of [ScArc](ScArc.md) and [ScNotchs](ScNotchs.md) components, this create a gauge with many possibilities of customize.<br />
The class has been designed to starting simple and user friendly but provides all needed functions to customize it in every parts.

- **[ScSeekBar](ScSeekBar.md)**<br />
This class extend the [ScGauge](ScGauge.md) class add the possibility to input the progress value by touching on the component.<br />
Also create the pointer for slide the current value.<br />
Noted than this class class offer a infinite possibilities of customization for understand it better first I can suggest to take a look to the [ScGauge](ScGauge.md) documentation.

- **[ScSlidingPanel](ScSlidingPanel.md)**<br />
This is a very simple sliding panel manager.<br />
Respect to others components same this it follow the parent alignment.
Put the component inside a RelativeLayout, give an alignment and the panel slide from.


# Usage

via Gradle:
```java
android {
    ...
    buildTypes {
        ...
        repositories {
            ...
            maven { url "https://jitpack.io" }
        }
    }
}
...
dependencies {
    ...
    compile 'com.github.paroca72:sc-widgets:1.1.0'
}
```

#License
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
