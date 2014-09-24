#Jenkins UI Themes Plugin

This Plugin allows Jenkins Core and other Plugins (that depend on it) to define modularised CSS via
[LESS](http://lesscss.org/) definition files.

The [JENKINS-24143](https://github.com/tfennelly/jenkins/tree/JENKINS-24143) branch of Jenkins Core is currently enabled to use this plugin.

## How it works
The plugin tries to create 2 in-memory stylesheets that it then serves on well know URLs:

1. {{jenkins-rootURL}}/styles/core.css: The main Jenkins Core "assembled" styles.  The plugin looks for and processes a '/css/style.less' in Jenkins Core (as well as looking for a '/css/variables.less').  The resulting CSS is served on this URL.
1. {{jenkins-rootURL}}/styles/plugins.css: The aggregated plugin styles i.e. browser loads all styles from a single url.  The plugin looks for and processes a '/less/style.less' on each installed Plugin.  The resulting CSS is served on this URL.

The Plugin installs a Servlet `Filter` (via `PluginServletFilter.addFilter()`) and uses that to serve the pre-processed CSS directly from memory.  This is not considered to be a security risk since it's just UI style rules.

### How it applies a "theme"
The idea is that, as well as defining modular styles for general styling, we can also define multiple versions of different "themes" using [LESS](http://lesscss.org/) definition files.  We allow each of these themes
to be configured through the plugin configuration page i.e. allow an admin (or maybe per login) to select which version of a given theme they would like to
use for the installation (not yet done - currently using System variables).  Changing the theme selections would reassemble the CSS, applying the selected
themes (via simple theme name variables).

Examples of themes would be:

1. Icon Set e.g. "classic" (default), "font-awesome"
1. Build History pane text overflow e.g. "show" (default), "hide-scroll", "hide-ellipsis"
1. Main Logo e.g. "Jenkins" (default), "XXXX"
1. Show/hide icons on task menu items (left menu) e.g. "show" (default), "hide"

At present, the themes are applied based on variables configured from System variables (course it would be better to have a Jenkins config page for this).  These
variables are then applied to LESS `@import` statements through an import translation scheme that translates the imports by substituting the configured value for
theme variables.  The plugin looks at all LESS `@import` statements.  If it sees a `#` prefix on the imported file name it treats the file name (without the .less suffix)
as being a theme variable name.  It looks for a configured theme variable with that name and, if it find one, translates the `@import` using the configured value for that
variable name and attempts to perform the resource import.

Lets take the icon theme and how it's configured in the `icon-shim-plugin`(see next section).  The icons LESS `@import` is as follows:

```
@import "./icons/#icons.less";
```

This plugin translates this import in a few steps:

1. Check is resource file name prefixed with `#`.
1. Extract variable name, which is `icons` (remove the `#` and `.less`).
1. Check theme variables for a variable named `theme-icons` (name is given a `theme-` prefix).
1. Replace the import's `#icons` token with the configured value of the `theme-icons` variable.

So if the `theme-icons` variable value is `classic` (the default), the above `@import` gets translated to `@import "./icons/classic-icons.less";`.
If the configured value is `font-awesome`, the `@import` gets translated to `@import "./icons/font-awesome-icons.less";`. See the
[icons folder](https://github.com/jenkinsci/icon-shim-plugin/tree/font-awesome/plugin/src/main/webapp/less/icons) in on the `font-awesome`
branch of the `icon-shim-plugin`.  The result of this translation scheme is that the plugin assembles the CSS from a different set of
LESS definition files, depending on the theme variable configurations.

## Icon Themes Example
The [icon-shim-plugin](https://github.com/jenkinsci/icon-shim-plugin) is an example of a plugin that uses the UI Themes Plugin.
Its main/master branch defines an icon theme for the "classic" Jenkins icons.  We also experimented with adding an icon
theme that uses the [Font Awesome icon set](http://fortawesome.github.io/).  That's a work in progress on the [font-awesome](https://github.com/jenkinsci/icon-shim-plugin/tree/font-awesome)
branch. The following screenshot shows that theme enabled on the [JENKINS-24143](https://github.com/tfennelly/jenkins/tree/JENKINS-24143) branch of Jenkins Core.

![font-awesome-sample](https://raw.githubusercontent.com/jenkinsci/icon-shim-plugin/font-awesome/plugin/src/main/webapp/less/icons/font-awesome/font-awesome-sample.png)

Note that this is still a WIP.  Among other things, the icon positioning looks a bit off.  To run this locally:

1. Checkout and build (`mvn clean install`) the [JENKINS-24143](https://github.com/tfennelly/jenkins/tree/JENKINS-24143) branch of Jenkins Core project.
1. Checkout and build (`mvn clean install`) the [font-awesome](https://github.com/jenkinsci/icon-shim-plugin/tree/font-awesome) branch of the Icon Shim Plugin project.
1. Change to the `plugin` directory of the Icon Shim Plugin project.
1. Execute `mvn hpi:run -Dtheme-icons=font-awesome`.
1. Got to http://localhost:8080/jenkins/ in your browser.

You'll notice that icons contributed by plugins are still the `classic` icons.  Plugins can contribute theme based icons in the same way as
the Icon Shim Plugin contributes the Jenkins Core set of icons i.e. by defining the appropriate LESS definition files and including
them using a `@import "./icons/#icons.less";` import (see above).