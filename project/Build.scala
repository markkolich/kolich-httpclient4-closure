/**
 * Copyright (c) 2015 Mark S. Kolich
 * http://mark.koli.ch
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

import sbt._
import sbt.Keys._

object Dependencies {
  
  // Internal dependencies

  private val kolichCommon = "com.kolich" % "kolich-common" % "0.3" % "compile"

  // External dependencies
  
  private val httpClient = "org.apache.httpcomponents" % "httpclient" % "4.3.6" % "compile"

  val deps = Seq(
    // Internal.
    kolichCommon,
    // External.
    httpClient)

}

object Resolvers {

  private val kolichRepo = "Kolich repo" at "http://markkolich.github.io/repo"

  val depResolvers = Seq(kolichRepo)

}

object HttpClient4Closure extends Build {

  import Dependencies._
  import Resolvers._

  private val aName = "kolich-httpclient4-closure"
  private val aVer = "3.1"
  private val aOrg = "com.kolich"

  lazy val httpClient4Closure: Project = Project(
    aName,
    new File("."),
    settings = Defaults.coreDefaultSettings ++ Seq(resolvers := depResolvers) ++ Seq(
      version := aVer,
      organization := aOrg,
      scalaVersion := "2.10.2",
      javacOptions ++= Seq("-Xlint", "-g"),
      shellPrompt := {(state: State) => {"%s:%s> ".format(aName, aVer)}},
      // True to export the packaged JAR instead of just the compiled .class files.
      exportJars := true,
      // Disable using the Scala version in output paths and artifacts.
      // When running 'publish' or 'publish-local' SBT would append a
      // _<scala-version> postfix on artifacts. This turns that postfix off.
      crossPaths := false,
      // Keep the scala-lang library out of the generated POM's for this artifact. 
      autoScalaLibrary := false,
      // Only add src/main/java and src/test/java as source folders in the project.
      // Not a "Scala" project at this time.
      unmanagedSourceDirectories in Compile <<= baseDirectory(_ / "src/main/java")(Seq(_)),
      unmanagedSourceDirectories in Test <<= baseDirectory(_ / "src/test/java")(Seq(_)),
      //fork in run := true,
      // Also append the "examples" package to the classpath.
      //unmanagedSourceDirectories in Test <+= baseDirectory(_ / "src/examples/java"),
      // Tell SBT to include our .java files when packaging up the source JAR.
      unmanagedSourceDirectories in Compile in packageSrc <<= baseDirectory(_ / "src/main/java")(Seq(_)),
      // Override the SBT default "target" directory for compiled classes.
      classDirectory in Compile <<= baseDirectory(_ / "target/classes"),
      // Tweaks the name of the resulting JAR on a "publish" or "publish-local".
      artifact in packageBin in Compile <<= (artifact in packageBin in Compile, version) apply ((artifact, ver) => {
        val newName = artifact.name + "-" + ver
        Artifact(newName, artifact.`type`, artifact.extension, artifact.classifier, artifact.configurations, artifact.url)
      }),
      // Tweaks the name of the resulting source JAR on a "publish" or "publish-local".
      artifact in packageSrc in Compile <<= (artifact in packageSrc in Compile, version) apply ((artifact, ver) => {
        val newName = artifact.name + "-" + ver
        Artifact(newName, artifact.`type`, artifact.extension, artifact.classifier, artifact.configurations, artifact.url)
      }),
      // Tweaks the name of the resulting POM on a "publish" or "publish-local".
      artifact in makePom <<= (artifact in makePom, version) apply ((artifact, ver) => {
        val newName = artifact.name + "-" + ver
        Artifact(newName, artifact.`type`, artifact.extension, artifact.classifier, artifact.configurations, artifact.url)
      }),
      // Do not bother trying to publish artifact docs (scaladoc, javadoc). Meh.
      publishArtifact in packageDoc := false,
      // Override the global name of the artifact.
      artifactName <<= (name in (Compile, packageBin)) { projectName =>
        (config: ScalaVersion, module: ModuleID, artifact: Artifact) =>
          var newName = projectName
          if (module.revision.nonEmpty) {
            newName += "-" + module.revision
          }
          newName + "." + artifact.extension
      },
      // Override the default 'package' path used by SBT. Places the resulting
      // JAR into a more meaningful location.
      artifactPath in (Compile, packageBin) ~= { defaultPath =>
        file("dist") / defaultPath.getName
      },
      // Override the default 'test:package' path used by SBT. Places the
      // resulting JAR into a more meaningful location.
      artifactPath in (Test, packageBin) ~= { defaultPath =>
        file("dist") / "test" / defaultPath.getName
      },
      libraryDependencies ++= deps,
      retrieveManaged := true)
  )

}
