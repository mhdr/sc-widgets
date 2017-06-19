# ScSlidingPanel
A useful sliding panel for android

![ScreenShot](https://github.com/Paroca72/sc-widgets/blob/master/raw/sc-slidingpanel/1.jpg)

#Documentation
This is a very simple sliding panel manager.<br />
Respect to others components same this it follow the parent alignment.
Put the component inside a RelativeLayout, give an alignment and the panel slide from.

> For example:<br />
> If the component have the alignParentBottom to true the panel sliding from bottom.


### XML properties
- **scc_layout**               -> reference value: default 0 (load the xml content from resources)
- **scc_start_open**           -> boolean value: default true
- **scc_toggle_ontouch**       -> boolean value: default true
- **scc_animate_alpha**        -> boolean value: default false
- **scc_animate_translation**  -> boolean value: default true
- **scc_duration**             -> int value: default 500 (the animation duration in milliseconds)
- **scc_handle_size**          -> dimension value: default 0
- **scc_offset**               -> dimension value: default 0

### Methods
- **get/setLayout**             -> integer value
- **get/setStartOpen**          -> boolean value
- **get/setToggleOnTouch**      -> boolean value
- **get/setAnimateAlpha**       -> boolean value
- **get/setAnimateTranslation** -> boolean value
- **get/setDuration**           -> integer value (milliseconds)
- **get/setHandleSize**         -> integer value (pixel)
- **close([boolean smooth])**   -> close the panel (default true)
- **open([boolean smooth])**    -> open the panel (default true)
- **toggle([boolean smooth])**  -> toggle the panel (default true)
- **isOpen()**                  -> return a boolean


### Listener
- setOnChangeListener
- setOnHandleDragListener


#Usage example

### From xml
```xml
<!-- Define the container -->
<RelativeLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <!-- Sliding from top -->
    <scapps.com.library.ScSlidingPanel
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_alignParentTop="true">

            <!-- HERE THE CONTENT -->
            <!-- or load by scsl_layout xml property -->
            <!-- or load by setLayout method -->

    </scapps.com.library.ScSlidingPanel>

</RelativeLayout>
```

### From code
```java
ScSlidingPanel scs = new ScSlidingPanel(this);
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
