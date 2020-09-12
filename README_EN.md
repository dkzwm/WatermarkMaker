# WatermarkMaker
## English| [中文](README.md) 

A lightweight library for creating watermark images.

## Demo
Download [Demo.apk](https://raw.githubusercontent.com/dkzwm/WatermarkMaker/master/apk/demo.apk)
## Snapshot
<img src='snapshot.gif'></img>
## Installation
Add the following dependency to your build.gradle file:
```
dependencies {
    implementation 'me.dkzwm.graphics.wmm:core:0.0.1'
}
```
## How to use
```kotlin
    val maker = WatermarkMaker.Builder
        .create(this, ColorSize(300, 300))
        .beginRow()
        .newTextMark("Test").setTextColor(mTextColor).setTextSize(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                14F,
                resources.displayMetrics
            )
        ).setRowGravity(WatermarkMaker.RowGravity.CENTER).done()
        .newImageMark(R.mipmap.ic_launcher).setScale(0.2F).setDegree(0F).done()
        .drawBaseOnBaseline(false)
        .endRow()
        .setItemDegree(45F)
        .setItemGravity(WatermarkMaker.ItemGravity.CENTER)
        .setItemBackgroundImage(
            BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_round),
            100,
            45F,
            true
        )
        .setCanvasGravity(WatermarkMaker.CanvasGravity.LEFT_TOP)
        .setRepeat(WatermarkMaker.Repeat.REPEAT)
        .build()
    val bitmap = maker.make()
```

## License
	--------

    	Copyright (c) 2020 dkzwm

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
	SOFTWARE.

