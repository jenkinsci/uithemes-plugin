/* jslint node: true */
/* global describe, it, expect */

"use strict";

var helper = require('../../helper');
var tab = helper.require('view/widgets/tab');

describe("view/widgets/tab", function () {

    var sample1 = helper.requireTestRes('widgets/tab/sample-01.html');

    it("- test", function (done) {

        helper.testWithJQuery(sample1, function ($) {
            var tabContainer = $('#tab-container');

            function getTab(tabId) {
                var tabIdSelector = '[tab-id="' + tabId + '"]';
                return $('.tab' + tabIdSelector, tabContainer);
            }
            function getTabContent(tabId) {
                var tabIdSelector = '[tab-id="' + tabId + '"]';
                return $('.tab-content' + tabIdSelector, tabContainer);
            }
            function isActive(tabId) {
                return (getTab(tabId, tabContainer).hasClass('active') &&
                        getTabContent(tabId, tabContainer).hasClass('active'));
            }

            tab.activate();

            // First tab should be active by default
            expect(isActive("1")).toBe(true);
            expect(isActive("2")).toBe(false);

            // Click tab "2" and check it becomes active.
            getTab("2").click();
            expect(isActive("1")).toBe(false);
            expect(isActive("2")).toBe(true);

            done();
        });
    });

});
