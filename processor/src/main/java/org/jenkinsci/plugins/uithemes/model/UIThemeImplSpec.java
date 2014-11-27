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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UIThemeImplSpec {

    private static final Logger LOGGER = Logger.getLogger(UIThemeImplSpec.class.getName());

    public Map<String, UIThemeImplSpecProperty> properties = new LinkedHashMap<String, UIThemeImplSpecProperty>();

    public UIThemeImplSpec addProperty(String name, UIThemeImplSpecProperty property) {
        properties.put(name, property);
        return this;
    }

    public UIThemeImplSpecProperty getProperty(String name) {
        return properties.get(name);
    }

    public Map<String, String> getDefaultConfig() {
        Map<String, String> defaultConfig = new LinkedHashMap<String, String>();
        Set<Map.Entry<String, UIThemeImplSpecProperty>> entries = properties.entrySet();

        for (Map.Entry<String, UIThemeImplSpecProperty> entry : entries) {
            UIThemeImplSpecProperty specProperty = entry.getValue();
            String propertyName = entry.getKey();
            String defaultValue = specProperty.defaultValue;

            if (defaultValue == null) {
                if (specProperty.permittedValues != null && specProperty.permittedValues.length > 0) {
                    defaultValue = specProperty.permittedValues[0];
                } else {
                    if (specProperty.type == UIThemeImplSpecProperty.Type.NUMBER) {
                        defaultValue = "0";
                    } else if (specProperty.type == UIThemeImplSpecProperty.Type.COLOR) {
                        defaultValue = "000000";
                    } else {
                        defaultValue = "";
                    }
                }

                LOGGER.log(Level.WARNING, "UI Theme implementation property ''{0}'' is not configured with a default value. Defaulting to ''{1}''.", new String[] {propertyName, defaultValue});
            }

            defaultConfig.put(propertyName, defaultValue);
        }

        return defaultConfig;
    }
}
