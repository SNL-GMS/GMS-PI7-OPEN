# An opinionated library for making Java HTTP services with some behavior conventions and defaults.

## High-level goals
- **Separate service implementation logic from HTTP specific logic** 

    Avoid having all service code deal with parsing requests, extracting headers, building response bodies, knowing about status code numbers.
- **Support testable service implementations** 

    Turn the typical service implementation into standard Java code that can be unit tested without a webserver.  Encourage a declarative approach to defining the service to improve testability and readability.
- **Increase consistency among services:**

    Services built using this library should share at least the above attributes.
- **Provide sensible defaults (convention over configuration)**
    
    Particularly server attributes such as port number and thread pool configuration
- **Encapsulate users of the library from specifics of the underlying HTTP library:** 
    It may prove difficult to replace the underlying HTTP library and maintain the same interfaces of this library, but an attempt is made at that ability.  This attribute hopefully also simplifies the setup logic for the HTTP service by users of the library.

## Technical decisions and 'opinions':
- **Only allow supported HTTP verbs** 

    For GMS, that's just `POST`, thus the verb is not part of the route definition
- **Automatically return appropriately encoded response based on `Accept` header of the request** 

    Either JSON or msgpack response can be returned from any endpoint, controlled by the `Accept` header of the request; the route handler code does not have to implement this functionality
- **Provide appropriate deserializer based on Content-Type** 

    An `ObjectMapper` is provided to the route handler for use in deserialization that is appropriately selected based on the `Content-Type` of the request
- **Defaults for some common server attributes** 

    Port of `8080`, sensible HTTP thread pool sizes, etc.
- **Define the service using a value object** 

    The service is defined through a value class called `ServiceDefinition`, and `Service.start` takes this as input to instantiate the service
- **Provide validation on server properties** 

- **Provide a standard health check route**

    Always implement a standardized route (e.g. `/alive`) that is used for service healthchecks via e.g. Docker.

    Validate e.g. port number and route URL's *before* sending to underlying HTTP service library
- **(future) Eliminate management of serialization technology (e.g. `ObjectMapper`'s) in service implementation** 

    Use a serialization object with standard project configuration internally in this library
    (note: this is not implemented; even if it is, the ability to override the `ObjectMapper`'s used internally would likely remain)

# Developer usage guide

## Reading HTTP requests with the [Request](java/gms/shared/frameworks/frameworks-http-service-library/src/main/java/gms/shared/frameworks/service/Request.java) interface
The `Request` interface defines methods to extract information from HTTP requests without knowing the details of a particular HTTP server implementation.  You can get the body of the request as a `String` (via `getBody()`) or raw `byte[]` (via `getRawBody()`).  You can get a particular HTTP header (`Optional<String> getHeader(String name)`) or ask for all of them (`Map<String, String> getHeaders()`).  You can also ask for the `ContentType` of the request, although in practice the library looks at this for you and gives you an appropriate deserializer.

`Request` is immutable.

## Returning HTTP responses with [Response](java/gms/shared/frameworks/frameworks-http-service-library/src/main/java/gms/shared/frameworks/service/Response.java)
A Response is made of an [HttpStatus Code](java/gms/shared/frameworks/frameworks-http-service-library/src/main/java/gms/shared/frameworks/service/HttpStatus.java#L93) (Enum) and either a body or an error message.  `Response` has a type parameter `T` that specifies the type of the body.  `Optional<String> getErrorMessage()` retrieves the error message of the response if there is an error.  A `Response` has *either* a body (e.g. on success) or an error message; this means only one of the `Optional`'s for body and error message should be present for an instance of `Response`.  This property is enforced by the exposed static creation methods on the `Response`: there is no creation operation that takes both an error message and a body.

There are several static creation methods on the `Response` class (no constructor), all return `Response<T>`:
- `Response.success(T body)`

    Indicates successful handling of a request.  It sets the status code to `OK` (200), the body to the provided one, and the error message to `Optional.empty`.
- `Response.clientError(String msg)`

    Indicates the request is considered invalid.  Sets the status code to `BAD_REQUEST` (400), the body to `Optional.empty`, and the error message to the provided one.
- `Response.serverError(String msg)`

    Indicates the server hit an unexpected error.  This is used internally in this library if a `RequestHandler` throws an Exception.  Sets the status code to `INTERNAL_SERVER_ERROR` (500), the body to `Optional.empty`, and the error message to the provided one.
- `Response.error(HttpStatus.Code statusCode, String msg)`
 
    Used for an arbitrary error response that isn't a `BAD_REQUEST` or `INTERNAL_SERVER_ERROR`.  Sets the status code and error message to what is provided, body to `Optional.empty`.  The status code must be in the range `[400, 599]` (the error range for HTTP codes).

`Response` is immutable.

## Transforming a `Request` into a `Response`: the [RequestHandler](java/gms/shared/frameworks/frameworks-http-service-library/src/main/java/gms/shared/frameworks/service/RequestHandler.java) interface

`RequestHandler` is a `@FunctionalInterface` that takes a `Request` and a deserializer and returns a `Response`.  It is defined as:
```java
Response<R> handle(Request request, ObjectMapper deserializer);
```

`RequestHandler`'s are what define the behavior of each service endpoint (path).  They are used as an attribute of `Route` to specify the code to call when a request comes to a particular HTTP endpoint.  As a `@FunctionalInterface`, a lambda function or method reference of the proper signature can be used as a `RequeestHandler` (rather than making a class that implements `RequestHandler`).

### Relate a path to a `RequestHandler` with [Route](java/gms/shared/frameworks/frameworks-http-service-library/src/main/java/gms/shared/frameworks/service/Route.java)

A `Route` contains two things: a path string such as `/some/endpoint`, and a `RequestHandler`.  The meaning of a `Route` is that requests that arrive at the specific path will be directed to be handled by the specified `RequestHandler`.  If the path string doesn't start with `/`, it will be prepended with that leading slash.

## Declaring the service with [ServiceDefinition](java/gms/shared/frameworks/frameworks-http-service-library/src/main/java/gms/shared/frameworks/service/ServiceDefinition.java)
The attributes of the service are defined in `ServiceDefinition`.  All fields in this class except the exposed `ObjectMapper`'s are immutable.  This class defines properties such as:
- Settings for the HTTP server, such as port number it runs on and thread pool configuration
- Serialization objects (`ObjectMapper`'s) to use for JSON and msgpack
- A set of `Route`'s, which define the behavior of the service

A Builder is provided to construct `ServiceDefinition`'s.  Some usage examples:

Create a basic definition with only defaults:
```java
ServiceDefinition def = ServiceDefinition.builder().build();
```
Create a definition with one route and a particular port number:
```java
ServiceDefinition def = ServiceDefinition.builder()
  .setRoutes(Set.of(Route.create("/foo", SomeClass::foo)))  // handler provided via method reference; could also be an inline lambda function or class that implements RequestHandler
  .setPort(8081)
  .build();
```
Create a definition with multiple routes, custom serializers, customize HTTP properties:
```java
Set<Route> routes = ....   // your stuff
ServiceDefinition def = ServiceDefinition.builder()
  .setRoutes(routes)
  .setJsonMapper(yourJsonObjMapper)
  .setMsgpackMapper(yourMsgpackMapper)
  .setMinThreadPoolSize(50)
  .setMaxThreadPoolSize(100)
  .setThreadIdleTimeoutMillis(10000)
  .build();
```

Passing null values into any Builder method throws an unchecked exception.  There is also additional validation in `build()`, such as that the port number and thread pool configuration is valid.

## Controlling the application with the [HttpService](java/gms/shared/frameworks/frameworks-http-service-library/src/main/java/gms/shared/frameworks/service/HttpService.java) class

The `HttpService` class contains methods to start the service, stop the service, and determine if the service is running.  It's constructor takes a single argument, a `ServiceDefinition`.

Once created, the service can be started; if the service is running (i.e. this method has been called before), an exception is thrown.
```java
ServiceDefinition def = ...
HttpService myService = new HttpService(def);
myService.start();
System.out.println("Service running: " + myService.isRunning());  // prints "Service running: true"
```

Stop the service; if it is not running, this is a no-op.
```java
myService.stop();
System.out.println("Service running: " + myService.isRunning());  // prints "Service running: false"
```