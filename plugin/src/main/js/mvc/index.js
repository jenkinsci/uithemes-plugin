/*
 * Copyright (C) 2014 CloudBees Inc.
 *
 * All rights reserved.
 */

var jqUtil = require('jenkins-js-util/jQuery');

/**
 * Lightweight MVC.
 */

var controllers = {};
var activeContexts = {};

/**
 * Register a controller.
 * @param controller The controller to be registered.
 */
exports.register = function (controller) {
    var controllerName = controller.getName();

    if (exports.isRegistered(controllerName)) {
        throw "A controller named '" + controllerName + "' is already registered.";
    }
    controllers[controllerName] = controller;
}

/**
 * Is the named controller already registered.
 * @param controllerName The name of the controller.
 * @return true if the controller is registered, otherwise false.
 */
exports.isRegistered = function (controllerName) {
    return controllers.hasOwnProperty(controllerName);
}

/**
 * Apply controllers.
 */
exports.applyControllers = function (onElement, allowReapply) {
    var targetEls = getWidgets(onElement);

    jqUtil.forEachElement(targetEls, function(targetEl) {
        if (allowReapply || !targetEl.hasClass('uit-controller-applied')) {
            var controllerName = targetEl.attr('uit-controller');

            if (controllerName) {
                var controller = controllers[controllerName];

                if (controller) {
                    var model = controller.getModel();
                    var view = controller.getView();
                    var mvcContext = new MVCContext(controllerName, targetEl);

                    activeContexts[mvcContext.getInstanceId()] = mvcContext;
                    try {
                        targetEl.addClass('uit-widget');
                        targetEl.addClass('uit-controller-applied');
                        targetEl.addClass(controllerName);

                        model.getModelData.call(mvcContext, function (modelData) {
                            mvcContext.modelData = modelData;
                            view.render.call(mvcContext, modelData, targetEl);
                        });
                    } finally {
                        mvcContext.setApplyCompleted();
                    }
                } else {
                    console.error("No controller named '" + controllerName + "'.");
                }
            } else {
                console.error("'widget-element' must define 'controller'.");
            }
        }
    });
}

exports.newContext = function (controllerName, targetEl) {
    return new MVCContext(controllerName, targetEl);
}


/**
 * Get all widgets in the specified element, or full document if inElement is not specified.
 * @param inElement The element to search for.  If undefined, the whole document is searched.
 */
function getWidgets(inElement) {
    if (inElement) {
        return jqUtil.getJQuery()("[uit-controller]", inElement);
    } else {
        return jqUtil.getJQuery()("[uit-controller]");
    }
}


function MVCContext(controllerName, targetEl) {
    if (!controllerName) {
        throw "No 'controllerName' supplied to MVCContext.";
    }
    if (!targetEl || targetEl.size() === 0) {
        throw "No 'targetEl' name supplied to MVCContext.";
    }

    this.targetEl = targetEl;
    this.controllerName = controllerName;
    this.instanceId = targetEl.attr('instanceId');
    if (!this.instanceId) {
        this.instanceId = controllerName;
    }
    this.applyCompleted = false;
}

MVCContext.prototype.isApplyCompleted = function() {
    return this.applyCompleted;
}

MVCContext.prototype.setApplyCompleted = function() {
    this.applyCompleted = true;
}

MVCContext.prototype.getContext = function(instanceId) {
    return activeContexts[instanceId];
}

MVCContext.prototype.getControllerName = function() {
    return this.controllerName;
}

MVCContext.prototype.getInstanceId = function() {
    return this.instanceId;
}

MVCContext.prototype.getTargetElement = function() {
    return this.targetEl;
}

MVCContext.prototype.getModelData = function() {
    return this.modelData;
}

MVCContext.prototype.attr = function(attributeName, defaultVal) {
    var attrVal = this.targetEl.attr(attributeName);
    if (attrVal) {
        return attrVal;
    }  else {
        return defaultVal;
    }
}

MVCContext.prototype.requiredAttr = function(attributeName) {
    var attrVal = this.targetEl.attr(attributeName);
    if (!attrVal) {
        throw "Required attribute '" +  attributeName + "' not defined on MVC controller '" + this.controllerName + "' element.";
    }
    return attrVal;
}

MVCContext.prototype.on = function(eventName, callback) {
    this.targetEl.on("jenkins:" + eventName, callback);
}

MVCContext.prototype.trigger = function(eventName, eventData) {
    this.targetEl.triggerHandler({type: "jenkins:" + eventName, eventData: eventData});
}

MVCContext.prototype.onModelChange = function(callback) {
    this.on("ModelChange", callback);
}

MVCContext.prototype.modelChange = function(modelData) {
    this.modelData = modelData;
    this.trigger("ModelChange", modelData);
}
