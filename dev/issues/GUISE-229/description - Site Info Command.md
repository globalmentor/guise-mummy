# `GUISE-229`: Surface site statistics including redirect inventory.

## Objective

Make site composition data—especially configured redirects—readily queryable, so that a developer can understand what a site contains and verify redirect declarations without manual source inspection or deployment.

## Problem

Guise Mummy currently provides no way to answer basic questions about a site's composition: how many pages, images, and collections it contains, or what redirects are declared. Redirect information is particularly opaque—`mummy/altLocation` properties are scattered across individual source files and only become visible during S3 deployment logic. There is no summary view at any point in the build process.

The immediate practical need is to audit which redirects a site declares, including their source and target paths, to verify correctness when testing against a new deployment mechanism. But the need generalizes: any site of moderate size benefits from a summary of what it contains.

## Desired Behavior

After the functionality is implemented, a developer can obtain at minimum:

- **Artifact counts by category**: total artifacts, pages, collections (directories), images, and other file types.
- **Redirect inventory**: total count of `mummy/altLocation` declarations, distinguishable by whether they are collection redirects (paths ending in `/`) or page redirects.
- **Redirect detail**: the specific source → target path mapping for each declared redirect, available on request (e.g., via a verbose option).

The output is human-readable console text. The information is derived from the site's source model, not from deployment-specific constructs.

## Constraints

- **The artifact tree is only available in memory after the PLAN phase.** `DirectoryMummifier.plan()` during `LifeCyclePhase.PLAN` builds the `MummyPlan` artifact tree. Redirect declarations (`Artifact.PROPERTY_TAG_MUMMY_ALT_LOCATION`) are properties on individual `Artifact` nodes in this tree. The `target/site-description/` directory holds per-artifact sidecar metadata (`.@.turf` files) but no aggregate site statistics. Any approach to reporting site composition therefore requires at least the PLAN phase to have executed.
- **Full mummification should not be necessary.** The PLAN phase discovers and categorizes all artifacts without generating the target site. Producing statistics should not require MUMMIFY or later phases, and ideally should not produce side effects in `target/site/`.
- **Redirects are source-model properties, not deployment constructs.** Each artifact may declare a `mummy/altLocation` property. Whether a redirect targets a collection or a page is determined by the URI path form. The S3 deployment layer (`S3Website`, `S3ArtifactRedirectDeployObject`) adds deployment-specific concerns (routing rules vs. object redirects, AWS threshold limits) that are irrelevant to reporting what the site declares.
- **The artifact tree is walkable.** `MummyPlan.getRootArtifact()` provides the root, and `CollectionArtifact.getChildArtifacts()` provides children. An existing `printArtifactDescription()` method in `GuiseMummy` (annotated `//TODO transfer to CLI`) already demonstrates recursive traversal at TRACE level.
- **The lifecycle is cumulative.** The phases INITIALIZE → VALIDATE → PLAN → MUMMIFY → PREPARE_DEPLOY → DEPLOY execute in sequence, each building on the prior. There is no existing phase or mechanism dedicated to reporting; this is a query against post-PLAN state, not a transformation feeding a subsequent phase.

## Acceptance Criteria

- Artifact counts by category (pages, collections, images, other) are reported.
- The total number of configured redirects is reported, distinguishing collection redirects from page redirects.
- Individual redirect source → target mappings are available (at least on request, e.g., via a verbosity option).
- Producing statistics does not require generating the target site.
- The solution is consistent with the existing Guise Mummy lifecycle and CLI architecture.

## Non-Goals

- Machine-readable output formats (JSON, YAML, etc.).
- Full site map generation (`sitemap.xml` or similar).
- Reporting deployment-specific concerns (S3 routing rule counts, redirect-means strategy, threshold configuration).
- Modifying or extending the `target/site-description/` sidecar format with aggregate data.

## Guidance

- **Hugo precedent.** Hugo prints a build summary after every build (`Pages | 42`, `Aliases | 12`, etc.). This is the closest precedent among static site generators. No major SSG offers a standalone "content analysis" command separate from building.
- **Possible approaches** include: (1) a new CLI subcommand (e.g., `guise info`) that internally runs through PLAN and queries the resulting artifact tree; (2) a summary printed at the end of `guise mummify`, since the data is already available post-PLAN; (3) a `--dry-run` or `--plan-only` flag on the existing `mummify` command. These are not mutually exclusive. The best approach depends on whether the primary use is one-off querying or routine build feedback.
- **Orientation.** The CLI is a single picocli `@Command` class at [cli/src/main/java/dev/guise/cli/GuiseCli.java](cli/src/main/java/dev/guise/cli/GuiseCli.java) with subcommands as methods. `GuiseCli.logProjectInfo()` already prints basic project configuration. `GuiseMummy.printArtifactDescription()` walks the artifact tree recursively. Artifact type classification uses `instanceof` against `CollectionArtifact`, and `Mummifier` type checks (`PageMummifier`, `ImageMummifier`, `OpaqueFileMummifier`). The `altLocation` property is accessed via `artifact.getResourceDescription().findPropertyValue(PROPERTY_TAG_MUMMY_ALT_LOCATION)`.
