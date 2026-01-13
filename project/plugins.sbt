// Code coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.4.4")

// To publish
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.11.0")

// https://github.com/sbt/sbt/issues/6997#issuecomment-1310637232
ThisBuild / libraryDependencySchemes +=
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
