# TomTom IndiGO example applications

## Introduction

This repository contains the source code of example application built on TomTom IndiGO platform.

### `buildSrc/`

Code used by Gradle to sync the Gradle modules and build the app.

### `build-logic/`

Code listing the different repositories and module versions needed for building.

### `gradle/`

The Gradle wrapper used to sync the Gradle modules and build the app.

### `examples/`

The root directory for all the example apps. This folder can be removed if only the template
application (in the `template/` folder) is needed, see below.

### `template/`

This is a template application to create your own IVI application, it contains the minimum needed
to create a TomTom IndiGO application with all the default functionality from the platform.

### `keystore/`

The keys needed to sign the application with the same keys as the TomTom Indigo reference hardware
build (see
[Flashing the reference hardware](https://developer.tomtom.com/tomtom-indigo/documentation/integrating-tomtom-indigo/flashing-the-reference-hardware))
for more details on the reference hardware).

### `permissions/`

Platform permissions needed on hardware for some of the functionality (related to media) to work.
This file will work with the template app, if another example app is used, please update the
package name at the top of the file. See
[Installing TomTom IndiGO on Hardware](https://developer.tomtom.com/tomtom-indigo/documentation/integrating-tomtom-indigo/installing-tomtom-indigo-on-hardware)
for more information about the permissions.

## Building and running

In order to build the project using dependencies from the IVI Nexus repository, credentials need to
be provided to access this repository. This can be done in different ways:

### `Store credentials in ~/.gradle/gradle.properties`

```bash
nexusUsername=<username>
nexusPassword=<password>
```

__Note:__ On some operating systems, a hash character `#` in the username or password must be
escaped as `\#` to be properly recognized.

And then build with:

```bash
./gradlew build
```

### `Specify credentials through the commandline`

```bash
./gradlew -PnexusUsername=<username> -PnexusPassword=<password> build
```

## Setting up the development environment

For the entire setup process, please consult the TomTom IndiGO developer portal:
[Getting Started](https://developer.tomtom.com/tomtom-indigo/documentation/getting-started/introduction)

## Copyright

Copyright © 2022 TomTom NV. All rights reserved.

This software is the proprietary copyright of TomTom NV and its subsidiaries and may be
used for internal evaluation purposes or commercial use strictly subject to separate
license agreement between you and TomTom NV. If you are the licensee, you are only permitted
to use this software in accordance with the terms of your license agreement. If you are
not the licensee, you are not authorized to use this software in any manner and should
immediately return or destroy it.
