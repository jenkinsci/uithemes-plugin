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

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UIThemeImplSpecProperty {

    public String title;
    public String description;
    public Type type = Type.STRING;
    public String defaultValue;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String[] permittedValues;

    public static enum Type {
        STRING,
        NUMBER,
        COLOR
    }

    public String getTitle() {
        return title;
    }

    public UIThemeImplSpecProperty setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public UIThemeImplSpecProperty setDescription(String description) {
        this.description = description;
        return this;
    }

    public Type getType() {
        return type;
    }

    public UIThemeImplSpecProperty setType(Type type) {
        this.type = type;
        return this;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public UIThemeImplSpecProperty setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String[] zgetPermittedValues() {
        return permittedValues;
    }

    public UIThemeImplSpecProperty setPermittedValues(String... permittedValues) {
        this.permittedValues = permittedValues;
        return this;
    }
}
