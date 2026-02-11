# `GUISE-227`: Update Guise Mummy to Java 25.

## Objective

Upgrade Guise Mummy and all its modules to target Java 25, adopting modern language features and eliminating compiler warnings.

## Problem

Guise Mummy targets an older Java version and uses legacy idioms (traditional `/** */` Javadoc, old-style `instanceof` without pattern matching, classic `switch` statements, `String.format()`, mutable `collect(toList())`, etc.) that are verbose and do not take advantage of improvements available in modern Java.

## Desired Behavior

All modules (`guise-mummy`, `guise-mesh`, `guise-cli`, `guise-tomcat`) compile against and target Java 25, with:

- JEP 467 Markdown documentation comments (`///`) replacing all traditional `/** */` Javadoc.
- Modern language features applied where they improve clarity: pattern matching for `instanceof` (Java 16), pattern matching in `switch` (Java 21), enhanced `switch` expressions (Java 14), `Stream.toList()` (Java 16), `String.formatted()` (Java 15), unnamed variables `_` (Java 22).
- Zero compiler warnings from the upgraded source.

## Constraints

- The conversion to Markdown Javadoc must preserve all semantic content: `@param`, `@return`, `@throws`, `@see`, `@apiNote`, `@implSpec`, `@implNote`, and `{@inheritDoc}` tags remain as-is. The `@see` tag with URLs must retain the HTML `<a>` link form. The `{@value}` inline tag is valid in `///` comments and does not require conversion.
- The `S3Website.plan()` method uses intentional fall-through in a `switch` statement (`OPTIMAL` → `ROUTING_RULE`); converting to an enhanced `switch` expression requires structural rethinking or should be left as-is.
- Pattern matching for `instanceof` in `equals()` methods (e.g. `AbstractArtifact`, `AbstractS3DeployObject`) must preserve the negation idiom correctly (`!(object instanceof Foo foo)` or restructure).
- Dependencies (GlobalMentor libraries, AWS SDK, etc.) may impose their own minimum Java version constraints that must be verified.

## Acceptance Criteria

- All modules compile with `javac` targeting Java 25 with no errors.
- `mvn javadoc:aggregate` completes with no errors.
- No `/** */` Javadoc comment blocks remain in any source file.
- No `{@link}` or `{@code}` inline tags remain (converted to Markdown `[...]` and backticks respectively).
- Pattern matching `instanceof` replaces all old-style `instanceof`-then-cast idioms.
- All unit and integration tests pass.

## Non-Goals

- Adopting Java 25 preview features.
- Introducing `sealed` classes, `record` types, or virtual threads — the existing class hierarchy does not call for these.
- Upgrading dependency library versions beyond what is needed for Java 25 compatibility.
- Refactoring application architecture or module boundaries.

## Guidance

The `instanceof` modernization and `switch` pattern matching opportunities are concentrated in a few key files: `NavigationManager`, `MeshIterator`, `DefaultMummyPlan`, `AbstractPageMummifier`, `DefaultImageMummifier`, `JexlMexlEvaluator`, `S3Website`, and `S3`. The `MeshIterator.toIterator()` method and `NavigationManager.navigationItemsFromUrfList()` are particularly strong candidates for `switch` with type patterns due to their long if-else-instanceof chains.
