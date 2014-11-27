/*
 * Copyright (C) 2014 CloudBees Inc.
 *
 * All rights reserved.
 */
var gulp = require('gulp');
var gutil = require('gulp-util');
var browserifyFactory = require('browserify');
var hbsfy = require("hbsfy");
var source = require('vinyl-source-stream');
var jasmine = require('gulp-jasmine-phantom');
var junitReporter = require('./gulp/junit_reporter');
var less = require('gulp-less');

var testSources = 'src/test/js/**/*';
var testSpecs = 'src/test/js/**/*-spec.js';
var testResources = 'src/test/resources/**/*';

var adjunctOutputDir = './target/generated-adjuncts/org/jenkinsci/plugins/uithemes/ui';

gulp.task('default', ['build', 'test']);

gulp.task('build', function () {
    return buildUIThemes();
});

gulp.task('less', function () {
    bundleCSS();
});

gulp.task('test', function () {
    // Run all the tests
    return runTestSpecs(testSpecs, {
                reporter: new junitReporter.JUnitXmlReporter({
                    savePath: 'target/surefire-reports',
                    filePrefix: 'JasmineReport'
                })
            });
});

gulp.task('watch', function () {
    watchSrc();
    watchTests();
});

function buildUIThemes() {
    var browserify = createUIThemesBrowserify();
    bundleCSS();
    return bundleUIThemes(browserify);
}

function watchSrc() {
    var browserify = createUIThemesBrowserify();

    gulp.watch(['./src/main/**/*.js', './src/main/**/*.hbs'], function(event) {
        gutil.log('Changes detected in UIThemes source. Rerunning tests and repackaging UI resources.\n\t' + event.path);
        // Run all tests on a src change
        bundleUIThemes(browserify, true);
        runTestSpecs(testSpecs);
    });
    gutil.log('Watching UIThemes source for changes.');
    watchCSS();
}

function watchCSS() {
    gulp.watch('./src/main/**/*.less', function(event) {
        gutil.log('Changes detected in UIThemes LESS source. Reassembling styles.\n\t' + event.path);
        bundleCSS();
    });
    gutil.log('Watching UIThemes LESS source changes.');
}

function watchTests() {
    // Watch all test specs for change, but only run the changed file
    gulp.watch(testSources, function(event) {
        if (event.path.slice(0 - '-spec.js'.length) === '-spec.js') {
            // Spec source change.  Rerun that test only...
            runTestSpecs(event.path);
        } else {
            // Non spec source change.  Rerun all tests...
            runTestSpecs(testSpecs);
        }
    });
    // Watch all test resources for change, rerunning all tests on change
    gulp.watch(testResources, function() {
        runTestSpecs(testSpecs);
    });
    gutil.log('Watching UIThemes test specs for changes.');
}

function runTestSpecs(testsToRun, jasmineOptions) {
    if (testsToRun === testSpecs) {
        gutil.log('Running all test spec (' + testsToRun + ').');
    } else {
        gutil.log('Running test spec: ' + testsToRun);
    }

    jasmineOptions = jasmineOptions || {};
    jasmineOptions.integration = false;
    jasmineOptions.keepRunner = true;

    function jasmineRun(runSrc) {
        return gulp.src(runSrc)
            .pipe(jasmine(jasmineOptions)
                .on('error', function () {
                        gutil.log('Finish time: ' + new Date());
                    })
            );
    }

    // Setup the test env before running.
    jasmineRun('src/test/js/test-env.js');

    return jasmineRun(testsToRun);
}

function createUIThemesBrowserify() {
    gutil.log('Creating Browserify instance.');
    var browserify = browserifyFactory({
        entries: ['./src/main/js/ui-themes.js'],
        extensions: ['.js', '.hbs'],
        cache: {},
        packageCache: {},
        fullPaths: true
    });
    browserify.transform(hbsfy);
    return browserify;
}

function bundleUIThemes(browserify, continueOnError) {
    gutil.log('Bundling UIThemes source code (using Browserify) to ' + adjunctOutputDir);

    var bundler = browserify.bundle();
    if (continueOnError) {
        // See http://truongtx.me/2014/07/15/handle-errors-while-using-gulp-watch/
        bundler.on('error', function (err) {
            logError(err);
            this.end();
        });
    }
    return bundler.pipe(source('ui-themes.js'))
        .pipe(gulp.dest(adjunctOutputDir));
}

function bundleCSS() {
    gulp.src('./src/main/less/ui-themes.less')
        .pipe(less())
        .pipe(gulp.dest(adjunctOutputDir));
}

function logError(err) {
    gutil.log(gutil.colors.red('Error: ' + err.message));
}
