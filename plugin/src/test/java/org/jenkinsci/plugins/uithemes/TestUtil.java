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

import com.gargoylesoftware.htmlunit.Page;
import org.jenkinsci.plugins.uithemes.rest.model.StatusResponse;
import org.jenkinsci.plugins.uithemes.util.JSONReadWrite;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class TestUtil {

    public static <T> T getJSON(String url, Class<T> to, JenkinsRule jenkinsRule) throws IOException, SAXException {
        JenkinsRule.WebClient webClient = jenkinsRule.createWebClient();

        Page runsPage = webClient.goTo(url, "application/json");
        String jsonResponse = runsPage.getWebResponse().getContentAsString();

        //System.out.println(jsonResponse);

        return JSONReadWrite.fromString(jsonResponse, to);
    }

    public static StatusResponse putJSON(String url, Object content, JenkinsRule jenkinsRule) throws IOException {
        String response;
        if (content instanceof String) {
            response = sendToJenkins("PUT", url, (String) content, "application/json", jenkinsRule);
        } else {
            response = sendToJenkins("PUT", url, JSONReadWrite.toString(content), "application/json", jenkinsRule);
        }

        //System.out.println(response);

        return JSONReadWrite.fromString(response, StatusResponse.class);
    }

    public static byte[] readStream(InputStream stream) throws IOException {
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        byte[] byteBuf = new byte[1024];
        int readCount = 0;

        while ((readCount = stream.read(byteBuf)) != -1) {
            bytesOut.write(byteBuf, 0, readCount);
        }

        return bytesOut.toByteArray();
    }

    private static String sendToJenkins(String method, String url, String content, String contentType, JenkinsRule jenkinsRule) throws IOException {
        String jenkinsUrl = jenkinsRule.jenkins.getRootUrl();
        URL urlObj = new URL(jenkinsUrl + url);
        HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

        try {
            conn.setRequestMethod(method.toUpperCase());

            if (contentType != null) {
                conn.setRequestProperty("Content-Type", contentType);
            }
            if (content != null) {
                byte[] bytes = content.getBytes(Charset.forName("UTF-8"));

                conn.setDoOutput(true);

                conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
                final OutputStream os = conn.getOutputStream();
                try {
                    os.write(bytes);
                    os.flush();
                } finally {
                    os.close();
                }
            }

            return getContentAsString(conn);
        } finally {
            conn.disconnect();
        }
    }

    private static String getContentAsString(final HttpURLConnection con) throws IOException {
        return new String(getContent(con), "UTF-8");
    }

    private static byte[] getContent(final HttpURLConnection con) throws IOException {
        try {
            InputStream inputStream = con.getInputStream();

            if (inputStream != null) {
                try {
                    return readStream(inputStream);
                } finally {
                    inputStream.close();
                }
            }
        } catch (IOException e) {
            // No content...
        }

        return null;
    }

}
