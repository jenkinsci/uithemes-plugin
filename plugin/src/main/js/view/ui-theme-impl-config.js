/*
 * Copyright (C) 2013 CloudBees Inc.
 *
 * All rights reserved.
 */

var templates = require('./templates');
var jqUtil = require('jenkins-js-util/jQuery');
var colorPicker = require('./widgets/color-picker');

exports.render = function (modelData, onElement) {
    var $ = jqUtil.getJQuery();
    var uiThemeImplConfig = templates.apply('ui-theme-impl-config', modelData);
    var themesConfigMVCContext = this.getContext('ui-themes-config');

    onElement.empty().append(uiThemeImplConfig);

    var implConfigInputs = $('.impl-config-value', uiThemeImplConfig);

    // Add a color picker to all COLOR inputs.
    colorPicker.addColorPicker($('input.COLOR', uiThemeImplConfig));

    // Scrape all properties and save on pressing of the Save/Apply button.
    function update() {
        var userConfig = {};
        implConfigInputs.each(function () {
            var input = $(this);
            var name = input.attr('name');
            var value = input.val();
            userConfig[name] = value;
        });
        modelData.updateImplConfig(userConfig);
    }

    $('.save', uiThemeImplConfig).click(function() {
        update();
        themesConfigMVCContext.trigger("ConfigChange");
    });

    $('.reset', uiThemeImplConfig).click(function() {
        // Set each input value using the defaults
        implConfigInputs.each(function() {
            var input = $(this);
            var defaultValue = input.attr('defaultValue');
            input.val(defaultValue);
            input.change();
        });
    });
}