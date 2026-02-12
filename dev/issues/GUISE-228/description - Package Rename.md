# `GUISE-228`: Rename `io.guise` package to `dev.guise`.

## Objective

Rename the Java package hierarchy from `io.guise` to `dev.guise` across all modules, and update all `guise.io` references to `guise.dev`, reflecting the domain move.

## Problem

The Guise domain is moving from `guise.io` to `guise.dev`. Java convention maps the reversed domain name to the package hierarchy, so `io.guise` must become `dev.guise`. The current package name, Maven groupId, XML namespace URIs, web site URLs, and related references will no longer correspond to the project's canonical domain.

## Desired Behavior

All Java packages, Maven coordinates, directory structures, and documentation references reflect the `dev.guise` identity:

- Java packages: `dev.guise.mesh`, `dev.guise.mummy`, `dev.guise.mummy.deploy.aws`, `dev.guise.catalina.webresources`, `dev.guise.cli`, etc.
- Maven groupId: `dev.guise`.
- Project URL: `https://guise.dev/mummy/`.
- XML namespace URIs: `https://guise.dev/name/mesh/`, `https://guise.dev/name/mummy/`.
- All web site URLs referencing `guise.io` point to `guise.dev`.
- Documentation Maven coordinates and class-name references updated accordingly.
- A migration guide section is added to a readme documenting the namespace URI change for existing Guise sites.

## Constraints

- **XML namespace URI migration is a breaking change.** The Guise Mesh and Guise Mummy XHTML namespace URIs (`https://guise.io/name/mesh/`, `https://guise.io/name/mummy/`) are embedded in Java constants (`GuiseMesh.NAMESPACE_STRING`, `GuiseMummy.NAMESPACE_STRING`), user-facing XHTML templates (including the demo project `.template.xhtml`), test resources, and documentation. Migrating these to `guise.dev` will break existing Guise sites that use the old namespace URIs in their XHTML content. Few Guise sites exist in the wild beyond our control, so the mitigation is a migration guide section in a readme documenting the required namespace update.
- **Maven Central artifact continuity.** The existing `io.guise` groupId has published artifacts. The new `dev.guise` groupId must be registered and verified for Maven Central publishing. Consumers depending on `io.guise:guise-*` coordinates will need to update their dependencies.
- **Resource file paths** under `src/main/resources/` and `src/test/resources/` mirror the package hierarchy (e.g., `io/guise/mummy/GuiseMummy-config.properties`, test XHTML and image files). These paths must be renamed in tandem with the Java packages so that classpath-relative resource loading continues to work.
- **No Java module descriptors exist** (`module-info.java`), so JPMS module naming is not a concern.

## Acceptance Criteria

- No Java source file contains `package io.guise` or `import io.guise`.
- No `io/guise/` directory remains under any `src/` tree.
- All `pom.xml` files use `<groupId>dev.guise</groupId>`.
- The `exe.main.class` property in `cli/pom.xml` references `dev.guise.cli.GuiseCli`.
- All `readme.md` Maven coordinate examples and class-name references use `dev.guise`.
- The root POM `<url>` reflects the `guise.dev` domain.
- XML namespace URIs in Java constants, XHTML templates, test resources, and documentation use `guise.dev`.
- A migration guide section documents the namespace URI change for existing users.
- All modules compile, and all tests pass.
- The demo projects function correctly with `guise.dev` namespace URIs.

## Non-Goals

- Publishing the renamed artifacts to Maven Central (that is a release activity).
- Renaming Maven artifact IDs (e.g., `guise-mummy` remains `guise-mummy`).
- Refactoring package structure beyond the `io` → `dev` prefix swap.
- Updating external systems (CI, JIRA, documentation sites) to reflect the new domain.

## Guidance

The rename is mechanically straightforward but touches nearly every file. The four modules and their source/test/resource trees that require directory renames are rooted at:

- `cli/src/main/java/io/guise/cli/`
- `mesh/src/main/java/io/guise/mesh/` and `mesh/src/test/java/io/guise/mesh/`
- `mummy/src/main/java/io/guise/` and `mummy/src/test/java/io/guise/`
- `mummy/src/main/resources/io/guise/` and `mummy/src/test/resources/io/guise/`
- `tomcat/src/main/java/io/guise/catalina/`

Beyond the Java source tree, the `guise.io` domain also appears in XML namespace URI constants (`GuiseMesh.NAMESPACE_STRING`, `GuiseMummy.NAMESPACE_STRING`), XHTML templates (including the demo project at `demo-basic/src/site/.template.xhtml`), and test resource XHTML files. All of these must be updated to `guise.dev`. The Guise Skeleton CSS file in `demo-basic/` is a vendored dependency artifact and should not be modified here; it will be updated when Guise Skeleton itself migrates.
