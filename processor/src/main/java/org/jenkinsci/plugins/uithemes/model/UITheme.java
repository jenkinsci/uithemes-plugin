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

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UI Theme.
 * <p/>
 * See <a href="README.md">README.md</a>.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UITheme {

    private static final Logger LOGGER = Logger.getLogger(UITheme.class.getName());

    private String name;
    private String title;
    private String description;
    private Map<String, UIThemeImplementation> implementations = new LinkedHashMap<String, UIThemeImplementation>();
    private List<DeferredContribution> deferredContributions = new ArrayList<DeferredContribution>();

    public UITheme(String name, String title, String description) {
        this.name = name;
        this.title = title;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getThemeImplNames() {
        return implementations.keySet();
    }

    public UIThemeImplementation getImpl(String name) {
        return implementations.get(name);
    }

    public UIThemeImplementation getDefaultImpl() {
        for (UIThemeImplementation uiThemeImpl : implementations.values()) {
            if (uiThemeImpl.isDefault()) {
                return uiThemeImpl;
            }
        }
        if (!implementations.isEmpty()) {
            return implementations.values().iterator().next();
        }
        return null;
    }

    public UITheme registerImpl(UIThemeImplementation impl) {
        if (implementations.put(impl.getName(), impl) != null) {
            LOGGER.log(Level.WARNING, "Overwriting UI Theme Implementation ''{0}''.", impl.getQName());
        } else {
            LOGGER.log(Level.FINE, "Added UI Theme Implementation ''{0}''.", impl.getQName());
        }

        // Check for and add any deferred contributions to this theme impl.
        Iterator<DeferredContribution> deferredContributionsIt = deferredContributions.iterator();
        while (deferredContributionsIt.hasNext()) {
            DeferredContribution deferredContribution = deferredContributionsIt.next();
            if (deferredContribution.getTargetName().equals(impl.getQName())) {
                impl.add(deferredContribution.getContribution());
                LOGGER.log(Level.FINE, "Theme implementation contribution ''{0}'' successfully registered (deferred registration).", deferredContribution.getContribution().getQName()); // see contribute method below
                deferredContributionsIt.remove();
            }
        }

        return this;
    }

    public UITheme contribute(UIThemeContribution contribution) {
        if (!contribution.getThemeName().equals(name)) {
            LOGGER.log(Level.WARNING, "Theme name mismatch. ''{0}'' != ''{1}''.", new String[] {contribution.getThemeName(), name});
            return this;
        }

        UIThemeImplementation impl = getImpl(contribution.getThemeImplName());

        if (impl == null) {
            QName implQName = UIThemeImplementation.toQName(contribution.getThemeName(), contribution.getThemeImplName());
            LOGGER.log(Level.WARNING, "Theme implementation ''{0}'' not registered yet. Registration of contribution ''{1}'' is deferred until theme implementation is registered.",
                    new Object[] {implQName, contribution.getQName()});  // see registerImpl method above
            deferredContributions.add(new DeferredContribution(implQName, contribution));
        } else {
            impl.add(contribution);
            LOGGER.log(Level.FINE, "Theme implementation contribution ''{0}'' successfully registered.", contribution.getQName());
        }

        return this;
    }

    public List<UIThemeContribution> getThemeImplContributions(String themeImplName) {
        UIThemeImplementation impl = getImpl(themeImplName);
        if (impl != null) {
            return impl.getContributions();
        }
        return Collections.emptyList();
    }
}
