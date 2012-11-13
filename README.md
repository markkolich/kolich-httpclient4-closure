# kolich-httpclient4-closure

A convenient Java wrapper around the Apache Commons HttpClient 4.x library.

As is, using HttpClient is often cumbersome and bulky for typical `HEAD`, `GET`, `POST`, `PUT` and `DELETE` operations.  For example, it often takes multiple lines of boiler plate Java to send a simple `GET` request, check the resulting status code, read a response (if any) and close/free the connection back into the connection pool.

In most scenarios, the typical HttpClient usage pattern almost always involves:

1. Creating a new `HttpHead`, `HttpGet`, `HttpPost`, `HttpPut`, or `HttpDelete` instance specific to the operation.
2. Setting an request body ("entity") to be sent with the request, if any.
3. Actually sending the request.
4. Checking the HTTP response status code.  If successful, do something.  If not successful, do something else.
5. Reading a response entity, if any.  If one exists, convert it to a `String` so you can do something with it.
6. Freeing the response and releasing the connection back into the underlying thread-safe connection pool.

In *most* cases, you want to avoid all of this boiler plate and just freakin' send HTTP requests without worrying about the internal wiring of HttpClient.  The intent of this library is to let you do that in a cleaner, repeatable and understandable way. 

## Latest Version

The latest stable version of this library is <a href="http://markkolich.github.com/repo/com/kolich/kolich-httpclient4-closure/0.0.2">0.0.2</a>.

## Resolvers

If you wish to use this artifact, you can easily add it to your existing Maven or SBT project using <a href="https://github.com/markkolich/markkolich.github.com#marks-maven2-repository">my GitHub hosted Maven2 repository</a>.

### SBT

```scala
resolvers += "Kolich repo" at "http://markkolich.github.com/repo"

val kolichHttpClient4Closure = "com.kolich" % "kolich-httpclient4-closure" % "0.0.2" % "compile"
```

### Maven

```xml
<repository>
  <id>Kolichrepo</id>
  <name>Kolich repo</name>
  <url>http://markkolich.github.com/repo/</url>
  <layout>default</layout>
</repository>

<dependency>
  <groupId>com.kolich</groupId>
  <artifactId>kolich-httpclient4-closure</artifactId>
  <version>0.0.2</version>
  <scope>compile</scope>
</dependency>
```

## Usage

Technically speaking, this library does not use "closures" (Lambda expressions) but rather a well defined pattern with **anonymous classes**.

### Functional Concepts

Some concepts in this library, like the `HttpResponseEither<F,S>`, were borrowed directly from the Functional Programming world, namely Scala.  In this case, `HttpResponseEither<F,S>` uses Java generics such that this library can return *either* a left type `F` indicating failure, or a right type `S` indicating success.  It's up to you, the developer, to define what these types are &mdash; the definition of a "successful" return type varies from application to application, and developer to developer.

When you "get back" an `HttpResponseEither<F,S>` you can check for success by calling the `success` method on the result.

```java
final HttpResponseEither<Exception,String> result =
  new HttpClient4Closure<Exception,String>(client) {
    // ...
  }.get("http://example.com");

if(result.success()) {
  // It worked!
}
```

To extract the return value of type `S` on success, call `right` on the result.

```java
final String s = result.right();
```

To extract the return value of type `F` on failure, call `left` on the result.

```java
final Exception cause = result.left();
```

Note that if you call `right` on a request that failed, expect a `null` return value.  Similarly, if you call `left` on a request that succeeded, also expect a `null` return value.

A few other things to keep in mind:

* This library automatically releases/frees all connection resources when a request has finished, either successfully or unsuccessfully.  You don't have to worry about closing any internal entity streams, that's done for you.
* All return types are developer-defined based on how you parameterize your `HttpResponseEither<F,S>`.  It's up to you to write a `success` method which converts an `HttpSuccess` object to your desired success type `S`.
* The default definition of "success" is any request that 1) completes without `Exception` and 2) receives an HTTP status code that is less than (`<`) 400 Bad Request.  You can easily override this default behavior by implementing a custom `check` method as needed.
* If you need to manipulate the request immeaditely before execution, you should override the `before` method.  This lets you do things like sign the request, or add the right authentication headers (if needed) before execution.

### Get an HttpClient

Before you can make HTTP requests using `kolich-httpclient4-closure` you need an `HttpClient` instance.  You can use my `KolichDefaultHttpClient` helper class packaged with this library to build a pre-configured `HttpClient` as desired.

```java
import com.kolich.http.KolichDefaultHttpClient.KolichHttpClientFactory;

final HttpClient client = KolichHttpClientFactory.getNewInstanceWithProxySelector();
```

Or, pass a `String` to the factory method to set the HTTP `User-Agent` on your new `HttpClient` instance.

```java
final HttpClient client = KolichHttpClientFactory.getNewInstanceWithProxySelector("IE6");
```

By default, the `KolichHttpClientFactory` always returns an `HttpClient` instance backed by a **thread-safe** `PoolingClientConnectionManager`.

### HttpClient Factory for Beans

You can use the `KolichHttpClientFactory` class to also instantiate an `HttpClient` for your beans:

```xml
<bean id="SomeFooBarBean" class="com.foo.bar.SomeBean">
  <constructor-arg>
    <bean class="com.kolich.http.KolichDefaultHttpClient.KolichHttpClientFactory"
      factory-method="getNewInstanceWithProxySelector">
      <!-- User-agent -->
      <constructor-arg><value>IE6</value></constructor-arg>
    </bean>
  </constructor-arg>			    	
</bean>
```

### Examples

#### HEAD

Send a `HEAD` request and expect back an array of HTTP response headers on success.  Drop any failures on the floor.

```java
final HttpResponseEither<Void,Header[]> result =
  new HttpClient4Closure<Void,Header[]>(client) {
  @Override
  public Header[] success(final HttpSuccess success) {
    return success.getResponse().getAllHeaders();
  }
}.head("http://example.com");

final Header[] headers = result.right();
```

#### GET

Send a `GET` request expecting either a `String` back on success, or an `Exception` on failure &mdash; this is represented by the `HttpResponseEither<Exception,String>` return type.

```java
final HttpResponseEither<Exception,String> result =
  new HttpClient4Closure<Exception,String>(client) {
  @Override
  public String success(final HttpSuccess success) throws Exception {
    return EntityUtils.toString(success.getResponse().getEntity(), "UTF-8");
  }
  @Override
  public Exception failure(final HttpFailure failure, final HttpContext context) {
    return failure.getCause();
  }
}.get("http://example.com");
```

Or, send a `GET` request still expecting a `String` on success, but drop any failures on the floor &mdash; expect a `null` return value in place of success type `S` if anything went wrong.

```java
final HttpResponseEither<Void,String> result =
  new HttpClient4Closure<Void,String>(client) {
  @Override
  public String success(final HttpSuccess success) throws Exception {
    return EntityUtils.toString(success.getResponse().getEntity(), "UTF-8");
  }
}.get(new HttpGet("http://skdfjlsdf.example.com")); // Obvious 404

System.out.println(result.right()); // Prints "null"
```

Send a `GET` request and stream the result on success to an existing and open `OutputStream`.

```java
import org.apache.commons.io.IOUtils;

final OutputStream os = ...; // Existing and open output stream

final HttpResponseEither<Void,Long> result =
  new HttpClient4Closure<Void,Long>(client) {
  @Override
  public Long success(final HttpSuccess success) throws Exception {
    return IOUtils.copyLarge(
      success.getResponse().getEntity().getContent(),
      os);
  }
}.get("http://api.example.com/path/to/big/resource");

// Success result is the number of bytes copied.
System.out.println("I copied " + result.right() + " total bytes.");
```

#### POST

Send a `POST` request but manipulate the `HttpBaseRequest` object before execution by overriding the `before` method.  Expect a `String` on success, and an `Integer` on failure.

```java
final HttpResponseEither<Integer,String> result =
  new HttpClient4Closure<Integer,String>(client) {
  @Override
  public void before(final HttpRequestBase request) {
    request.addHeader("Authorization", "super-secret-password!");
  }
  @Override
  public String success(final HttpSuccess success) throws Exception {
    return EntityUtils.toString(success.getResponse().getEntity(), "UTF-8");
  }
  @Override
  public Integer failure(final HttpFailure failure, final HttpContext context) {
    // Return the HTTP status code if anything went wrong.
    return failure.getStatusCode();
  }
}.post("http://api.example.com");
```

Or, send a `POST` with some form variables, ignoring failures, and convert a successful response to some custom entity using Google's <a href="http://code.google.com/p/google-gson/">GSON</a> toolkit.

```java
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

final HttpPost request = new HttpPost("http://api.example.com");
// Set a POST-body on the request.
final List<NameValuePair> params = new ArrayList<NameValuePair>();
params.add(new BasicNameValuePair("foo", "bar"));
params.add(new BasicNameValuePair("cat", "dog"));
request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

final HttpResponseEither<Void,MyClazz> result =
  new HttpClient4Closure<Void,MyClazz>(client) {
  @Override
  public MyClazz success(final HttpSuccess success) throws Exception {
    // Expects JSON from the server in response to the POST.
    return new GsonBuilder().create().fromJson(
      EntityUtils.toString(success.getResponse().getEntity(), "UTF-8"),
      MyClazz.class);
  }
}.post(request);

final MyClazz entity = result.right(); // Kewl
```

#### PUT

Send a `PUT` request with an existing and open `InputStream` from another source.  Expect an `Integer` back on success and nothing on failure.

```java
final InputStream is = ...; // Existing and open input stream

final HttpResponseEither<Void,Integer> result =
  new HttpClient4Closure<Void,Integer>(client) {
  @Override
  public void before(final HttpRequestBase request) {
    ((HttpPut)request).setEntity(new InputStreamEntity(is, contentLength));
  }
  @Override
  public String success(final HttpSuccess success) throws Exception {
    return success.getStatusCode();
  }
}.put("http://api.example.com/upload");
```

#### DELETE

Send a `DELETE` request with a custom `success` check &mdash; in this example, the server returns a `410 Gone` when the resource is deleted successfully but we don't want a 410 response to indicate failure. 

```java
final HttpResponseEither<Integer,Void> result =
  new HttpClient4Closure<Integer,Void>(client) {
  @Override
  public boolean check(final HttpResponse response, final HttpContext context) {
    // Success is 410, any other status code is failure.
    return (response.getStatusLine().getStatusCode() == 410);
  }
  @Override
  public Void success(final HttpSuccess success) throws Exception {
    return null; // Meh, for Void
  }
  @Override
  public Integer failure(final HttpFailure failure, final HttpContext context) {
    // Return the HTTP status code if anything went "wrong".
    return failure.getStatusCode();
  }
}.delete("http://api.example.com/go/away");

if(result.success()) {
  // History.
}
```

## Building

This Java library and its dependencies are built and managed using <a href="https://github.com/harrah/xsbt">SBT 0.12.1</a>.

To clone and build `kolich-httpclient4-closure`, you must have <a href="http://www.scala-sbt.org/release/docs/Getting-Started/Setup">SBT 0.12.1 installed and configured on your computer</a>.

The kolich-httpclient4-closure SBT <a href="https://github.com/markkolich/kolich-httpclient4-closure/blob/master/project/Build.scala">Build.scala</a> file is highly customized to build and package this Java artifact.  It's written to manage all dependencies and versioning.

To build, clone the repository.

    #~> git clone git://github.com/markkolich/kolich-httpclient4-closure.git

Run SBT from within kolich-httpclient4-closure.

    #~> cd kolich-httpclient4-closure
    #~/kolich-httpclient4-closure> sbt
    ...
    kolich-httpclient4-closure:0.0.2>

You will see a `kolich-httpclient4-closure` SBT prompt once all dependencies are resolved and the project is loaded.

In SBT, run `package` to compile and package the JAR.

    kolich-httpclient4-closure:0.0.2> package
    [info] Compiling 17 Java sources to ~/kolich-httpclient4-closure/target/classes...
    [info] Packaging ~/kolich-httpclient4-closure/dist/kolich-httpclient4-closure-0.0.2.jar ...
    [info] Done packaging.
    [success] Total time: 4 s, completed

Note the resulting JAR is placed into the **kolich-httpclient4-closure/dist** directory.

To create an Eclipse Java project for kolich-httpclient4-closure, run `eclipse` in SBT.

    kolich-httpclient4-closure:0.0.2> eclipse
    ...
    [info] Successfully created Eclipse project files for project(s):
    [info] kolich-httpclient4-closure

You'll now have a real Eclipse **.project** file worthy of an Eclipse import.

Note your new **.classpath** file as well -- all source JAR's are fetched and injected into the Eclipse project automatically.

## Dependencies

Currently, this artifact is built around <a href="http://hc.apache.org/">Apache Commons HttpClient</a> version **4.2.2**.

This library does **not** work with HttpClient 3.x.  If you are *still* using HttpClient 3.x you should really consider upgrading given that 3.x is past end-of-life and no longer supported. 

It also firmly depends on my common package of utility classes, <a href="https://github.com/markkolich/kolich-common">kolich-common</a>.

## Licensing

Copyright (c) 2012 <a href="http://mark.koli.ch">Mark S. Kolich</a>

All code in this artifact is freely available for use and redistribution under the <a href="http://opensource.org/comment/991">MIT License</a>.

See <a href="https://github.com/markkolich/kolich-httpclient4-closure/blob/master/LICENSE">LICENSE</a> for details.
