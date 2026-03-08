# [GUISE-229] Summary: Site statistics and redirect inventory via `guise plan`.

## What Was Built

A new `guise plan` CLI subcommand that runs the Guise Mummy lifecycle through the PLAN phase only and prints a human-readable site composition summary: artifact counts by category (pages, collections, images, other, posts) and a redirect inventory distinguishing collection targets from page targets. A `--verbose` flag expands the output with per-redirect source → target mappings, showing decoded Unicode paths (not percent-encoded) in their native site-root-relative form. Existing subcommands (`mummify`, `prepare-deploy`, `deploy`) gained a `--describe-plan` flag for the same output after their PLAN phase.

The core implementation is `PlanDescriber`, which walks the artifact tree in a single pass, classifies artifacts by mummifier type, and collects redirect entries from `mummy/altLocation` properties. It follows the same URI processing chain as `S3Website.planResource()` — parse as `URIPath`, resolve against the artifact target URI, relativize against the site root — so redirect paths are computed identically to deployment.

## Architecture Decisions

The describe behavior is phase-bound: it executes inside `GuiseMummy.mummify()` at the PLAN phase, controlled by a `Set<MummyExecution>` parameter. This keeps the engine in control of phase ordering rather than exposing post-phase state to the CLI. A post-hoc accessor approach was rejected because describe is a phase-bound action, not a query.

During implementation, two reusable static methods were extracted to `Artifact.relativizeResourceReference()` — a pure URI-level overload and an artifact-aware overload that handles the `CollectionArtifact` trailing-slash concern. This replaced duplicated logic in `AbstractMummyPlan`, `S3.plan()`, and the describer itself.

## Course Corrections

The initial implementation used ad-hoc string manipulation (`replace('\\', '/')`) for path display, violating the codebase's established path/URI coordinate system separation. A remediation plan (10 steps) rewrote the path handling to use the proper URI-domain processing chain.

A critical finding during real-site testing: zero redirects were detected because stale `target/site-description/` files from before [GUISE-228] still declared the old `guise.io` namespace. Property lookups under the new `guise.dev` namespace found nothing. This prompted the creation of a migration guide for GUISE-228 (committed separately).

The redirect display went through a design refinement: paths were initially shown with a leading `/` prefix, but this misrepresents the Guise Mummy model where all resource references are genuinely relative to the site root (no leading `/`) to preserve deployment-location independence. The display was corrected to show paths in their natural relative form. The non-ASCII test cases were also strengthened to exercise `toDecodedString()` on both source and destination paths.

## Deferred Work

- **Asset and veiled artifact counts**: Asset status is transient (detected during planning but not persisted on `Artifact`). `DirectoryArtifact.isNavigable()` always returns `true`. These model limitations prevent accurate reporting; documented in the remediation plan.
- **Segment-by-segment `URIPath` comparator**: Flat string comparison of URI paths produces unintuitive ordering where `a-page.html` sorts before `alpha/`. A TODO document describes the desired comparator for `globalmentor-core`.

[GUISE-228]: ../GUISE-228/
