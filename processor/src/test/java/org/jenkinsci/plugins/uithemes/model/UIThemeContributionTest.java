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

import org.jenkinsci.plugins.uithemes.util.JenkinsUtil;
import org.junit.Assert;
import org.junit.Test;
import org.lesscss.Resource;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UIThemeContributionTest {

    @Test
    public void test_no_model_errors() throws NoSuchMethodException, IOException {
        JenkinsUtil.JenkinsUtilTestSetup.setup();
        UIThemeContribution themeContribution = new UIThemeContribution("contrib1", "themeA", "themeAImpl", UIThemeContributionTest.class) {
            @Override
            protected Map<String, String> getUserThemeImplConfig(File userHome) throws IOException {
                Map<String, String> map = new HashMap<String, String>();
                map.put("backgroudColor", "#FFF");
                return map;
            }
        };

        Resource lessResource = themeContribution.createUserLessResource(new File(JenkinsUtil.JENKINS_USER_HOME, "tfennelly"), null);
        Assert.assertTrue(lessResource.getName().endsWith("users/tfennelly/themes/themeA/themeAImpl/theme.less"));
        Assert.assertTrue(lessResource.exists());
    }

    @Test
    public void test_with_model_errors() throws NoSuchMethodException, IOException {
        JenkinsUtil.JenkinsUtilTestSetup.setup();
        UIThemeContribution themeContribution = new UIThemeContribution("contrib1", "themeA", "themeAImpl", UIThemeContributionTest.class) {
            @Override
            protected Map<String, String> getUserThemeImplConfig(File userHome) throws IOException {
                Map<String, String> map = new HashMap<String, String>();
                // don't add a 'backgroudColor' property, forcing a template failure
                return map;
            }
        };

        try {
            themeContribution.createUserLessResource(new File(JenkinsUtil.JENKINS_USER_HOME, "tfennelly"), null);
            Assert.fail("Expected IOException");
        } catch(IOException e) {
            String message = e.getMessage();
            //System.out.println(message);
            Assert.assertTrue(message.contains("> Template Contributor: org.jenkinsci.plugins.uithemes.model.UIThemeContributionTest"));
            Assert.assertTrue(message.contains("> Template: /jenkins-themes/themeA/themeAImpl/contrib1/theme-template.less"));
            Assert.assertTrue(message.contains("> Template Error Expression: ${backgroudColor} (Line 2, Column 20)"));
            Assert.assertTrue(message.contains("> Theme Implementation Config: {} !!EMPTY!!"));
        }
    }
}
