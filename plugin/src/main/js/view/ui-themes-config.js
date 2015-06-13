/*
 * Copyright (C) 2013 CloudBees Inc.
 *
 * All rights reserved.
 */

var templates = require('./templates');
var tab = require('./widgets/tab');
var jqUtil = require('jenkins-js-util/jQuery');

exports.render = function (modelData, onElement) {
    var $ = jqUtil.getJQuery();
    var themesConfigMVCContext = this;

    function _render(theModel) {
        //console.log(theModel.themes[0]);
        var uiThemesConfig = templates.apply('ui-themes-config', theModel);

        onElement.empty().append(uiThemesConfig);
        tab.activate(onElement);
        $('.selection', onElement).click(function () {
            var implSelector = $(this);
            var themeName = implSelector.attr('name');
            var themeImplName = implSelector.val();
            theModel.updateImplSelection(themeName, themeImplName);
            themesConfigMVCContext.trigger("ConfigChange");
        });

        function reloadThemeCSS() {
            // Remove the uithemes css from the page and re-add it, forcing styles
            // to be reloaded.
            var head = $("head");
            var uiThemeStyle = $("link[href*='uithemes/css']", head);
            uiThemeStyle.each(function () {
                var link = $(this);
                var originalHref = link.attr('original-href');
                var href;

                link.remove();
                if (!originalHref) {
                    href = link.attr('href');
                    link.attr('original-href', href);
                } else {
                    href = originalHref;
                }

                // Add a random param to bypass caching
                link.attr('href', href + '?p=' + (new Date().getTime()));
                head.append(link);
            });
        }

        var applied = $('.applied', uiThemesConfig);
        applied.hide();

        themesConfigMVCContext.on("ConfigChange", function() {
            reloadThemeCSS();
            applied.show();
            applied.fadeOut(1500);
        });
    }

    _render(modelData);
    this.onModelChange(function(event) {
        var activeTabId = tab.getActiveTabId(onElement);
        _render(event.eventData);
        tab.activateTab(activeTabId, onElement);
    });
}
