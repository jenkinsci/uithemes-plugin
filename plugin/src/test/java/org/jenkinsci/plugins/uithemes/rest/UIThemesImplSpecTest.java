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
import org.jenkinsci.plugins.uithemes.model.UIThemeImplSpec;
import org.jenkinsci.plugins.uithemes.model.UIThemeImplSpecProperty;
import org.jenkinsci.plugins.uithemes.rest.model.StatusResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UIThemesImplSpecTest extends AbstractUIThemesTest {

    // See AbstractUIThemesTest for setup

    @Before
    public void before() {
        addContributors();
    }

    @Test
    public void test_GET_theme_impl_spec_bad_params() throws Exception {
        // create a user
        User.get("tfennelly", true, Collections.emptyMap());

        // Missing parameters should result in errors
        StatusResponse response = TestUtil.getJSON("user/tfennelly/uithemes-rest/implspec", StatusResponse.class, jenkinsRule);
        Assert.assertEquals("ERROR", response.status);
        Assert.assertEquals("Request parameter 'theme-name' is required.", response.message);
        response = TestUtil.getJSON("user/tfennelly/uithemes-rest/implspec?theme-name=header", StatusResponse.class, jenkinsRule);
        Assert.assertEquals("ERROR", response.status);
        Assert.assertEquals("Request parameter 'theme-impl-name' is required.", response.message);

        // unknown theme name should cause an error
        response = TestUtil.getJSON("user/tfennelly/uithemes-rest/implspec?theme-name=XXX&theme-impl-name=lite", StatusResponse.class, jenkinsRule);
        Assert.assertEquals("ERROR", response.status);
        Assert.assertEquals("Unknown theme 'XXX'.", response.message);

        // unknown theme impl name should cause an error
        response = TestUtil.getJSON("user/tfennelly/uithemes-rest/implspec?theme-name=header&theme-impl-name=XXX", StatusResponse.class, jenkinsRule);
        Assert.assertEquals("ERROR", response.status);
        Assert.assertEquals("Unknown theme implementation 'XXX' on theme named 'header'.", response.message);

        // A theme impl that does not specify a spec should cause an error
        response = TestUtil.getJSON("user/tfennelly/uithemes-rest/implspec?theme-name=header&theme-impl-name=default", StatusResponse.class, jenkinsRule);
        Assert.assertEquals("ERROR", response.status);
        Assert.assertEquals("Theme implementation 'default:header' does not specify an implementation spec i.e. it is not configurable.", response.message);
    }

    @Test
    public void test_GET_theme_impl_spec() throws Exception {
        // create a user
        User.get("tfennelly", true, Collections.emptyMap());

        StatusResponse response = TestUtil.getJSON("user/tfennelly/uithemes-rest/implspec?theme-name=header&theme-impl-name=lite", StatusResponse.class, jenkinsRule);
        Assert.assertEquals("OK", response.status);
        UIThemeImplSpec implSpec = response.dataTo(UIThemeImplSpec.class);
        UIThemeImplSpecProperty bgColor = implSpec.properties.get("backgroundColor");
        Assert.assertNotNull(bgColor);
        Assert.assertEquals(UIThemeImplSpecProperty.Type.COLOR, bgColor.type);
        Assert.assertEquals("#FFF", bgColor.defaultValue);
    }

}
