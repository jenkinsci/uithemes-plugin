/*
 * Copyright (C) 2014 CloudBees Inc.
 *
 * All rights reserved.
 */

var url = require('./url');
var json = require('./json');
var jqProxy = require('../jQuery');

exports.execAsyncGET = function (resPathTokens, success, params) {
    var $ = jqProxy.getJQuery();

    $.ajax({
        url: url.concatPathTokens(resPathTokens),
        type: 'get',
        dataType: 'json',
        data: params,
        success: success
    });
}

exports.execSyncPUT = function (resPathTokens, data, success, params) {
    var $ = jqProxy.getJQuery();

    var ajaxUrl = url.concatPathTokens(resPathTokens);
    if (params) {
        ajaxUrl += ('?' + url.toQueryString(params));
    }

    if (typeof data === 'object' || $.isArray(data)) {
        data = json.myStringify(data);
    }

    $.ajax({
        url: ajaxUrl,
        type: 'put',
        async: false,
        dataType: 'json',
        contentType: 'application/json',
        data: data,
        success: success
    });
}

exports.jenkinsAjaxGET = function (path, success) {
    new Ajax.Request(path, {
        method : 'get',
        onSuccess: success
    });
}

exports.jenkinsAjaxPOST = function (path, success) {
    new Ajax.Request(path, {
        method : 'post',
        onSuccess: success
    });
}