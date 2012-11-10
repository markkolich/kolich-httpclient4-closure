# kolich-http-client

A convenient Java wrapper around the Apache Commons HttpClient 4.x library.

As is, using HttpClient is often cumbersome and bulky for typical `GET`, `POST`, `PUT` and `DELETE` operations.  For example, it often takes multiple lines of boiler plate Java to send a simple `GET` request, check the resulting status code, read a response (if any) and close/free the connection back into the connection pool.

In most scenarios, the typical HttpClient usage pattern almost always involves ...

1. Creating a new `HttpGet`, `HttpPost`, `HttpPut`, or `HttpDelete` instance specific to the operation.
2. Setting an request body ("entity") to be sent with the request, if any.
3. Actually sending the request.
4. Checking the HTTP response status code.  If successful, do something.  If not successful, do something else.
5. Reading a response entity, if any.  If one exists, convert it to a `String` so you can do something with it.
6. Freeing the response and releasing the connection back into the underlying thread-safe connection pool.

In *most* cases, you want to avoid all of this boiler plate and just freakin' send HTTP requests without worrying about the internal wiring of HttpClient.  The intent of this library is to let you do that.

## Latest Version

The latest stable version of this library is <a href="http://markkolich.github.com/repo/com/kolich/kolich-http-client/0.0.1">0.0.1</a>.

## Resolvers

If you wish to use this artifact, you can easily add it to your existing Maven or SBT project using <a href="https://github.com/markkolich/markkolich.github.com#marks-maven2-repository">my GitHub hosted Maven2 repository</a>.

### SBT

```scala
resolvers += "Kolich repo" at "http://markkolich.github.com/repo"

val kolichHttpClient = "com.kolich" % "kolich-http-client" % "0.0.1" % "compile"
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
  <artifactId>kolich-http-client</artifactId>
  <version>0.0.1</version>
  <scope>compile</scope>
</dependency>
```

## Usage

TODO

## Building

This Java library and its dependencies are built and managed using <a href="https://github.com/harrah/xsbt">SBT 0.12.1</a>.

To clone and build kolich-http-client, you must have <a href="http://www.scala-sbt.org/release/docs/Getting-Started/Setup">SBT 0.12.1 installed and configured on your computer</a>.

The kolich-http-client SBT <a href="https://github.com/markkolich/kolich-http-client/blob/master/project/Build.scala">Build.scala</a> file is highly customized to build and package this Java artifact.  It's written to manage all dependencies and versioning.

To build, clone the repository.

    #~> git clone git://github.com/markkolich/kolich-http-client.git

Run SBT from within kolich-http-client.

    #~> cd kolich-http-client
    #~/kolich-http-client> sbt
    ...
    kolich-http-client:0.0.1>

You will see a `kolich-http-client` SBT prompt once all dependencies are resolved and the project is loaded.

In SBT, run `package` to compile and package the JAR.

    kolich-http-client:0.0.1> package
    [info] Compiling 17 Java sources to ~/kolich-http-client/target/classes...
    [info] Packaging ~/kolich-http-client/dist/kolich-http-client-0.0.1.jar ...
    [info] Done packaging.
    [success] Total time: 4 s, completed

Note the resulting JAR is placed into the **kolich-http-client/dist** directory.

To create an Eclipse Java project for kolich-http-client, run `eclipse` in SBT.

    kolich-http-client:0.0.1> eclipse
    ...
    [info] Successfully created Eclipse project files for project(s):
    [info] kolich-http-client

You'll now have a real Eclipse **.project** file worthy of an Eclipse import.

Note your new **.classpath** file as well -- all source JAR's are fetched and injected into the Eclipse project automatically.

## Dependencies

Currently, this artifact depends on the <a href="http://hc.apache.org/">Apache Commons HttpClient</a> version **4.2.2**.

This library does **not** work with HttpClient 3.x.  If you are *still* using HttpClient 3.x you should really consider upgrading given that 3.x is past end-of-life and no longer supported. 

It also firmly depends on my common package of utility classes, <a href="https://github.com/markkolich/kolich-common">kolich-common</a>.

## Licensing

Copyright (c) 2012 <a href="http://mark.koli.ch">Mark S. Kolich</a>

All code in this artifact is freely available for use and redistribution under the <a href="http://opensource.org/comment/991">MIT License</a>.

See <a href="https://github.com/markkolich/kolich-http-client/blob/master/LICENSE">LICENSE</a> for details.
