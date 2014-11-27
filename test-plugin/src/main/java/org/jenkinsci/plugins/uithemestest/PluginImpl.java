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
package org.jenkinsci.plugins.uithemestest;

import hudson.Plugin;
import org.jenkinsci.plugins.uithemes.UIThemeContributor;
import org.jenkinsci.plugins.uithemes.UIThemesProcessor;
import org.jenkinsci.plugins.uithemes.model.UIThemeContribution;
import org.jenkinsci.plugins.uithemes.model.UIThemeImplSpec;
import org.jenkinsci.plugins.uithemes.model.UIThemeImplSpecProperty;
import org.jenkinsci.plugins.uithemes.model.UIThemeSet;

import java.util.Properties;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class PluginImpl extends Plugin implements UIThemeContributor {

    @Override
    public void contribute(UIThemeSet themeSet) {
        // What we do in this code here would normally be spread out across multiple contributors and Jenkins core itself.

        // Register a few themes.
        themeSet.registerTheme("icon", "Icons", "The set of Icons used by Jenkins");
        themeSet.registerTheme("status-balls", "Status Balls/Orbs", "The set of Status Balls/Orbs used by Jenkins");
        themeSet.registerTheme("header", "Page Header", "The Jenkins page header styles");
        themeSet.registerTheme("console", "Console", "The Console/Terminal styles");

        // register some theme implementations
        themeSet.registerThemeImpl("icon", "default", "Default", "Classic Jenkins Icon Set");
        themeSet.registerThemeImpl("icon", "font-awesome", "Font Awesome", "<a href='http://fortawesome.github.io/Font-Awesome/'>Font Awesome</a> vector Icons");

        themeSet.registerThemeImpl("status-balls", "default", "Default")
                .setThemeImplSpec(
                        new UIThemeImplSpec()
                                .addProperty("size", new UIThemeImplSpecProperty().setTitle("Size").setDescription("Size of the ball").setType(UIThemeImplSpecProperty.Type.NUMBER).setDefaultValue("24"))
                                .addProperty("bgColor", new UIThemeImplSpecProperty().setTitle("Background Color").setType(UIThemeImplSpecProperty.Type.COLOR).setDefaultValue("CCC"))
                );
        themeSet.registerThemeImpl("status-balls",  "css3-animated", "CSS3 Animated")
                .setUsageDetails("<strong>Note</strong> that CSS3 Animations can cause performance issues in some browser environments.")
                .setThemeImplSpec(
                        new UIThemeImplSpec()
                                .addProperty("size", new UIThemeImplSpecProperty().setTitle("Size").setDescription("Size of the ball").setType(UIThemeImplSpecProperty.Type.NUMBER).setDefaultValue("24"))
                                .addProperty("bgColor", new UIThemeImplSpecProperty().setTitle("Background Color").setType(UIThemeImplSpecProperty.Type.COLOR).setDefaultValue("CCC"))
                );

        themeSet.registerThemeImpl("header", "default", "Default")
                .setThemeImplSpec(
                        new UIThemeImplSpec()
                                .addProperty("bgColor", new UIThemeImplSpecProperty().setTitle("Background Color").setType(UIThemeImplSpecProperty.Type.COLOR).setDefaultValue("CCC"))
                                .addProperty("visible", new UIThemeImplSpecProperty().setTitle("Logo Visible").setPermittedValues("visible", "hidden").setDefaultValue("visible"))
                                .addProperty("logo", new UIThemeImplSpecProperty().setDefaultValue("images/butler.png"))
                );
        themeSet.registerThemeImpl("console",       "default", "Default")
                .setThemeImplSpec(
                        new UIThemeImplSpec()
                                .addProperty("bgColor", new UIThemeImplSpecProperty().setType(UIThemeImplSpecProperty.Type.COLOR).setDefaultValue("AAA"))
                );

        // Contribute to the theme implementations.
        themeSet.contribute(new UIThemeContribution("default-icon-core", "icon", "default", PluginImpl.class)); // plugins might contribute more to this theme
        themeSet.contribute(new UIThemeContribution("font-awesome-icon-core", "icon", "font-awesome", PluginImpl.class)); // plugins might contribute more to this theme
        themeSet.contribute(new UIThemeContribution("default-status-balls-core", "status-balls", "default", PluginImpl.class));
        themeSet.contribute(new UIThemeContribution("css3-status-balls-core", "status-balls", "css3-animated", PluginImpl.class));
        themeSet.contribute(new UIThemeContribution("default-header-core", "header", "default", PluginImpl.class));
        themeSet.contribute(new UIThemeContribution("default-console-core", "console", "default", PluginImpl.class));
    }

    @Override
    public void postInitialize() throws Exception {
        UIThemesProcessor themesProcessor = UIThemesProcessor.getInstance();

        // Set up the environment variables for this Jenkins instance.
        Properties jenkinsEnv = new Properties();
        jenkinsEnv.setProperty("rootURL", ".."); // TODO: Jenkins.getInstance().getRootUrl() returns null ?? Probably needs the context of a request.
        UIThemesProcessor.createJenkinsEnvVariablesLESSFile(jenkinsEnv);

        // Delete all user theme pre-generated data, forcing a refresh.
        themesProcessor.deleteAllUserThemes();

        // Add the core contributors.
        themesProcessor.addContributor(this);
    }
}
