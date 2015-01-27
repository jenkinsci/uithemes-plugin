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
package org.jenkinsci.plugins.uithemes.contributions;

import org.jenkinsci.plugins.uithemes.UIThemesPlugin;
import org.jenkinsci.plugins.uithemes.model.UIThemeContribution;
import org.jenkinsci.plugins.uithemes.model.UIThemeImplSpec;
import org.jenkinsci.plugins.uithemes.model.UIThemeImplSpecProperty;
import org.jenkinsci.plugins.uithemes.model.UIThemeSet;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class StatusBalls {

    public static void contribute(UIThemeSet uiThemeSet) {
        registerCSS3StatusBalls(uiThemeSet);
    }

    private static void registerCSS3StatusBalls(UIThemeSet themeSet) {
        // #1 ...
        themeSet.registerThemeImpl("status-balls", "css3-animated", "CSS3 Animated", "CSS3 Animated Status Balls/Orbs")
                .setUsageDetails("<strong>Note</strong> that CSS3 Animations can cause browser performance issues in some browser environments.")
                .setThemeImplSpec(
                        new UIThemeImplSpec()
                        .addProperty("successColor", new UIThemeImplSpecProperty()
                                .setTitle("Success Color")
                                .setDescription("Success Color Indicator")
                                .setType(UIThemeImplSpecProperty.Type.COLOR)
                                .setDefaultValue("28ca41")) // Lime Green
                        .addProperty("warningColor", new UIThemeImplSpecProperty()
                                .setTitle("Warning Color")
                                .setDescription("Warning Color Indicator")
                                .setType(UIThemeImplSpecProperty.Type.COLOR)
                                .setDefaultValue("fdc230")) // Golden / Orange
                        .addProperty("errorColor", new UIThemeImplSpecProperty()
                                .setTitle("Error Color")
                                .setDescription("Error Color Indicator")
                                .setType(UIThemeImplSpecProperty.Type.COLOR)
                                .setDefaultValue("fd6159")) // Tomato Red
                        .addProperty("notBuiltColor", new UIThemeImplSpecProperty()
                                .setTitle("Not Built Color")
                                .setDescription("Not Built Color Indicator")
                                .setType(UIThemeImplSpecProperty.Type.COLOR)
                                .setDefaultValue("808080"))  // Grey
                        .addProperty("disabledColor", new UIThemeImplSpecProperty()
                                .setTitle("Disabled Color")
                                .setDescription("Disabled Color Indicator")
                                .setType(UIThemeImplSpecProperty.Type.COLOR)
                                .setDefaultValue("808080"))  // Grey
                        .addProperty("abortedColor", new UIThemeImplSpecProperty()
                                .setTitle("Aborted Color")
                                .setDescription("Aborted Color Indicator")
                                .setType(UIThemeImplSpecProperty.Type.COLOR)
                                .setDefaultValue("808080")) // Grey
                );

        // #2 ...
        themeSet.contribute(new UIThemeContribution("css3-animated-status-balls", "status-balls", "css3-animated", UIThemesPlugin.class));

        // #3 ...
        // see plugin/src/main/resources/jenkins-themes/status-balls/css3-animated/css3-animated-status-balls/theme-template.less
    }
}
