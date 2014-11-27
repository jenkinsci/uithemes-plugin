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
package org.jenkinsci.plugins.uithemes.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jenkinsci.plugins.uithemes.util.JSONReadWrite;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class StatusResponse {

    private static final Logger LOGGER = Logger.getLogger(StatusResponse.class.getName());

    public String status = "OK";
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String message;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String detail;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Object data;

    public <T> T dataTo(Class<T> to) throws IOException {
        if (data == null) {
            return null;
        }
        if (to.isInstance(data)) {
            return to.cast(data);
        }
        if (data instanceof String) {
            return JSONReadWrite.fromString((String) data, to);
        } else {
            String asString = JSONReadWrite.toString(data);
            return JSONReadWrite.fromString(asString, to);
        }
    }

    public static StatusResponse OK() {
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.status = "OK";
        return statusResponse;
    }

    public static StatusResponse OK(Object data) {
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.status = "OK";
        statusResponse.data = data;
        return statusResponse;
    }

    public static StatusResponse ERROR() {
        StatusResponse statusResponse = new StatusResponse();
        statusResponse.status = "ERROR";
        return statusResponse;
    }

    public static StatusResponse ERROR(String message) {
        StatusResponse statusResponse = ERROR();
        statusResponse.message = message;
        return statusResponse;
    }

    public static StatusResponse ERROR(Throwable t) {
        return ERROR(t.getMessage(), t);
    }

    public static StatusResponse ERROR(String message, Throwable t) {
        StatusResponse statusResponse = ERROR(message);
        statusResponse.setException(t);
        LOGGER.log(Level.INFO, message, t);
        return statusResponse;
    }

    private StatusResponse setStatus(String status) {
        this.status = status;
        return this;
    }

    private StatusResponse setMessage(String message) {
        this.message = message;
        return this;
    }

    private StatusResponse setDetail(String detail) {
        this.detail = detail;
        return this;
    }

    private StatusResponse setException(Throwable t) {
        this.detail = ExceptionUtils.getStackTrace(t);
        return this;
    }
}
