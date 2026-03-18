# Plan: Artifact Tree Walker

Add a general-purpose artifact tree walker to the Guise Mummy artifact model. `ArtifactTreeWalker` is a top-level class providing static `walk()` methods for depth-first pre-order traversal of artifact trees. It contains a nested `Visitor` functional interface for the visit callback. `MummyPlan.walk()` provides a convenience entry point from the plan. The walker descends into all comprised artifacts and communicates subsumption status to the visitor, allowing visitor-level control over subsumed artifact handling.

## Overview

**Single chunk** — all additive code. No existing files are modified in incompatible ways. Compiles and tests at every step.

- Step 1: `ArtifactTreeWalker` with nested `Visitor` interface
- Step 2: `ArtifactTreeWalker.walk()` static methods
- Step 3: `MummyPlan.walk()` convenience default method
- Step 4: `ArtifactTreeWalkerTest` — walker tests

**Notable decisions:**

- **`ArtifactTreeWalker` as the primary type.** The walker is the entry point; the visitor callback has no independent utility outside tree traversal (no `accept()` on `Artifact`, no double-dispatch, no standalone use cases in the codebase). The name `ArtifactTreeWalker` makes the tree-traversal semantics explicit — analogous to the DOM `TreeWalker`. `ArtifactWalker` would be ambiguous (could imply walking through an artifact's properties rather than walking a tree of artifacts).
- **Nested `ArtifactTreeWalker.Visitor`** for the callback interface. Since the visitor has no meaning outside the walker — the `subsumed` parameter is specifically a tree-walk concept — nesting scopes it to its only consumer. This follows the `Map.Entry` principle: scope a type inside its only meaningful context. The short name `Visitor` is unambiguous within the nesting context; `ArtifactVisitor` as a nested type would stutter (`ArtifactTreeWalker.ArtifactVisitor`). While the JDK and major frameworks use top-level visitors (`FileVisitor`, `ClassVisitor`), those visitors have independent utility (multi-method interfaces, double-dispatch). Our single-method `@FunctionalInterface` callback does not.
- **`MummyPlan.walk()` as a convenience default method** that delegates to `ArtifactTreeWalker.walk(getRootArtifact(), visitor)`. Every current consumer obtains its root artifact from the plan, so this eliminates the boilerplate and makes the typical call site `plan.walk(visitor)` or `context.getPlan().walk(visitor)`. Analogous to `Document.createTreeWalker()` providing a factory on the root container.
- **Walk all comprised artifacts, communicate subsumption as a boolean.** Earlier designs (the "comprised − subsumed" filter in `S3.plan()`, the `getChildArtifacts()` loop in `PlanDescriber`) made subsumption filtering a traversal-level concern — the walker decided which artifacts to skip. This inverts the responsibility: the walker visits everything; the visitor decides. This eliminates `findContentArtifact()` for visitors that only need the content artifact's path or metadata (they receive it directly as a subsumed visit), and enables use cases where subsumed artifacts need different handling in different contexts — all in a single walk. See the conversation history for the full analysis.
- **Subsumption as a `boolean`, not an enum or rich context.** Subsumption is the only *relational* property the visitor cannot derive from the artifact alone — it depends on the parent composite's `getSubsumedArtifacts()`. Intrinsic properties like navigability, asset designation, and mummifier type are available directly on the `Artifact` interface. A boolean is sufficient for the single relational property; an enum would mix relational and intrinsic concerns. If a second relational property emerges (e.g., depth), a lightweight context record would be appropriate at that point.
- **Recursive depth-first pre-order.** Each artifact is visited before its comprised children. Recursive rather than iterative (stack-based): simpler implementation, and the recursive approach is shared by `DefaultMummyPlan.initialize()` and `S3.plan()`.
- **`Visitor.visit()` does not throw checked exceptions.** The artifact model is abstract — artifacts hold paths and metadata, but the visitor interface is not inherently I/O-bound. `PlanDescriber`, manifest analysis, and other non-I/O consumers would be burdened by a checked `IOException`. Consumers that perform I/O during visitation (e.g., deployment) use the established `throwingConsumer()`/`UncheckedIOException` pattern, with the wrapping and unwrapping occurring in the same calling scope. Most Java visitors (`ElementVisitor`, `ClassVisitor`, `TypeVisitor`) do not declare checked exceptions; `FileVisitor` is the exception, and it visits files, not an abstract model.
- **Scope: walker only, no caller conversion.** This plan adds the walker and tests it. Converting existing callers (`PlanDescriber`, `S3.plan()`) to use it is a follow-up concern. Converting `PlanDescriber` would also fix a latent gap: it uses `getChildArtifacts()` (only on `CollectionArtifact`), so it silently skips `AspectualArtifact` composites. The walker handles all composite types uniformly.

---

## Step 1: `ArtifactTreeWalker` with Nested `Visitor` Interface

### Location

New file: `mummy/src/main/java/dev/guise/mummy/ArtifactTreeWalker.java`

### Design

```java
/// Walks artifact trees in depth-first pre-order, visiting all
/// [comprised artifacts][CompositeArtifact#comprisedArtifacts()] including
/// [subsumed][CompositeArtifact#getSubsumedArtifacts()] artifacts.
///
/// The [Visitor] receives the subsumption status of each artifact, allowing it to decide how to handle
/// subsumed artifacts — skip for counting, include for indexing, etc.
///
/// @implNote The current implementation uses recursion for simplicity. The artifact tree is abstract and
/// not necessarily backed by a filesystem, so no particular depth bound is assumed. For foreseeable usage
/// (website artifact trees), recursion depth is well within Java's default stack limits. If a future data
/// source produced trees with unusual depth characteristics, the implementation could switch to an
/// iterative approach using an explicit stack without changing the API.
/// @see MummyPlan#walk(ArtifactTreeWalker.Visitor)
public final class ArtifactTreeWalker {

	/// A callback for receiving artifacts during tree traversal.
	///
	/// @apiNote The `subsumed` flag communicates a **relational** property — whether the artifact has been
	/// absorbed into its parent composite — that the visitor cannot derive from the artifact alone.
	/// Intrinsic artifact properties such as navigability or asset designation are available directly
	/// on the [Artifact] interface.
	@FunctionalInterface
	public interface Visitor {

		/// Visits an artifact during tree traversal.
		///
		/// @param artifact The artifact being visited.
		/// @param subsumed Whether this artifact has been subsumed by its parent composite artifact and should
		///        not appear as a separate IRI path reference.
		void visit(Artifact artifact, boolean subsumed);

	}

	private ArtifactTreeWalker() { // prevent instantiation
	}

	// walk() methods added in Step 2

}
```

The `Visitor` interface is `@FunctionalInterface` for lambda use. Callers invoke:

```java
ArtifactTreeWalker.walk(rootArtifact, (artifact, subsumed) -> {
	if(!subsumed) {
		// count, deploy, etc.
	}
});
```

---

## Step 2: `ArtifactTreeWalker.walk()` Static Methods

### Location

Static methods inside `ArtifactTreeWalker` (`mummy/src/main/java/dev/guise/mummy/ArtifactTreeWalker.java`), replacing the `// walk() methods added in Step 2` placeholder.

### Public entry point

```java
/// Walks the artifact tree rooted at the given artifact, visiting each artifact in depth-first pre-order.
///
/// The walk descends into all [comprised artifacts][CompositeArtifact#comprisedArtifacts()] of each
/// [CompositeArtifact], including [subsumed][CompositeArtifact#getSubsumedArtifacts()] artifacts. The
/// visitor receives the subsumption status of each artifact, allowing it to decide how to handle subsumed
/// artifacts — skip for counting, include for indexing, etc.
///
/// @apiNote This provides a single traversal mechanism for artifact tree walks, replacing the repeated
/// recursion boilerplate in deployment planning, plan description, and other artifact-processing code.
/// The subsumption flag is a relational property determined by the parent composite; the visitor cannot
/// derive it from the artifact alone.
/// @param root The artifact to start the walk from. If not a [CompositeArtifact], the visitor is called
///        once for this artifact and the walk completes.
/// @param visitor The visitor to invoke for each artifact.
/// @see MummyPlan#walk(ArtifactTreeWalker.Visitor)
public static void walk(final Artifact root, final Visitor visitor) {
	walk(root, false, visitor);
}
```

### Recursive overload

```java
/// Walks the artifact tree rooted at the given artifact with the given subsumption status.
///
/// @param artifact The current artifact being visited.
/// @param subsumed Whether this artifact is subsumed by its parent composite.
/// @param visitor The visitor receiving each artifact.
private static void walk(final Artifact artifact, final boolean subsumed, final Visitor visitor) {
	visitor.visit(artifact, subsumed);
	if(artifact instanceof CompositeArtifact compositeArtifact) {
		final Set<Artifact> subsumedArtifacts = toSet(compositeArtifact.getSubsumedArtifacts());
		compositeArtifact.comprisedArtifacts().forEach(comprisedArtifact ->
				walk(comprisedArtifact, subsumedArtifacts.contains(comprisedArtifact), visitor));
	}
}
```

The two methods are clean overloads — `walk(Artifact, Visitor)` vs. `walk(Artifact, boolean, Visitor)` — differing in arity. The private overload is the recursive implementation; the public overload is the entry point that starts the root as non-subsumed.

### Key implementation details

- **`toSet()`** on the subsumed collection: coerces the `Collection` returned by `getSubsumedArtifacts()` to a `Set` for fast `O(1)` `contains()` lookup, returning the collection directly if it is already a `Set` (which it is for all current implementations) and copying only when necessary.
- **`Stream.forEach()`** on `comprisedArtifacts()`: iterates the comprised stream directly. Since the visitor does not throw checked exceptions, there is no need for the `(Iterable<Artifact>)stream::iterator` workaround that would be required to propagate checked exceptions from a for-each loop.
- **Root artifact visited with `subsumed=false`**: the root is never subsumed regardless of whether a caller passes a subsumed artifact as the root. The walker has no parent context for the root.

---

## Step 3: `MummyPlan.walk()` Convenience Default Method

### Location

Default method on `MummyPlan` (`mummy/src/main/java/dev/guise/mummy/MummyPlan.java`).

### Design

```java
/// Walks the plan's artifact tree, visiting each artifact in depth-first pre-order.
///
/// @implSpec The default implementation delegates to [ArtifactTreeWalker#walk(Artifact, ArtifactTreeWalker.Visitor)]
/// with this plan's [root artifact][#getRootArtifact()].
/// @param visitor The visitor to invoke for each artifact.
/// @see ArtifactTreeWalker#walk(Artifact, ArtifactTreeWalker.Visitor)
public default void walk(final ArtifactTreeWalker.Visitor visitor) {
	ArtifactTreeWalker.walk(getRootArtifact(), visitor);
}
```

This makes the typical call site:

```java
context.getPlan().walk((artifact, subsumed) -> {
	// process artifact
});
```

rather than:

```java
ArtifactTreeWalker.walk(context.getPlan().getRootArtifact(), (artifact, subsumed) -> {
	// process artifact
});
```

---

## Step 4: `ArtifactTreeWalkerTest` — Walker Tests

### Location

New file: `mummy/src/test/java/dev/guise/mummy/ArtifactTreeWalkerTest.java` — the walker is its own class, so its tests belong in a dedicated test class.

### Test infrastructure

Tests build artifact trees using the existing `DummyArtifact` and `DirectoryArtifact` classes with mocked `Mummifier` instances — the same pattern used by `PlanDescriberTest` and `DefaultMummyPlanTest`. Visit order and subsumption status are captured via a `List` of local records built in the visitor lambda.

Paths are constructed using `OperatingSystem.getTempDirectory()` as a base, following the existing convention. No actual I/O occurs — paths are used only for artifact identity.

```java
record VisitRecord(Artifact artifact, boolean subsumed) {}
```

### Test cases

#### `testWalkSingleArtifact()`

Tests that [ArtifactTreeWalker#walk(Artifact, Visitor)] visits a single non-composite artifact.

- Walk a `DummyArtifact` → exactly one visit, `subsumed=false`.

#### `testWalkEmptyDirectory()`

Tests that [ArtifactTreeWalker#walk(Artifact, Visitor)] handles a directory with no children and no content artifact.

- Walk a `DirectoryArtifact(mummifier, source, target, null, Set.of())` → exactly one visit (the directory), `subsumed=false`.

#### `testWalkDirectoryWithChildren()`

Tests that [ArtifactTreeWalker#walk(Artifact, Visitor)] traverses children in depth-first order, all as non-subsumed.

- Build a `DirectoryArtifact` with two `DummyArtifact` children (no content artifact)
- Walk → three visits: directory (`subsumed=false`), child A (`subsumed=false`), child B (`subsumed=false`)
- Verify the directory is visited first (pre-order)

#### `testWalkDirectoryWithContentArtifact()`

Tests that [ArtifactTreeWalker#walk(Artifact, Visitor)] visits the content artifact as subsumed.

- Build a `DirectoryArtifact` with a `DummyArtifact` as content artifact, no other children
- Walk → two visits: directory (`subsumed=false`), content artifact (`subsumed=true`)

#### `testWalkDirectoryWithContentAndChildren()`

Tests that [ArtifactTreeWalker#walk(Artifact, Visitor)] correctly distinguishes content (subsumed) from children (non-subsumed).

- Build a `DirectoryArtifact` with a content artifact and two child artifacts
- Walk → four visits: directory (`subsumed=false`), children (`subsumed=false` each), content (`subsumed=true`)
- Verify all children are `subsumed=false` and the content artifact is `subsumed=true`

#### `testWalkNestedDirectories()`

Tests that [ArtifactTreeWalker#walk(Artifact, Visitor)] handles nested directory trees with content artifacts at multiple levels.

- Root directory with child subdirectory; subdirectory has its own content artifact and a leaf file child
- Walk → visits root, subdirectory, subdirectory's leaf child (non-subsumed), subdirectory's content (subsumed)
- Verify depth-first order and correct subsumption at each level

#### `testWalkAspectualArtifact()`

Tests that [ArtifactTreeWalker#walk(Artifact, Visitor)] visits aspects of an [AspectualArtifact] as non-subsumed.

- Create a mock `CompositeArtifact` with `getSubsumedArtifacts()` returning empty and `comprisedArtifacts()` returning two aspects
- Walk → three visits: composite (`subsumed=false`), aspect A (`subsumed=false`), aspect B (`subsumed=false`)

This specifically validates the invariant that `AspectualArtifact` subsumption is always empty — aspects are non-subsumed comprised artifacts.

### Not tested

- **Post-order semantics**: not needed because the walker is pre-order by design. `DefaultMummyPlan.initialize()` needs post-order for its referent-source-path overwrites, but that walk has different structural requirements (parent context, post-order, principal mapping) and stays separate.

---

## Files Modified

| File | Change |
|---|---|
| `mummy/src/main/java/dev/guise/mummy/ArtifactTreeWalker.java` | **New** — walker class with nested `Visitor` interface and `walk()` static methods |
| `mummy/src/main/java/dev/guise/mummy/MummyPlan.java` | Add default `walk()` convenience method |
| `mummy/src/main/java/dev/guise/mummy/deploy/aws/S3.java` | Replace `Set.copyOf()` with `toSet()` on line 388 |
| `mummy/src/test/java/dev/guise/mummy/ArtifactTreeWalkerTest.java` | **New** — walker tests |

---

## Alternatives Considered

### Top-level `ArtifactVisitor` with `Artifact.walk()` static methods

**Considered:** A separate top-level `ArtifactVisitor` interface with the walk implementation as static methods on `Artifact`, following the `FileVisitor` / `Files.walkFileTree()` separation.

**Rejected:** The visitor callback has no independent utility outside tree traversal — there's no `accept()` on `Artifact`, no double-dispatch, no use case in the codebase where an `ArtifactVisitor` would be created and used without a walk. The `subsumed` parameter is specifically a tree-walk concept. Where top-level visitors *do* exist in libraries (`FileVisitor`, `ClassVisitor`, `ElementVisitor`), they have independent utility (multi-method interfaces, double-dispatch, rich event sinks). Our single-method `@FunctionalInterface` callback does not. Additionally, nodes in the JDK and major frameworks never host the traversal implementation — `Files.walkFileTree()` is on the `Files` utility class, not on `Path`. Putting `walk()` on `Artifact` conflates the node with the traversal mechanism.

### `CompositeArtifact.walk()` with nested `CompositeArtifact.Visitor`

**Considered:** Placing both the visitor and walk on `CompositeArtifact`, since the decomposition methods (`comprisedArtifacts()`, `getSubsumedArtifacts()`) are declared there.

**Rejected:** Walking a single non-composite artifact is a valid degenerate case — the visitor is called once and the walk completes. Restricting the entry point to `CompositeArtifact` would force callers to bifurcate their code for composite vs. non-composite roots.

### Walk non-subsumed only

**Considered:** The walker skips subsumed artifacts as a traversal-level concern, matching the "comprised − subsumed" filter in `S3.plan()` and the `getChildArtifacts()` pattern in `PlanDescriber`.

**Rejected:** This forces visitors that need subsumed artifacts to use a separate walk. Walking all comprised and communicating subsumption to the visitor unifies the traversal — one walker serves all use cases. The visitor decides whether to skip, not the walker.

### Subsumption as an enum instead of a boolean

**Considered:** A sealed type or enum (e.g., `SubsumptionStatus.Independent` / `SubsumptionStatus.Subsumed`) instead of a bare `boolean` for the subsumption parameter.

**Rejected:** Subsumption is the only relational property, so a boolean is sufficient. An enum would not mix well with intrinsic artifact properties (navigability, asset designation) which belong on the `Artifact` interface, not on a walker-scoped status type. If a second relational property emerges, a lightweight context record would replace the boolean at that point.

### `Iterable ::iterator` traversal instead of `Stream.forEach()`

**Considered:** Converting `comprisedArtifacts()` to an `Iterable` via `(Iterable<Artifact>)stream::iterator` for use in a for-each loop, matching the existing `S3.plan()` pattern.

**Rejected:** That pattern exists specifically to propagate checked exceptions from the loop body through a `Stream`. Since the `Visitor.visit()` method does not declare checked exceptions, `Stream.forEach()` works directly and is simpler. If checked exceptions were reintroduced, the `Iterable ::iterator` approach would become necessary again.

### `Visitor.visit()` declaring `throws IOException`

**Considered:** Declaring `throws IOException` on `visit()` (and consequently on `walk()`), matching `FileVisitor`'s convention. Lambdas that don't perform I/O would simply not throw.

**Rejected:** The artifact model is abstract — not inherently I/O-bound. Most Java visitors (`ElementVisitor`, `ClassVisitor`, `TypeVisitor`) do not declare checked exceptions; `FileVisitor` is the outlier because it visits files. Consumers that perform I/O during visitation use the `throwingConsumer()`/`UncheckedIOException` pattern, with the wrapping and unwrapping in the same calling scope. Keeping the visitor free of checked exceptions avoids burdening non-I/O consumers.

[GUISE-230]: ./
