#Jenkins UI Themes

This repository contains components that, when combined, provide the basis on which Jenkins can offer
per-login customizable/configurable UI "themes".

We've already integrated it into Jenkins Core on a WIP branch at [tfennelly:JENKINS-24143-uithemes-v2](https://github.com/tfennelly/jenkins/tree/JENKINS-24143-uithemes-v2).
Simply pull, build and run:

```
git checkout -b JENKINS-24143-uithemes-v2
git pull https://github.com/tfennelly/jenkins.git JENKINS-24143-uithemes-v2
mvn clean install -DskipTests=true
```

Note, before pulling, you may need to reset the head revision on your branch so as to line up with the revision history of
[tfennelly:JENKINS-24143-uithemes-v2](https://github.com/tfennelly/jenkins/tree/JENKINS-24143-uithemes-v2).

The following is an example screennshot of the user UI Themes Configuration screen on [tfennelly:JENKINS-24143-uithemes-v2](https://github.com/tfennelly/jenkins/tree/JENKINS-24143-uithemes-v2).

![config-screen](./images/config-screen.png)

And here's a short screen recording:

<a href="http://youtu.be/ZlD0zf1VCAs" target="_blank"><img src="http://img.youtube.com/vi/ZlD0zf1VCAs/0.jpg" /></a>

[tfennelly:JENKINS-24143-uithemes-v2](https://github.com/tfennelly/jenkins/tree/JENKINS-24143-uithemes-v2) contains a number of themes, some of which have multiple
implementations:

* __Page Header__: Page header theme. Two implementations:
    * Classic: The standard black header and logo. Logo, background and font color are configurable. (defined in Jenkins core)
    * Lite: A lighter colored implementation (as in screenshot above). Logo is configurable. (defined in Jenkins core)
* __Icons__: Icons theme. Two implementations:
    * Classic: The standard Jenkins icons. (defined in Jenkins core)
    * Font Awesome: A set of icons derived from the [Font Awesome](http://fortawesome.github.io/Font-Awesome/) scalable vector icons (as in screenshot above). Defined in [font-awesome-icons-plugin](https://github.com/jenkinsci/font-awesome-icons-plugin).
* __Status Balls/Orbs__: Status indicators e.g. build status. Two implementations:
    * Classic: The image based icons. (defined in Jenkins core)
    * CSS3 Animations: Pure CSS3 animated status icons i.e. no images required. All status colors are configurable, allowing color blind users to configure to taste (see config screen in above screenshot). (defined in Jenkins core)
* __Console__: Console/terminal output theme. Two implementations:
    * Classic: The standard console styling with a white background. (defined in Jenkins core)
    * Dark: A dark version from Kevin Burke's (@kevinburke) Jenkins pull request [#1272](https://github.com/jenkinsci/jenkins/pull/1272). (defined in Jenkins core)

# Configuring Theme Config Defaults

If a given user has not configured a theme implementation, this plugin will check for that theme implementation config on the `anonymous` user and will use that if present.
Therefore, if you want to configure e.g. the default icon in the top navbar for all users, simply configure the relevant theme implementations on the `anonymous` user.

# Architecture
__TODO__
