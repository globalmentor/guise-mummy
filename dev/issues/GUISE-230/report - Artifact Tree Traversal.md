# Artifact Tree Traversal in Guise Mummy

An opportunistic survey of how the Guise Mummy codebase walks the artifact tree, captured while working on [GUISE-230] (Flange deployment) and [GUISE-229] (plan description). The S3 collection artifact semantics fix (`plans/2026-03-10-s3-collection-artifact-semantics.md`) brought all the traversal approaches into sharp focus and motivates this summary.

## The Artifact Composite Model

The artifact tree is a recursive structure built on `CompositeArtifact`, which provides three accessor methods that partition its constituents:

| Method | Returns | Semantics |
|---|---|---|
| `comprisedArtifacts()` | All constituents | Exhaustive: every artifact that structurally belongs to this composite |
| `getSubsumedArtifacts()` | Absorbed implementation details | Artifacts that should not appear as independent entities in the site's IRI space |
| comprised − subsumed | Independently addressable members | Artifacts that exist as separate model entities |

Two subtypes specialize this:

- **`CollectionArtifact`** (implemented by `DirectoryArtifact`) — adds `getChildArtifacts()`, a convenience accessor equivalent to "comprised − subsumed." For a `DirectoryArtifact`, comprised = children + content artifact; subsumed = the content artifact (if any); children = the non-subsumed members.

- **`AspectualArtifact`** — adds `getAspects()`. The subsumed set is empty, so comprised = children = aspects. The subsumption pattern does not apply.

The critical structural feature is `DirectoryArtifact`'s content artifact — for example, `index.xhtml` within a directory. This artifact is *subsumed*: it provides the directory's content and metadata (via description delegation), but it is not independently addressable. The directory artifact `foo/` is the canonical resource; `foo/index.xhtml` is an implementation detail.

## Why Walk the Tree

Six use cases currently walk or will walk the artifact tree:

1. **Plan initialization** — build indexes (principal-artifact map, parent-artifact map, source-path lookup).
2. **Mummification** — generate output files from source artifacts.
3. **Plan description** — count artifacts by type and collect redirects for a summary report.
4. **Deployment planning** — create platform-specific deploy objects (S3 keys, redirect rules).
5. **Metadata index construction** — map filesystem paths to artifact metadata for external synchronizers.
6. **Redirect extraction** — collect `altLocation` properties into a redirect map for external deployers.

Uses 5 and 6 are planned for the Flange integration; the others already exist.

## Current Approaches

### The three recursion strategies

Every walk makes the same structural decision: which artifacts to recurse into. In practice there are only two distinct recursion sets, though they appear under three different code patterns:

**Full comprised** (`comprisedArtifacts()`): Visit every artifact including subsumed ones. Used only by `DefaultMummyPlan.initialize()`, which must index *all* artifacts — subsumed artifacts need principal-artifact mappings, and their referent source paths must be registered (then overwritten by the principal's paths).

**Non-subsumed** (`comprisedArtifacts()` minus `getSubsumedArtifacts()`, or equivalently `getChildArtifacts()` for collections): Visit only independently addressable artifacts. Used by everything else. The two code patterns for this are:

- `getChildArtifacts()` — used by `PlanDescriber`, `GuiseMummy.printArtifactDescription()`, and `DirectoryMummifier.mummify()`. This is the `CollectionArtifact`-level convenience accessor. It works only for collections (non-collection composites fall through with no recursion).

- `comprisedArtifacts()` with `getSubsumedArtifacts()` filter — used by `S3.plan()`. This is the `CompositeArtifact`-level equivalent that handles all composite types uniformly, including `AspectualArtifact` where the subsumed set is empty and the filter is a no-op.

The second form is more general. The first is simpler but implicitly assumes the only relevant composite type is `CollectionArtifact`.

### Content-finding

Orthogonal to recursion strategy, some walks need to locate the content artifact of a collection:

- **`S3.plan()`** and the planned **`buildArtifactIndex()`** use `DirectoryArtifact.findContentArtifact()` to find the content artifact for byte-level operations (uploading content, indexing by content path). This is where the bytes live.

- **`DirectoryMummifier.mummify()`** also uses `findContentArtifact()`, but for orchestration — the content artifact must be mummified before children, not mixed in with them.

- **`PlanDescriber`**, **`collectRedirects()`**, and **`DefaultMummyPlan.initialize()`** do *not* need content-finding. They process each artifact's metadata directly; for collections, description delegation makes the content artifact's metadata available through the collection artifact.

### Per-artifact action

This is where the walks diverge most:

| Walk | Action at each artifact |
|---|---|
| `DefaultMummyPlan.initialize()` | Populate three maps with different keys, order-dependent |
| `DirectoryMummifier.mummify()` | Polymorphic dispatch to each artifact's mummifier |
| `PlanDescriber.describeTo()` | Type-based counting + redirect collection |
| `S3.plan()` | URI derivation + deploy object creation (template method) |
| *(planned)* `collectRedirects()` | Redirect map population |
| *(planned)* `buildArtifactIndex()` | Path→artifact map population |

## Assessment

### What's the same

Four of the six walks (and both planned ones) share the same core structure:

1. Given an artifact, do something with it.
2. If it's a `CompositeArtifact`, recurse into its non-subsumed comprised artifacts.

The "do something" varies, but the tree traversal mechanics are identical. The content-finding concern is an optional addition to this core — some walks need it, some don't, but it doesn't change the recursion structure.

### What's genuinely different

**`DefaultMummyPlan.initialize()`** is the only walk that visits all comprised artifacts (including subsumed ones). It also has order-dependent map population logic — the principal artifact must overwrite subsumed artifacts' referent source paths. This is a structurally different traversal with unique post-visit semantics.

**`DirectoryMummifier.mummify()`** is not really a tree walk in the visitor sense. The recursion is emergent from the polymorphic `mummifier.mummify()` dispatch — each artifact type controls its own traversal. A generic walker would fight this strategy pattern rather than help it.

### Toward a unified non-subsumed walk

The four current "non-subsumed" walks and both planned walks could share a common traversal mechanism. The variation points are:

- **Content-finding** — whether the visitor receives a callback for the content artifact of a collection, separate from the main artifact callback.
- **Per-artifact action** — the visitor's business logic.

A minimal abstraction might look like:

```
walkNonSubsumed(root, visitor)
  for each artifact (depth-first, skipping subsumed):
    visitor.visit(artifact)
    if artifact is a DirectoryArtifact with content:
      visitor.visitContent(directoryArtifact, contentArtifact)  // default no-op
```

This captures the recursion pattern, the subsumed-artifact filter, and the optional content-finding concern in one place. Walks that don't need content-finding ignore `visitContent`. Walks that do (deployment, index building) override it.

The `DefaultMummyPlan` walk stays separate — it has different recursion semantics (full comprised) and unique ordering requirements. `DirectoryMummifier.mummify()` stays separate — it's a polymorphic dispatch mechanism, not a visitor.

### Model observations

- The current `getChildArtifacts()` convenience method on `CollectionArtifact` works only for collections, silently skipping `AspectualArtifact` composites. If more composite types are added in the future, code using `getChildArtifacts()` for recursion will silently miss them. The "comprised − subsumed" pattern at the `CompositeArtifact` level is more robust. A standard walker would naturally use this form.

- Content-finding is currently tied to `DirectoryArtifact` — only `DirectoryArtifact.findContentArtifact()` provides it. If `CollectionArtifact` gained a general `findContent()` abstraction (returning the artifact whose bytes represent the collection's content), the walker could operate at the `CollectionArtifact` level without coupling to the implementation type. The `S3.plan()` code already has a comment noting this coupling.

- The fact that only two recursion strategies exist (full comprised vs. non-subsumed) despite six walks suggests the model is well-factored. The "do I need subsumed artifacts?" question has a clear answer for each use case: yes for indexing the full artifact identity graph, no for everything else.

### Recommendation

A standard non-subsumed walker with optional content-finding would eliminate the repeated traversal boilerplate in four to six call sites and prevent future deployers and plan features from reinventing the pattern. It would also centralize the "comprised − subsumed" filtering logic, which is currently duplicated and expressed differently (`getChildArtifacts()` in some places, explicit `Set.copyOf` + `contains` filter in others).

This is not urgent — the current code is correct and the pattern is well understood. But as new deployment targets (Flange, and potentially others) and plan features ([GUISE-229]) are added, the value of a shared walker increases. The natural time to introduce it would be when a third or fourth deployer arrives, or when refactoring the artifact model to lift `findContentArtifact()` to `CollectionArtifact`.

[GUISE-229]: ../../GUISE-229/
[GUISE-230]: ./
