Go Maven Poller
===================

[![Build Status](https://travis-ci.org/1and1/go-maven-poller.svg)](https://travis-ci.org/1and1/go-maven-poller)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.oneandone/go-maven-poller/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.oneandone/go-maven-poller) 
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/3384a8a750e34feaadfa5647eff4f3c3)](https://www.codacy.com/app/Stephan-FuhrmannOrganization/go-maven-poller?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=1and1/go-maven-poller&amp;utm_campaign=Badge_Grade)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

This is a Maven repository package plugin for [Go CD](http://www.go.cd/) and a remix of the [aresok's](https://github.com/aresok/go-maven-poller) codebase. The major differences are:

* Building with Maven instead of Ant
* Added proxy option to repository configuration
* This plugin parses the *maven-metadata.xml* of a Maven repository instead of using the Nexus API and therefore can be used for a broader range of *Artifactory* and *Nexus* repositories. 
* Use of GoCD JSON API

Tested on following repositories:

* Maven Central ([Link](https://repo1.maven.org/maven2/))
* JBoss Nexus ([Link](https://repository.jboss.org/nexus/content/repositories/))
* Bintray jCenter ([Link](http://jcenter.bintray.com/))
* Artifactory ([Link](https://jfrog.com/artifactory/))

### Requirements

The go-maven-poller requires at least the following environment:
* GoCD 17.4.0 and later
* JDK 8 - 12 and later (the JDK GoCD is running with)

### Installation

Download [`go-maven-poller.jar`](https://github.com/1and1/go-maven-poller/releases) into the `plugins/external` directory of your GoCD server and restart it.

You can also download a signed jar from the [maven repository](https://repo1.maven.org/maven2/com/oneandone/go-maven-poller/) and verify the GPG signature with my [GPG public key](https://github.com/sfuhrm.gpg).

### Repository definition

Repo URL must be a valid http or https URL. Basic authentication (user:password@host/path) is supported.
You may specify a proxy. If your GoCD server system doesn't use the same timezone as the repository, you may set
a specific time zone.
If 'Latest version Tag' is specified, the value of it will be used to determine, if new version is available. It will be not compared to other versions of the package.

![Add a Maven repository][1]

### Package definition

Group Id and Artifact Id refer to the corresponding entries in `pom.xml`. 
Click check package to make sure the plugin understands what you are looking for.
You may set lower and upper version bounds to further narrow down the versions you
want to look for.

![Define a package as material for a pipeline][2]

### Published Environment Variables

The following information is made available as environment variables for tasks:

```
GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_LABEL
GO_REPO_<REPO-NAME>_<PACKAGE-NAME>_REPO_URL
GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_GROUP_ID
GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_ARTIFACT_ID
GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_PACKAGING
GO_PACKAGE_<REPO-NAME>_<PACKAGE-NAME>_LOCATION
```
The LOCATION variable points to a downloadable url.

### Downloading the Package

To download the package locally on the agent, we could write a curl (or wget) task like this:

![Download artifact][3]

```xml
            <exec command="/bin/bash" >
            <arg>-c</arg>
            <arg>curl -o /tmp/mypkg.jar $GO_PACKAGE_REPONAME_PKGNAME_LOCATION</arg>
            </exec>
```

When the task executes on the agent, the environment variables get substituted and the package gets downloaded.
          
### Notes

This plugin will detect at max one package revision per minute (the default interval at which Go materials poll). If multiple versions of a package get published to a repo in the time interval between two polls, Go will only register the latest version in that interval.

[1]: img/add-repo.png  "Define Maven Package Repository"
[2]: img/add-pkgs.png  "Define package as material for a pipeline"
[3]: img/download.png  "Download artifact"
