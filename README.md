# kolich-httpclient4-closure

A Java 7+ wrapper around the **synchronous** Apache Commons HttpClient 4.

For most applications, the synchronous `HttpClient` closure is acceptable &mdash; the thread making the request will block, waiting for the request to complete before continuing.  If you need a pure asynchronous solution, please see the [Async Http Client](https://github.com/AsyncHttpClient/async-http-client).

## Overview

Using `HttpClient` directly is often cumbersome for vanilla `HEAD`, `GET`, `POST`, `PUT` and `DELETE` requests.  For example, it often takes multiple lines of boiler plate Java to send a simple `GET` request, check the resulting status code, read a response (if any), and release the connection back into the connection pool.

In *most* implementations, the typical `HttpClient` usage pattern almost always involves:

1. Creating a new `HttpHead`, `HttpGet`, `HttpPost`, `HttpPut`, or `HttpDelete` instance specific to the operation.
2. Setting an request body ("entity") to be sent with the request, if any.
3. Actually sending the request.
4. Checking the HTTP response status code.  If successful, do something.  If not successful, do something else.
5. Reading a response entity, if any.  If one exists, convert the entity to something useful so you can do something with it.
6. Freeing the response entity and releasing the connection back into the underlying connection pool.

The intent of this library is to let you do all of this in a cleaner, repeatable and more understandable manner.

Many would argue that this library simply trades one set of "boiler plate" for another.  True.  However, the patterns used here are much easier to grasp and they help you prevent obvious mistakes &mdash; like forgetting to close an `InputStream` when you're done with a response entity, which almost always manifests itself as a nasty leak under a heavy load.

## Latest Version

The latest stable version of this library is <a href="http://markkolich.github.io/repo/com/kolich/kolich-httpclient4-closure/2.2">2.2</a>.

## Resolvers

If you wish to use this artifact, you can easily add it to your existing Maven or SBT project using <a href="https://github.com/markkolich/markkolich.github.com#marks-maven2-repository">my GitHub hosted Maven2 repository</a>.

### SBT

```scala
resolvers += "Kolich repo" at "http://markkolich.github.io/repo"

val kolichHttpClient4Closure = "com.kolich" % "kolich-httpclient4-closure" % "2.2" % "compile"
```

### Maven

```xml
<repository>
  <id>Kolichrepo</id>
  <name>Kolich repo</name>
  <url>http://markkolich.github.io/repo/</url>
  <layout>default</layout>
</repository>

<dependency>
  <groupId>com.kolich</groupId>
  <artifactId>kolich-httpclient4-closure</artifactId>
  <version>2.2</version>
  <scope>compile</scope>
</dependency>
```

## Functional Concepts

Formally speaking, this library does not use "closures" (Lambda expressions) but rather a well defined pattern with **anonymous classes**.

Some concepts in this library, like the `com.kolich.common.either.Either<F,S>`, were borrowed directly from Scala.  In this case, `Either<F,S>` uses Java generics so that this library can return *either* a left type `F` indicating failure, or a right type `S` indicating success.  It's up to you, the developer, to define what these types are when you define your (anonymous) class &mdash; the definition of a "successful" return type varies from application to application.

When you receive an `Either<F,S>` you can check for success by calling the `success` method on the result.

```java
final Either<Exception,String> result =
  new HttpClient4Closure<Exception,String>(client) {
    // ...
  }.get("http://example.com"); // blocks

if(result.success()) {
  // It worked!
}
```

### Using Either&lt;F,S&gt;

When presented with an `Either<F,S>`, you can extract the return value of type `S` on success by calling `right`.

```java
final String s = result.right();
```

On the other hand, to extract the return value of type `F` on failure, call `left`.

```java
final Exception cause = result.left();
```

Note that if you call `right` on a request that failed, expect a `null` return value.  Likewise, if you call `left` on a request that succeeded, also expect a `null` return value.

### Usage Details

A few other details you'll probably be interested in:

* This library automatically releases/frees all connection resources allocated/opened by the underlying HTTP client when a request has finished, either successfully or unsuccessfully.  You don't have to worry about closing any internal HTTP client entity streams, that's done for you.
* All return types are developer-defined based on how you parameterize your `Either<F,S>`.  It's up to you to write a `success` method which converts an `HttpSuccess` object to your desired success type `S`.
* The default definition of "success" is any request that 1) completes without `Exception` and 2) receives an HTTP status code that is less than (`<`) 400 Bad Request.  You can easily override this default behavior by implementing a custom `check` method as needed.
* If you need to manipulate the request immediately before execution, you should override the `before` method.  This lets you do things like sign the request, or add the right authentication headers before the request is sent.
* If you need to examine the raw response immediately after execution, before the result is checked for success, you should override the `after` method.

There are a number of examples highlighting these closure entry points in the content below.

### Get an HttpClient

Before you can make HTTP requests, you need an `HttpClient` instance.  You can use your own `HttpClient` (as instantiated elsewhere by another class), or you can use my `KolichDefaultHttpClient` factory class packaged with this library to snag a pre-configured `HttpClient`.

```java
import com.kolich.http.KolichDefaultHttpClient.KolichHttpClientFactory;

final HttpClient client = KolichHttpClientFactory.getNewInstanceWithProxySelector();
```

Or, pass a `String` to the factory method to set the HTTP `User-Agent` on your new `HttpClient` instance.

```java
final HttpClient client = KolichHttpClientFactory.getNewInstanceWithProxySelector("IE6, srsly");
```

By default, the `KolichHttpClientFactory` always returns an `HttpClient` instance backed by a **thread-safe** `PoolingClientConnectionManager`.  There's currently no support for passing your own connection manager to my `KolichHttpClientFactory` &mdash; if you need to use your own connection manager, it's safest to just build your own `HttpClient` instance elsewhere. 

#### HttpClient Factory for Beans

You can use the `KolichHttpClientFactory` to also instantiate an `HttpClient` as a bean:

```xml
<bean id="YourHttpClient"
  class="com.kolich.http.KolichDefaultHttpClient.KolichHttpClientFactory"
  factory-method="getNewInstanceWithProxySelector">
  <!-- Set a custom User-Agent string too, if you want. -->
  <constructor-arg><value>IE6, srsly</value></constructor-arg>
</bean>

<bean id="SomeBean" class="com.foo.bar.SomeBean">
  <property name="httpClient" ref="YourHttpClient" />			    	
</bean>
```

### Web-Proxy Support

Some environments require outgoing HTTP/HTTPS connections to use a web-proxy.

Fortunately, `HttpClient` integrates nicely with `java.net.ProxySelector` which makes it possible to automatically detect proxy settings across platforms.

That said, you can easily create a proxy-aware `HttpClient` instance using the right factory method.

Get a new `HttpClient` from the `KolichHttpClientFactory`.

```java
import com.kolich.http.KolichDefaultHttpClient.KolichHttpClientFactory;

final HttpClient usesProxy = KolichHttpClientFactory.getNewInstanceWithProxySelector();

final HttpClient noProxy = KolichHttpClientFactory.getNewInstanceNoProxySelector();
```

When using the `getNewInstanceWithProxySelector` static factory methods, the underlying client will automatically use the JVM's `java.net.ProxySelector` to discover what web-proxy to use when establishing outgoing HTTP connections.  On all platforms, you can manually tell the JVM's default `java.net.ProxySelector` what web-proxy to use by setting the `http.proxyHost` and `http.proxyPort` VM arguments for vanilla HTTP connections.  For outgoing HTTPS connections, use `https.proxyHost` and `https.proxyPort`.

```bash
java -Dhttp.proxyHost=proxy.example.com -Dhttp.proxyPort=3128 \
     -Dhttps.proxyHost=proxy.example.com -Dhttps.proxyPort=3128 \
     -jar runme.jar
```

## Examples

Synchronous, or blocking, HTTP requests block the thread of execution until the request has finished, either successfully or unsuccessfully.  When making synchronous requests, the execution thread waits for the HTTP transaction to complete.  Generally speaking, this model is acceptable for your everyday "vanilla" application.  However, in some applications this may be suboptimal, given that the requesting thread is blocked waiting on the HTTP transaction to finish, and consequently cannot do any additional work. 

### HEAD

Send a `HEAD` request and expect back an array of HTTP response headers on success.  Drop any failures on the floor &mdash; expect a `null` return value in place of success type `S` if anything went wrong.

```java
final Either<Void,Header[]> result =
  new HttpClient4Closure<Void,Header[]>(client) {
  @Override
  public Header[] success(final HttpSuccess success) {
    return success.getResponse().getAllHeaders();
  }
}.head("http://example.com");

final Header[] headers = result.right();
```

### GET

Send a `GET` request expecting either a `String` back on success, or an `Exception` on failure &mdash; this is represented by the `Either<Exception,String>` return type.

```java
final Either<Exception,String> result =
  new HttpClient4Closure<Exception,String>(client) {
  @Override
  public String success(final HttpSuccess success) throws Exception {
    return EntityUtils.toString(success.getEntity(), "UTF-8");
  }
  @Override
  public Exception failure(final HttpFailure failure) {
    return failure.getCause();
  }
}.get("http://example.com");
```

Or, send a `GET` request still expecting a `String` on success, but drop any failures on the floor &mdash; expect a `null` return value in place of success type `S` if anything went wrong.

```java
final Either<Void,String> result =
  new HttpClient4Closure<Void,String>(client) {
  @Override
  public String success(final HttpSuccess success) throws Exception {
    return EntityUtils.toString(success.getEntity(), "UTF-8");
  }
}.get(new HttpGet("http://example.com/sdljflk8831sdflk")); // Obvious 404

System.out.println(result.right()); // Prints "null"
```

Send a `GET` request and stream the result on success to an existing and open `OutputStream`.

```java
import org.apache.commons.io.IOUtils;

final OutputStream os = ...; // Existing and open output stream

final Either<Void,Long> result =
  new HttpClient4Closure<Void,Long>(client) {
  @Override
  public Long success(final HttpSuccess success) throws Exception {
    return IOUtils.copyLarge(success.getContent(), os);
  }
}.get("http://api.example.com/path/to/big/resource");

// Success result is the number of bytes copied.
System.out.println("I copied " + result.right() + " total bytes.");
```

Send a `GET` request and extract a `List<Cookie>` (list of `Cookie`'s) from the response, using the default `HttpContext` and a per-request `BasicCookieStore`.

```java
import static org.apache.http.client.protocol.ClientContext.COOKIE_STORE;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;

final Either<Integer,List<Cookie>> mmmm =
  new HttpClient4Closure<Integer,List<Cookie>>(client) {
  @Override
  public void before(final HttpRequestBase request, final HttpContext context) {
    context.setAttribute(COOKIE_STORE, new BasicCookieStore());
  }
  @Override
  public List<Cookie> success(final HttpSuccess success) {
    // Extract a list of cookies from the request.
    // Might be empty.
    return ((CookieStore)success.getContext()
      .getAttribute(COOKIE_STORE)).getCookies();
  }
  @Override
  public Integer failure(final HttpFailure failure) {
    return failure.getStatusCode();
  }
}.get("http://example.com");

// Get the list of extracted cookies from the response
// and print them out.  Mmmm, cookies.
final List<Cookie> cookies;
if((cookies = mmmm.right()) != null) {
  for(final Cookie c : cookies) {
    System.out.println(c.getName() + " -> " + c.getValue());
  }
}
```

### POST

Send a `POST` request but manipulate the `HttpBaseRequest` object before execution by overriding the `before` method.  Expect a `String` on success, and an `Integer` on failure.

```java
final Either<Integer,String> result =
  new HttpClient4Closure<Integer,String>(client) {
  @Override
  public void before(final HttpRequestBase request) {
    request.addHeader("Authorization", "super-secret-password!");
  }
  @Override
  public String success(final HttpSuccess success) throws Exception {
    return EntityUtils.toString(success.getEntity(), "UTF-8");
  }
  @Override
  public Integer failure(final HttpFailure failure) {
    // Return the HTTP status code if anything went wrong.
    return failure.getStatusCode();
  }
}.post("http://api.example.com");
```

Or, send a `POST` with some form variables, ignoring failures, and convert a successful response to some custom entity using Google's <a href="http://code.google.com/p/google-gson/">GSON</a> toolkit.

```java
import com.google.gson.GsonBuilder;

final HttpPost request = new HttpPost("http://api.example.com");
// Set a POST-body on the request.
final List<NameValuePair> params = new ArrayList<NameValuePair>();
params.add(new BasicNameValuePair("foo", "bar"));
params.add(new BasicNameValuePair("cat", "dog"));
request.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

final Either<Void,MyClazz> result =
  new HttpClient4Closure<Void,MyClazz>(client) {
  @Override
  public MyClazz success(final HttpSuccess success) throws Exception {
    // Expects JSON from the server in response to the POST.
    return new GsonBuilder().create().fromJson(
      EntityUtils.toString(success.getEntity(), "UTF-8"),
      MyClazz.class);
  }
}.post(request);

final MyClazz entity = result.right(); // Kewl
```

### PUT

Send a `PUT` request with an existing and open `InputStream` from another source.  Expect an `Integer` back on success and nothing on failure.

```java
final InputStream is = ...; // Existing and open input stream

final Either<Void,Integer> result =
  new HttpClient4Closure<Void,Integer>(client) {
  @Override
  public void before(final HttpRequestBase request) {
    ((HttpPut)request).setEntity(new InputStreamEntity(is, contentLength));
  }
  @Override
  public Integer success(final HttpSuccess success) throws Exception {
    return success.getStatusCode();
  }
}.put("http://api.example.com/upload");

if(result.success()) {
  System.out.println("PUT resulted in a " + result.right() + " status.");
}
```

### DELETE

Send a `DELETE` request with a custom `success` check &mdash; in this example, the server returns a `410 Gone` when the resource is deleted successfully but we don't want a 410 response to indicate failure. 

```java
final Either<Integer,Void> result =
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
  public Integer failure(final HttpFailure failure) {
    // Return the HTTP status code if anything went "wrong".
    return failure.getStatusCode();
  }
}.delete("http://api.example.com/go/away");

if(result.success()) {
  // Got 401 Gone response from server, resource is "history".
}
```

## Helpers

To ease development, a number of helper closures are available out-of-the-box as found in the <a href="https://github.com/markkolich/kolich-httpclient4-closure/tree/master/src/main/java/com/kolich/http/helpers">com.kolich.http.helpers</a> package.  These helpers are packaged and shipped with this library and are intended to help developers avoid much of the closure boiler plate for the most common operations.

Below are several examples using these helper closures.

### StringOrNullClosure

Send a `GET` and if the request was successful extract the response body as a `UTF-8` encoded `String`.  If unsuccessful, return `null`.

```java
import com.kolich.http.helpers.StringClosures.StringOrNullClosure;

final Either<Void,String> s = new StringOrNullClosure(client)
  .get("http://google.com");

final String html;
if((html = s.right()) != null) {
  System.out.println("Your HTML: " + html);
}
```

### ByteArrayOrHttpFailureClosure

Send a `POST` and if the request was successful extract the response body as a `byte[]` array.  If unsuccessful, return an `HttpFailure` object.

```java
import com.kolich.http.helpers.ByteArrayClosures.ByteArrayOrHttpFailureClosure;

final Either<HttpFailure,byte[]> r = new ByteArrayOrHttpFailureClosure(client)
  .post("http://api.example.com/resource");

final byte[] bytes = r.right();
```

### StatusCodeAndHeadersClosure

Send a `GET` and blindly ignore if the request was "successful" or not.  Extract the resulting HTTP status code and headers on the response &mdash; even if the server responded with an "unsuccessful" status code.

```java
import com.kolich.http.helpers.StatusCodeAndHeaderClosures.StatusCodeAndHeadersClosure;

// Tip: Use URIBuilder to add query parameters to the URI of a GET.
// (see example usage below)
import org.apache.http.client.utils.URIBuilder;

final StatusCodeAndHeadersClosure sah = new StatusCodeAndHeadersClosure(client) {
  @Override
  public void before(final HttpRequestBase request) throws Exception {
    // Add some query parameters to the request URI before its sent.
    final URIBuilder b = new URIBuilder(request.getURI());
    b.addParameter("foo", "bar");
    request.setURI(b.build());
  }
};

// Send the request.
sah.get("https://example.com/get/status/and/headers");

System.out.println("Got status " + sah.getStatusCode());
System.out.println("Found " + sah.getHeaderList().size() + " headers too!");
```

### GsonOrHttpFailureClosure&lt;S&gt;

Send a `POST` and if the request succeeded, use GSON to convert the response body (a blob of JSON) to successful type `S`.  If the request failed, expect an `HttpFailure` object.

Note, `YourType` below is assumed to be an entity class (a domain object) defined by your application or schema.

```java
import com.kolich.http.helpers.GsonClosures.GsonOrHttpFailureClosure;

import com.google.gson.Gson;

final Gson gson = ...; // Get a GSON instance.

// Send the POST, and if the request is successful, use the GSON instance
// to unmarshall the response body to a valid YourType object.
final Either<HttpFailure,YourType> g =
  new GsonOrHttpFailureClosure<YourType>(client, gson, YourType.class)
    .post("https://api.example.com/resource.json");

// Will be null if the request failed.
final YourType t = g.right();
```

Or, use a GSON `TypeToken` if your expected successful type `S` is a generic type, and consequently, you cannot use `.class` due to Java's type erasure.

```java
import com.kolich.http.helpers.GsonClosures.GsonOrHttpFailureClosure;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

final Gson gson = ...; // Get a GSON instance.

// Send the POST, and if the request is successful, use the GSON instance
// to unmarshall the response body to a valid List<YourType> object.
final Either<HttpFailure,List<YourType>> g =
  new GsonOrHttpFailureClosure<List<YourType>>(
    client, gson, new TypeToken<List<YourType>>(){}.getType()
  ).post("https://api.example.com/resource.json");

// Will be null if the request failed.
final List<YourType> lt = g.right();
```

## Building

This Java library and its dependencies are built and managed using <a href="https://github.com/harrah/xsbt">SBT</a>.

To clone and build kolich-httpclient4-closure, you must have <a href="http://www.scala-sbt.org/release/docs/Getting-Started/Setup">SBT installed and configured on your computer</a>.

The kolich-httpclient4-closure SBT <a href="https://github.com/markkolich/kolich-httpclient4-closure/blob/master/project/Build.scala">Build.scala</a> file is highly customized to build and package this Java artifact.  It's written to manage all dependencies and versioning.

To build, clone the repository.

    #~> git clone git://github.com/markkolich/kolich-httpclient4-closure.git

Run SBT from within kolich-httpclient4-closure.

    #~> cd kolich-httpclient4-closure
    #~/kolich-httpclient4-closure> sbt
    ...
    kolich-httpclient4-closure:2.2>

You will see a `kolich-httpclient4-closure` SBT prompt once all dependencies are resolved and the project is loaded.

In SBT, run `package` to compile and package the JAR.

    kolich-httpclient4-closure:2.2> package
    [info] Compiling 27 Java sources to ~/kolich-httpclient4-closure/target/classes...
    [info] Packaging ~/kolich-httpclient4-closure/dist/kolich-httpclient4-closure-2.2.jar ...
    [info] Done packaging.
    [success] Total time: 4 s, completed

Note the resulting JAR is placed into the **kolich-httpclient4-closure/dist** directory.

To create an Eclipse Java project for kolich-httpclient4-closure, run `eclipse` in SBT.

    kolich-httpclient4-closure:2.2> eclipse
    ...
    [info] Successfully created Eclipse project files for project(s):
    [info] kolich-httpclient4-closure

You'll now have a real Eclipse **.project** file worthy of an Eclipse import.

Note your new **.classpath** file as well &mdash; all source JAR's are fetched and injected into the Eclipse project automatically.

## Licensing

Copyright (c) 2014 <a href="http://mark.koli.ch">Mark S. Kolich</a>

All code in this artifact is freely available for use and redistribution under the <a href="http://opensource.org/comment/991">MIT License</a>.

See <a href="https://github.com/markkolich/kolich-httpclient4-closure/blob/master/LICENSE">LICENSE</a> for details.
