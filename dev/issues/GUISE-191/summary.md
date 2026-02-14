# `GUISE-191` Summary: Upgrade Apache Commons JEXL from 3.1 to 3.6.2.

## Overview

Upgraded JEXL from version 3.1 (2017) to 3.6.2 (2026), closing a nine-year dependency gap spanning six major feature releases. The primary challenge was adapting to the JEXL 3.3+ permissions system ([JEXL-357](https://issues.apache.org/jira/browse/JEXL-357)), which blocks introspection of application classes by default, requiring explicit configuration to allow expression evaluation against domain types like `MeshIterator` and `Artifact`.

## Permissions Architecture

JEXL 3.3 introduced a security model that restricts introspection to core JDK packages under `RESTRICTED`. Application classes require explicit permission. The implementation evolved through two major design iterations:

**Initial approach: Package-level permissions** via `Package...` varargs. The caller would pass packages like `Artifact.class.getPackage()`, and `JexlMexlEvaluator` would map these to JEXL's `.*` wildcard syntax. This was rejected because the set of introspected classes is small (5 types across mesh and mummy) and relatively stable — new types only appear when context variables are introduced. Package wildcards granted unnecessarily broad access.

**Final approach: Class-level permissions** via `ClassPermissions` with package fallback. Both `GuiseMesh` and `JexlMexlEvaluator` accept `Set<Class<?>> additionalClasses` and `Set<Package> additionalPackages`. `ClassPermissions` provides exact canonical name matching for the 5 specific types (`MeshIterator`, `Artifact`, `MummyPlan`, `CollectionArtifact`, `Mummifier`), with `.compose()` chaining package wildcards when broader access is genuinely needed. `Set` is the idiomatic modern Java collection type, avoiding array-generics issues with varargs.

A key insight during implementation: **permission responsibility belongs at the layer that creates the introspected instances.** `GuiseMesh` owns `MeshIterator` creation and prepends `MeshIterator.class` to the permission set before delegating to `JexlMexlEvaluator`. The evaluator is merely a receiver of permissions, not an owner of domain knowledge. This clarified the abstraction boundary: callers of `GuiseMesh` specify their own types, `GuiseMesh` contributes its types, and `JexlMexlEvaluator` remains ignorant of specific domain classes.

## Implementation Details

- **`JexlMexlEvaluator`:** Package-private constructor accepts `Set<Class<?>>` and `Set<Package>`, converting to arrays internally for `ClassPermissions(classArray).compose(packageWildcards)`. Removed `INSTANCE` singleton since configurations vary by caller. Error messages switched from `getMessage()` to `getDetail()` for clean user-facing output ([JEXL-340](https://issues.apache.org/jira/browse/JEXL-340)).
- **`GuiseMesh`:** New `GuiseMesh(Set<Class<?>>, Set<Package>)` constructor merges `MeshIterator.class` with caller-supplied classes using pre-`this()` constructor logic (Java 25 JEP 482) and `Stream.concat().collect(toUnmodifiableSet())`. Retained no-arg constructor for backward compatibility.
- **`AbstractPageMummifier`:** Changed field initialization to explicit `Set.of(Artifact.class, MummyPlan.class, CollectionArtifact.class, Mummifier.class)`.
- **Custom resolver bypass:** Confirmed via JEXL 3.6.2 source inspection that `URF_PROPERTY_RESOLVER` bypasses `Introspector` entirely — URF property access requires no permission.
- **Test fix:** `GuiseMeshTest` changed from `Object.class.getSuperclass()` (denied by `RESTRICTED`) to a `Map` missing key lookup for null testing.
- **Documentation:** Added comprehensive Mesh module readme covering architecture, template attributes, getting started, and expression permissions API.
- **Modern Java:** Used JEP 456 unnamed variables (`_`) to suppress warnings for unused try-with-resources and lambda parameters.

## Test Coverage

All 93 tests pass (27 mesh, 66 mummy). Key regression coverage: `testMxEachWithIterVar` validates `MeshIterator` introspection, `shouldRetrieveUrfProperty` confirms custom resolver bypass, `shouldNotSeeUrfPojoProperty` verifies strategy isolation.
