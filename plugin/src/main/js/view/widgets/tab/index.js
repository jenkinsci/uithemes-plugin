/*
 * Copyright (C) 2014 CloudBees Inc.
 *
 * All rights reserved.
 */

var jqProxy = require('../../../jQuery');

exports.activate = function(container) {
    var $ = jqProxy.getJQuery();
    var tabBar = $('.tab-bar', container);
    var tabs = $('.tab', tabBar);

    if (tabs.size() > 0) {
        _activateTab($(tabs.get(0)), container);

        tabs.click(function() {
            _activateTab($(this), container);
        });
    } else {
        alert("Cannot activate a tab section. Failed to find a set of tabs.");
    }
}

exports.activateTab = function(tabId, container) {
    var $ = jqProxy.getJQuery();
    var tabBar = $('.tab-bar', container);
    var tabIdSelector = '[tab-id="' + tabId + '"]';
    _activateTab($('.tab' + tabIdSelector, tabBar), container);
}

exports.getActiveTabId = function(container) {
    var $ = jqProxy.getJQuery();
    var activeTab = $('.tab-bar .tab.active', container);
    return activeTab.attr('tab-id');
}

function _activateTab(tab, container) {
    var $ = jqProxy.getJQuery();
    var tabId = tab.attr('tab-id');
    var tabBar = $('.tab-bar', container);
    var tabContentFrame = $('.tab-content-frame', container);
    var tabs = $('.tab', tabBar);
    var tabContentBlocks = $('.tab-content', tabContentFrame);

    tabs.removeClass('active');
    tabContentBlocks.removeClass('active');
    var tabIdSelector = '[tab-id="' + tabId + '"]';
    $('.tab' + tabIdSelector, tabBar).addClass('active');
    $('.tab-content' + tabIdSelector, tabContentFrame).addClass('active');
}
