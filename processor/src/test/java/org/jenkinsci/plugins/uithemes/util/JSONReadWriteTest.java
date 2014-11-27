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
package org.jenkinsci.plugins.uithemes.util;

import org.jenkinsci.plugins.uithemes.model.UserUIThemeConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.jenkinsci.plugins.uithemes.model.UserUIThemeConfiguration.UserUIThemeSelection;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class JSONReadWriteTest {

    @Test
    public void test_readWrite_string() throws Exception {
        UserUIThemeConfiguration themeConfiguration = new UserUIThemeConfiguration();

        UserUIThemeSelection iconSelection = new UserUIThemeSelection();
        iconSelection.themeName = "icon";
        iconSelection.implName = "font-awesome";
        themeConfiguration.userThemes.add(iconSelection);

        UserUIThemeSelection statusBallsSelection = new UserUIThemeSelection();
        statusBallsSelection.themeName = "status-balls";
        statusBallsSelection.implName = "css3-animated";
        themeConfiguration.userThemes.add(statusBallsSelection);

        File file = new File("./target/JSONReadWriteTest/useruiconfig.json");

        JSONReadWrite.toUTF8File(themeConfiguration, file);

        UserUIThemeConfiguration themeConfiguration2 = JSONReadWrite.fromUTF8File(file, UserUIThemeConfiguration.class);
        Assert.assertEquals(2, themeConfiguration2.userThemes.size());
        Assert.assertEquals("icon", themeConfiguration2.userThemes.get(0).themeName);
        Assert.assertEquals("font-awesome", themeConfiguration2.userThemes.get(0).implName);
        Assert.assertEquals("status-balls", themeConfiguration2.userThemes.get(1).themeName);
        Assert.assertEquals("css3-animated", themeConfiguration2.userThemes.get(1).implName);
    }
}
