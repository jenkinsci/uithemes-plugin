/*
 * Copyright (C) 2013 CloudBees Inc.
 *
 * All rights reserved.
 */

exports.myStringify = function (object) {
    // This is a hack because prototypeJS stupidly modifies Array.prototype.toJSON with a daft
    // modification that results in arrays being serialized as string, so when deserialized they are strings.

    var arrayToJSONFunc = Array.prototype.toJSON;
    try {
        delete Array.prototype.toJSON;
        return JSON.stringify(object);
    } finally {
        Array.prototype.toJSON = arrayToJSONFunc;
    }
}