/*
 * Copyright (C) 2014 CloudBees Inc.
 *
 * All rights reserved.
 */

var mvc = require('../../mvc');
var handlebars = require('handlebars');
var hbsfyRuntime = require('hbsfy/runtime');
var jqProxy = require('../../jQuery');

/**
 * Templating support.
 */

// Create the template cache..
var templateCache = {
    'ui-themes-config': require('./ui-themes-config.hbs'),
    'ui-theme-impl-config': require('./ui-theme-impl-config.hbs')
};

// Initialise handlebars with helpers

function registerHBSHelper(name, helper) {
    // Need to register the helper with both handlebars and hbsfy runtime so
    // the work in both precompiled and non-precompiled (e.g. under watch) modes.
    handlebars.registerHelper(name, helper);
    hbsfyRuntime.registerHelper(name, helper);
}

registerHBSHelper('dumpObj', function(object) {
    return JSON.stringify(object, undefined, 4);
});

registerHBSHelper('ifCond', function (v1, operator, v2, options) {
    switch (operator) {
        case '==':
            return (v1 === v2) ? options.fn(this) : options.inverse(this);
        case '===':
            return (v1 === v2) ? options.fn(this) : options.inverse(this);
        case '!=':
            return (v1 !== v2) ? options.fn(this) : options.inverse(this);
        case '!==':
            return (v1 !== v2) ? options.fn(this) : options.inverse(this);
        case '<':
            return (v1 < v2) ? options.fn(this) : options.inverse(this);
        case '<=':
            return (v1 <= v2) ? options.fn(this) : options.inverse(this);
        case '>':
            return (v1 > v2) ? options.fn(this) : options.inverse(this);
        case '>=':
            return (v1 >= v2) ? options.fn(this) : options.inverse(this);
        case '&&':
            return (v1 && v2) ? options.fn(this) : options.inverse(this);
        case '||':
            return (v1 || v2) ? options.fn(this) : options.inverse(this);
        default: {
            if (options) {
                return options.inverse(this);
            } else {
                console.log('Unknown operator "' + operator + '".');
            }
        }
    }
});

function getTemplate(templateName) {
    var templateInstance = templateCache[templateName];
    if (!templateInstance) {
        throw 'No template by the name "' + templateName + '".  Check plugin/src/main/js/view/templates/index.js and make sure the template is registered in the templateCache.';
    }
    return  templateInstance;
}

/**
 * Get a template from the template cache.
 * @param templateName The template name.
 * @returns The template instance.
 */
exports.get = function (templateName) {
    return  getTemplate(templateName);
}

/**
 * Apply the named template to the provided data model.
 * @param templateName The name of the template.
 * @param dataModel The data model to which the template is to be applied.
 * @param divWrap Flag indicating whether the templating result is to be wrapped in a div
 * element (default true).  Needed if the html produced by the template contains text nodes
 * at the root level.
 * @returns jQuery DOM.
 */
exports.apply = function (templateName, dataModel, divWrap) {
    var templateInstance = getTemplate(templateName);
    var html = templateInstance(dataModel);
    var jQueryDom;

    if (divWrap === undefined || divWrap) {
        jQueryDom = jqProxy.getJQuery()('<div>' + html + '</div>');
    } else {
        jQueryDom = jqProxy.getJQuery()(html);
    }

    // Apply all controllers before returning...
    mvc.applyControllers(jQueryDom);

    return jQueryDom;
}