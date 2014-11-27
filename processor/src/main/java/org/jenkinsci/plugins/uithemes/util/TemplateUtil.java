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
package org.jenkinsci.plugins.uithemes.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import hudson.Util;
import org.jenkinsci.plugins.uithemes.UIThemesProcessor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class TemplateUtil {

    private static final Logger LOGGER = Logger.getLogger(UIThemesProcessor.class.getName());

    public static Template createJenkinsEnvVariablesTemplate() {
        return createLESSTemplate("Jenkins-Environment-Variables", "/jenkins-themes/core/jenkins/env-template.less", TemplateUtil.class);
    }

    public static Template createLESSTemplate(String templateName, String templatePath, Class<?> loaderClass) {
        String templateText = loadLESSTemplateText(templatePath, loaderClass);

        if (templateText != null) {
            Reader templateReader = new StringReader(templateText);

            try {
                try {
                    Configuration config = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
                    config.setNumberFormat("#.####");
                    return new Template(templateName, templateReader, config);
                } finally {
                    templateReader.close();
                }
            } catch (IOException e) {
                throw new IllegalStateException("Exception creating FreeMarker Template instance for template:\n\n[" + templateText + "]\n\n", e);
            }
        } else {
            return null;
        }
    }

    private static String loadLESSTemplateText(String templatePath, Class<?> loaderClass) {
        InputStream templateResStream = loaderClass.getResourceAsStream(templatePath);

        if (templateResStream != null) {
            try {
                Reader templateResStreamReader = new InputStreamReader(templateResStream, "UTF-8");
                StringWriter writer = new StringWriter();
                Util.copyStreamAndClose(templateResStreamReader, writer);
                return writer.toString();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, String.format("Error reading LESS resource template file '%s'.", templatePath), e);
            }

        } else {
            LOGGER.log(Level.INFO, "No UI Theme Contribution LESS template found at ''{0}'' on the classpath.", templatePath);
        }

        return null;
    }
}
