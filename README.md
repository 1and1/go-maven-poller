*Current build status:* [![Build Status](https://travis-ci.org/1and1/go-maven-poller.svg)](https://travis-ci.org/1and1/go-maven-poller)

## Go Maven Poller

This is Maven repository package plugin for [Go CD](http://www.go.cd/) and a remix of the [aresok's](https://github.com/aresok/go-maven-poller) codebase. The major differences are:

* Usage of Maven instead of Ant
* Added proxy option to repository configuration
* This plugin parses the *maven-metadata.xml* of a Maven repository instead of using the Nexus API and therefore can be used for a broader range of *Artifactory* and *Nexus* repositories. 
* Use of [GoCD JSON API](http://www.go.cd/documentation/developer/writing_go_plugins/package_material/json_message_based_package_material_extension.html)

Though the plugin is working for at least one Artifactory repository and the Maven Central, which it was tested on, it may not be perfect and we'll be working on it. In the meantime feel free and help us out with your pull requests!

Test on following repositories:

* Maven Central ([Link](https://repo1.maven.org/maven2/))
* JBoss Nexus ([Link](https://repository.jboss.org/nexus/content/repositories/))
* Bintray Joda ([Link](http://dl.bintray.com/jodastephen/maven/))
* Artifactory


### Installation
Just checkout the README of [this](https://github.com/aresok/go-maven-poller) repository.
