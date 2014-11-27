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

import hudson.model.User;
import org.jenkinsci.plugins.uithemes.TestUtil;
import org.jenkinsci.plugins.uithemes.model.UserUIThemeConfiguration;
import org.jenkinsci.plugins.uithemes.rest.model.StatusResponse;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collections;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UIThemesSelectionConfigTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void test_config_PUT_GET() throws Exception {
        // create a user
        User.get("tfennelly", true, Collections.emptyMap());

        StatusResponse response = TestUtil.getJSON("user/tfennelly/uithemes-rest/config", StatusResponse.class, jenkinsRule);
        UserUIThemeConfiguration userThemeConfig = response.dataTo(UserUIThemeConfiguration.class);

        // should be no configured themes at start
        Assert.assertEquals(0, userThemeConfig.userThemes.size());

        // configure some selections and save...
        userThemeConfig.addSelection("icon", "font-awesome");
        userThemeConfig.addSelection("console", "dark-terminal");
        response = TestUtil.putJSON("user/tfennelly/uithemes-rest/config", userThemeConfig, jenkinsRule);
        Assert.assertEquals("OK", response.status);

        // Get it again and check...
        response = TestUtil.getJSON("user/tfennelly/uithemes-rest/config", StatusResponse.class, jenkinsRule);
        userThemeConfig = response.dataTo(UserUIThemeConfiguration.class);
        Assert.assertEquals(2, userThemeConfig.userThemes.size());
        Assert.assertEquals("icon", userThemeConfig.userThemes.get(0).themeName);
        Assert.assertEquals("font-awesome", userThemeConfig.userThemes.get(0).implName);
        Assert.assertEquals("console", userThemeConfig.userThemes.get(1).themeName);
        Assert.assertEquals("dark-terminal", userThemeConfig.userThemes.get(1).implName);
    }

    @Test
    public void test_config_bad_request() throws Exception {
        // create a user
        User.get("tfennelly", true, Collections.emptyMap());

        // Send an invalid JSON payload...
        StatusResponse response = TestUtil.putJSON("user/tfennelly/uithemes-rest/config", "{\"someUnknownField\": []}", jenkinsRule);

        // Check that there's an error...
        Assert.assertEquals("ERROR", response.status);
        Assert.assertTrue(response.message.startsWith("Unrecognized field \"someUnknownField\""));
    }
}
