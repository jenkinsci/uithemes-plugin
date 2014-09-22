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
package org.jenkinsci.plugins.uithemes;

import hudson.PluginManager;
import hudson.PluginWrapper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UIThemesPluginTest {

    private FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
    private ServletContext servletContext = Mockito.mock(ServletContext.class);
    private HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
    private HttpServletResponse httpServletResponse = Mockito.mock(HttpServletResponse.class);
    private FilterChain filterChain = Mockito.mock(FilterChain.class);

    @Test
    public void test_core_styles() throws Exception {
        MockedUIThemesPlugin uiThemesPlugin = new MockedUIThemesPlugin();

        uiThemesPlugin.postInitialize();

        // See UIThemesPlugin.doFilter ... it checks getPathInfo
        Mockito.when(httpServletRequest.getPathInfo()).thenReturn("/styles/core.css");

        uiThemesPlugin.filter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        Assert.assertEquals(1, uiThemesPlugin.responses.size());
        Assert.assertTrue(uiThemesPlugin.responses.get(0).endsWith("/*\n" +
                " * Pretending to be the core Jenkins style\n" +
                " */\n" +
                "#header {\n" +
                "  color: #eeeeee;\n" +
                "}"));
    }

    @Test
    public void test_plugin_styles() throws Exception {
        MockedUIThemesPlugin uiThemesPlugin = new MockedUIThemesPlugin();
        List<PluginWrapper> pluginList = uiThemesPlugin.pluginManager.getPlugins();

        // Add 3 dummy plugin wrappers.  See MockedUIThemesPlugin.getPluginLESSStyleResourceURL
        pluginList.add(Mockito.mock(PluginWrapper.class));
        pluginList.add(Mockito.mock(PluginWrapper.class));
        pluginList.add(Mockito.mock(PluginWrapper.class));
        pluginList.add(Mockito.mock(PluginWrapper.class));

        uiThemesPlugin.postInitialize();

        // See UIThemesPlugin.doFilter ... it checks getPathInfo
        Mockito.when(httpServletRequest.getPathInfo()).thenReturn("/styles/plugins.css");

        uiThemesPlugin.filter.doFilter(httpServletRequest, httpServletResponse, filterChain);

        Assert.assertEquals(1, uiThemesPlugin.responses.size());

        // There are 4 plugins.  First 3 have LESS files.  Last one does not.  Second
        // plugin's LESS is corrupt, so it should be ignored.
        Assert.assertTrue(uiThemesPlugin.responses.get(0).endsWith("/* ------------------------------------------------------------------------------\n" +
                "   Styles for plugin: Plugin 1\n" +
                "   ------------------------------------------------------------------------------ */\n" +
                "#header {\n" +
                "  color: #eeeeee;\n" +
                "}\n" +
                "#plugin1 {\n" +
                "  color: #111;\n" +
                "}\n" +
                "\n" +
                "\n" +
                "/* ------------------------------------------------------------------------------\n" +
                "   Styles for plugin: Plugin 2\n" +
                "   ------------------------------------------------------------------------------ */\n" +
                "       /* Error processing plugin styles - corrupt LESS definition. */\n" +
                "\n" +
                "/* ------------------------------------------------------------------------------\n" +
                "   Styles for plugin: Plugin 3\n" +
                "   ------------------------------------------------------------------------------ */\n" +
                "#header {\n" +
                "  color: #eeeeee;\n" +
                "}\n" +
                "#plugin3 {\n" +
                "  color: #333;\n" +
                "}"));
    }

    private class MockedUIThemesPlugin extends UIThemesPlugin {

        private List<String> responses = new ArrayList<String>();
        private PluginManager pluginManager = new PluginManager(null, new File("./")) {
            List<PluginWrapper> pluginList = new ArrayList<PluginWrapper>();
            @Override
            protected Collection<String> loadBundledPlugins() throws Exception {
                return null;
            }

            @Override
            public List<PluginWrapper> getPlugins() {
                return pluginList;
            }
        };

        @Override
        public PluginManager getPluginManager() {
            return pluginManager;
        }

        @Override
        protected void registerStylesFilter(UIThemesPlugin.StylesFilter filter) throws ServletException {

            Mockito.when(filterConfig.getServletContext()).thenReturn(servletContext);
            try {
                URL resource = UIThemesPluginTest.class.getResource("/css/style.less");
                Mockito.when(servletContext.getResource("/css/style.less")).thenReturn(resource);
            } catch (MalformedURLException e) {
                Assert.fail(e.getMessage());
            }

            filter.init(filterConfig);
        }

        @Override
        protected void writeCSSResponse(byte[] cssBytes, HttpServletResponse httpServletResponse) throws IOException {
            responses.add(new String(cssBytes, Charset.forName("UTF-8")).trim());
        }

        @Override
        public URL getPluginLESSStyleResourceURL(PluginWrapper plugin) throws MalformedURLException {
            List<PluginWrapper> pluginList = pluginManager.getPlugins();
            int index = pluginList.indexOf(plugin) + 1;

            Mockito.when(plugin.getDisplayName()).thenReturn("Plugin " + index);

            return MockedUIThemesPlugin.class.getResource(String.format("/less/plugin%d.less", index));
        }
    }
}
