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
import org.jenkinsci.plugins.uithemes.rest.model.StatusResponse;
import org.jenkinsci.plugins.uithemes.rest.model.UIThemeList;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UIThemesListThemesTest extends AbstractUIThemesTest {

    // See AbstractUIThemesTest for setup

    @Test
    public void test_GET_themes() throws Exception {
        // create a user
        User.get("tfennelly", true, Collections.emptyMap());

        // No themes registered...
        StatusResponse response = TestUtil.getJSON("user/tfennelly/uithemes-rest/themes", StatusResponse.class, jenkinsRule);
        Assert.assertEquals("OK", response.status);
        UIThemeList uiThemes = response.dataTo(UIThemeList.class);
        Assert.assertEquals(0, uiThemes.themes.size());
        Assert.assertEquals(0, uiThemes.themes.size());

        // register a few theme contributors + implementations
        addContributors();

        // fetch them again...
        response = TestUtil.getJSON("user/tfennelly/uithemes-rest/themes", StatusResponse.class, jenkinsRule);
        Assert.assertEquals("OK", response.status);
        uiThemes = response.dataTo(UIThemeList.class);

        // should be 3 themes
        Assert.assertEquals(3, uiThemes.themes.size());
        Assert.assertEquals("icon", uiThemes.themes.get(0).name);
        Assert.assertEquals("Icons", uiThemes.themes.get(0).title);
        Assert.assertEquals("default", uiThemes.themes.get(0).defaultImpl);
        Assert.assertEquals("[default, font-awesome]", uiThemes.themes.get(0).implementations.toString());
        Assert.assertEquals("status-balls", uiThemes.themes.get(1).name);
        Assert.assertEquals("Status Balls", uiThemes.themes.get(1).title);
        Assert.assertEquals("default", uiThemes.themes.get(1).defaultImpl);
        Assert.assertEquals("[default, doony-balls, css3-animated]", uiThemes.themes.get(1).implementations.toString());
        Assert.assertEquals("header", uiThemes.themes.get(2).name);
        Assert.assertEquals("Page Header", uiThemes.themes.get(2).title);
        Assert.assertEquals("default", uiThemes.themes.get(2).defaultImpl);
        Assert.assertEquals("[default, lite]", uiThemes.themes.get(2).implementations.toString());

        // The "lite" header theme should be configurable. See @Before method above.
        Assert.assertEquals(2, uiThemes.themes.get(2).implementations.size());
        Assert.assertEquals(false, uiThemes.themes.get(2).implementations.get(0).isConfigurable);
        Assert.assertEquals(true, uiThemes.themes.get(2).implementations.get(1).isConfigurable);
    }
}
