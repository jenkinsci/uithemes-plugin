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

import hudson.Extension;
import hudson.model.Action;
import hudson.model.TransientUserActionFactory;
import hudson.model.User;
import org.jenkinsci.plugins.uithemes.UIThemesProcessor;
import org.jenkinsci.plugins.uithemes.model.UITheme;
import org.jenkinsci.plugins.uithemes.model.UIThemeImplSpec;
import org.jenkinsci.plugins.uithemes.model.UIThemeImplementation;
import org.jenkinsci.plugins.uithemes.model.UIThemeSet;
import org.jenkinsci.plugins.uithemes.model.UserUIThemeConfiguration;
import org.jenkinsci.plugins.uithemes.rest.model.StatusResponse;
import org.jenkinsci.plugins.uithemes.rest.model.UIThemeList;
import org.jenkinsci.plugins.uithemes.util.JSONReadWrite;
import org.jenkinsci.plugins.uithemes.util.JenkinsUtil;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Extension
public class UIThemesRestAPI extends TransientUserActionFactory implements Action {

    public static final String URL_BASE = "uithemes-rest";

    private User user;
    private File userHome;

    public String getUrlName() {
        return URL_BASE;
    }

    /**
     * Get the user's theme configuration i.e. selections etc.
     * @param req The HTTP request.
     * @return {@link UserUIThemeConfiguration} JSON serialized.
     */
    public final HttpResponse doConfig(StaplerRequest req) {
        String method = req.getMethod().toUpperCase();

        try {
            if (method.equals("GET")) {
                UserUIThemeConfiguration themeConfiguration = UserUIThemeConfiguration.fromUserHome(user);
                if (themeConfiguration == null) {
                    themeConfiguration = new UserUIThemeConfiguration();
                }
                return new JSONStaplerResponse(StatusResponse.OK(themeConfiguration));
            } else if (method.equals("PUT")) {
                UserUIThemeConfiguration themeConfiguration = JSONReadWrite.fromRequest(req, UserUIThemeConfiguration.class);
                UserUIThemeConfiguration.toUserHome(user, themeConfiguration);
                deleteUserThemeCSS(); // regenerate
                return new JSONStaplerResponse(StatusResponse.OK());
            } else {
                return new JSONStaplerResponse(StatusResponse.ERROR(String.format("Unsupported '%s' operation.", method)));
            }
        } catch (Exception e) {
            return new JSONStaplerResponse(StatusResponse.ERROR(e));
        }
    }

    /**
     * Get the set of available/installed Themes.
     * @param req The HTTP request.
     * @return {@link UIThemeSet} JSON serialized.
     */
    public final HttpResponse doThemes(StaplerRequest req) {
        String method = req.getMethod().toUpperCase();

        try {
            if (method.equals("GET")) {
                UIThemeSet themeSet = getUiThemeSet();

                return new JSONStaplerResponse(StatusResponse.OK(UIThemeList.fromInternal(themeSet)));
            } else {
                return new JSONStaplerResponse(StatusResponse.ERROR(String.format("Unsupported '%s' operation.", method)));
            }
        } catch (Exception e) {
            return new JSONStaplerResponse(StatusResponse.ERROR(e));
        }
    }

    /**
     * Get the {@link UIThemeImplSpec} for a named theme implementation.
     * @param req The HTTP request. Request must contain {@code theme-name} and {@code theme-impl-name} params.
     * @return {@link UIThemeImplSpec} JSON serialized.
     * @see #doImplconfig
     */
    public final HttpResponse doImplspec(StaplerRequest req) {
        String method = req.getMethod().toUpperCase();

        try {
            if (method.equals("GET")) {
                UIThemeImplementation impl;
                try {
                    impl = getThemeImpl(req);
                } catch (IllegalArgumentException e) {
                    return new JSONStaplerResponse(StatusResponse.ERROR(e.getMessage()));
                }

                UIThemeImplSpec themeImplSpec = impl.getThemeImplSpec();
                if (themeImplSpec == null) {
                    return new JSONStaplerResponse(StatusResponse.ERROR(String.format("Theme implementation '%s:%s' does not specify an implementation spec i.e. it is not configurable.", impl.getName(), impl.getThemeName())));
                }

                return new JSONStaplerResponse(StatusResponse.OK(themeImplSpec));
            } else {
                return new JSONStaplerResponse(StatusResponse.ERROR(String.format("Unsupported '%s' operation.", method)));
            }
        } catch (Exception e) {
            return new JSONStaplerResponse(StatusResponse.ERROR(e));
        }
    }

    /**
     * Get the user's configuration of a named theme implementation.
     * @param req The HTTP request. Request must contain {@code theme-name} and {@code theme-impl-name} params.
     * @return {@link java.util.Map} of name/value pairs, JSON serialized.
     * @see #doImplspec
     */
    public final HttpResponse doImplconfig(StaplerRequest req) {
        String method = req.getMethod().toUpperCase();

        try {
            ThemeImplNameRequestParamValues nameParams;
            try {
                nameParams = new ThemeImplNameRequestParamValues(req);
            } catch (IllegalArgumentException e) {
                return new JSONStaplerResponse(StatusResponse.ERROR(e.getMessage()));
            }

            File themeImplConfigFile = UIThemesProcessor.getUserThemeImplConfigFile(nameParams.themeName, nameParams.themeImplName, userHome);
            if (method.equals("GET")) {
                if (!themeImplConfigFile.exists()) {
                    // If the user has not configured the theme, get the anonymous user config as the default config.
                    themeImplConfigFile = UIThemesProcessor.getUserThemeImplConfigFile(nameParams.themeName, nameParams.themeImplName, JenkinsUtil.JENKINS_ANONYMOUS_USER_HOME);
                }

                if (themeImplConfigFile.exists()) {
                    Map config = JSONReadWrite.fromUTF8File(themeImplConfigFile, Map.class);
                    return new JSONStaplerResponse(StatusResponse.OK(config));
                } else {
                    return new JSONStaplerResponse(StatusResponse.OK(Collections.emptyMap()));
                }
            } else if (method.equals("PUT")) {
                if (!themeImplConfigFile.exists()) {
                    // make sure the theme impl is exists before saving
                    try {
                        getThemeImpl(req);
                    } catch (IllegalArgumentException e) {
                        return new JSONStaplerResponse(StatusResponse.ERROR(e.getMessage()));
                    }
                }
                // Read the new config form the request and store it.
                Map configToStore = JSONReadWrite.fromRequest(req, Map.class);
                JSONReadWrite.toUTF8File(configToStore, themeImplConfigFile);
                // regenerate
                if (userHome.equals(JenkinsUtil.JENKINS_ANONYMOUS_USER_HOME)) {
                    // The anonymous user theme configs are used as default configs for users that have
                    // not configured specific theme impls. For that reason, we delete all user theme configs
                    // if an anonymous theme impl config is made.
                    UIThemesProcessor.getInstance().deleteAllUserThemes();
                } else {
                    deleteUserThemeCSS();
                }
                return new JSONStaplerResponse(StatusResponse.OK());
            } else {
                return new JSONStaplerResponse(StatusResponse.ERROR(String.format("Unsupported '%s' operation.", method)));
            }
        } catch (Exception e) {
            return new JSONStaplerResponse(StatusResponse.ERROR(e));
        }
    }

    private void deleteUserThemeCSS() {
        UIThemesProcessor.getUserThemesCSSFile(userHome).delete();
    }

    private UIThemeImplementation getThemeImpl(StaplerRequest req) throws IllegalArgumentException {
        ThemeImplNameRequestParamValues nameParams = new ThemeImplNameRequestParamValues(req);

        UIThemeSet themeSet = getUiThemeSet();
        UITheme theme = themeSet.getTheme(nameParams.themeName);
        if (theme == null) {
            throw new IllegalArgumentException(String.format("Unknown theme '%s'.", nameParams.themeName));
        }
        UIThemeImplementation impl = theme.getImpl(nameParams.themeImplName);
        if (impl == null) {
            throw new IllegalArgumentException(String.format("Unknown theme implementation '%s' on theme named '%s'.", nameParams.themeImplName, nameParams.themeName));
        }

        return impl;
    }

    private UIThemeSet getUiThemeSet() {
        UIThemesProcessor themeProcessor = UIThemesProcessor.getInstance();
        return themeProcessor.getUiThemeSet();
    }

    @Override
    public Collection<? extends Action> createFor(User target) {
        user = target;
        userHome = JenkinsUtil.getJenkinsUserHome(user);
        return Collections.singletonList(this);
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    private class ThemeImplNameRequestParamValues {
        private String themeName;
        private String themeImplName;

        private ThemeImplNameRequestParamValues(StaplerRequest req) throws IllegalArgumentException {
            this.themeName = req.getParameter("theme-name");
            if (this.themeName == null) {
                throw new IllegalArgumentException("Request parameter 'theme-name' is required.");
            }
            this.themeImplName = req.getParameter("theme-impl-name");
            if (this.themeImplName == null) {
                throw new IllegalArgumentException("Request parameter 'theme-impl-name' is required.");
            }
        }
    }
}
