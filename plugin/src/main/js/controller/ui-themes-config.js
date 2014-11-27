/*
 * Copyright (C) 2014 CloudBees Inc.
 *
 * All rights reserved.
 */

var model = require('../model/ui-themes-config');
var view = require('../view/ui-themes-config');

exports.getName = function() {
    return 'ui-themes-config';
}

exports.getModel = function() {
    return model;
}

exports.getView = function() {
    return view;
}
