# Plan: JEXL 3.1 → 3.6.2 Upgrade

## Progress

- [x] Step 1: Update dependency version
- [ ] Step 2: Configure JEXL permissions (redesign: class-level via `Set` instead of package-level via varargs)
- [x] Step 3: Improve error messages

## Overview

Four files changed: `JexlMexlEvaluator` and `GuiseMesh` in the `mesh` module, `AbstractPageMummifier` in the `mummy` module, and the parent POM. No new dependencies, no new Guise Mesh features. Permissions constructors on `GuiseMesh` accept `Set<Class<?>>` and `Set<Package>` so that callers need no knowledge of `JexlMexlEvaluator`.

## Steps

### 1. Update dependency version

In the parent `pom.xml`, change `commons-jexl3` version from `3.1` to `3.6.2`:

```xml
<!-- line ~166 -->
<version>3.6.2</version>
```

No change to `mesh/pom.xml` — version is inherited.

### 2. Configure JEXL permissions

Since JEXL 3.3 ([JEXL-357]), the default permissions are `JexlPermissions.RESTRICTED`, which allows introspection only of core JDK packages (`java.lang.*`, `java.util.*`, `java.math.*`, `java.nio.*`, `java.text.*`, `org.w3c.dom.*`). Application classes are blocked by default. The `compose()` method extends `RESTRICTED` with additional package wildcards — it does not replace.

JEXL permissions gate **standard JEXL introspection** only. Custom `PropertyResolver` implementations (like Guise Mesh's `URF_PROPERTY_RESOLVER`) bypass the permissions check entirely — the resolver receives the object directly and invokes methods via cast, never going through `Introspector`. This was confirmed by reading the JEXL 3.6.2 source:

- `Uberspect.getPropertyGet()` dispatches to custom resolvers without passing through `Introspector` ([Uberspect.java line 380](https://github.com/apache/commons-jexl/blob/rel/commons-jexl-3.6.2/src/main/java/org/apache/commons/jexl3/internal/introspection/Uberspect.java#L380)).
- Permissions are checked inside `Introspector.getMap()`, which is only called by built-in resolvers (`PROPERTY`, `MAP`, `DUCK`, etc.) ([Introspector.java line 304](https://github.com/apache/commons-jexl/blob/rel/commons-jexl-3.6.2/src/main/java/org/apache/commons/jexl3/internal/introspection/Introspector.java#L304)).
- The `.*` wildcard matching is recursive up the package hierarchy ([Permissions.java `wildcardAllow()`](https://github.com/apache/commons-jexl/blob/rel/commons-jexl-3.6.2/src/main/java/org/apache/commons/jexl3/internal/introspection/Permissions.java#L229)), so `dev.guise.mummy.*` covers `dev.guise.mummy.mummify` and all sub-packages.

JEXL also provides `JexlPermissions.ClassPermissions(Class<?>... allow)`, which permits introspection of individually named classes atop `RESTRICTED` — exact canonical name matching, not broadened to packages. `ClassPermissions.compose(String... src)` chains correctly, returning a new `ClassPermissions` that checks both the explicit class set and the composed package wildcards. This enables combining class-level and package-level permissions in a single `JexlPermissions` instance.

#### What needs permissions

The set of application classes reachable from MEXL expressions is small and stable. Class-level permissions via `ClassPermissions` provide tighter security than package-level wildcards while remaining easy to maintain. Package-level permissions remain available via `compose()` for cases where broader access is genuinely needed.

Two layers contribute classes:

**`GuiseMesh` base (always present):**

- `MeshIterator` — set as the `iter` variable during `mx:each` iteration; resolved through standard JEXL `PROPERTY` introspection.

**`AbstractPageMummifier` (caller-supplied):**

- `Artifact` — injected as the `artifact` context variable.
- `MummyPlan` — injected as the `plan` context variable.
- `CollectionArtifact` — a subtype of `Artifact` accessed when navigating collection pages.
- `Mummifier` — reachable via `artifact.getMummifier()`; resides in `dev.guise.mummy.mummify`.

The module dependency goes `mummy` → `mesh`, not the reverse — `mesh` has no dependency on `mummy`. The `mesh` module cannot import `Artifact.class` or other mummy types.

**Design options:**

1. **Hardcode both permission strings in `JexlMexlEvaluator`.** The `mesh` module adds `MeshIterator.class.getPackageName() + ".*"` (refactoring-safe). The `dev.guise.mummy.*` permission is a string literal documenting an inter-module contract. Simplest, but couples the `mesh` module to `mummy`'s package structure.

2. **Accept `String... additionalPackages` in the constructor.** The caller constructs package permission strings like `Artifact.class.getPackageName() + ".*"`. Keeps `mesh` free of `mummy` knowledge, but the caller must know the JEXL `.*` wildcard syntax — leaking an implementation-specific permission expression format through the API.

3. **Accept `JexlPermissions` in the constructor.** Type-safe, but exposes a JEXL API type through `JexlMexlEvaluator`'s public API, breaching the abstraction that JEXL is an implementation detail.

4. **Accept `Package...` in `JexlMexlEvaluator` constructor.** *(Reconsidered — see Option 5.)* The caller passes `Package` instances (obtained via e.g. `Artifact.class.getPackage()`). `JexlMexlEvaluator` internally maps each to the JEXL-specific permission string (`package.getName() + ".*"`). The `Package` type is a standard JDK type — no JEXL leakage, no syntax assumptions, refactoring-safe via archetypal class references. **Reconsidered** because it forces callers (e.g. `AbstractPageMummifier`) to know about `JexlMexlEvaluator`, which is an implementation detail of the `mesh` module. The public API surface is `GuiseMesh`, not the evaluator.

5. **Promote `Package...` to `GuiseMesh` constructor.** *(Reconsidered — see Option 6.)* `GuiseMesh` accepts `Package... additionalPackages` and internally creates a `JexlMexlEvaluator` with those packages. The evaluator constructor remains package-private. Callers interact only with `GuiseMesh` — no knowledge of `JexlMexlEvaluator`. Since the varargs signature accepts zero arguments, it subsumes the existing no-arg `GuiseMesh()` constructor. **Reconsidered** because package-level permissions are coarser than necessary — the set of classes accessible from expressions is small and known, so permitting entire packages opens access to classes that will never appear in expressions. Additionally, varargs forces array-based signatures, which are problematic with generics and less idiomatic in modern Java than `Set`.

6. **`Set<Class<?>>` + `Set<Package>` on `GuiseMesh`, using `ClassPermissions`.** `GuiseMesh` accepts `Set<Class<?>> additionalClasses` and `Set<Package> additionalPackages`. Class-level uses JEXL's `ClassPermissions` for exact class matching atop `RESTRICTED`; package-level uses `.compose()` for wildcard matching. `Set` is the idiomatic modern Java collection type — no array generics issues, immutable via `Set.of()`, self-documenting. Internal conversion to arrays is encapsulated within `JexlMexlEvaluator`.

**Chosen approach: Option 6 (`Set<Class<?>>` + `Set<Package>` on `GuiseMesh`).** The class set is small (currently 5 types across both layers) and stable — new types would only be added when new context variables are introduced, which is a significant API change that warrants updating permissions. Class-level permissions provide the tightest security without maintenance burden. Package-level permissions remain available as a secondary mechanism for cases where genuinely broad access is needed.

Specifically:

- **`JexlMexlEvaluator`:**
  - Constructor becomes package-private with `Set<Class<?>> permittedClasses, Set<Package> permittedPackages`.
  - Internally converts `permittedClasses` to `Class<?>[]` for the `ClassPermissions` constructor, and maps each package to `package.getName() + ".*"` for `.compose()`.
  - Constructs permissions as: `new JexlPermissions.ClassPermissions(classArray).compose(packageWildcards)`. When both sets are empty, `ClassPermissions` with no classes delegates directly to `RESTRICTED`.
  - Drop the `INSTANCE` singleton. Configurations vary by caller; per-`GuiseMesh` reuse is handled by the field in `AbstractPageMummifier`.

- **`GuiseMesh`:**
  - Add `GuiseMesh(Set<Class<?>> additionalClasses, Set<Package> additionalPackages)` constructor. Always prepends `MeshIterator.class` to the class set. Delegates to the existing `(MexlEvaluator, MeshInterpolator)` constructor:
    ```java
    public GuiseMesh(final Set<Class<?>> additionalClasses, final Set<Package> additionalPackages) {
        this(new JexlMexlEvaluator(mergeClasses(MeshIterator.class, additionalClasses), additionalPackages),
            DefaultMeshInterpolator.INSTANCE);
    }
    ```
  - Retain no-arg `GuiseMesh()` as a convenience constructor delegating to the two-`Set` constructor with `Set.of()` for both. `new GuiseMesh()` still works for simple use.
  - Replace `mergePackages()` with `mergeClasses(Class<?> baseClass, Set<Class<?>> additional)` — returns a new immutable `Set<Class<?>>` with the base class prepended.
  - Document via `@param` the semantic contract (which classes/packages become accessible in MEXL expressions). Document via `@implSpec` that `MeshIterator.class` is always included.

- **`AbstractPageMummifier`:**
  - Change field initialization to pass the mummy types as classes:
    ```java
    private final GuiseMesh guiseMesh = new GuiseMesh(
        Set.of(Artifact.class, MummyPlan.class, CollectionArtifact.class, Mummifier.class),
        Set.of());
    ```
  - No imports of `JexlMexlEvaluator` or `DefaultMeshInterpolator`.
  - Retain inline comment explaining **why** these classes are needed.

#### Permissions documentation placement

Each layer documents only what it knows:

- **`JexlMexlEvaluator` constructor `@param`/`@implSpec`:** Documents the generic semantics — classes are permitted via `ClassPermissions` for exact matching; packages are permitted via `compose()` for wildcard matching. No mention of specific types.
- **`GuiseMesh(Set, Set)` constructor `@param`/`@implSpec`:** Documents that each `Class` and `Package` enables JEXL introspection. Documents via `@implSpec` that `MeshIterator.class` is always included. No mention of specific mummy types.
- **`AbstractPageMummifier` inline comment:** Documents the `dev.guise.mummy`-specific rationale at the point of use — what context variables require the permission and why.

**`MeshIterator`** is the only `dev.guise.mesh` class currently requiring permission. During `mx:each` iteration, `GuiseMesh` sets the iter variable (default `iter`) to a `MeshIterator` instance. The `RESOLVER_STRATEGY` does not intercept `MeshIterator` (only `UrfResourceDescription`), so JEXL uses standard `PROPERTY` resolution, which requires introspection permission. Examples:

| Expression | Method introspected |
|---|---|
| `${iter.current}` | `MeshIterator.getCurrent()` |
| `${iter.index}` | `MeshIterator.getIndex()` |
| `${iter.first}` | `MeshIterator.isFirst()` |
| `${iter.last}` | `MeshIterator.isLast()` |

**Mummy types** — `AbstractPageMummifier` injects `plan` (`MummyPlan`) and `artifact` (`Artifact`) into the Mesh context. Both are interfaces resolved through standard JEXL introspection. `CollectionArtifact` is a subtype of `Artifact` for collection pages. `Mummifier` is reachable via `artifact.getMummifier()`. Examples:

| Expression | Method introspected |
|---|---|
| `${plan.rootArtifact}` | `MummyPlan.getRootArtifact()` |
| `${artifact.sourcePath}` | `Artifact.getSourcePath()` |
| `${artifact.navigable}` | `Artifact.isNavigable()` |
| `${artifact.mummifier}` | `Artifact.getMummifier()` → `Mummifier` |

#### What does NOT need permissions

- **`UrfResourceDescription`** (`io.urf.model`) — the `RESOLVER_STRATEGY` intercepts all `UrfResourceDescription` instances and routes them to `URF_PROPERTY_RESOLVER`, which casts and calls `findPropertyValueByHandle()` directly. The JEXL `Introspector` is never consulted. The `page` context variable (set to `artifact.getResourceDescription()`) and any chained URF property access are handled entirely by the custom resolver.
- **`Map`**, **`String`**, **`Path`**, primitives — all in packages already allowed by `RESTRICTED` (`java.util.*`, `java.lang.*`, `java.nio.*`).
- **`AtomicReference`** (`java.util.concurrent.atomic`) — covered by `java.util.*` since the wildcard match walks up the package hierarchy.

[JEXL-357]: https://issues.apache.org/jira/browse/JEXL-357

### 3. Improve error messages

In `JexlMexlEvaluator.evaluate()`, replace `jexlException.getMessage()` with `jexlException.getDetail()`:

```java
// Before:
throw new MexlException("Error in MEXL expression `%s`: %s".formatted(expression, jexlException.getMessage()), jexlException);

// After:
throw new MexlException("Error in MEXL expression `%s`: %s".formatted(expression, jexlException.getDetail()), jexlException);
```

`getDetail()` (added in 3.2 per [JEXL-340]) returns a clean description such as `"undefined variable foobar"` without the internal JEXL source location prefix that `getMessage()` includes.

## Verification

- `mvn test -pl mesh` — all `JexlMexlEvaluatorTest` and `GuiseMeshTest` tests must pass.
- `mvn test -pl mummy` — the `MummifySmokeIT` smoke test exercises `page.title` via `smoke-mesh.xhtml`, which validates that URF property resolution works end-to-end without `io.urf.*` permission.
- Key regression points:
  - `testMxEachWithIterVar` — exercises `iter.current`, which requires `MeshIterator` introspection (class-level permission via `ClassPermissions`).
  - `shouldRetrieveUrfProperty` / `shouldRetrieveUrfPropertyAsArray` — exercises URF property resolver (custom resolver, no permission needed).
  - `shouldNotSeeUrfPojoProperty` — confirms the custom resolver strategy still hides POJO getters on `UrfResourceDescription`.

## What is NOT changing

- **No logging changes.** The JEXL-387 NPE was caused by an uninitialized logger in JEXL 3.2.x and is fixed in 3.3+. No `jcl-over-slf4j`, no explicit logger on `JexlBuilder`.
- **No new dependencies.**
- **No new MEXL language features** (`let`/`const`, lambda, `switch`, `try`-`catch`).
- **No changes to `MexlEvaluator` interface.**

[JEXL-340]: https://issues.apache.org/jira/browse/JEXL-340
