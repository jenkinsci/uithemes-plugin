/*
 * Copyright (C) 2014 CloudBees Inc.
 *
 * All rights reserved.
 */

var gutil = require('gulp-util');
var fs = require('fs');
var requireUncached = require("require-uncached");
var jquery_js = fs.readFileSync("./node_modules/jquery/dist/jquery.min.js", "utf-8");

/**
 * require one of the source modules.
 * <p/>
 * Takes care of resolving relative paths etc.
 *
 * @param module The module name.
 * @returns The module instance.
 */
exports.require = function (module) {
    return require('../../main/js/' + module);
}

/**
 * require a test resource.
 * <p/>
 * Gets them from the test/resources dir.
 * @param resource The resource path.
 * @returns The resource instance.
 */
exports.requireTestRes = function (resource) {
    if (endsWith(resource, '.html') || endsWith(resource, '.txt')) {
        return fs.readFileSync('./src/test/resources/' + resource, "utf-8");
    } else {
        return requireUncached('../resources/' + resource);
    }
}

/**
 * Log a message to the console using Gulp logger.
 * @param message The message to log.
 */
exports.log = function (message) {
    if (typeof message === 'object') {
        gutil.log(JSON.stringify(message, undefined, 4));
    } else {
        gutil.log(message);
    }
}

/**
 * Log an error to the console using Gulp logger.
 * <p/>
 * Colorizes the log.
 * @param message The error message to log.
 */
exports.error = function (message) {
    gutil.log(gutil.colors.red(message));
}

/**
 * Mock a set of functions on the specified module.
 * @param module The module to mock.  The name of the module, or the module instance.
 * @param mocks An object containing the mocks.
 */
exports.spyOn = function (module, mocks) {
    var moduleInstance;

    if (typeof module === 'string') {
        // It's the module name
        moduleInstance = this.require(module);
    } else if (typeof module === 'object') {
        // It's the resolved module instance
        moduleInstance = module;
    }

    // apply the mocks....
    for (var propToMock in mocks) {
        if (mocks.hasOwnProperty(propToMock)) {
            jasmine.getEnv().spyOn(moduleInstance, propToMock).and.callFake(mocks[propToMock]);
        }
    }
}

exports.trim = function(str) {
    var jqProxy = exports.require('./jQuery');
    var $ = jqProxy.getJQuery();

    return $.trim(str);
}

exports.compareMultilineText = function(text1, text2, trimLines) {
    var text1Lines = text1.split(/^/m);
    var text2Lines = text2.split(/^/m);

    if (text1Lines.length !== text2Lines.length) {
        console.log("Text1 has " + text1Lines.length + " lines of text, while Text2 has " + text2Lines.length);
        return false
    }

    for (var i = 0; i < text1Lines.length; i++) {
        var text1Line = text1Lines[i];
        var text2Line = text2Lines[i];

        if (trimLines) {
            text1Line = exports.trim(text1Line);
            text2Line = exports.trim(text2Line);
        }

        if (text1Line !== text2Line) {
            console.log("Texts do not match at line number " + (i + 1) + ". [" + text1Line + "](" + text1Line.length + " chars) [" + text2Line + "](" + text2Line.length + " chars)");
            return false;
        }
    }

    console.log("Texts match");
    return true;
}

/**
 * An alias for the {@link spyOn} function.
 */
exports.mock = exports.spyOn;

/**
 * Create a mock MVC config element.
 * <p/>
 * Just needs to look a bit like a jQuery object so we can get attributes off it.
 * @param config The config object.
 */
exports.mockControllerConfigElement = function(config) {
    config.attr = function(attributeName) {
        return config[attributeName];
    }
    return config;
}

exports.mvcContext = function(element) {
    var mvc = exports.require('./mvc');
    return mvc.newContext('mock-controller', element);
}

exports.mvcContextWithMockConfig = function(config) {
    var mockConfigEl = exports.mockControllerConfigElement(config);
    var mvc = exports.require('./mvc');
    return mvc.newContext('mock-controller', mockConfigEl);
}

exports.mvcRegister = function(controllers) {
    var mvc = exports.require('./mvc');

    function register(controllerName) {
        if (!mvc.isRegistered(controllerName)) {
            var controllerModule = exports.require('./controller/' + controllerName);
            mvc.register(controllerModule);
        }
    }

    if (typeof controllers === 'string') {
        // just one controller name in a string
        register(controllers);
    } else {
        // it's an array of controller names
        for (var i = 0; i < controllers.length; i++) {
            register(controllers[i]);
        }
    }

    return mvc;
}

exports.mvcRun = function(controllers, applyOnElement) {
    var mvc = exports.mvcRegister(controllers);

    if (applyOnElement) {
        mvc.applyControllers(applyOnElement)
    }
}

exports.testWithJQuery = function (content, testFunc) {
    var jsdom = require('jsdom');

    jsdom.env({
        html: content,
        src: [jquery_js],
        done: function (err, window) {
            var jQuery = exports.require('jQuery');

            jQuery.setWindow(window);
            jQuery.setJQuery(window.$);

            try {
                testFunc(window.$);
            } catch (e) {
                exports.error(e);
            }
        }
    });
}

function endsWith(string, value) {
    if (string.length < value.length) {
        return false;
    }
    return (string.slice(0 - value.length) === value);
}