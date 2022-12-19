// To publish
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.11")

// https://github.com/sbt/sbt/issues/6997#issuecomment-1310637232
ThisBuild / libraryDependencySchemes +=
  "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
