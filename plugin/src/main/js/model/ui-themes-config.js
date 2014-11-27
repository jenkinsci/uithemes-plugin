/*
 * Copyright (C) 2014 CloudBees Inc.
 *
 * All rights reserved.
 */

var restApi = require('./rest-api');

exports.getModelData = function (callback) {
    var mvcContext = this;
    var userUrl = mvcContext.requiredAttr("objectUrl");

    restApi.getUserThemesConfig(userUrl, function(userThemesConfig) {
        if (userThemesConfig.status === "OK") {
            restApi.getUserAvailableThemes(userUrl, function(availableThemes) {
                if (availableThemes.status === "OK") {
                    var userThemeSelections = userThemesConfig.data.userThemes;
                    var availableThemes = availableThemes.data.themes;
                    var mashedupDataModel = mashupUserThemeData(availableThemes, userThemeSelections);

                    var updateImplSelectionFunc = function (themeName, newSelection) {
                        var theme = getTheme(themeName, availableThemes);
                        if (theme) {
                            if (theme.implSelection === newSelection) {
                                return;
                            }

                            var selection = getImplSelection(themeName, userThemeSelections);
                            if (selection) {
                                // this is the existing selection... not new... ignore
                                if (selection.implName === newSelection) {
                                    return;
                                }
                                selection.implName = newSelection;
                            } else {
                                userThemeSelections.push({
                                    themeName: themeName,
                                    implName: newSelection
                                });
                            }
                            theme.implSelection = newSelection;

                            restApi.putUserThemesConfig(userUrl, {userThemes: userThemeSelections}, function () {
                                // Remash and fire model change event
                                mashedupDataModel = mashupUserThemeData(availableThemes, userThemeSelections);
                                mvcContext.modelChange({
                                    themes: mashedupDataModel,
                                    updateImplSelection: updateImplSelectionFunc
                                });
                            });
                        }
                    };

                    callback({
                        themes: mashedupDataModel,
                        updateImplSelection: updateImplSelectionFunc
                    });
                }
            });
        }
    });
}

function mashupUserThemeData(availableThemes, selections) {
    function mashupTheme(theme) {
        var selection = getImplSelection(theme.name, selections);
        if (selection) {
            theme.implSelection = selection.implName;
        } else {
            theme.implSelection = theme.defaultImpl;
        }

        for (var i = 0; i < theme.implementations.length; i++) {
            theme.implementations[i].isSelection = (theme.implementations[i].name === theme.implSelection);
        }
    }

    for (var i = 0; i < availableThemes.length; i++) {
        mashupTheme(availableThemes[i]);
    }

    return availableThemes;
}

function getTheme(themeName, themes) {
    for (var i = 0; i < themes.length; i++) {
        if (themes[i].name === themeName) {
            return themes[i];
        }
    }
}

function getImplSelection(themeName, selections) {
    for (var i = 0; i < selections.length; i++) {
        if (selections[i].themeName === themeName) {
            return selections[i];
        }
    }
    return undefined;
}
