# Guise Mummy Project

The Guise™ Mummy project comprises several products and related utilities:

* [Guise™ Mummy](mummy/)
: Static site generator.
* [Guise™ Mesh](mesh/)
: Template transformation engine used in Guise Mummy.
* [Guise™ CLI](cli/)
: The command-line interface for invoking Guise Mummy for static site generation and deployment.

## Build

Guise requires Java 11 and has been tested with the [OpenJDK 11](https://openjdk.java.net/projects/jdk/11/) on Windows 10. Building requires a recent version of [Apache Maven](https://maven.apache.org/).

Build Guise Mummy using the following command:

```
mvn clean install
```

This will create all of the projects. _Building all projects is necessary, even if you want to use only one._ The CLI can be invoked using the following (substituting the appropriate version for `x.x.x`):

```
cli/target/bin/guise
```

On Windows the build process creates an executable CLI, which you may copy and use as desired, or execute in place:

```
cli\target\bin\guise.exe
```

## Usage

Guise usage is explained in each of the subprojects, listed above.

## Issues

Issues tracked by [JIRA](https://globalmentor.atlassian.net/projects/GUISE).

## Changelog

- 0.1.0: First working release of Guise Mummy.
