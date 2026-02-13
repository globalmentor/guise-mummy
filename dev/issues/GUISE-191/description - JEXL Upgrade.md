# `GUISE-191`: Upgrade JEXL from 3.1 to 3.6.x.

## Objective

Upgrade the Apache Commons JEXL dependency from v3.1 to the latest 3.6.x release, gaining years of bug fixes, security hardening, and improved error diagnostics.

## Problem

Guise Mesh depends on JEXL 3.1 (released 2017). The current release is 3.6.2 (2026-02-05). The gap spans nine years and six major feature releases. Beyond staleness, the outdated version lacks the `JexlException.getDetail()` API ([JEXL-340](https://issues.apache.org/jira/browse/JEXL-340)), forcing `JexlMexlEvaluator` to wrap the noisy `getMessage()` output (which includes internal caller location) rather than providing clean error messages to users.

## History

This upgrade was originally deferred during the Guise 5.x release cycle. An attempt to upgrade to 3.2.1 was blocked by [JEXL-387](https://issues.apache.org/jira/browse/JEXL-387), an internal `NullPointerException` caused by uninitialized logging. That bug was resolved in 3.3. However, JEXL 3.3 also introduced a new permissions system ([JEXL-357](https://issues.apache.org/jira/browse/JEXL-357)) that by default restricts which classes, methods, and fields JEXL can access via introspection. During the JEXL-387 investigation, this was discovered to prevent JEXL from finding `MeshIterator.getCurrent()` and similar methods without explicit permission configuration.

## Desired Behavior

Guise Mesh uses a current, supported version of JEXL with no regressions in MEXL expression evaluation, and MEXL error messages presented to users are clean (e.g. "undefined variable foobar" rather than including internal JEXL caller locations).

## Constraints

- Starting in JEXL 3.3, the permissions system ([JEXL-357](https://issues.apache.org/jira/browse/JEXL-357)) requires explicit configuration to allow introspection of application classes. Without this, JEXL will silently fail to find methods such as `MeshIterator.getCurrent()` and `UrfResourceDescription` property accessors. This must be addressed in the `JexlBuilder` configuration.
- The JEXL integration is cleanly isolated to `JexlMexlEvaluator` in the `mesh` module. It uses `JexlBuilder`, `JexlUberspect.PropertyResolver`, `JexlUberspect.ResolverStrategy`, `JexlPropertyGet`, and `JexlEngine.TRY_FAILED`. Any API changes in these areas across 3.1→3.6.x must be accounted for.
- The JEXL version is managed in the parent `pom.xml` (`commons-jexl3` version `3.1`).

## Acceptance Criteria

- The `commons-jexl3` dependency version in `pom.xml` is updated to 3.6.x (latest).
- JEXL permissions are configured so that `MeshIterator`, `UrfResourceDescription`, and any other classes accessed via MEXL expressions are introspectable.
- MEXL error messages use `JexlException.getDetail()` (from [JEXL-340](https://issues.apache.org/jira/browse/JEXL-340)) to provide clean messages without internal caller locations.
- All existing `JexlMexlEvaluatorTest` and `GuiseMeshTest` tests pass without regressions.

## Non-Goals

- Rewriting the MEXL evaluation layer or replacing JEXL with another expression library.
- Adopting new JEXL language features (e.g. `let`/`const`, `switch`, lambda expressions, `try`-`catch`) in MEXL expressions.

## Guidance

- The [JEXL release notes](https://commons.apache.org/proper/commons-jexl/changes.html) enumerate fixes across all versions since 3.1.
- [JEXL-340](https://issues.apache.org/jira/browse/JEXL-340) added `JexlException.getDetail()`, which returns a clean message such as "undefined variable foobar" without the internal source location prefix. The current `JexlMexlEvaluator.evaluate()` wraps `jexlException.getMessage()` — switching to `getDetail()` would improve user-facing error messages. [JEXL-341](https://issues.apache.org/jira/browse/JEXL-341) complementarily improved the information provided in caught exceptions.
- [JEXL-357](https://issues.apache.org/jira/browse/JEXL-357) (3.3) introduced the permissions system. The `JexlBuilder` API provides methods such as `permissions()` for configuring allowed classes/packages. The discussion in [JEXL-387](https://issues.apache.org/jira/browse/JEXL-387) documents the specific failure mode encountered with Guise Mesh classes.
- The JEXL integration surface is concentrated in `mesh/src/main/java/dev/guise/mesh/JexlMexlEvaluator.java`. Everything else touches JEXL only through the `MexlEvaluator` abstraction.
