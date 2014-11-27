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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UIThemesImplConfigTest extends AbstractUIThemesTest {

    // See AbstractUIThemesTest for setup

    @Before
    public void before() {
        addContributors();
    }

    @Test
    public void test_impl_config_bad_params() throws Exception {
        // create a user
        User.get("tfennelly", true, Collections.emptyMap());

        // Missing parameters should result in errors
        StatusResponse response = TestUtil.getJSON("user/tfennelly/uithemes-rest/implconfig", StatusResponse.class, jenkinsRule);
        Assert.assertEquals("ERROR", response.status);
        Assert.assertEquals("Request parameter 'theme-name' is required.", response.message);
        response = TestUtil.getJSON("user/tfennelly/uithemes-rest/implconfig?theme-name=header", StatusResponse.class, jenkinsRule);
        Assert.assertEquals("ERROR", response.status);
        Assert.assertEquals("Request parameter 'theme-impl-name' is required.", response.message);
    }

    @Test
    public void test_GET_PUT_impl_config() throws Exception {
        // create a user
        User.get("tfennelly", true, Collections.emptyMap());

        // First get on the config should return an empty config
        StatusResponse response = TestUtil.getJSON("user/tfennelly/uithemes-rest/implconfig?theme-name=header&theme-impl-name=lite", StatusResponse.class, jenkinsRule);
        Assert.assertEquals("OK", response.status);
        Map<String, String> implConfig = response.dataTo(Map.class);
        Assert.assertTrue(implConfig.isEmpty());

        // Lets create a config and save it...
        implConfig.put("backgroundColor", "#FFF");
        response = TestUtil.putJSON("user/tfennelly/uithemes-rest/implconfig?theme-name=header&theme-impl-name=lite", implConfig, jenkinsRule);
        Assert.assertEquals("OK", response.status);

        // Lets try get it back...
        response = TestUtil.getJSON("user/tfennelly/uithemes-rest/implconfig?theme-name=header&theme-impl-name=lite", StatusResponse.class, jenkinsRule);
        Assert.assertEquals("OK", response.status);
        implConfig = response.dataTo(Map.class);
        String bgColor = implConfig.get("backgroundColor");
        Assert.assertNotNull(bgColor);
        Assert.assertEquals("#FFF", bgColor);
    }

}
