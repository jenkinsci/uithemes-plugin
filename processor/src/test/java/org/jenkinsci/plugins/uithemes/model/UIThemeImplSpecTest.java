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

import org.jenkinsci.plugins.uithemes.model.UIThemeImplSpec;
import org.jenkinsci.plugins.uithemes.model.UIThemeImplSpecProperty;
import org.jenkinsci.plugins.uithemes.util.JSONReadWrite;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UIThemeImplSpecTest {

    @Test
    public void test_serialize() throws IOException {
        UIThemeImplSpec implSpec = createThemeImplSpec();

        String serlialized = JSONReadWrite.toString(implSpec);
        // System.out.println(serlialized);

        UIThemeImplSpec specOut = JSONReadWrite.fromString(serlialized, UIThemeImplSpec.class);
        // System.out.println(JSONReadWrite.toString(specOut));
        Assert.assertEquals(2, specOut.properties.size());
        Assert.assertEquals(UIThemeImplSpecProperty.Type.COLOR, specOut.properties.get("backgroundColor").type);
        Assert.assertEquals(UIThemeImplSpecProperty.Type.STRING, specOut.properties.get("visibility").type);
        Assert.assertEquals("[visible, hidden]", Arrays.asList(specOut.properties.get("visibility").permittedValues).toString());
    }

    @Test
    public void test_getDefaultConfig() throws IOException {
        UIThemeImplSpec implSpec = createThemeImplSpec();
        Map<String, String> defaultConfig;

        // Test with no defaults configured i.e. it will default the defaults :)
        defaultConfig = implSpec.getDefaultConfig();
        Assert.assertEquals("{backgroundColor=000000, visibility=visible}", defaultConfig.toString());

        // Set a default on the backgroundColor and test again
        implSpec.getProperty("backgroundColor").setDefaultValue("FFFFFF");
        defaultConfig = implSpec.getDefaultConfig();
        Assert.assertEquals("{backgroundColor=FFFFFF, visibility=visible}", defaultConfig.toString());
    }

    private UIThemeImplSpec createThemeImplSpec() {
        UIThemeImplSpec specIn = new UIThemeImplSpec();

        UIThemeImplSpecProperty backgroundColor = new UIThemeImplSpecProperty();
        backgroundColor.type = UIThemeImplSpecProperty.Type.COLOR;
        specIn.addProperty("backgroundColor", backgroundColor);

        UIThemeImplSpecProperty visibility = new UIThemeImplSpecProperty();
        visibility.permittedValues = new String[] {"visible", "hidden"};
        specIn.addProperty("visibility", visibility);
        return specIn;
    }
}
