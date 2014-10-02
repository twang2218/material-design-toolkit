
Material Design Toolkit
========================

[![Build Status](https://travis-ci.org/twang2218/material-design-toolkit.png?branch=master)](https://travis-ci.org/twang2218/material-design-toolkit)

Implementation of some building blocks of Material Design

Usage
------

### RenderScript

To use RenderScript, these 2 lines should be added to your project gradle file:

```groovy
android {
    ...
    defaultConfig {
        ...
        renderscriptTargetApi 19
        renderscriptSupportMode true
    }
}
```

Widgets
-------

 * Ripple
 * Shadow

Palette
--------

Color Palette from [Material Design guideline > Style > Color](http://www.google.com/design/spec/style/color.html)

![Color Palette](art/resource_color.png)