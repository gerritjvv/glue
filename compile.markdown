---
layout: doc
title: Compile Glue
permalink: compile.html
category: tutorial
---


{% include nav.markdown %}


#Introduction



#Pre Compile Setup

Make sure you have at least Java 1.6 or higher and Maven 2 installed.

Glue has not been tested against the openjdk so please use the Oracle Java SDK.

Some dependencies might not always be in the mave repositories, for these use cases each project will provide a "deps" folder with an "install.sh" file.

For glue-unit please run "glue-unit/deps/install.sh"


Important: Please go into each project and run mvn clean install, do not run it from the parent dir.

# Directory strucure


The top directories are

* core
* gluecron
* glue-ui


## core

All core projects are maven projects.

Core contains the glue server code and is split into several sub projects.

The glue-rest project is where the glue packages are built from and contains the main class and rest runtime setup.

glue-unit contains the workflow engine code and generic interfaces that are used by the other sub projects.

## gluecron

Glue cron contains the data driven code and server instance.


## glue-ui

The glue-ui is written in grails and requires a grails 2 framework installation before you can compile and build it.

The built unit is a war file.


## Building RPM or DEB

RPMs or DEBs can be built for glue-rest and gluecron


cd into the correct project and run mvn rpm:rpm

for a deb run the alien command on the rpm

