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
* @GET
* @PUT
* @POST
* @DELETE

for servicing basic HTTP methods, and:

* @Produces
* @After
* @Before

for supporting different schemes of service requests, along with the support for the Android platform in the
form of:

* @BindService
* @ContentProvider
* @Export

These annotations will define service endpoints that will be created and served by your application. They are
similar in concept with [JAX-RS](http://en.wikipedia.org/wiki/Java_API_for_RESTful_Web_Services) annotations,
but are much more simple and easy to use.

### @GET, @PUT, @POST, @DELETE

These annotations should be placed right before method reponsible for particular HTTP method handling. Each of these annotations has one parameter in which you place path to your service. A path starts with a slash and follows the rules of the URI with the special exceptions for parameter indicators. A parameter indicator starts with a colon followed by parameter name. A special case is a parameter starting with an asterisk, which means that this parameter will consume the suffix of an URI without its arguments (specified after the question mark). There can be only one such parameter indicator in the path. The parameter can have a default value specified. The default value is specified with the equation mark. No spaces are allowed. The parameter name along with its default value can end on slash, question mark or end of path definition. Examples:

#### @GET Example without parameters
```java
@GET("/path.to.myservice")
public String myService() {
    return "Hello, world!";
}
```
Please note that the service has to return something. You can't define a service with the void method. However you can return a null value.

#### @PUT Example with parameters
```java
@PUT("/path/:name/:id")
public String dataFromUrl(int id, String name) {
	return name + id;
}
```
The parameters are identified by name and are matched with the service method parameters.

#### @DELETE Example with parameters and default values
```java
@DELETE("/something/*path=?id=1")
public int dataFromUrl(int id, String path) {
	return id + path.length();
}
```
Please be aware of the fact that if you don't define the default value of a parameter it can have a *null* value in case of a String. The definition _*path=_ is a valid definition of empty string default value.

#### @POST Example
```java
@POST("/something/:param1=1?param2=2")
public long dataFromUrl(long param1, long param2) {
	return param1 ^ param2;
}
```
Please note that in the case of POST method the paramater param2 is not sent in the URI but in the body of HTTP request.

#### The parameters, arguments and return types

The parameters specified in the path argument of an annotation are matched against method arguments. They must match exactly the count of method arguments. However, a method can have additional arguments of type: *Context*, *Request*, *Cookies* and *Cookie*. If they are present they will contain the fields associated with the request. The *Cookie* argument has to have the name matching cookie name. If this is unfeasible use Cookies argument instead.

The *Context* argument is a Android context object for accessing functions of underlying application. The *Request* is a simple wrapper around HTTP request to retrieve relevant information from HTTP request.

The types of specified parameters are automatically converted to types of matching arguments. As a type you can use all primitive types of java plus *String*, *JSONObject*, *JSONArray* and *JSONValue*.

As for method return types you can use all parameter types plus *Response* (simple wrapper around HTTP response), *File* and *InputStream*

#### Exceptions

You cant throw any type of exception from the body of a method, however only *HttpException* type from *io.reon.http* package will be interpreted as a relevant HTTP reponse error type.

### @Produces

This annotation specifies the default value of MIME data type as returned from the service method. If it is not changed in a method body it will be present in HTTP response as its *Content-Type* header value.

### @Before, @After

### @BindService

### @ContentProvider

### @Export
