# REON.IO SDK

(work in progress)

REON.IO is a connectivity platform and application framework for devices. This SDK will help you to build
mobile and web applications that not only work with REON.IO platform, but provide portable, secure remote
control and access capabilities.

Bear in mind that this is alpha quality software and things can and will rapidly change in time.

## How to build

You will require the following to build REON.IO SDK:
* Latest stable [Oracle JDK](http://www.oracle.com/technetwork/java/)
* Latest Google [Android SDK](http://developer.android.com/sdk)

The project is built using [Gradle](http://gradle.org/). To build it, type:

```
 $ ./gradlew build
```

You don't always need to do this as there are prebuilt artifacts available at
[JCenter](http://bintray.com/bintray/jcenter). The latest version is
[here](http://bintray.com/reon-io/reon-sdk/reon-sdk/view).

## How to use

To integrate REON.IO SDK with your application add the following dependencies to your main build.gradle file:
```
buildscript {
	repositories {
		jcenter()
	}
	dependencies {
		classpath 'com.android.tools.build:gradle:1.3.0'
		classpath 'com.neenbedankt.gradle.plugins:android-apt:1.4'
	}
}
```

Add the following dependencies to your application module build.gradle file (replacing SNAPSHOT with the
current version of REON.IO SDK):

```
apply plugin: 'com.neenbedankt.android-apt'

dependencies {
	compile 'io.reon:reon-api:0.3-SNAPSHOT'
	compile 'io.reon:reon-api-java:0.3-SNAPSHOT'
	compile 'io.reon:reon-api-android:0.3-SNAPSHOT'
	apt 'io.reon:reon-compiler:0.3-SNAPSHOT'
}
```

You don't always need all the artifacts and plugins if you are building for the java platform only. Experiment
by yourself.

## How to develop with REON.IO SDK

This SDK gives you the possibility to develop standalone, web, mobile or hybrid (web frontend / mobile backend
or mobile frontend / web backend) applications for REON.IO platform. The application consists of:
* frontend (web or mobile) module, for interaction with the user
* backend (web or mobile) module, for interaction with the device
* the proxy (as a standalone REON.IO application) for secure, peer to peer connectivity.

Each REON.IO application is advertising their services (RESTfuly) through REON.IO platform. Application can
only connect to REON.IO app or its instances on a different nodes. The application is identified by its
package name and by convention all its web services are placed under adequate web context:

```
http://localhost/my.reon.app.package
```
This context is only available to REON.IO app and can't be used externaly as it requires authorization by
a security token.

You can easily define web services through REON.IO annotations, which are:
* @@GET
* @@PUT
* @@POST
* @@DELETE
for servicing basic HTTP methods, and:
* @@Produces
* @@After
* @@Before
for supporting different schemes of service requests, along with the support for the Android platform in the
form of:
* @@BindService
* @@ContentProvider
* @@Export

These annotations will define service endpoints that will be created and served by your application. They are
similar in concept with [JAX-RS](http://en.wikipedia.org/wiki/Java_API_for_RESTful_Web_Services) annotations,
but are much more simple and easy to use.

### @@GET, @@PUT, @@POST, @@DELETE

### @@Produces

### @@Before, @@After

### @@BindService

### @@ContentProvider

### @@Export
