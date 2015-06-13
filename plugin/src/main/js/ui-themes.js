/*
 * Copyright (C) 2014 CloudBees Inc.
 *
 * All rights reserved.
 */

var mvc = require('./mvc');

// Register controllers...
mvc.register(require('./controller/ui-themes-config'));
mvc.register(require('./controller/ui-theme-impl-config'));

// Apply controllers to the whole document.
var jqUtil = require('jenkins-js-util/jQuery');
var $ = jqUtil.getJQuery();
$(function() {
    mvc.applyControllers();
});
