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

import freemarker.template.Template;
import freemarker.template.TemplateException;
import hudson.Extension;
import hudson.Plugin;
import hudson.PluginManager;
import hudson.PluginWrapper;
import hudson.model.Action;
import hudson.model.RootAction;
import hudson.model.User;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.uithemes.jelly.CSSStaplerResponse;
import org.jenkinsci.plugins.uithemes.less.LESSProcessor;
import org.jenkinsci.plugins.uithemes.model.UITheme;
import org.jenkinsci.plugins.uithemes.model.UIThemeContribution;
import org.jenkinsci.plugins.uithemes.model.UIThemeImplementation;
import org.jenkinsci.plugins.uithemes.model.UIThemeSet;
import org.jenkinsci.plugins.uithemes.model.UserUIThemeConfiguration;
import org.jenkinsci.plugins.uithemes.util.JSONReadWrite;
import org.jenkinsci.plugins.uithemes.util.JenkinsUtil;
import org.jenkinsci.plugins.uithemes.util.TemplateUtil;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.StaplerRequest;
import org.lesscss.Resource;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Extension
public final class UIThemesProcessor implements RootAction {

    private static final Logger LOGGER = Logger.getLogger(UIThemesProcessor.class.getName());

    private static UIThemesProcessor jenkinsThemesProcessor;

    private LESSProcessor lessProcessor;
    private final List<UIThemeContributor> contributors = new ArrayList<UIThemeContributor>();
    private volatile UIThemeSet themeSet;

    public UIThemesProcessor() {
        try {
            lessProcessor = new LESSProcessor();
        } catch (NoClassDefFoundError e) {
            LOGGER.log(Level.SEVERE, "UI Themes not supported because LESS processing is not available.");
        }
    }

    public static UIThemesProcessor getInstance() {
        if (jenkinsThemesProcessor != null) {
            return jenkinsThemesProcessor;
        }

        for (Action action : Jenkins.getInstance().getExtensionList(RootAction.class)) {
            if (action instanceof UIThemesProcessor) {
                jenkinsThemesProcessor = (UIThemesProcessor) action;
                return jenkinsThemesProcessor;
            }
        }
        LOGGER.log(Level.SEVERE, "Unable to find UIThemesProcessor RootAction instance on Jenkins instance. " +
                                "Creating an instance, assuming this is a test environment, otherwise this is a problem !!");
        return new UIThemesProcessor();
    }

    protected List<UIThemeContributor> getContributors() {
        return contributors;
    }

    public synchronized UIThemesProcessor addContributor(UIThemeContributor contributor) {
        if (contributor != null) {
            contributors.add(contributor);
            themeSet = null; // recreate
        } else {
            LOGGER.log(Level.WARNING, "Attempted to add a 'null' contributor.", new UnsupportedOperationException());
        }
        return this;
    }

    public synchronized UIThemesProcessor removeContributor(UIThemeContributor contributor) {
        contributors.remove(contributor);
        themeSet = null; // recreate
        return this;
    }

    public void addPluginContributors(PluginManager pluginManager) throws IOException {
        List<PluginWrapper> plugins = pluginManager.getPlugins();

        for (PluginWrapper pluginWrapper : plugins) {
            Plugin plugin = pluginWrapper.getPlugin();
            if (plugin instanceof UIThemeContributor) {
                addContributor((UIThemeContributor) plugin);
            }
        }
    }

    public void reset() {
        contributors.clear();
        themeSet = null;
    }

    public File getUserThemesCSS(User user) throws IOException {
        File userHome = JenkinsUtil.getJenkinsUserHome(user);

        if (userHome == null || !userHome.exists()) {
            userHome = JenkinsUtil.JENKINS_ANONYMOUS_USER_HOME;
        }

        File cssFile = getUserThemesCSSFile(userHome);

        if (cssFile.exists()) {
            return cssFile;
        }

        return generateUIThemeSet(userHome);
    }


    public synchronized void deleteAllUserThemes() {
        LOGGER.log(Level.FINE, "Deleting all user theme styles.");

        // Delete the theme css for each user dir
        File[] userHomes = JenkinsUtil.JENKINS_USER_HOME.listFiles();
        if (userHomes != null && userHomes.length > 0) {
            for (File userHome : userHomes) {
                if (userHome.isDirectory()) {
                    File cssFile = getUserThemesCSSFile(userHome);
                    if (cssFile.exists()) {
                        cssFile.delete();
                    }
                }
            }
        }
    }

    public synchronized UIThemeSet getUiThemeSet() {
        if (themeSet == null) {
            themeSet = new UIThemeSet();
            for (UIThemeContributor contributor : contributors) {
                contributor.contribute(themeSet);
            }
        }
        return themeSet;
    }

    private synchronized File generateUIThemeSet(File userHome) throws IOException {
        userHome = normalizeUserHome(userHome);

        File cssFile = getUserThemesCSSFile(userHome);
        if (cssFile.exists()) {
            return cssFile;
        }

        if (userHome == JenkinsUtil.JENKINS_ANONYMOUS_USER_HOME) {
            LOGGER.log(Level.FINE, "Assembling default UI themes.");
        }

        UIThemeSet themeSet = getUiThemeSet();

        UserUIThemeConfiguration themeConfiguration = UserUIThemeConfiguration.fromUserHome(userHome);
        StringBuilder themeStylesBuilder = new StringBuilder();

        // Create/update the global jenkins variables LESS file
        JenkinsUtil.createJenkinsEnvVariablesLESSFile();

        // Generate the user theme styles based on the available themes and the users
        // theme selections, using the theme default implementation where the user has not made an
        // implementation selection for a given theme.
        addGenerationTime(themeStylesBuilder);

        if (lessProcessor == null) {
            addLESSProcessingNotAvailable(themeStylesBuilder);
        } else {
            for (String themeName : themeSet.getThemeNames()) {
                UITheme theme = themeSet.getTheme(themeName);
                UIThemeImplementation impl = null;

                if (themeConfiguration != null) {
                    String themeSelection = themeConfiguration.getUserThemeSelection(themeName);
                    if (themeSelection != null) {
                        impl = theme.getImpl(themeSelection);
                    }
                }
                if (impl == null) {
                    impl = theme.getDefaultImpl();
                }
                if (impl != null) {
                    List<UIThemeContribution> contributions = impl.getContributions();

                    if (!contributions.isEmpty()) {
                        for (UIThemeContribution themeContribution : contributions) {
                            Resource lessResource = themeContribution.createUserLessResource(userHome, impl);
                            addContributionHeader(themeStylesBuilder, themeContribution);
                            if (lessResource != null) {
                                try {
                                    themeStylesBuilder.append(lessProcessor.process(lessResource));
                                } catch (Exception e) {
                                    LOGGER.log(Level.WARNING, String.format("Error processing LESS resource contribution '%s' from theme implementation '%s'.",
                                            lessResource.getName(), themeContribution.getQName()), e);
                                }
                            } else {
                                themeStylesBuilder.append("     /* No resource */\n\n");
                                LOGGER.log(Level.WARNING, "Theme implementation ''{0}'' returned a null LESS resource.", themeContribution.getQName().toString());
                            }
                        }
                    } else {
                        LOGGER.log(Level.WARNING, "Theme implementation ''{0}'' has zero theme contributions. At least one is expected.", impl.getQName().toString());
                    }
                } else {
                    LOGGER.log(Level.WARNING, "Unknown/Unimplemented theme named ''{0}''.", themeName);
                }
            }
        }

        // Store the generated theme CSS in the user dir.
        FileUtils.write(cssFile, themeStylesBuilder.toString(), "UTF-8");

        return cssFile;
    }

    private File normalizeUserHome(File userHome) {
        if (userHome == null || !userHome.exists()) {
            userHome = JenkinsUtil.JENKINS_ANONYMOUS_USER_HOME;
        }
        return userHome;
    }

    public static File getUserThemesDir(File userHome) {
        return new File(userHome, "themes");
    }

    public static File getUserThemeImplDir(String themeName, String themeImplName, File userHome) {
        return new File(getUserThemesDir(userHome), String.format("%s/%s", themeName, themeImplName));
    }

    public static File getUserThemesCSSFile(File userHome) {
        return new File(getUserThemesDir(userHome), "themes.css");
    }

    public static File getUserThemeImplConfigFile(String themeName, String themeImplName, File userHome) {
        return new File(getUserThemeImplDir(themeName, themeImplName, userHome), "config.json");
    }

    public static File getUserThemeImplLESSFile(String themeName, String themeImplName, File userHome) {
        return new File(getUserThemeImplDir(themeName, themeImplName, userHome), "theme.less");
    }

    public static Map<String, String> getUserThemeImplConfig(String themeName, String themeImplName, File userHome) throws IOException {
        File themeImplConfigFile = getUserThemeImplConfigFile(themeName, themeImplName, userHome);

        if (!themeImplConfigFile.exists()) {
            // If the user has not configured the theme, get the anonymous user config as the default config.
            themeImplConfigFile = getUserThemeImplConfigFile(themeName, themeImplName, JenkinsUtil.JENKINS_ANONYMOUS_USER_HOME);
        }

        if (themeImplConfigFile.exists()) {
            return JSONReadWrite.fromUTF8File(themeImplConfigFile, Map.class);
        } else {
            return Collections.emptyMap();
        }
    }

    private void addGenerationTime(StringBuilder stylesBuilder) {
        stylesBuilder.append("/*\n");
        stylesBuilder.append(String.format("      Generated %s\n", (new Date()).toString()));
        stylesBuilder.append("*/\n");
    }

    private void addLESSProcessingNotAvailable(StringBuilder stylesBuilder) {
        stylesBuilder.append("/*\n");
        stylesBuilder.append("      Unable to generate CSS styles. Possible reasons:\n" +
                             "      > Running under the Jenkins Core test harness, which does not have a compatible version of the Mozilla Rhino JavaScript engine.\n" +
                             "      > The Mozilla Rhino JavaScript engine has been removed from the classpath.\n");
        stylesBuilder.append("*/\n");
    }

    private void addContributionHeader(StringBuilder stylesBuilder, UIThemeContribution themeContribution) {
        stylesBuilder.append("/*\n");
        stylesBuilder.append(String.format("      Theme Contribution '%s'\n", themeContribution.getQName()));
        stylesBuilder.append("*/\n");
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "uithemes";
    }

    public final HttpResponse doCss(StaplerRequest req) throws IOException {
        User user = User.current();
        File cssFile = getUserThemesCSS(user);
        return new CSSStaplerResponse(cssFile);
    }
}
