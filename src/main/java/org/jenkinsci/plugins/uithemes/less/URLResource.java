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

import hudson.PluginWrapper;
import org.jenkinsci.plugins.uithemes.UIThemesPlugin;
import org.lesscss.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class URLResource implements Resource {

    private static final Logger LOGGER = Logger.getLogger(URLResource.class.getName());

    /**
     * Jenkins core variables LESS resource alias.  Core variables allow us to centrally
     * define color variables (and other stuff) that can then be used by core Jenkins and
     * plugins.
     */
    public static final String VARIABLES_ALIAS = "/core/variables.less";

    private final URI resConfigURI;
    private URL resClasspathURL;
    private URLResource coreVariables;
    private UIThemesPlugin themesPlugin;

    public URLResource(URL resClasspathURL) {
        this.resClasspathURL = resClasspathURL;
        if (resClasspathURL != null) {
            try {
                this.resConfigURI = resClasspathURL.toURI();
            } catch (URISyntaxException e) {
                throw new IllegalStateException(String.format("Invalid LESS classpath resource '%s'.", resClasspathURL.toString()), e);
            }
        } else {
            this.resConfigURI = null;
        }
    }

    public URLResource(String resPath) {
        try {
            this.resConfigURI = new URI(resPath);
            if (this.resConfigURI.isAbsolute()) {
                this.resClasspathURL = getPluginResoureURL(resPath);
                if (resClasspathURL == null) {
                    this.resClasspathURL = this.resConfigURI.toURL();
                }
            } else {
                this.resClasspathURL = URLResource.class.getResource(resPath);
            }
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Invalid LESS classpath resource '%s'.", resPath), e);
        }
    }

    public URLResource setCoreVariables(URLResource coreVariables) {
        this.coreVariables = coreVariables;
        return this;
    }

    public URLResource setThemesPlugin(UIThemesPlugin plugin) {
        this.themesPlugin = plugin;
        return this;
    }

    public URI getResConfigURI() {
        return resConfigURI;
    }

    @Override
    public boolean exists() {
        return exists(Level.WARNING);
    }

    public boolean exists(Level logLevel) {
        if (resClasspathURL != null) {
            try {
                InputStream stream = resClasspathURL.openStream();
                if (stream != null) {
                    try {
                        return true;
                    } finally {
                        stream.close();
                    }
                }
            } catch (IOException e) {
                // Fall through and fail...
            }
        }
        LOGGER.log(logLevel, String.format("LESS resource '%s' does not exist or is not accessible through the specified URL.", getName()));
        return false;
    }

    @Override
    public long lastModified() {
        // TODO: Work this out properly.  ATM it is always modified
        return System.currentTimeMillis();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (exists()) {
            return resClasspathURL.openStream();
        }
        return null;
    }

    @Override
    public URLResource createRelative(String relativeResourcePath) throws IOException {
        if (exists()) {
            if (coreVariables != null && URLResource.isCoreVariables(relativeResourcePath)) {
                return coreVariables;
            }

            // Try for a themed resource.
            URLResource themedResource = getThemedResource(relativeResourcePath);
            if (themedResource != null) {
                return themedResource;
            }

            // Might be a resource imported from a plugin e.g. "plugin:icon-shim:/less/mixins.less"
            URLResource pluginResource = getPluginResoure(relativeResourcePath);
            if (pluginResource != null) {
                return pluginResource;
            }

            String urlAsString = resClasspathURL.toString();
            int astrixIdx = urlAsString.lastIndexOf('!');

            // Need to do this because URI.resolve does not work for "jar:file:/xxx!resource" type URIs
            if (astrixIdx != -1) {
                String jar = urlAsString.substring(0, astrixIdx);
                String resInJar = urlAsString.substring(astrixIdx + 1);
                return new URLResource(new URL(jar + "!" + URI.create(resInJar).resolve(relativeResourcePath)))
                        .setThemesPlugin(themesPlugin)
                        .setCoreVariables(coreVariables);
            } else {
                return new URLResource(resConfigURI.resolve(relativeResourcePath).toString())
                        .setThemesPlugin(themesPlugin)
                        .setCoreVariables(coreVariables);
            }
        } else {
            return null;
        }
    }

    private URLResource getThemedResource(final String relativeResourcePath) {
        if (themesPlugin != null) {
            String themeVariableName = extractThemeVariableName(relativeResourcePath);

            if (themeVariableName != null) {
                String themeConfig = themesPlugin.getThemeVariables().getProperty("theme-" + themeVariableName);
                if (themeConfig != null) {
                    // Try for the named theme config LESS resource e.g. "classic-icons.less"
                    String namedThemeResource = relativeResourcePath.replace("#" + themeVariableName, themeConfig + "-" + themeVariableName);
                    try {
                        URLResource themedResource = createRelative(namedThemeResource);
                        if (themedResource.exists()) {
                            return themedResource;
                        }
                    } catch (IOException e) {
                    }

                    // Try for a default theme config LESS resource i.e. just the name of the theme e.g. "icons.less"
                    String defaultThemeResource = relativeResourcePath.replace("#" + themeVariableName, themeVariableName);
                    try {
                        URLResource themedResource = createRelative(defaultThemeResource);
                        if (themedResource.exists()) {
                            return themedResource;
                        }
                    } catch (IOException e) {
                    }
                }
            }
        }

        return null;
    }

    protected static String extractThemeVariableName(String relativeResourcePath) {
        File resource = new File(relativeResourcePath);
        String name = resource.getName();

        if (name.startsWith("#")) {
            name = name.substring(1);
            if (name.endsWith(".less")) {
                name = name.substring(0, ".less".length());
            }

            return name;
        }
        return null;
    }

    private URLResource getPluginResoure(final String pluginResource) {
        URL pluginResourceURL = getPluginResoureURL(pluginResource);

        if (pluginResourceURL != null) {
            return new URLResource(pluginResourceURL)
                    .setThemesPlugin(themesPlugin)
                    .setCoreVariables(coreVariables);
        }

        return null;
    }

    private URL getPluginResoureURL(final String pluginResource) {
        if (themesPlugin == null) {
            return null;
        }

        try {
            URI pluginResourceURI = new URI(pluginResource);

            if (pluginResourceURI.isAbsolute() && pluginResourceURI.getScheme().equalsIgnoreCase("plugin")) {
                pluginResourceURI = new URI(pluginResourceURI.getSchemeSpecificPart());
                String pluginName = pluginResourceURI.getScheme();

                PluginWrapper plugin = themesPlugin.getPluginManager().getPlugin(pluginName);
                if (plugin != null) {
                    String resourcePath = pluginResourceURI.getPath();
                    return themesPlugin.getPluginLESSResourceURL(plugin, resourcePath);

                } else {
                    LOGGER.log(Level.WARNING, String.format("@import '%s' is invalid.  Unknown plugin '%s'.", pluginResource, pluginName));
                }
            }
        } catch(Exception e) {
            LOGGER.log(Level.WARNING, "Error resolving plugin LESS resource.", e);
        }

        return null;
    }

    public static boolean isCoreVariables(String relativeResourcePath) {
        return VARIABLES_ALIAS.equals(relativeResourcePath);
    }

    @Override
    public String getName() {
        return resConfigURI.toString();
    }
}
