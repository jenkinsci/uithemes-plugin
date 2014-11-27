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

import org.jenkinsci.plugins.uithemes.less.URLResource;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UIThemeSetTest {

    @Test
    public void test() {
        UIThemeSet uiThemeSet = new UIThemeSet();
        UIThemeContribution baseClassicIcons = new UIThemeContribution("classic-base", "icon", "classic", UIThemeSetTest.class);

        // Should return false if the theme is not registered.
        Assert.assertFalse(uiThemeSet.contribute(baseClassicIcons));

        uiThemeSet.registerTheme("icon", "Jenkins Icon Theme");
        Assert.assertEquals("[icon]", uiThemeSet.getThemeNames().toString());

        // Should not be any need to register baseClassicIcons again. Should get registered once the theme is registered, even though
        // the "icon" "classic" theme impl is still not registered. Once the theme impl gets registered the
        // contribution should get added automatically (deferred contribution).

        // Register the "classic" implementation of the icons theme
        uiThemeSet.registerThemeImpl("icon", "classic", "Classic Jenkins Icons");
        Assert.assertEquals("[classic]", uiThemeSet.getThemeImplNames("icon").toString());

        // The baseClassicIcons should be auto-registered as a deferred contribution
        Assert.assertEquals("[{icon:classic}classic-base]", uiThemeSet.getThemeImplContributions("icon", "classic").toString());

        // make another contribution to the classic icons
        Assert.assertTrue(uiThemeSet.contribute(new UIThemeContribution("classic-some-other-styles", "icon", "classic", UIThemeSetTest.class)));

        Assert.assertEquals("[{icon:classic}classic-base, {icon:classic}classic-some-other-styles]", uiThemeSet.getThemeImplContributions("icon", "classic").toString());

        // Now try adding another icon theme impl, but this time make the contribution before the theme impl is registered. Should
        // result in a deferred registration of the contribution, but this time from the UITheme (Vs the UIThemeSet, as above).
        uiThemeSet.contribute(new UIThemeContribution("font-awesome", "icon", "font-awesome", UIThemeSetTest.class));

        uiThemeSet.registerThemeImpl("icon", "font-awesome", "FontAwesome Jenkins Icons");
        Assert.assertEquals("[classic, font-awesome]", uiThemeSet.getThemeImplNames("icon").toString());
        Assert.assertEquals("[{icon:font-awesome}font-awesome]", uiThemeSet.getThemeImplContributions("icon", "font-awesome").toString());
    }
}
