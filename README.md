![ZparkIO logo](https://raw.githubusercontent.com/leobenkel/ZparkIO/main/assets/ZparkIO_animated.gif)

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![release-badge][]][release]
[![maven-central-badge][]][maven-search]
![CI](https://github.com/leobenkel/zparkio/workflows/CI/badge.svg)
[![BCH compliance](https://bettercodehub.com/edge/badge/leobenkel/ZparkIO?branch=main)](https://bettercodehub.com/)
[![Coverage Status](https://coveralls.io/repos/github/leobenkel/ZparkIO/badge.svg?branch=main)](https://coveralls.io/github/leobenkel/ZparkIO?branch=main)
[![Mutation testing badge](https://img.shields.io/endpoint?style=flat&url=https%3A%2F%2Fbadge-api.stryker-mutator.io%2Fgithub.com%2Fleobenkel%2FZparkIO%2Fmain)](https://dashboard.stryker-mutator.io/reports/github.com/leobenkel/ZparkIO/main)



[release]:              https://github.com/leobenkel/zparkio/releases
[release-badge]:        https://img.shields.io/github/tag/leobenkel/zparkio.svg?label=version&color=blue
[maven-search]:         https://search.maven.org/search?q=g:com.leobenkel%20a:zparkio*
[maven-search-test]:         https://search.maven.org/search?q=g:com.leobenkel%20a:zparkio-test*
[leobenkel-github-badge]:     https://img.shields.io/badge/-Github-yellowgreen.svg?style=social&logo=GitHub&logoColor=black
[leobenkel-github-link]:      https://github.com/leobenkel
[leobenkel-linkedin-badge]:     https://img.shields.io/badge/-Linkedin-yellowgreen.svg?style=social&logo=LinkedIn&logoColor=black
[leobenkel-linkedin-link]:      https://linkedin.com/in/leobenkel
[leobenkel-personal-badge]:     https://img.shields.io/badge/-Website-yellowgreen.svg?style=social&logo=data:image/svg+xml;base64,PHN2ZyBoZWlnaHQ9JzMwMHB4JyB3aWR0aD0nMzAwcHgnICBmaWxsPSIjMDAwMDAwIiB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiB2ZXJzaW9uPSIxLjEiIHg9IjBweCIgeT0iMHB4IiB2aWV3Qm94PSIwIDAgNjQgNjQiIGVuYWJsZS1iYWNrZ3JvdW5kPSJuZXcgMCAwIDY0IDY0IiB4bWw6c3BhY2U9InByZXNlcnZlIj48Zz48Zz48cGF0aCBkPSJNNDEuNiwyNy4yYy04LjMsMC0xNSw2LjctMTUsMTVzNi43LDE1LDE1LDE1YzguMywwLDE1LTYuNywxNS0xNVM0OS45LDI3LjIsNDEuNiwyNy4yeiBNNTEuNSwzNmgtMy4zICAgIGMtMC42LTEuNy0xLjQtMy4zLTIuNC00LjZDNDguMiwzMi4yLDUwLjIsMzMuOSw1MS41LDM2eiBNNDEuNiwzMS41YzEuMywxLjIsMi4zLDIuNywzLDQuNGgtNkMzOS4zLDM0LjIsNDAuNCwzMi43LDQxLjYsMzEuNXogICAgIE0zNy40LDMxLjNjLTEsMS40LTEuOCwyLjktMi40LDQuNmgtMy4zQzMzLjEsMzMuOSwzNS4xLDMyLjIsMzcuNCwzMS4zeiBNMzAuMyw0NWMtMC4yLTAuOS0wLjQtMS44LTAuNC0yLjhjMC0xLDAuMS0yLDAuNC0yLjkgICAgaDMuOWMtMC4xLDEtMC4yLDEuOS0wLjIsMi45YzAsMC45LDAuMSwxLjksMC4yLDIuOEgzMC4zeiBNMzEuNyw0OC4zSDM1YzAuNiwxLjcsMS40LDMuNCwyLjQsNC44QzM1LDUyLjIsMzMsNTAuNSwzMS43LDQ4LjN6ICAgICBNNDEuNiw1Mi45Yy0xLjMtMS4yLTIuMy0yLjgtMy4xLTQuNWg2LjFDNDQsNTAuMSw0Mi45LDUxLjcsNDEuNiw1Mi45eiBNMzcuNiw0NWMtMC4yLTAuOS0wLjItMS44LTAuMi0yLjhjMC0xLDAuMS0yLDAuMy0yLjloOCAgICBjMC4yLDAuOSwwLjMsMS45LDAuMywyLjljMCwxLTAuMSwxLjktMC4yLDIuOEgzNy42eiBNNDUuOCw1My4xYzEtMS40LDEuOC0zLDIuNC00LjhoMy4zQzUwLjIsNTAuNSw0OC4yLDUyLjIsNDUuOCw1My4xeiBNNDksNDUgICAgYzAuMS0wLjksMC4yLTEuOCwwLjItMi44YzAtMS0wLjEtMi0wLjItMi45aDMuOWMwLjIsMC45LDAuNCwxLjksMC40LDIuOWMwLDEtMC4xLDEuOS0wLjQsMi44SDQ5eiI+PC9wYXRoPjxwYXRoIGQ9Ik0zNCwyNS45Yy0wLjktMC43LTEuOC0xLjMtMi45LTEuOGMyLTIuMSwzLjItNC45LDMuMi03LjljMC02LjMtNS4xLTExLjQtMTEuNC0xMS40UzExLjYsOS45LDExLjYsMTYuMiAgICBjMCwzLjEsMS4yLDUuOSwzLjIsNy45Yy00LjEsMi02LjgsNS40LTcuMSw5LjRsLTAuMywzLjhjMCwyLDcsMy42LDE1LjYsMy42YzAuMiwwLDAuNSwwLDAuNywwQzI0LjIsMzQuMywyOC4yLDI4LjYsMzQsMjUuOXogICAgIE0yMyw4LjhjNC4xLDAsNy40LDMuMyw3LjQsNy40cy0zLjMsNy40LTcuNCw3LjRzLTcuNC0zLjMtNy40LTcuNFMxOC45LDguOCwyMyw4Ljh6Ij48L3BhdGg+PC9nPjwvZz48L3N2Zz4=&logoColor=black
[leobenkel-personal-link]:      https://leobenkel.com
[leobenkel-patreon-link]:            https://www.patreon.com/leobenkel
[leobenkel-patreon-badge]: https://img.shields.io/badge/-Patreon-yellowgreen.svg?style=social&logo=Patreon&logoColor=black
[maven-central-link]:           https://maven-badges.herokuapp.com/maven-central/com.leobenkel/zparkio_2.11
[maven-central-badge]:          https://maven-badges.herokuapp.com/maven-central/com.leobenkel/zparkio_2.11/badge.svg
[maven-central-link-test]:           https://maven-badges.herokuapp.com/maven-central/com.leobenkel/zparkio-test_2.11
[maven-central-badge-test]:          https://maven-badges.herokuapp.com/maven-central/com.leobenkel/zparkio-test_2.11/badge.svg


# ZparkIO
Boiler plate framework to use [Spark](https://github.com/apache/spark) and [ZIO](https://github.com/zio/zio) together.

The goal of this framework is to blend Spark and ZIO in an easy to use system for data engineers.

Allowing them to use Spark in a new, faster, more reliable way, leveraging ZIO power.

## Table of Contents

* [What is this library for ?](#what-is-this-library-for-)
* [More About ZparkIO](#more-about-zparkio)
    * [Public Presentation](#public-presentation)
    * [Migrate your Spark Project to ZparkIO](#migrate-your-spark-project-to-zparkio)
* [Why would you want to use ZIO and Spark together?](#why-would-you-want-to-use-zio-and-spark-together)
* [How to use?](#how-to-use)
    * [Include dependencies](#include-dependencies)
        * [Unit-test](#unit-test)
    * [How to use in your code?](#how-to-use-in-your-code)
        * [Main](#main)
        * [Spark](#spark)
        * [Command lines](#command-lines)
        * [Helpers](#helpers)
        * [Unit test](#unit-test-1)
* [Examples](#examples)
    * [Simple example](#simple-example)
    * [More complex architecture](#more-complex-architecture)
* [Authors](#authors)
    * [Leo Benkel](#leo-benkel)

Created by [gh-md-toc](https://github.com/ekalinin/github-markdown-toc)

## What is this library for ?

This library will implement all the boiler plate for you to be able to include Spark and ZIO in your ML project.

It can be tricky to use ZIO to save an instance of Spark to reuse in your code and this library solve all the boilerplate problem for you.

## More About ZparkIO

### Public Presentation

Feel free to look at the slides on [Google Drive](https://docs.google.com/presentation/d/1gyFJpH2mzJ9ghSTsIMrUHWA9rCtSn2ML9ERUFvuYSp8) or on [SlideShare](https://www.slideshare.net/LeoBenkel/2020-0326-meet-up-z-parkio-230980911) presented during the [ScalaSF meetup](https://www.meetup.com/SF-Scala/events/268998404/) on Thursday, March 26, 2020. You can also watch [the presentation on Youtube](https://www.youtube.com/embed/Ov7WZroBkv0?start=507&end=2416).

ZparkIO was on `version 0.7.0`, so things might be out of date.

### Migrate your Spark Project to ZparkIO
[Migrate from Plain Spark to ZparkIO](https://medium.com/@AyoubFakir/migrating-from-a-plain-spark-application-to-zio-with-zparkio-8fcd5f5da6ab)

## Why would you want to use ZIO and Spark together?

From my experience, using ZIO/Future in combination with Spark can speed up drastically the performance of your job. The reason being that sources (BigQuery, Postgresql, S3 files, etc...) can be fetch in parallel while the computation are not on hold. Obviously ZIO is much better than Future but it is harder to set up. Not anymore!

Some other nice aspect of ZIO is the error/exception handling as well as the build-in retry helpers. Which make retrying failed task a breath within Spark.

## How to use?

I hope that you are now convinced that ZIO and Spark are a perfect match. Let's see how to use this Zparkio.

One of the easiest way to use ZparkIO is to use the [giter8 template project](https://github.com/leobenkel/zparkio.g8):

```
sbt new leobenkel/zparkio.g8
```

### Include dependencies

First include the library in your project:

```sbt
libraryDependencies += "com.leobenkel" %% "zparkio" % "[SPARK_VERSION]_[VERSION]"
```
With version being: [![maven-central-badge][]][maven-search] [![release-badge][]][release].

To checkout out the [Spark Versions](https://github.com/leobenkel/ZparkIO/blob/main/sparkVersions) and the [Version](https://github.com/leobenkel/ZparkIO/blob/main/VERSION).

This library depends on [Spark](https://github.com/apache/spark), [ZIO](https://github.com/zio/zio) and [Scallop](https://github.com/scallop/scallop). 

#### Unit-test

You can also add 

```sbt
libraryDependencies += "com.leobenkel" %% "zparkio-test" % "[VERSION]"
```
With version being: [![maven-central-badge-test][]][maven-search-test] .

To get access to helper function to help you write unit tests.

### How to use in your code?

There is a [project example](https://github.com/leobenkel/ZparkIO/tree/main/examples/Example1_mini/src) you can look at. But here are the details.

#### Main

The first thing you have to do is extends the `ZparkioApp` trait. For an example you can look at the [ProjectExample](https://github.com/leobenkel/ZparkIO/tree/main/examples/Example1_mini/src): [Application](https://github.com/leobenkel/ZparkIO/blob/main/examples/Example1_mini/src/main/scala/com/leobenkel/example1/Application.scala).

#### Spark

By using this architecture, you will have access to `SparkSesion` anywhere in your `ZIO` code, via 
```scala
import com.leobenkel.zparkio.Services._

for {
  spark <- SparkModule()
} yield {
  ???
}
```

for instance you can see its use [here](https://github.com/leobenkel/ZparkIO/blob/cd958947d26996a15f100f397d8c471a07f047d3/examples/Example1_mini/src/main/scala/com/leobenkel/example1/Application.scala#L30).

#### Command lines

You will also have access to all your command lines automatically parsed, generated and accessible to you via: 

[CommandLineArguments](https://github.com/leobenkel/ZparkIO/blob/cd958947d26996a15f100f397d8c471a07f047d3/examples/Example1_mini/src/main/scala/com/leobenkel/example1/Arguments.scala#L11-L22) ;
it is recommended to make this helper function to make the rest of your code easier to use.

Then using it, [like here](https://github.com/leobenkel/ZparkIO/blob/cd958947d26996a15f100f397d8c471a07f047d3/examples/Example1_mini/src/main/scala/com/leobenkel/example1/Application.scala#L29), is easy.

#### Helpers

In the [implicits](https://github.com/leobenkel/Zparkio/blob/main/Library/src/main/scala/com/leobenkel/zparkio/implicits.scala) object, that you can include everywhere. You are getting specific helper functions to help streamline your projects.

#### Unit test

Using this architecture will literally allow you to [run your main as a unit test](https://github.com/leobenkel/ZparkIO/blob/main/examples/Example1_mini/src/test/scala/com/leobenkel/example1/ApplicationTest.scala).

## Examples

### Simple example

Take a look at the simple project example to see example of working code using this library: 
[SimpleProject](https://github.com/leobenkel/ZparkIO/tree/main/examples/Example1_mini).

### More complex architecture

A full-fledged, production-ready project will obviously need more code than the simple example.
For this purpose, and upon suggestion of several awesome people, I added a more complex project.
This is a WIP and more will be added as I go. 
[MoreComplexProject](https://github.com/leobenkel/ZparkIO/tree/main/examples/Example2_small/src).

## Authors

### Leo Benkel

* [![leobenkel-github-badge][]][leobenkel-github-link]
* [![leobenkel-linkedin-badge][]][leobenkel-linkedin-link]
* [![leobenkel-personal-badge][]][leobenkel-personal-link]
* [![leobenkel-patreon-badge][]][leobenkel-patreon-link]

## Alternatives

* [univalence/zio-spark](https://github.com/univalence/zio-spark)
