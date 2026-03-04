# Design: `--describe` Flag for Site Plan Description

## Overview

This document specifies the CLI surface and output format for the `--describe` functionality that surfaces site composition data from the `MummyPlan` artifact tree. The feature makes the plan — an in-memory data structure built during the PLAN lifecycle phase — visible as human-readable console output.

## Architecture

The describe behavior is **phase-bound**: it executes inside `GuiseMummy.mummify()` at the PLAN phase, after the plan is constructed and before subsequent phases proceed. It is controlled by passing `MummyExecution.DESCRIBE_PLAN` in a `Set<MummyExecution>` parameter to `mummify()`. This is analogous to Maven plugin executions bound to a lifecycle phase.

The CLI's role is limited to translating `--describe` / `--describe-plan` flags into the execution set. The `PlanDescriber` class lives in the `mummy` module and is invoked by `GuiseMummy`, not by `GuiseCli`.

## CLI Surface

### New Subcommand: `guise plan`

A new `plan` subcommand is added to `GuiseCli`. It executes the lifecycle through the PLAN phase (INITIALIZE → VALIDATE → PLAN) and prints the plan description. This is the primary entry point for querying site composition without generating the target site.

The `plan` subcommand accepts the standard project/directory options already present on other subcommands (`<project>`, `--site-source-dir`, `--site-target-dir`, `--site-description-target-dir`).

#### `--describe` flag

```
guise plan --describe
```

Prints a human-readable summary of the site plan to stdout. **Defaults to `true`**, making `guise plan` effectively the "info" command — running it without any flags prints the plan description. The flag exists so it can be explicitly disabled (`--describe=false`) if needed, and for consistency with `--describe-plan` on other commands.

#### `--verbose` flag

```
guise plan --describe --verbose
```

Expands the description to include per-item listings (e.g., individual redirect mappings) rather than counts only.

`--verbose` / `-v` is provided by `BaseCliApplication` with `ScopeType.INHERIT` and is available on all subcommands. It does not need per-subcommand declaration.

### Existing Subcommands: `--describe-plan`

Commands that execute phases beyond PLAN — `mummify`, `prepare-deploy`, `deploy` — gain a `--describe-plan` flag. When present, the plan description is printed after the PLAN phase completes and before subsequent phases execute.

```
guise mummify --describe-plan
guise deploy --describe-plan --verbose
```

The flag name differs from `--describe` because the "plan" is not the primary object of these commands; the flag must name what is being described.

### Flag Summary

| Command          | Flag               | Effect                                     |
|------------------|--------------------|--------------------------------------------|  
| `guise plan`     | `--describe`       | Print plan description (default behavior)  |
| `guise mummify`  | `--describe-plan`  | Print plan description after PLAN phase    |
| `guise prepare-deploy` | `--describe-plan` | Print plan description after PLAN phase |
| `guise deploy`   | `--describe-plan`  | Print plan description after PLAN phase    |
| any of above     | `--verbose`        | Expand description with per-item listings  |

`--verbose` is inherited from `BaseCliApplication` and is independent of `--describe`/`--describe-plan`: it controls detail level wherever a description is printed.

## Output Format

### Default Output (without `--verbose`)

The default format is a compact summary with artifact counts and redirect totals:

```
Site Plan
  Source:       C:\project\src\site
  Artifacts:    147
    Pages:        42
    Collections:  15
    Images:       68
    Other:        22
    Posts:        7
  Redirects:    12
    Collection:   3
    Page:         9
    Warnings:     1 [!]

  [!] redirect target is outside the site boundary
```

Design notes:

- **"Site Plan"** heading establishes context. The source directory is shown as the native filesystem path (via `context.getSiteSourceDirectory()`) — it is a filesystem location, not a web reference, so it renders in platform-native form.
- **Artifact counts** are classified by mummifier type: `PageMummifier` -> Pages, `CollectionArtifact` (via `DirectoryMummifier`) -> Collections, `ImageMummifier` -> Images, everything else -> Other.
- **Redirect counts** are derived from artifacts with a `PROPERTY_TAG_MUMMY_ALT_LOCATION` property. The distinction between collection and page redirects is determined by `instanceof CollectionArtifact`.
- **Warning count** appears when any redirects have diagnostic warnings (e.g., alt locations that resolve outside the site boundary). A **legend** explaining each warning marker is always appended when warnings exist, regardless of verbose mode.
- Counts are left-aligned at a consistent column via fixed-width label formatting (`%-14s`). No right-alignment or dynamic width computation.

### Verbose Output (with `--verbose`)

The `--verbose` flag appends per-item listings below the summary. Initially, this means listing each redirect:

```
Site Plan
  Source:       C:\project\src\site
  Artifacts:    147
    Pages:        42
    Collections:  15
    Images:       68
    Other:        22
    Posts:        7
  Redirects:    12
    Collection:   3
    Page:         9
    Warnings:     1 [!]

  Redirect Details:
    /old-section/ -> /new-section/
    /legacy/ -> /current/
    /archive/ -> /blog/
    /old-page.html -> /new-page
    /2019/post-one -> /blog/post-one
    /../../escaped.html -> /page.html [!]
    ...

  [!] redirect target is outside the site boundary
```

Design notes:

- Redirect details are grouped under a "Redirect Details" subsection.
- Each line shows the alt location reference (the old path that triggers the redirect, resolved and relativized via the URI domain) and the artifact's resource reference (where the redirect sends the request). Both are site-relative `URIPath` values displayed with a leading `/`.
- The ASCII `->` arrow is used instead of Unicode `→` for Windows terminal compatibility.
- If a redirect has a diagnostic warning (e.g., alt location resolves outside the site boundary), the warning marker (e.g., `[!]`) is appended after the target path.
- Redirect lines are **not column-aligned** — each line is `/%s -> /%s` with no padding. This avoids the problem of one long path forcing excessive whitespace on all other entries. The `->` delimiter is sufficient for scanning.
- Collection redirects (ending `/`) appear before page redirects, each group sorted alphabetically by alt location reference.
- The legend explaining warning markers appears after the redirect details. It is emitted regardless of verbose mode whenever warnings exist.
- Future `--verbose` expansions (e.g., listing veiled artifacts, posts) can add additional subsections without changing the structure.

## Data Extraction

### Artifact Classification

Classification is by mummifier type, checked via `instanceof` on `artifact.getMummifier()`:

| Mummifier type    | Category    |
|-------------------|-------------|
| `PageMummifier`   | Pages       |
| `ImageMummifier`  | Images      |
| (is `CollectionArtifact`) | Collections |
| Everything else   | Other       |

The collection check is on the artifact itself (`instanceof CollectionArtifact`), not the mummifier, since `DirectoryMummifier` does not implement a marker interface. Collections are counted first (before mummifier checks) since the root artifact and all directories are collection artifacts regardless of their mummifier type.

**Posts** are an orthogonal dimension: `Artifact.isPost()` is checked independently of the mummifier-type classification. A post can be a page or a collection. The `Posts:` count is displayed as a sub-line under the artifact counts.

### Redirect Extraction

For each artifact in the tree, check `artifact.getResourceDescription().findPropertyValue(Artifact.PROPERTY_TAG_MUMMY_ALT_LOCATION)`. If present, the artifact declares a redirect.

The `altLocation` value is a **URI path reference** (not a filesystem path). It is processed through the same typed chain used by `S3Website.planResource()`:

1. Parse as `URIPath` via `URIPath.of(String)`
2. Resolve against the artifact's target path URI (with `toCollectionURI()` applied for `CollectionArtifact` instances, since during PLAN phase target directories may not yet exist on disk)
3. Relativize against the site target root URI via `URIPath.relativize()`
4. Check site boundary via `URIPath.isSubPath()` — flag with `[!]` warning if the resolved path escapes the site

The resulting `RedirectEntry` record holds:

- **`altLocationReference`** (`URIPath`): The alternate (old) site-relative resource reference that triggers the redirect — an incoming request to this path will be redirected.
- **`resourceReference`** (`URIPath`): The artifact's current site-relative resource reference where the redirect sends the request.
- **`collection`** (`boolean`): Determined by `artifact instanceof CollectionArtifact`.
- **`optionalWarning`** (`Optional<PlanWarning>`): A diagnostic warning, if any (e.g., `REDIRECT_OUTSIDE_SITE`).

### Tree Traversal

The artifact tree is walked starting from `MummyPlan.getRootArtifact()`. For each `CollectionArtifact`, recurse into `getChildArtifacts()`. This is the same traversal pattern used by the existing `GuiseMummy.printArtifactDescription()`.

## Output Destination

The plan description is printed to stdout via `System.out`, not via the logger. This is user-requested information, not logging or error output. No ANSI terminal styling is applied — output is plain text.

## Future Directions

- **`--describe=tree`**: A tree-view output showing the full artifact hierarchy with type annotations. This would be a separate output mode, not a verbosity level.
- **`--describe=report`**: A sectioned report with full details per category.
- **Machine-readable output**: `--format=json` or similar, as a separate concern from the `--describe` flag.
- **Asset count**: Requires model change — `isAsset()` status is not persisted on `Artifact` after planning. Detection is transient in `DirectoryMummifier.plan()` via the asset name pattern. See [remediation plan](../plans/2026-02-15-plan-describer-remediation.md#deferred-reporting-categories).
- **Veiled artifact count**: Requires model fix — `DirectoryArtifact.isNavigable()` always returns `true`, so veiled directories cannot be detected. Only file artifacts have a reliable `isNavigable()` signal.
- **Collections with authored content**: The `findContentArtifact()` API works, but phantom content files are auto-generated during planning for most directories, making the count misleading. Useful only after asset detection is resolved.
- **Subsumed artifacts**: Derivable but a count alone is not actionable — requires report-level detail showing which artifacts are subsumed and by what.

[GUISE-229]: ../../GUISE-229/
