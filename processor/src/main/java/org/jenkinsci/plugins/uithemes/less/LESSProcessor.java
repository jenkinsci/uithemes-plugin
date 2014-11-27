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

import jenkins.model.Jenkins;
import org.lesscss.LessCompiler;
import org.lesscss.LessException;
import org.lesscss.LessSource;
import org.lesscss.Resource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class LESSProcessor {

    private final LessCompiler lessCompiler;

    public LESSProcessor() {
        List<String> options = Arrays.asList(
                "--relative-urls",
                "--clean-css"
        );
        lessCompiler = new LessCompiler(options);
    }

    public String process(Resource lessResource) throws IOException, LessException {
        return lessCompiler.compile(new LessSource(lessResource));
    }

    public byte[] processToBytes(Resource lessResource) throws IOException, LessException {
        return process(lessResource).getBytes(Charset.forName("UTF-8"));
    }
}
