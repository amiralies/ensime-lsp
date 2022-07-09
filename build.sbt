ThisBuild / scalaVersion := "2.13.8"
ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "Ensime LSP",
    libraryDependencies += Dependencies.lsp4j
  )

enablePlugins(AssemblyPlugin)

mainClass in assembly := Some("ensimelsp.Main")
