/*
 * Copyright (C) 2014 CloudBees Inc.
 *
 * All rights reserved.
 */

var $ = require('jquery');
var theWindow;

/**
 * jQuery wrapper module.  Other modules should access jQuery via this module (i.e. never access the 'jquery'
 * module directly).  That allows us to inject the jQuery function (aka dollar) to be used by modules during
 * testing etc.
 */

/**
 * Get the jQuery function (aka dollar).
 * @returns The jQuery function (aka dollar).
 */
exports.getJQuery = function () {
    return $;
}

/**
 * Set the jQuery function (aka dollar).
 * <p/>
 * Allows us to inject a the jQuery reference function (e.g. JSDOM) for testing.  To be precise, JSDOM does not mock jQuery.
 * JSDOM mocks the window, document ala headless browser etc.  The jQuery lib it hooks in is the original jQuery lib.
 * @param jquery The jQuery function (aka dollar).
 */
exports.setJQuery = function (jquery) {
    $ = jquery
}

/**
 * Get the active window.
 * @returns The active window.
 */
exports.getWindow = function () {
    if (theWindow) {
        return theWindow;
    } else {
        return window;
    }
}

/**
 * Set the active window.
 * <p/>
 * Allows us to inject a the JSDOM window instance for testing.
 * @param theWindowToUse The window to use.
 */
exports.setWindow = function (theWindowToUse) {
    theWindow = theWindowToUse;
}

/**
 * Get all widgets in the specified element, or full document if inElement is not specified.
 * @param inElement The element to search for.  If undefined, the whole document is searched.
 */
exports.getWidgets = function (inElement) {
    if (inElement) {
        return exports.getJQuery()("[uit-controller]", inElement);
    } else {
        return exports.getJQuery()("[uit-controller]");
    }
}

/**
 * jQuery .each() wrapper.
 * @param elementList The element list to iterate.
 * @param func The function to apply to the elements.
 */
exports.forEachElement = function (elementList, func) {
    elementList.each(function() {
        func(exports.getJQuery()(this));
    });
}

function roundUp(number) {
    // only round up if there's a decimal part, otherwise return the number
    if (Math.floor(number) === number) {
        return number;
    }
    return Math.floor(number + 1);
}
function roundDown(number) {
    return Math.floor(number);
}

/**
 * Is the specified x and y coordinate "over" the supplied box coordinates.
 * <p/>
 * Useful for testing if the mouse pointer is inside a region on the page.
 * Use getElementsEnclosingBoxCoords to get the enclosing box.
 *
 * @param x The X coordinate.
 * @param y The Y coordinate.
 * @param boxCoords The box coordinates to check against.
 */
exports.isCoordInBox = function (x, y, boxCoords) {
    if (x < boxCoords.topLeft.x) {
        // to the left
        return false;
    }
    if (y < boxCoords.topLeft.y) {
        // above
        return false;
    }

    if (x > boxCoords.topRight.x) {
        // to the right
        return false;
    }
    if (y > boxCoords.bottomLeft.y) {
        // below
        return false;
    }

    return true;
}

/**
 * Get the box coordinates for an element.
 * <p/>
 * X and Y coordinates for each of the four corners, rounded up and down so as to get
 * the "outside" of the box.
 * @param element The element.
 * @returns The element's box coordinates.
 */
exports.getElementBoxCoords = function (element) {
    var elementOffset = element.offset();

    var topLeft = {
        x: roundDown(elementOffset.left),
        y: roundDown(elementOffset.top)
    };
    var bottomLeft = {
        x: topLeft.x,
        y: roundUp(elementOffset.top + element.height())
    };
    var topRight = {
        x: roundUp(elementOffset.left + element.width()),
        y: topLeft.y
    };
    var bottomRight = {
        x: topRight.x,
        y: bottomLeft.y
    };

    return {
        topLeft: topLeft,
        bottomLeft: bottomLeft,
        topRight: topRight,
        bottomRight: bottomRight
    };
}

/**
 * Get the coordinates of a box that encloses all the supplied elements
 * @param elements The elements.
 */
exports.getElementsEnclosingBoxCoords = function(elements) {
    var boxCoords = exports.getElementBoxCoords(elements[0]);

    for (var i = 1; i < elements.length; i++) {
        var nextElCoords = exports.getElementBoxCoords(elements[i]);

        boxCoords.topLeft.x = Math.min(boxCoords.topLeft.x, nextElCoords.topLeft.x);
        boxCoords.topLeft.y = Math.min(boxCoords.topLeft.y, nextElCoords.topLeft.y);
        boxCoords.bottomLeft.x = boxCoords.topLeft.x;
        boxCoords.bottomLeft.y = Math.max(boxCoords.bottomLeft.y, nextElCoords.bottomLeft.y);
        boxCoords.topRight.x = Math.max(boxCoords.topRight.x, nextElCoords.topRight.x);
        boxCoords.topRight.y = boxCoords.topLeft.y;
        boxCoords.bottomRight.x = boxCoords.topRight.x;
        boxCoords.bottomRight.y = boxCoords.bottomLeft.y;
    }

    return boxCoords;
}

/**
 * Stretch the supplied boxCoords in the specified direction.
 * @param boxCoords The box coordinates to stretch.
 * @param direction The direction in which to stretch.  One of 'left', 'right',
 * 'up' or 'down'.
 * @param to The x or y coordinate to which the box is to be stretched.  The interpretation
 * of this depends on the 'direction' parameter.
 */
exports.stretchBoxCoords = function (boxCoords, direction, to) {
    if (direction === 'left') {
        // 'to' is a x coordinate
        boxCoords.topLeft.x = to;
        boxCoords.bottomLeft.x = to;
    } else if (direction === 'right') {
        // 'to' is a x coordinate
        boxCoords.topRight.x = to;
        boxCoords.bottomRight.x = to;
    } else if (direction === 'up') {
        // 'to' is a y coordinate
        boxCoords.topLeft.y = to;
        boxCoords.topRight.y = to;
    } else if (direction === 'down') {
        // 'to' is a y coordinate
        boxCoords.bottomLeft.y = to;
        boxCoords.bottomRight.y = to;
    }
}

/**
 * Extract the element attributes as a JSON object.
 * @param element The element from which to extract an object.
 * @param requiredAttrs The element attributes to be mapped, or a function to be
 * called to get the list of attributes.
 */
exports.toObject = function (element, requiredAttrs) {
    var object = {};
    if (requiredAttrs !== undefined) {
        if (typeof requiredAttrs === 'function') {
            requiredAttrs = requiredAttrs();
        }

        for (var i = 0; i < requiredAttrs.length; i++) {
            if (!exports.attrToObject(element, requiredAttrs[i], object)) {
                console.error("Required attribute '" + requiredAttrs[i] + "' not defined on element.");
            }
        }
    }
    return object;
}

/**
 * Map a single attribute value to the supplied object
 * @param element The element from which to extract the attribute value.
 * @param attrName The name of the attribute whose value is to be mapped.
 * @param object The target object.
 * @return True if a value is mapped to the target object, otherwise false.
 */
exports.attrToObject = function (element, attrName, object) {
    var attrVal = element.attr(attrName);

    if (!attrVal) {
        return false;
    }

    object[attrName] = attrVal;
    return true;
}

/**
 * Provide the ability to find out where the mouse is.
 */
var lastKnownXY = {
    x: undefined,
    y: undefined
};
exports.getMouseTracker = function() {
    if (lastKnownXY.x === undefined) {

        exports.getJQuery()('body').mousemove(function(event) {
            lastKnownXY.x = event.pageX;
            lastKnownXY.y = event.pageY;
        });
    }
    return lastKnownXY;
}