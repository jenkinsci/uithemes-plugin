/*
 * Copyright (C) 2014 CloudBees Inc.
 *
 * All rights reserved.
 */

var test = require('../../../util/test');
var isTestEnv = test.isTestEnv();

if (!isTestEnv) {
    require('./pick-a-color');
}

exports.addColorPicker = function (inputElements) {
    if (isTestEnv) {
        console.log('Test environment.  Cannot use color picker.');
        return;
    }

    inputElements.pickAColor({
        showSpectrum: true,
        showSavedColors: false,
        saveColorsPerElement: true,
        fadeMenuToggle: true,
        showHexInput: true,
        showBasicColors: true,
        allowBlank: false,
        inlineDropdown: true
    });
}