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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import hudson.Util;
import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class JSONReadWrite {

    static final Charset UTF8 = Charset.forName("UTF-8");

    public static final ObjectMapper jsonMapper = new ObjectMapper();

    public static <T> T fromString(String string, Class<T> to) throws IOException {
        ObjectReader reader = jsonMapper.reader(to);
        return reader.readValue(string);
    }

    public static <T> T fromBytes(byte[] bytes, Charset encoding, Class<T> to) throws IOException {
        return fromString(new String(bytes, encoding), to);
    }

    public static <T> T fromUTF8File(File file, Class<T> to) throws IOException {
        if (!file.exists()) {
            return null;
        }
        String string = FileUtils.readFileToString(file, UTF8);
        return fromString(string, to);
    }

    public static <T> T fromRequest(StaplerRequest req, Class<T> to) throws IOException {
        String contentEncoding = req.getCharacterEncoding();
        StringWriter writer = new StringWriter();

        if (contentEncoding == null) {
            contentEncoding = "UTF-8";
        }

        Util.copyStreamAndClose(new InputStreamReader(req.getInputStream(), contentEncoding), writer);

        return fromString(writer.toString(), to);
    }

    public static String toString(Object object) throws IOException {
        StringWriter stringWriter = new StringWriter();
        jsonMapper.writeValue(stringWriter, object);
        return stringWriter.toString();
    }

    public static byte[] toUTF8Bytes(Object object) throws IOException {
        return toString(object).getBytes(UTF8);
    }

    public static void toUTF8File(Object object, File file) throws IOException {
        String string = toString(object);
        FileUtils.write(file, string, UTF8);
    }
}
