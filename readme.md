# Guise

The Guise™ Internet application ecosystem comprises three main products and related utilities:

* Guise™ Skeleton
: Simple, semantic, bare-bones CSS framework. _Guise Skeleton is not yet included in this distribution._
* **[Guise™ Mummy](mummy/)**
: Static site generator.
* [Guise™ Framework](framework/)
: Maintainable and scalable Internet application framework. _Guise Framework is being updated from its legacy implementation and is not ready for production use._
* [Guise™ CLI](cli/)
: The command-line interface for invoking and working with the three products.

Currently Guise Mummy is functional and can be used with sites using XHTML5 source.

## Download

The Guise modules are available in the Maven Central Repository with the [io.guise](https://search.maven.org/search?q=g:io.guise) artifact ID. This aggregate parent project is available in the Maven Central Repository as [io.guise:guise](https://search.maven.org/search?q=g:io.guise%20AND%20a:guise).

## Build

Guise requires Java 11 and has been tested with the [OpenJDK 11](https://openjdk.java.net/projects/jdk/11/) on Windows 10. Building requires a recent version of [Apache Maven](https://maven.apache.org/).

Build Guise using the following command:

```
mvn clean install
```

This will create all of the projects. _Building all projects is necessary, even if you want to use only one._ The CLI can be invoked using the following (substituting the appropriate version for `x.x.x`):

```
java -jar cli/target/guise-cli-x.x.x-exe.jar
```

On Windows the build process creates an executable CLI, which you may copy and use as desired, or execute in place:

```
cli\target\guise.exe
```

## Usage

Guise usage is explained in each of the subprojects, listed above.

## Issues

Issues tracked by [JIRA](https://globalmentor.atlassian.net/projects/GUISE).

## Changelog

- 0.1.0: First working release of Guise Mummy.
