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

import hudson.model.User;
import org.jenkinsci.plugins.uithemes.UIThemesProcessor;
import org.jenkinsci.plugins.uithemes.util.JSONReadWrite;
import org.jenkinsci.plugins.uithemes.util.JenkinsUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UserUIThemeConfiguration {

    public static final String FILE_THEME_USER_CONFIG = "user-theme-config.json";

    public List<UserUIThemeSelection> userThemes = new ArrayList<UserUIThemeSelection>();

    public static class UserUIThemeSelection {
        public String themeName;
        public String implName;
    }

    public String getUserThemeSelection(String themeName) {
        for (UserUIThemeSelection themeSelection : userThemes) {
            if (themeSelection.themeName.equals(themeName)) {
                return themeSelection.implName;
            }
        }
        return null;
    }

    public UserUIThemeConfiguration addSelection(String themeName, String themeImplName) {
        UserUIThemeSelection selection = new UserUIThemeSelection();
        selection.themeName = themeName;
        selection.implName = themeImplName;
        userThemes.add(selection);
        return this;
    }

    public static UserUIThemeConfiguration fromUserHome(File userHome) throws IOException {
        return JSONReadWrite.fromUTF8File(new File(UIThemesProcessor.getUserThemesDir(userHome), FILE_THEME_USER_CONFIG), UserUIThemeConfiguration.class);
    }

    public static UserUIThemeConfiguration fromUserHome(User user) throws IOException {
        return fromUserHome(JenkinsUtil.getJenkinsUserHome(user));
    }

    public static void toUserHome(File userHome, UserUIThemeConfiguration themeConfiguration) throws IOException {
        JSONReadWrite.toUTF8File(themeConfiguration, new File(UIThemesProcessor.getUserThemesDir(userHome), FILE_THEME_USER_CONFIG));
    }

    public static void toUserHome(User user, UserUIThemeConfiguration themeConfiguration) throws IOException {
        toUserHome(JenkinsUtil.getJenkinsUserHome(user), themeConfiguration);
    }
}
