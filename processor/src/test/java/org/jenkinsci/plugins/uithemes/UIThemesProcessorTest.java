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

import hudson.model.User;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.plugins.uithemes.model.UIThemeContribution;
import org.jenkinsci.plugins.uithemes.model.UIThemeSet;
import org.jenkinsci.plugins.uithemes.model.UserUIThemeConfiguration;
import org.jenkinsci.plugins.uithemes.util.JenkinsUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UIThemesProcessorTest {

    private UIThemesProcessor processor;
    private MockUIThemeContributor icon_default;
    private MockUIThemeContributor icon_font_awesome;
    private MockUIThemeContributor status_balls_default;
    private MockUIThemeContributor status_balls_doony;
    private MockUIThemeContributor status_balls_css3;
    private MockUIThemeContributor header_default;
    private MockUIThemeContributor header_lite;

    @Before
    public void before() throws NoSuchMethodException, IOException {
        JenkinsUtil.JenkinsUtilTestSetup.setup();
        processor = new UIThemesProcessor();
        processor.deleteAllUserThemes();
        icon_default = new MockUIThemeContributor("icon", "default");
        icon_font_awesome = new MockUIThemeContributor("icon", "font-awesome");
        status_balls_default = new MockUIThemeContributor("status-balls", "default");
        status_balls_doony = new MockUIThemeContributor("status-balls", "doony-balls");
        status_balls_css3 = new MockUIThemeContributor("status-balls", "css3-animated");
        header_default = new MockUIThemeContributor("header", "default");
        header_lite = new MockUIThemeContributor("header", "lite");
    }

    @Test
    public void test_add_remove_contributor() {
        addTestThemes(processor);

        Assert.assertEquals(7, processor.getContributors().size());
        Assert.assertTrue(processor.getContributors().contains(icon_default));
        Assert.assertTrue(processor.getContributors().contains(icon_font_awesome));

        processor.removeContributor(icon_default);
        processor.removeContributor(icon_font_awesome);

        Assert.assertEquals(5, processor.getContributors().size());
        Assert.assertFalse(processor.getContributors().contains(icon_default));
        Assert.assertFalse(processor.getContributors().contains(icon_font_awesome));
    }

    @Test
    public void test_generate_anonymous_user_theme_configured() throws IOException {
        addTestThemes(processor);

        User user = Mockito.mock(User.class);
        Mockito.when(user.getId()).thenReturn("anonymous");

        File cssFile = processor.getUserThemesCSS(user);

        Assert.assertEquals("./target/jenkins-home/users/anonymous/themes/themes.css", cssFile.getPath());
        assertDefaultStylesOnly(cssFile);
    }

    @Test
    public void test_generate_user_theme_unconfigured() throws IOException {
        addTestThemes(processor);

        User user = createUser("tfennelly");
        File cssFile = processor.getUserThemesCSS(user);

        Assert.assertEquals("./target/jenkins-home/users/tfennelly/themes/themes.css", cssFile.getPath());
        assertDefaultStylesOnly(cssFile);
    }

    @Test
    public void test_generate_user_theme_configured() throws IOException {
        addTestThemes(processor);

        User user = createUser("tfennelly");

        // configure 2 themes for the user
        UserUIThemeConfiguration themeConfiguration = new UserUIThemeConfiguration();
        themeConfiguration.addSelection(icon_font_awesome.contribution.getThemeName(), icon_font_awesome.contribution.getThemeImplName());
        themeConfiguration.addSelection(status_balls_css3.contribution.getThemeName(), status_balls_css3.contribution.getThemeImplName());
        UserUIThemeConfiguration.toUserHome(user, themeConfiguration);

        File cssFile = processor.getUserThemesCSS(user);

        Assert.assertEquals("./target/jenkins-home/users/tfennelly/themes/themes.css", cssFile.getPath());

        // Check that the default contributions have been replaced with he above selections
        // in the generated CSS
        String styles = FileUtils.readFileToString(cssFile, "UTF-8");
        Assert.assertTrue(styles.contains(icon_font_awesome.contribution.getQName().toString()));
        Assert.assertTrue(styles.contains(status_balls_css3.contribution.getQName().toString()));
        Assert.assertTrue(styles.contains(header_default.contribution.getQName().toString()));
        Assert.assertFalse(styles.contains(icon_default.contribution.getQName().toString()));
        Assert.assertFalse(styles.contains(status_balls_default.contribution.getQName().toString()));
        Assert.assertFalse(styles.contains(status_balls_doony.contribution.getQName().toString()));
        Assert.assertFalse(styles.contains(header_lite.contribution.getQName().toString()));
    }

    @Test
    public void test_generate_delete_lifecycle() throws IOException, InterruptedException {
        addTestThemes(processor);
        User user1 = createUser("user1");
        User user2 = createUser("user2");
        User user3 = createUser("user3");

        // User themes should not exist for any of the users.
        Assert.assertFalse(getUserThemesFile(user1).exists());
        Assert.assertFalse(getUserThemesFile(user2).exists());
        Assert.assertFalse(getUserThemesFile(user3).exists());

        long user1_1 = processor.getUserThemesCSS(user1).lastModified();
        long user2_1 = processor.getUserThemesCSS(user2).lastModified();
        long user3_1 = processor.getUserThemesCSS(user3).lastModified();
        long user1_2 = processor.getUserThemesCSS(user1).lastModified();
        long user2_2 = processor.getUserThemesCSS(user2).lastModified();
        long user3_2 = processor.getUserThemesCSS(user3).lastModified();

        // In each case, the second call to getUserThemesCSS should have
        // returned the already generated CSS file i.e. the have the same
        // date on them
        Assert.assertEquals(user1_1, user1_2);
        Assert.assertEquals(user2_1, user2_2);
        Assert.assertEquals(user3_1, user3_2);

        // Now delete all user themes and check again... should be new files.
        processor.deleteAllUserThemes();
        Thread.sleep(1500);
        Assert.assertFalse(getUserThemesFile(user1).exists());
        Assert.assertFalse(getUserThemesFile(user2).exists());
        Assert.assertFalse(getUserThemesFile(user3).exists());
        File user1_3 = processor.getUserThemesCSS(user1);
        File user2_3 = processor.getUserThemesCSS(user2);
        File user3_3 = processor.getUserThemesCSS(user3);
        Assert.assertNotEquals(user1_1, user1_3.lastModified());
        Assert.assertNotEquals(user2_1, user2_3.lastModified());
        Assert.assertNotEquals(user3_1, user3_3.lastModified());
    }

    private void assertDefaultStylesOnly(File cssFile) throws IOException {
        String styles = FileUtils.readFileToString(cssFile, "UTF-8");

        // Check that only the default contributions made it into the theme CSS
        Assert.assertTrue(styles.contains(icon_default.contribution.getQName().toString()));
        Assert.assertTrue(styles.contains(status_balls_default.contribution.getQName().toString()));
        Assert.assertTrue(styles.contains(header_default.contribution.getQName().toString()));
        Assert.assertFalse(styles.contains(icon_font_awesome.contribution.getQName().toString()));
        Assert.assertFalse(styles.contains(status_balls_doony.contribution.getQName().toString()));
        Assert.assertFalse(styles.contains(status_balls_css3.contribution.getQName().toString()));
        Assert.assertFalse(styles.contains(header_lite.contribution.getQName().toString()));
    }

    private void addTestThemes(UIThemesProcessor processor) {
        processor.addContributor(icon_default);
        processor.addContributor(icon_font_awesome);
        processor.addContributor(status_balls_default);
        processor.addContributor(status_balls_doony);
        processor.addContributor(status_balls_css3);
        processor.addContributor(header_default);
        processor.addContributor(header_lite);
    }

    private File getUserThemesFile(User user) {
        return UIThemesProcessor.getUserThemesCSSFile(JenkinsUtil.getJenkinsUserHome(user));
    }

    private User createUser(String username) {
        User user = Mockito.mock(User.class);
        Mockito.when(user.getId()).thenReturn(username);
        JenkinsUtil.JenkinsUtilTestSetup.mkUserDir(user);
        return user;
    }

    private class MockUIThemeContributor implements UIThemeContributor {
        private UIThemeContribution contribution;

        private MockUIThemeContributor(String themeName, String themeImplName) {
            this.contribution = new UIThemeContribution(themeName + "-" + themeImplName, themeName, themeImplName, MockUIThemeContributor.class);
        }
        @Override
        public void contribute(UIThemeSet themeSet) {
            if (themeSet.getThemeNames().isEmpty()) {
                themeSet.registerTheme(icon_default.contribution.getThemeName(), "Jenkins Icons");
                themeSet.registerTheme(status_balls_default.contribution.getThemeName(), "Jenkins Status Balls");
                themeSet.registerTheme(header_default.contribution.getThemeName(), "Jenkins Page Header");
            }
            themeSet.registerThemeImpl(contribution.getThemeName(), contribution.getThemeImplName(), contribution.getThemeImplName());
            themeSet.contribute(contribution);
        }
    }
}
