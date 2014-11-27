/*
 * Copyright (C) 2013 CloudBees Inc.
 *
 * All rights reserved.
 */

exports.isTestEnv = function() {
    if (window === undefined) {
        return true;
    } else if (window.navigator === undefined) {
        return true;
    } else if (window.navigator.userAgent === undefined) {
        return true;
    } else if (window.navigator.userAgent.toLowerCase().indexOf("phantomjs") !== -1) {
        return true;
    }
}