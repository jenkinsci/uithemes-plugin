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
package org.jenkinsci.plugins.uithemes.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jenkinsci.plugins.uithemes.model.UITheme;
import org.jenkinsci.plugins.uithemes.model.UIThemeImplementation;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UIThemeExt {

    public String name;
    public String title;
    public String description;
    public String defaultImpl;
    public List<Implementation> implementations = new ArrayList<Implementation>();

    public static UIThemeExt fromInternal(UITheme uiTheme) {
        UIThemeExt uiThemeExt = new UIThemeExt();
        uiThemeExt.name = uiTheme.getName();
        uiThemeExt.title = uiTheme.getTitle();
        uiThemeExt.description = uiTheme.getDescription();
        uiThemeExt.defaultImpl = uiTheme.getDefaultImpl().getName();

        for (String implName : uiTheme.getThemeImplNames()) {
            UIThemeImplementation impl = uiTheme.getImpl(implName);
            Implementation implExt = new Implementation();

            implExt.name = implName;
            implExt.title = impl.getTitle();
            implExt.description = impl.getDescription();
            implExt.usageDetails = impl.getUsageDetails();
            implExt.isConfigurable = (impl.getThemeImplSpec() != null);
            uiThemeExt.implementations.add(implExt);
        }

        return uiThemeExt;
    }

    public static class Implementation {
        public String name;
        public String title;
        public String description;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public String usageDetails;
        public boolean isConfigurable = false;

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Implementation that = (Implementation) o;
            if (name != null ? !name.equals(that.name) : that.name != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return name != null ? name.hashCode() : 0;
        }
    }
}
