# sc-widgets
This is a library of widgets.<br />
The 2.x version change completely the way to draw using the [ScDrawer](..\sc-drawer\ScDrawer.md) as base for create the [ScGauge](..\sc-gauge\ScGauge.md) and all classes inherited from it.
This using a path to follow and applying some features to draw extra on the path.
This way to think leaves a lot of freedom to the users to create particular components limited only by his imagination. 

> **IMPORTANT**
> The 2.x version it is NOT compatible with the previous versions.


- **[ScWidget](sc-widget\ScWidget.md)**<br />
The base class for all (or almost) widget components of this series.<br />
Contains just utility methods for facilitate the building of a component.

- **[ScSlidingPanel](ScSlidingPanel.md)**<br />
This is a very simple sliding panel manager.<br />
Respect to others components same this it follow the parent alignment.
Put the component inside a RelativeLayout, give an alignment and the panel slide from.


# Usage

via Gradle:
<br />
Add it in your root build.gradle at the end of repositories:
```java
allprojects {
	repositories {
		...
		maven { url "https://jitpack.io" }
	}
}
```

Add the dependency
```java
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
