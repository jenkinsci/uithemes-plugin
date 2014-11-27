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
package org.jenkinsci.plugins.uithemes.rest;

import org.jenkinsci.plugins.uithemes.UIThemeContributor;
import org.jenkinsci.plugins.uithemes.UIThemesProcessor;
import org.jenkinsci.plugins.uithemes.model.UIThemeContribution;
import org.jenkinsci.plugins.uithemes.model.UIThemeImplSpec;
import org.jenkinsci.plugins.uithemes.model.UIThemeImplSpecProperty;
import org.jenkinsci.plugins.uithemes.model.UIThemeImplementation;
import org.jenkinsci.plugins.uithemes.model.UIThemeSet;
import org.jenkinsci.plugins.uithemes.util.JenkinsUtil;
import org.junit.Before;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class AbstractUIThemesTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    public MockUIThemeContributor icon_default;
    public MockUIThemeContributor icon_font_awesome;
    public MockUIThemeContributor status_balls_default;
    public MockUIThemeContributor status_balls_doony;
    public MockUIThemeContributor status_balls_css3;
    public MockUIThemeContributor header_default;
    public MockUIThemeContributor header_lite;

    @Before
    public void setupThemeImpls() throws NoSuchMethodException, IOException {
        JenkinsUtil.JenkinsUtilTestSetup.setup();

        icon_default = new MockUIThemeContributor("icon", "default");
        icon_font_awesome = new MockUIThemeContributor("icon", "font-awesome");
        status_balls_default = new MockUIThemeContributor("status-balls", "default");
        status_balls_doony = new MockUIThemeContributor("status-balls", "doony-balls");
        status_balls_css3 = new MockUIThemeContributor("status-balls", "css3-animated");
        header_default = new MockUIThemeContributor("header", "default");
        header_lite = new MockUIThemeContributor("header", "lite");

        UIThemesProcessor.getInstance().reset();

        header_lite.setThemeImplSpec(new UIThemeImplSpec());
        UIThemeImplSpecProperty backgroundColor = new UIThemeImplSpecProperty();
        backgroundColor.type = UIThemeImplSpecProperty.Type.COLOR;
        backgroundColor.defaultValue = "#FFF";
        header_lite.themeImplSpec.properties.put("backgroundColor", backgroundColor);
    }

    protected void addContributors() {
        UIThemesProcessor processor = UIThemesProcessor.getInstance();
        processor.addContributor(icon_default);
        processor.addContributor(icon_font_awesome);
        processor.addContributor(status_balls_default);
        processor.addContributor(status_balls_doony);
        processor.addContributor(status_balls_css3);
        processor.addContributor(header_default);
        processor.addContributor(header_lite);
    }

    public class MockUIThemeContributor implements UIThemeContributor {
        private UIThemeContribution contribution;
        private UIThemeImplSpec themeImplSpec;

        private MockUIThemeContributor(String themeName, String themeImplName) {
            this.contribution = new UIThemeContribution(themeName + "-" + themeImplName, themeName, themeImplName, MockUIThemeContributor.class);
        }
        private UIThemesListThemesTest.MockUIThemeContributor setThemeImplSpec(UIThemeImplSpec themeImplSpec) {
            this.themeImplSpec = themeImplSpec;
            return this;
        }
        @Override
        public void contribute(UIThemeSet themeSet) {
            if (themeSet.getThemeNames().isEmpty()) {
                themeSet.registerTheme(icon_default.contribution.getThemeName(), "Icons");
                themeSet.registerTheme(status_balls_default.contribution.getThemeName(), "Status Balls");
                themeSet.registerTheme(header_default.contribution.getThemeName(), "Page Header");
            }
            UIThemeImplementation impl = themeSet.registerThemeImpl(contribution.getThemeName(), contribution.getThemeImplName(), contribution.getThemeImplName());
            impl.setThemeImplSpec(themeImplSpec);
            themeSet.contribute(contribution);
        }
    }
}
