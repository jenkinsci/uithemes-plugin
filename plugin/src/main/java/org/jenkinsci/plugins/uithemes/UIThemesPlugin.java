/*
 * The MIT License
 *
 * Copyright (c) 2013-2014, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.uithemes;

import hudson.Plugin;
import org.jenkins.ui.icon.Icon;
import org.jenkins.ui.icon.IconSet;
import org.jenkins.ui.icon.IconType;
import org.jenkinsci.plugins.uithemes.model.UIThemeContribution;
import org.jenkinsci.plugins.uithemes.model.UIThemeSet;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UIThemesPlugin extends Plugin implements UIThemeContributor {

    @Override
    public void postInitialize() throws Exception {
        // Register the icon definition to the IconSet
        IconSet.icons.addIcon(new Icon("icon-uithemes icon-sm", "uithemes/images/16x16/uithemes.png", Icon.ICON_SMALL_STYLE, IconType.PLUGIN).setUseCSSRendering(true));
        IconSet.icons.addIcon(new Icon("icon-uithemes icon-md", "uithemes/images/24x24/uithemes.png", Icon.ICON_MEDIUM_STYLE, IconType.PLUGIN).setUseCSSRendering(true));
        IconSet.icons.addIcon(new Icon("icon-uithemes icon-lg", "uithemes/images/32x32/uithemes.png", Icon.ICON_LARGE_STYLE, IconType.PLUGIN).setUseCSSRendering(true));
        IconSet.icons.addIcon(new Icon("icon-uithemes icon-xlg", "uithemes/images/48x48/uithemes.png", Icon.ICON_XLARGE_STYLE, IconType.PLUGIN).setUseCSSRendering(true));
    }

    @Override
    public void contribute(UIThemeSet themeSet) {
        // Contribute to the classic icon set.
        // See src/main/resources/jenkins-themes/icons/classic/classic-icons-uithemes/theme-template.less
        themeSet.contribute(new UIThemeContribution("classic-icons-uithemes", "icons", "classic", UIThemesPlugin.class));

        // Contribute to the font-awesome icon set.
        // See src/main/resources/jenkins-themes/icons/font-awesome-icons/font-awesome-uithemes/theme-template.less
        themeSet.contribute(new UIThemeContribution("font-awesome-uithemes", "icons", "font-awesome-icons", UIThemesPlugin.class));
    }
}
