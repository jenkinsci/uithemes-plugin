#Jenkins UI Themes Plugin

This Plugin allows Jenkins Core and other Plugins (that depend on it) to define modularised CSS via
[LESS](http://lesscss.org/) definition files.

## Icon Themes Example
The [icon-shim-plugin](https://github.com/jenkinsci/icon-shim-plugin) is an example of a plugin that uses the UI Themes Plugin.
Its main/master branch defines an icon theme for the "classic" Jenkins icons.  We also experimented with creating an icon
theme to use the Font Awesome icons.  That's a work in progress on the [font-awesome](https://github.com/jenkinsci/icon-shim-plugin/tree/font-awesome)
branch and the following is a screenshot where that theme is enabled.

![font-awesome-sample](https://raw.githubusercontent.com/jenkinsci/icon-shim-plugin/font-awesome/plugin/src/main/webapp/less/icons/font-awesome/font-awesome-sample.png)

TODO: Add more docs