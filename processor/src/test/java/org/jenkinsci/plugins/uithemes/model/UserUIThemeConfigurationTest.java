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
package org.jenkinsci.plugins.uithemes.model;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UserUIThemeConfigurationTest {

    @Test
    public void test_getSelection() throws Exception {
        UserUIThemeConfiguration themeConfiguration = createConfig();

        Assert.assertEquals("font-awesome", themeConfiguration.getUserThemeSelection("icon"));
        Assert.assertEquals("css3-animated", themeConfiguration.getUserThemeSelection("status-balls"));
        Assert.assertEquals(null, themeConfiguration.getUserThemeSelection("blah"));
    }

    @Test
    public void test_readWrite() throws Exception {
        UserUIThemeConfiguration themeConfiguration = createConfig();

        File userHome = new File("./target/UserUIThemeConfiguration/userXyz");
        FileUtils.deleteQuietly(userHome);
        Assert.assertFalse(userHome.exists());

        // make sure it returns null if the config is not set to file
        Assert.assertNull(UserUIThemeConfiguration.fromUserHome(userHome));

        // store the config
        UserUIThemeConfiguration.toUserHome(userHome, themeConfiguration);

        // make sure we can get it back
        UserUIThemeConfiguration themeConfiguration2 = UserUIThemeConfiguration.fromUserHome(userHome);
        Assert.assertEquals(2, themeConfiguration2.userThemes.size());
        Assert.assertEquals("icon", themeConfiguration2.userThemes.get(0).themeName);
        Assert.assertEquals("font-awesome", themeConfiguration2.userThemes.get(0).implName);
        Assert.assertEquals("status-balls", themeConfiguration2.userThemes.get(1).themeName);
        Assert.assertEquals("css3-animated", themeConfiguration2.userThemes.get(1).implName);
    }

    private UserUIThemeConfiguration createConfig() {
        UserUIThemeConfiguration themeConfiguration = new UserUIThemeConfiguration();

        UserUIThemeConfiguration.UserUIThemeSelection iconSelection = new UserUIThemeConfiguration.UserUIThemeSelection();
        iconSelection.themeName = "icon";
        iconSelection.implName = "font-awesome";
        themeConfiguration.userThemes.add(iconSelection);

        UserUIThemeConfiguration.UserUIThemeSelection statusBallsSelection = new UserUIThemeConfiguration.UserUIThemeSelection();
        statusBallsSelection.themeName = "status-balls";
        statusBallsSelection.implName = "css3-animated";
        themeConfiguration.userThemes.add(statusBallsSelection);
        return themeConfiguration;
    }
}
