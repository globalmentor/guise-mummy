# [GUISE-233] Per-Element Mesh Skip

## Objective

Guise Mesh supports a mechanism by which the textual content of a DOM element and its subtree can be designated as literal — exempt from `^{…}` interpolation — so that authored content containing that syntax is passed through unchanged.

## Acceptance Criteria

- Character data within a designated element is not scanned for `^{…}` interpolation expressions; the content is passed through verbatim.
- The designation is inherited by descendant content, covering the entire subtree without per-element annotation.
- The feature is implemented within `GuiseMesh` as a first-class Mesh capability, not as a workaround in the Guise Mummy caller layer.
- All existing Mesh behavior for elements that are not so designated is unchanged.

## Background

`DefaultMeshInterpolator` scans every text node for the `^{…}` interpolation delimiter. When a page contains literal content — such as embedded code examples or command snippets that include substrings like `^{tree}` (part of a Git command) — that was not authored as a Mesh template, Mesh attempts expression evaluation and fails or corrupts the content. There is currently no way to designate a subtree as raw content exempt from processing.

Guise Mummy applies Mesh after the full page document has been assembled (including page-template expansion), so any literal content embedded earlier in the pipeline is subject to interpolation. A per-element skip mechanism is the correct Mesh-layer solution.

## Constraints

- The implementation must reside in `GuiseMesh` (or an appropriate Mesh-layer class), not in the Guise Mummy caller.

## Orientation

- `GuiseMesh.meshElement()` and `GuiseMesh.meshChildNodes()` in `mesh/src/main/java/dev/guise/mesh/GuiseMesh.java` are the recursive processing entry points relevant to any skip implementation.
- `DefaultMeshInterpolator` in the same package defines the `^{…}` delimiter and drives text-node scanning.

## Out of Scope

The Guise Mummy feature for inserting external file content into pages at generation time — which would ultimately make manual bypass unnecessary in common cases — is a separate, anticipated ticket.
