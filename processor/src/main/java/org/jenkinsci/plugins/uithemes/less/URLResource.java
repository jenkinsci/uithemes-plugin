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

import org.jenkinsci.plugins.uithemes.UIThemesProcessor;
import org.lesscss.FileResource;
import org.lesscss.Resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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

    public static final String CORE_LESS_PREFIX = "/jenkins-themes/";
    public static final String JENKINS_ENV_ALIAS = CORE_LESS_PREFIX + "env.less";
    public static final String VARIABLES_ALIAS = CORE_LESS_PREFIX + "core/variables.less";

    private URI resConfigURI;
    private URL baseURL;
    private URL resClasspathURL;
    private URLResource coreVariables;
    private UIThemesProcessor themesProcessor;

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
                this.resClasspathURL = getRelativeResourceURL(resPath);
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

    public URLResource setBaseURL(URL baseURL) {
        this.baseURL = baseURL;
        return this;
    }

    public URLResource setCoreVariables(URLResource coreVariables) {
        this.coreVariables = coreVariables;
        return this;
    }

    public URLResource setThemesProcessor(UIThemesProcessor plugin) {
        this.themesProcessor = plugin;
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
    public Resource createRelative(String relativeResourcePath) throws IOException {
        if (exists()) {
            if (relativeResourcePath.startsWith(JENKINS_ENV_ALIAS)) {
                return new FileResource(UIThemesProcessor.getJenkinsEnvVariablesFile());
            }

            if (relativeResourcePath.startsWith(CORE_LESS_PREFIX)) {
                URLResource classpathRes = getClasspathLESSResource(relativeResourcePath);
                if (classpathRes != null) {
                    return classpathRes;
                }
            }

            String urlAsString = resClasspathURL.toString();
            int astrixIdx = urlAsString.lastIndexOf('!');

            // Need to do this because URI.resolve does not work for "jar:file:/xxx!resource" type URIs
            if (astrixIdx != -1) {
                String jar = urlAsString.substring(0, astrixIdx);
                String resInJar = urlAsString.substring(astrixIdx + 1);
                return new URLResource(new URL(jar + "!" + URI.create(resInJar).resolve(relativeResourcePath)))
                        .setThemesProcessor(themesProcessor)
                        .setCoreVariables(coreVariables);
            } else {
                return new URLResource(resConfigURI.resolve(relativeResourcePath).toString())
                        .setBaseURL(baseURL)
                        .setThemesProcessor(themesProcessor)
                        .setCoreVariables(coreVariables);
            }
        } else {
            return null;
        }
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

    private URLResource getClasspathLESSResource(String resourcePath) {
        URL resUrl = getClass().getResource(resourcePath);
        if (resUrl == null) {
            return null;
        }
        return new URLResource(resUrl);
    }

    @Override
    public String getName() {
        return resConfigURI.toString();
    }

    @Override
    public String toString() {
        return getName();
    }

    public static URL getResourceURL(String resPath, URL baseResourceURL) throws MalformedURLException {
        if (baseResourceURL == null) {
            return new URL(resPath);
        }

        String baseURL = baseResourceURL.toString();

        if (baseURL.endsWith("/")) {
            baseURL = baseURL.substring(0, baseURL.length() - 1);
        }
        if (resPath.startsWith("/")) {
            resPath = resPath.substring(1);
        }

        return new URL(baseURL + "/" + resPath);
    }

    protected URL getRelativeResourceURL(final String resourcePath) {
        try {
            URI pluginResourceURI = new URI(resourcePath);

            if (pluginResourceURI.isAbsolute() && pluginResourceURI.getScheme().equalsIgnoreCase("plugin")) {
                pluginResourceURI = new URI(pluginResourceURI.getSchemeSpecificPart());

                return getResourceURL(pluginResourceURI.getPath(), baseURL);
            }
        } catch(Exception e) {
            LOGGER.log(Level.WARNING, "Error resolving plugin LESS resource.", e);
        }

        return null;
    }
}
