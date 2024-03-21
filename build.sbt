import scala.sys.process._

import com.typesafe.tools.mima.plugin.MimaPlugin._
import interplay.ScalaVersions._

ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("releases")

// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
ThisBuild / dynverVTagPrefix := false

// Sanity-check: assert that version comes from a tag (e.g. not a too-shallow clone)
// https://github.com/dwijnand/sbt-dynver/#sanity-checking-the-version
Global / onLoad := (Global / onLoad).value.andThen { s =>
  dynverAssertTagVersion.value
  s
}

lazy val commonSettings = Seq(
  // Work around https://issues.scala-lang.org/browse/SI-9311
  scalacOptions ~= (_.filterNot(_ == "-Xfatal-warnings")),
  scalacOptions ++= {
    scalaBinaryVersion.value match {
      case "3" =>
        Seq("-source:3.0-migration")
      case _ =>
        Nil
    }
  },
  scalaVersion        := "2.13.13",                          // scala213,
  crossScalaVersions  := Seq("3.3.3", "2.13.13", "2.12.19"), // scala213,
  pomExtra            := scala.xml.NodeSeq.Empty,            // Can be removed when dropping interplay
  organization        := "com.github.xuwei-k",
  sonatypeProfileName := organization.value,
  version             := "5.1.0-fork-3",
  developers += Developer(
    "playframework",
    "The Play Framework Team",
    "contact@playframework.com",
    url("https://github.com/playframework")
  ),
)

lazy val `play-slick-root` = (project in file("."))
  .enablePlugins(PlayRootProject)
  .aggregate(
    `play-slick`,
    `play-slick-evolutions`
  )
  .settings(commonSettings)

lazy val `play-slick` = (project in file("src/core"))
  .enablePlugins(PlayLibrary, Playdoc, MimaPlugin)
  .configs(Docs)
  .settings(libraryDependencies ++= Dependencies.core)
  .settings(mimaSettings)
  .settings(commonSettings)

lazy val `play-slick-evolutions` = (project in file("src/evolutions"))
  .enablePlugins(PlayLibrary, Playdoc, MimaPlugin)
  .configs(Docs)
  .settings(libraryDependencies ++= Dependencies.evolutions)
  .settings(mimaSettings)
  .settings(commonSettings)
  .dependsOn(`play-slick` % "compile;test->test")

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(PlayDocsPlugin)
  .configs(Docs)
  .dependsOn(`play-slick`)
  .dependsOn(`play-slick-evolutions`)
  .settings(commonSettings)

ThisBuild / playBuildRepoName := "play-slick"

// Binary compatibility is tested against this version
val previousVersion: Option[String] = Some("5.0.2")

ThisBuild / mimaFailOnNoPrevious := false

def mimaSettings = Seq(
  mimaPreviousArtifacts := previousVersion.map(organization.value %% moduleName.value % _).toSet
)
