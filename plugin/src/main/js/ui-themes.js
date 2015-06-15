/*
 * Copyright (C) 2014 CloudBees Inc.
 *
 * All rights reserved.
 */

var mvc = require('jenkins-js-mvc');

// Register controllers...
mvc.register(require('./controller/ui-themes-config'));
mvc.register(require('./controller/ui-theme-impl-config'));

// Apply controllers to the whole document.
var $ = require('jenkins-js-util/jQuery').getJQuery();
$(function() {
    mvc.applyControllers();
});
