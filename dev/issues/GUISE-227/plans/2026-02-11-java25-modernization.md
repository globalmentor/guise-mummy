# Java 25 Modernization Plan

## Checklist

- [ ] Step 1: Pattern matching `instanceof` (~43 sites, 15 files)
  - Skip: `AbstractMummyPlan` lines 50/56 — boolean-only test, no cast
  - Skip: `S3Website` line 360 — boolean-only test in compound condition
  - Skip: `S3.java` line 351 — ternary expression, no cast needed
  - Skip: `DirectoryMummifier` line 175 — boolean-only test in compound condition
  - Skip: `PageMummifier` lines 125/149 — ternary expression, no cast to bind
  - Special: `MexlEvaluator` line 49 — `instanceof Optional` with unchecked cast; pattern matching applicable but requires `@SuppressWarnings`
  - Special: `AbstractPageMummifier` line 313 — `instanceof` in `while` loop condition; cannot use pattern variable
  - Special: `AbstractPageMummifier` line 253 — boolean `instanceof` inside lambda predicate, followed by cast in same expression
- [ ] Step 2: `switch` with type patterns (~2 sites, 2 files)
- [ ] Step 3: Enhanced `switch` expressions (~7 sites, 5 files)
  - Skip: `S3Website.plan()` — intentional fall-through from `OPTIMAL` to `ROUTING_RULE`
- [ ] Step 4: `Stream.toList()` (~11 sites, 7 files)
- [ ] Step 5: `String.formatted()` (~52 sites, 13 files)
- [ ] Step 6: Unnamed catch variables `_` (1 site)
- [ ] Compile and test

---

## Step 1: Pattern Matching `instanceof`

Convert old-style `instanceof`-then-cast to pattern matching `instanceof` (JEP 394, Java 16). Pattern variable bindings eliminate the separate downcast.

### 1a. Standard `instanceof`-with-cast sites

Each of these follows the pattern:
```java
// before
if(foo instanceof Bar) {
    final Bar bar = (Bar)foo;
    bar.doSomething();
}

// after
if(foo instanceof Bar bar) {
    bar.doSomething();
}
```

Or the negated `equals()` pattern:
```java
// before
if(!(object instanceof Foo)) { return false; }
return getKey().equals(((Foo)object).getKey());

// after
if(!(object instanceof Foo foo)) { return false; }
return getKey().equals(foo.getKey());
```

#### mummy module — main sources

| File | Line(s) | Pattern | Notes |
|---|---|---|---|
| `AbstractArtifact.java` | 88–91 | negated `equals()` | `!(object instanceof Artifact)` then cast on 91 |
| `AbstractS3DeployObject.java` | 81–84 | negated `equals()` | `!(object instanceof S3DeployObject)` then cast on 84 |
| `DefaultMummyPlan.java` | 59–62 | nested `instanceof` | `CompositeArtifact` then `CollectionArtifact` |
| `GuiseMummy.java` | 438–440 | standard | `CollectionArtifact` then cast on 440 |
| `DefaultImageMummifier.java` | 148–149 | standard | `AspectualArtifact` then cast on 149 |
| `DefaultImageMummifier.java` | 243–249 | standard | `JPEGImageWriteParam` then cast on 249 |
| `DirectoryMummifier.java` | 330–332 | `checkArgument` + cast | `checkArgument(artifact instanceof DirectoryArtifact, ...)` then cast on 332; convert to pattern in `if` or keep as assertion |
| `S3.java` | 353–355 | standard | `CompositeArtifact` then cast on 355 |
| `S3Website.java` | 442–444 | standard | `S3ArtifactRedirectDeployObject` then cast on 444 |
| `S3Website.java` | 490–493 | standard | Same type, `preparePutObject()` |
| `S3Website.java` | 504–506 | standard | Same type, `findDetailLabel()` |
| `XhtmlPageMummifier.java` | 55–57 | standard | `SAXParseException` then cast on 57 |
| `MarkdownPageMummifier.java` | 172–174 | negated + cast | `!(object instanceof Map)` then cast on 174 |
| `AbstractPageMummifier.java` | 528–532 | negated filter + cast | `!(childNode instanceof Element)` skip, then cast on 532 |
| `AbstractPageMummifier.java` | 694–695 | standard | `templateHeadChildNode instanceof Element` then cast on 695 |
| `AbstractPageMummifier.java` | 891–895 | negated filter + cast | Same pattern as 528 |
| `AbstractPageMummifier.java` | 1149–1153 | negated filter + cast | Same pattern as 528 |
| `AbstractPageMummifier.java` | 1316–1320 | negated filter + cast | Same pattern as 528 |
| `AbstractPageMummifier.java` | 253 | boolean + cast in lambda | See special cases below |
| `AbstractPageMummifier.java` | 910–914 | if-else chain | `toLong()`: `Long` / `Integer`; becomes switch in Step 2 |
| `MummyPlan.java` | 69 | ternary + cast | `principalArtifact instanceof CollectionArtifact ? ((CollectionArtifact)principalArtifact).getChildArtifacts()` — pattern variable eliminates the cast |

#### mesh module

| File | Line(s) | Pattern | Notes |
|---|---|---|---|
| `GuiseMesh.java` | 257–263 | if-else chain | `CharacterData` / `Element`; both cast |
| `JexlMexlEvaluator.java` | 46–49 | negated | `!(object instanceof UrfResourceDescription)` then cast (implicit via `null` return) |
| `JexlMexlEvaluator.java` | 64–65 | standard | `UrfResourceDescription` then cast on 65 |
| `JexlMexlEvaluator.java` | 146 | compound `&&` | `object instanceof UrfResourceDescription && key instanceof String`; both cast on 147 |
| `MeshIterator.java` | 143–162 | long if-else chain | Becomes switch in Step 2 |

### 1b. Ternary-only / boolean-only sites (no explicit cast follows — SKIP pattern variable)

These use `instanceof` only for a boolean check without casting, or in a ternary where the cast can be eliminated by pattern matching but is part of a complex expression. Assess individually.

| File | Line | Current Code | Action |
|---|---|---|---|
| `AbstractMummyPlan.java` | 50 | `toArtifact instanceof CollectionArtifact` (boolean arg) | **Skip** — no cast to bind |
| `AbstractMummyPlan.java` | 56 | Same | **Skip** |
| `S3Website.java` | 360 | `… instanceof CollectionArtifact` in compound `&&` | **Skip** — no cast |
| `S3.java` | 351 | Ternary: `artifact instanceof CollectionArtifact ? toCollectionURI(…) : …` | **Skip** — no cast needed |
| `S3.java` | 372 | `!(artifact instanceof CollectionArtifact)` — negated boolean guard | **Skip** — no cast |
| `DirectoryMummifier.java` | 175 | `registeredChildMummifier instanceof PageMummifier` — boolean only in `&&` | **Skip** — no cast |
| `MummyPlan.java` | 69 | Ternary: `principalArtifact instanceof CollectionArtifact ? ((CollectionArtifact)principalArtifact).getChildArtifacts()…` | **Moved to 1a** — has a cast |
| `PageMummifier.java` | 125 | Ternary: `principalArtifact instanceof CollectionArtifact ? Optional.of(principalArtifact) : …` | **Skip** — no cast |
| `PageMummifier.java` | 149 | Ternary: `principalArtifact instanceof CollectionArtifact ? plan.childArtifacts(…) : …` | **Skip** — no cast |

### 1c. Special cases

#### `AbstractPageMummifier.java` line 313 — `instanceof` in `while` condition

```java
} while(leadingSegment == null && (currentNode = currentNode.getParentNode()) instanceof Element);
```

Pattern variables in loop conditions for `while` don't flow into the loop body because the variable isn't definitely assigned at the point where the body would use it. The `currentNode` is only used in the *next* iteration's `do` body as `(Element)currentNode`. However, inspecting the code: the cast `(Element)currentNode` doesn't actually happen — the body uses `findAttributeNS((Element)currentNode, …)` which is at the *top* of the `do` block, where `currentNode` was set by the `while` condition's assignment. This is a case where the `instanceof` guards the assignment+loop continuation, but the cast happens at the top of the next `do` iteration, not after the `while`. **Skip** — the loop structure prevents using a pattern variable meaningfully.

#### `AbstractPageMummifier.java` line 253 — `instanceof` in lambda predicate

```java
.filter(not(navArtifact -> navArtifact instanceof SourcePathArtifact && ((SourcePathArtifact)navArtifact).isPost()))
```

This can use a pattern variable because within the `&&`, the pattern match is in scope:

```java
.filter(not(navArtifact -> navArtifact instanceof SourcePathArtifact sourcePathArtifact && sourcePathArtifact.isPost()))
```

#### `MexlEvaluator.java` line 49 — `instanceof Optional` with unchecked cast

```java
final Optional<Object> optionalResult = result instanceof Optional ? (Optional<Object>)result : Optional.ofNullable(result);
```

Pattern matching for `instanceof` doesn't help with raw-to-parameterized casts. The `@SuppressWarnings("unchecked")` is already present. **Skip** — no improvement possible.

### 1d. Alternatives Considered

- **`switch` with type patterns for all chains**: Considered for `GuiseMesh.meshChildNodes()` (2-branch if-else) and similar short chains. Rejected: a 2-branch if-else doesn't gain readability from `switch`. Reserve `switch` type patterns for chains of 3+ branches (Step 2).
- **No change for `equals()` methods**: Considered leaving the idiomatic `!(x instanceof T)` / `((T)x)` pattern in `equals()` unchanged. Rejected: the negated pattern variable `!(object instanceof Foo foo)` is well-established and simpler.

---

## Step 2: `switch` with Type Patterns

Convert long `instanceof` if-else chains to `switch` expressions with type patterns (JEP 441, Java 21).

### 2a. `MeshIterator.toIterator()` (mesh module, lines 142–165)

**Before** (8-branch if-else chain):
```java
if(object.getClass().isArray()) {
    if(object instanceof Object[]) {
        return asList((Object[])object).iterator();
    } else if(object instanceof int[]) { … }
      …
} else if(object instanceof Iterable) { … }
  …
```

**After** (`switch` expression):
```java
protected static Iterator<?> toIterator(@NonNull final Object object) {
    return switch(object) {
        case Object[] array -> asList(array).iterator();
        case int[] array -> stream(array).iterator();
        case long[] array -> stream(array).iterator();
        case double[] array -> stream(array).iterator();
        case Iterable<?> iterable -> iterable.iterator();
        case Iterator<?> iterator -> iterator;
        case Enumeration<?> enumeration -> enumeration.asIterator();
        case Stream<?> stream -> stream.iterator();
        case Map<?, ?> map -> map.entrySet().iterator();
        default -> Set.of(object).iterator();
    };
}
```

Note: The original code has an `isArray()` guard with an `else throw` for unsupported primitive arrays (e.g. `boolean[]`). The `switch` version handles this implicitly: `Object[]` matches reference arrays, and the three primitive array cases are explicit, so any other array (e.g. `boolean[]`, `float[]`) falls to `default → Set.of(object).iterator()` which wraps the array as a single object. This differs from the original behavior which threw `IllegalArgumentException` for unsupported primitive arrays.

**Decision**: Add a guarded `default` that checks `object.getClass().isArray()` and throws if so; otherwise wraps as single object. Or add explicit cases. The explicit guard is cleaner:

```java
default -> {
    if(object.getClass().isArray()) {
        throw new IllegalArgumentException(
                "Iteration not supported on array of type %s: %s."
                        .formatted(object.getClass().getComponentType().getName(), object));
    }
    yield Set.of(object).iterator();
}
```

### 2b. `AbstractPageMummifier.toLong()` (mummy module, lines 908–918)

**Before** (2-branch if-else + throw):
```java
if(object instanceof Long) {
    return (Long)object;
} else if(object instanceof Integer) {
    return Long.valueOf(((Integer)object).longValue());
} else {
    throw new IllegalArgumentException(…);
}
```

**After**:
```java
return switch(object) {
    case Long l -> l;
    case Integer i -> Long.valueOf(i.longValue());
    default -> throw new IllegalArgumentException(…);
};
```

### Alternatives Considered

- **`NavigationManager.navigationItemsFromUrfList()` inner if-else**: The `navObject` if-else chain (lines 340–378) has 3 outer branches (`CharSequence`, `URI`, `UrfResourceDescription`) with 2 nested branches inside `UrfResourceDescription`. Considered converting to a `switch`. Rejected: the `UrfResourceDescription` branch doesn't just cast and use — it does a nested `findPropertyValueByHandle` that produces a sub-chain, and the nesting would become confusing in a `switch`. Pattern matching `instanceof` alone (Step 1) is sufficient here.
- **`GuiseMesh.meshChildNodes()` if-else**: Only 2 branches (`CharacterData` / `Element`). Not worth a `switch` — too few branches.

---

## Step 3: Enhanced `switch` Expressions

Convert old-style `switch(x) { case A: …; break; … default: throw …; }` to enhanced switch expressions (JEP 361, Java 14).

### Sites

| File | Line | Switch on | Branches | Notes |
|---|---|---|---|---|
| `NavigationManager.java` | 167–175 | filename extension (String) | 2 + default | Simple arrow-case return |
| `Route53.java` | 238–245 | `resourceRecordType` (enum) | 1 + default | Return expression |
| `Dns.java` | 288–294 | `resourceRecordType` (enum) | 1 + default | Return expression |
| `CloudFront.java` | 266–296 | `domainValidation.validationStatus()` (enum) | 3 + default | Contains statements, I/O; use arrow-case with blocks |
| `DirectoryWidget.java` | 111–152 | `groupByValue` (String) | 2 + default | Contains whole blocks with `break`; convert to arrow-case with blocks |
| `DirectoryWidget.java` | 180–229 | `archetype` (String) | 1 + default | Contains block; convert to arrow-case |

### Skip

| File | Line | Reason |
|---|---|---|
| `S3Website.java` | 418–451 | Intentional fall-through from `OPTIMAL` to `ROUTING_RULE`. Enhanced `switch` does not support fall-through. Restructuring to extract the `ROUTING_RULE` body into a method and calling it from both cases would work but is a behavioral refactoring beyond scope. **Leave as-is.** |

### Example: `NavigationManager.loadNavigationFile()`

**Before**:
```java
switch(findFilenameExtension(navigationFile)
        .orElseThrow(…)) {
    case Text.LST_FILENAME_EXTENSION:
        return loadNavigationFileList(context, artifact, navigationFile);
    case TURF.FILENAME_EXTENSION:
        return loadNavigationFileTurf(context, artifact, navigationFile);
    default:
        throw new AssertionError(…);
}
```

**After**:
```java
return switch(findFilenameExtension(navigationFile)
        .orElseThrow(…)) {
    case Text.LST_FILENAME_EXTENSION -> loadNavigationFileList(context, artifact, navigationFile);
    case TURF.FILENAME_EXTENSION -> loadNavigationFileTurf(context, artifact, navigationFile);
    default -> throw new AssertionError(…);
};
```

### Alternatives Considered

- **Convert `S3Website.plan()` by extracting a `convertToRoutingRules()` method**: This would enable enhanced `switch` but changes the structure and introduces a new method. Rejected: not worth the refactoring churn for a cosmetic improvement. The `@SuppressWarnings("fallthrough")` documents the intent.

---

## Step 4: `Stream.toList()`

Convert `.collect(toList())` to `.toList()` (Java 16). `Stream.toList()` returns an unmodifiable list, which aligns with the project's preference for immutable collections.

### Sites

| File | Line | Action | Notes |
|---|---|---|---|
| `AbstractPageMummifier.java` | 373 | Convert | Result is a `List` passed to `extractSourceMetadata` |
| `GuiseMummy.java` | 315 | Convert | Local list of sections |
| `GuiseMummy.java` | 429 | Convert | Debug-only collection |
| `GuiseMummy.java` | 433 | Convert | Debug-only collection |
| `DirectoryWidget.java` | 227 | Convert | Blog archetype item list (collect then return) |
| `MarkdownPageMummifier.java` | 188 | Convert | YAML metadata entries |
| `Route53.java` | 183 | Convert | Log message construction |
| `Route53.java` | 218 | Convert | Resource records list — verified not mutated downstream |
| `Dns.java` | 263 | Convert | Site alt domains list |
| `Dns.java` | 302 | Convert | Resource records list |
| `NavigationManager.java` | 196 | Convert | Eager collection; `.toList()` is equally eager |

**Total: 11 sites.**

Note: `NavigationManager.java` line 196 has a comment "(important) collect the artifacts to a list to prevent any exceptions upon stream iteration after method return." The critical aspect is *eager evaluation*, and `.toList()` is also eager, so this site converts without issue.

### Alternatives Considered

- **`toUnmodifiableList()` instead**: `Stream.toList()` already returns an unmodifiable list in practice (throws on mutation). The formal spec says it returns an "unmodifiable List" (but not necessarily `Collections.unmodifiableList()`). Since the semantic is the same, prefer the shorter `.toList()`.

---

## Step 5: `String.formatted()`

Convert `String.format("…", args)` to `"…".formatted(args)` (Java 15). This is a mechanical, cosmetic change.

### Scope

- **mummy module main**: 45 sites
- **mesh module**: 3 sites (statically imported `format()` — see below)
- **tomcat module**: 3 sites
- **cli module**: 0 sites (uses picocli command framework)

**Total: ~51 sites.**

### Mesh module note

The mesh module uses `import static java.lang.String.format;` and calls `format("…", args)`. This is already terse. Converting to `"…".formatted(args)` would require removing the static import and is slightly longer than `format()`. For consistency, convert these; the static import of `format` is ambiguous (could be confused with `Formatter.format` or `PrintStream.format`), whereas `.formatted()` is unambiguous.

### Example

```java
// before
throw new IOException(String.format("Invalid markdown in `%s`.", name));

// after
throw new IOException("Invalid markdown in `%s`.".formatted(name));
```

When the `format()` call is statically imported:
```java
// before
throw new MexlException(format("Error in MEXL expression `%s`: %s", expression, jexlException.getMessage()), jexlException);

// after
throw new MexlException("Error in MEXL expression `%s`: %s".formatted(expression, jexlException.getMessage()), jexlException);
```

### Tomcat module note

`SiteDirResourceSet.java` has 2 `String.format()` calls inside `log.warn()` and `log.error()`. Since these use SLF4J logging, the `String.format()` is actually a pessimization — the message is always formatted even if the log level is disabled. However, converting to SLF4J `{}` interpolation would change the message format and is a separate concern. For this step, simply convert `String.format()` → `.formatted()`.

### Alternatives Considered

- **Leave `format()` static import in mesh module as-is**: It's already terse. But the inconsistency with the rest of the project and the ambiguity of `format()` (which `format`?) tips toward conversion.
- **Convert SLF4J `String.format()` to `{}` interpolation**: Out of scope for this step — it's a behavior-changing optimization, not a language modernization.

---

## Step 6: Unnamed Catch Variables `_`

Convert unused catch variables to `_` (JEP 456, Java 22).

### Sites

| File | Line | Exception | Notes |
|---|---|---|---|
| `CloudFront.java` | 247 | `InterruptedException interruptedException` | Body is empty (comment only) |

```java
// before
} catch(final InterruptedException interruptedException) {
    //if interrupted while sleeping, just try again and keep backing off
}

// after
} catch(final InterruptedException _) {
    //if interrupted while sleeping, just try again and keep backing off
}
```

Only 1 site found. Other catch clauses use the exception variable (for wrapping, logging, etc.).

---

## Implementation Order

1. **Step 1 (instanceof)** — largest scope, foundational; later steps may touch the same lines.
2. **Step 2 (switch type patterns)** — depends on Step 1 for `MeshIterator` which shares the same if-else chain.
3. **Step 3 (enhanced switch)** — independent of Steps 1–2 but logically grouped.
4. **Step 4 (toList)** — independent, mechanical.
5. **Step 5 (formatted)** — independent, mechanical. Do last among code changes since it's purely cosmetic and touches many lines.
6. **Step 6 (unnamed catch)** — trivial, include with any batch.
7. **Compile and test** — `mvn compile test-compile` after each step; full `mvn verify` at end.

Work each file-by-file within each step to keep changes reviewable. Commit after each step or logical group.
