# TODO: Refactor `S3`/`S3Website` deployment planning to use `ArtifactTreeWalker`.

The `S3.plan()` method performs its own manual recursive traversal of the artifact tree, including explicit `CompositeArtifact` detection, `getSubsumedArtifacts()` set construction, and recursive calls. This should be refactored to use the newly added `ArtifactTreeWalker`, which encapsulates depth-first pre-order traversal with subsumption status reporting.

## Current Structure

`S3` has a two-level `plan()` method chain:

- `plan(MummyContext, Artifact)` — entry point, computes `rootTargetPathUri`, delegates to the three-arg overload.
- `plan(MummyContext, URI, Artifact)` — per-artifact logic *and* recursion. Resolves the content artifact (via `DirectoryArtifact.findContentArtifact()` for collections, or the artifact itself for non-collections), calls `planResource()`, then manually iterates `comprisedArtifacts()` minus `getSubsumedArtifacts()` and recurses.

`S3Website` overrides:

- `plan(MummyContext, Artifact)` — calls `super.plan(...)` then does redirect-means post-processing.
- `planResource(...)` — calls `super.planResource(...)` then extracts `altLocation` redirects.

Neither subclass overrides the three-arg `plan()`.

## Refactoring Approach

Replace the two-arg `plan()` body with an `ArtifactTreeWalker.walk()` call. The per-artifact logic from the three-arg method moves into the visitor lambda. The visitor skips subsumed artifacts (via the `subsumed` flag), then performs content resolution and calls `planResource()`:

```java
protected void plan(final MummyContext context, final Artifact rootArtifact) throws IOException {
    final URI rootTargetPathUri = rootArtifact.getTargetPath().toUri();
    ArtifactTreeWalker.walk(rootArtifact, throwingConsumer((artifact, subsumed) -> {
        if (subsumed) { return; }
        // ... content-resolution logic (findContentArtifact, etc.) ...
        // ... planResource() call ...
    }));
}
```

The three-arg `plan(MummyContext, URI, Artifact)` becomes dead code. Since it is `protected`, removing it is a breaking change to the protected API surface. It should be deprecated with `forRemoval = true` initially (or removed outright if no external subclasses are expected).

## Content Resolution

The content-resolution logic moves into the visitor verbatim. This is the logic that pairs a `DirectoryArtifact` with its content artifact via `findContentArtifact()` — the directory provides the metadata and resource reference (`foo/`), while the content artifact provides the file path and S3 key (`foo/index.xhtml`). This pairing is essential for avoiding the double-redirect bug (see `plans/2026-03-10-s3-collection-artifact-semantics.md`): if the subsumed content artifact were processed independently, `S3Website.planResource()` would create a redirect to the content path rather than the canonical collection path.

## Checked Exceptions

`ArtifactTreeWalker.Visitor.visit()` does not throw checked exceptions. The visitor lambda wraps its body with `throwingConsumer()` / `UncheckedIOException`, which is already used in `S3` (e.g., the `foundContentArtifact.ifPresent(throwingConsumer(...))` call). The `Visitor` interface would need a two-arg throwing consumer variant, or the wrapping would be applied at the `walk()` call site. Note: `ArtifactTreeWalker.Visitor` is a `@FunctionalInterface` with `(Artifact, boolean)`, so the existing `throwingConsumer()` (which targets single-arg `Consumer`) would need a `BiConsumer` variant or an adapter.

## `PlanDescriber` — Same Refactoring Opportunity

`PlanDescriber.describeTo()` performs its own iterative (stack-based) traversal using `CollectionArtifact.getChildArtifacts()`. This has a latent gap: it only descends into `CollectionArtifact` composites, silently skipping `AspectualArtifact` composites. Converting to `ArtifactTreeWalker` would close this gap, since the walker uses `CompositeArtifact.comprisedArtifacts()` which handles all composite types uniformly.

## `S3Website` Compatibility

`S3Website`'s overrides (`plan(MummyContext, Artifact)` and `planResource(...)`) are structurally compatible. `planResource()` dispatches polymorphically from within the visitor. `S3Website.plan()` calls `super.plan(...)` then does post-processing — unchanged.
