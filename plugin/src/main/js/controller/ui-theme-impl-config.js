/*
 * Copyright (C) 2014 CloudBees Inc.
 *
 * All rights reserved.
 */

var model = require('../model/ui-theme-impl-config');
var view = require('../view/ui-theme-impl-config');

exports.getName = function() {
    return 'ui-theme-impl-config';
}

exports.getModel = function() {
    return model;
}

exports.getView = function() {
    return view;
}
