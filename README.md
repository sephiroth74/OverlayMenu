# Overlay Menu
Android Overlay Menu

Demo:
==

[![Demo Video](http://img.youtube.com/vi/voFWb40sBJQ/0.jpg)](http://youtu.be/voFWb40sBJQ)

Installation:
==

Add this dependency to your gradle script:

    compile 'it.sephiroth.android.library.overlaymenu:overlay-menu:1.0'


Usage:
==

Add an instance of the OverlayView in your layout:

    <it.sephiroth.android.library.overlaymenu.OverMenuView
        android:entries="@array/overmenuEntries"
        android:id="@+id/overmenu"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
        
where the overmenuEntries array is defined in your arrays.xml like this:

    <string-array name="overmenuEntries">
        <item>First Item</item>
        <item>Second Item</item>
        <item>Third Item</item>
        <item>Fourth Item</item>
        <item>Fifth Item</item>
        <item>Sixth Item</item>
        <item>Seventh Item</item>
    </string-array>
    
    
Then in your activity:

    overMenuView = (OverMenuView) findViewById(R.id.overmenu);
    overMenuView.setOnSelectionChangedListener(this);
    overMenuView.setOnMenuVisibilityChangeListener(this);
    
    
    @Override
    public void onSelectionChanged(final int position) {
        Log.d(TAG, "onSelectionChanged: " + position);
    }

    @Override
    public void onVisibilityChanged(final View view, final boolean visible) {
        Log.d(TAG, "onVisibilityChanged: " + view + ", " + visible);
    }
    
    
License:
==

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    