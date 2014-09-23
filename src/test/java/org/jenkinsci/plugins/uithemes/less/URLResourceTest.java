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
package org.jenkinsci.plugins.uithemes.less;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class URLResourceTest {

    @Test
    public void test_exists() {
        URLResource classpathResource = new URLResource("/less/file1.less");
        Assert.assertTrue(classpathResource.exists());

        classpathResource = new URLResource("/less/xxx.less");
        Assert.assertFalse(classpathResource.exists());
    }

    @Test
    public void test_createRelative() throws IOException {
        URLResource classpathResource = new URLResource("/less/file1.less");
        URLResource file2 = classpathResource.createRelative("sub/file3.less");

        Assert.assertEquals("/less/sub/file3.less", file2.getResConfigURI().toString());
        Assert.assertTrue(file2.exists());
    }

    @Test
    public void test_createRelative_jar() throws IOException {
        URL stringClassResURL = URLResourceTest.class.getResource("/java/lang/String.class");
        URLResource classpathResource = new URLResource(stringClassResURL);
        URLResource integerClassRes = classpathResource.createRelative("Integer.class");

        Assert.assertTrue(integerClassRes.getResConfigURI().toString().endsWith("/java/lang/Integer.class"));
        Assert.assertTrue(integerClassRes.exists());
    }

    @Test
    public void test_extractThemeVariableName() {
        Assert.assertEquals(null, URLResource.extractThemeVariableName("./icons.less"));
        Assert.assertEquals("icons", URLResource.extractThemeVariableName("./#icons"));
        Assert.assertEquals("icons", URLResource.extractThemeVariableName("./#icons.less"));
    }
}
