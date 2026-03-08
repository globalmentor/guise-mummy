# Guise Mummy Architecture

This document describes the internal architecture of Guise Mummy — its domain model, lifecycle, and the design invariants that keep the system consistent. It targets developers working on or integrating with Guise Mummy, not end users of the CLI.

## Overview

Guise Mummy is a static site generator that transforms a tree of source files (XHTML, Markdown, HTML, images, and arbitrary files) into a deployable static site. It is organized around three core concepts:

1. **Artifacts** — the domain model representing resources being processed.
2. **Mummifiers** — the processing strategies that transform source content into output.
3. **The Lifecycle** — the phased execution model from initialization through deployment.

## The Three Trees

Guise Mummy operates on three parallel directory trees, all rooted under the project directory:

| Tree | Default Path | Purpose |
|---|---|---|
| **Source tree** | `src/site/` | User-authored content: pages, images, data files |
| **Target tree** | `target/site/` | Generated site output: static files ready for deployment |
| **Description tree** | `target/site-description/` | Mummification metadata (sidecar descriptions) |

Each tree is represented by an absolute filesystem `Path`:

- `MummyContext.getSiteSourceDirectory()` — the source tree root
- `MummyContext.getSiteTargetDirectory()` — the target tree root
- `MummyContext.getSiteDescriptionTargetDirectory()` — the description tree root

The source and target trees have the same directory structure (though filenames may differ due to extension changes, bare-name transformations, and post-pattern date extraction). The description tree mirrors the target tree's structure.

### Rebasing Between Trees

To translate a path from one tree to another (for example, to find where a target artifact's description file lives), the codebase uses `Paths.changeBase(path, oldBase, newBase)`. This relativizes the path against the old base and resolves the result against the new base:

```java
changeBase(targetDirectory, context.getSiteTargetDirectory(), context.getSiteDescriptionTargetDirectory())
```

This is a pure filesystem operation — no URI conversion is involved.

## Artifacts

An `Artifact` is the central domain abstraction: it represents a resource being processed. Every artifact carries two absolute filesystem `Path` values:

- **`getSourcePath()`** — where the source content is (or would be) in the source tree.
- **`getTargetPath()`** — where the generated output will be placed in the target tree.

For a file artifact (like a page or image), both are file paths. For a `CollectionArtifact` (the only implementation being `DirectoryArtifact`), both are directory paths.

Artifact equality is determined by target path.

### Source Directory vs. Source Path

`Artifact.getSourceDirectory()` returns the _containing directory_ of the artifact source. For a file artifact, this is the parent directory; for a directory artifact, it is the directory itself. This distinction matters because an artifact's source may be a file _within_ a directory, while the directory itself is a separate collection artifact.

`Artifact.isSourcePathFile()` indicates whether the source refers to a file rather than a directory. This exists because during planning, the source path may not yet exist on disk, and the type system needs to capture the distinction without checking the filesystem.

### Artifact Categories

The `Artifact` interface defines a precise vocabulary documented in its Javadoc:

- **Composite artifact** — An artifact potentially composed of other artifacts. Provides `comprisedArtifacts()`, which yields _all_ constituent artifacts (for tree traversal).

- **Collection artifact** — A composite artifact with a _collection IRI path reference_ (one ending in `/`). The canonical implementation is `DirectoryArtifact`. Provides `getChildArtifacts()`, which yields the navigable members (a subset of comprised artifacts).

- **Content artifact** — A subsumed artifact of a directory that represents the directory's content. Historically `index.html`. A content artifact is comprised by its directory but is _not_ a child artifact. In the logical resource model, `foo/index.xhtml` is an implementation detail for storing the content of the `foo/` collection.

- **Subsumed artifact** — An artifact that has been absorbed into another and should not appear as a separate IRI path reference. The content artifact is the archetypal subsumed artifact. A subsumed artifact's _principal artifact_ is the one it has been subsumed into.

- **Principal artifact** — The canonical artifact for IRI path references. An artifact is normally its own principal artifact; a subsumed artifact's principal artifact is the artifact it was subsumed into. For example, `foo/index.html`'s principal artifact is the `foo/` directory artifact.

- **Veiled artifact** — Hidden from default navigation but still accessible if referred to directly. Designated by source filename convention (default: underscore prefix such as `_notes.md`).

- **Asset** — A veiled artifact that additionally will not produce generated pages. Designated by source filename convention (default: dollar-sign prefix such as `$template.html`).

- **Corporeal artifact** — An artifact that potentially contains content. The `CorporealSourceArtifact` interface provides `openSource(MummyContext)` for access to the content stream and `getSourceSize(MummyContext)` for the byte length.

The `AspectualArtifact` interface (extending `CompositeArtifact`) is related but not listed among the official categories. It represents an artifact with variant forms (aspects), such as an image with a preview or thumbnail. Each aspect shares the same source but produces a distinct target (e.g., `photo.jpg` → `photo-preview.jpg`).

### Comprised vs. Child Artifacts

These terms are distinct. In `DirectoryArtifact`:

- **`comprisedArtifacts()`** returns child artifacts _plus_ the content artifact (if any). Used for exhaustive tree traversal.
- **`getChildArtifacts()`** returns only the navigable collection members. Used for navigation.
- **`getSubsumedArtifacts()`** returns only the content artifact (if any).

### Referent Source Paths

Because a directory and its content file represent the same logical resource, `Artifact.getReferentSourcePaths()` returns all source filesystem paths that refer to the same artifact. For a `DirectoryArtifact` with a content file, this includes both `foo/` and `foo/index.xhtml`. This allows source-tree link resolution to find the correct artifact regardless of which path form was used in a reference.

### Artifact Properties

Artifacts carry metadata through an `UrfResourceDescription` (the URF resource description framework). Properties fall into two categories:

**General properties** (by handle): `title`, `name`, `label`, `description`, `author`, `artist`, `createdAt`, `publishedOn`, `copyright`, `icon`.

**Mummy-specific properties** (by tag URI in the `https://guise.dev/name/mummy/` namespace): `mummy/altLocation` (redirect alternate name), `mummy/order` (navigation order), `mummy/aspect` (variant designation), `mummy/template` (template path), `mummy/descriptionDirty` (incremental mummification flag), `mummy/sourceContentModifiedAt` (source timestamp for incremental detection).

## Mummifiers

A `Mummifier` is a processing strategy responsible for two operations:

1. **Planning** — creating an `Artifact` from source and target paths during the PLAN phase.
2. **Mummifying** — transforming source content into target output during the MUMMIFY phase.

### Mummifier Hierarchy

The mummifier type hierarchy has two lineages — an interface hierarchy and a class hierarchy — that converge in `AbstractSourcePathMummifier`:

**Interfaces:**
- **`Mummifier`** — core interface defining `mummify()`, `getSupportedFilenameExtensions()`, and `planArtifactTargetFilename()`.
- **`SourcePathMummifier`** — extends `Mummifier`; adds `plan()` and `getArtifactMediaType()`. This is the type used for mummifier registration.

**Classes:**
- **`AbstractMummifier`** — implements `Mummifier`; provides target description file handling (`getArtifactTargetDescriptionFile()`, `loadArtifactTargetDescription()`, `saveTargetDescription()`).
- **`AbstractSourcePathMummifier`** — extends `AbstractMummifier`, implements `SourcePathMummifier`; adds source description sidecar discovery.
- **`AbstractFileMummifier`** — extends `AbstractSourcePathMummifier`; orchestrates incremental mummification, metadata loading, fingerprint calculation, and description serialization.

### Concrete Mummifiers

| Mummifier | Extends | Source Types | Output |
|---|---|---|---|
| `XhtmlPageMummifier` | `AbstractPageMummifier` | `.xhtml` | HTML with template application, link relativization, navigation regeneration |
| `HtmlPageMummifier` | `XhtmlPageMummifier` | `.html`, `.htm` | HTML5 processing (reuses XHTML pipeline) |
| `MarkdownPageMummifier` | `AbstractPageMummifier` | `.md`, `.markdown` | Markdown → HTML conversion with template application |
| `DefaultImageMummifier` | `BaseImageMummifier` | `.gif`, `.jpg`, `.jpeg`, `.png` | Image optimization with optional aspect generation |
| `DirectoryMummifier` | `AbstractSourcePathMummifier` | directories | Recursive child discovery, content artifact identification, phantom page generation |
| `OpaqueFileMummifier` | `AbstractFileMummifier` | any (fallback) | File copy with no transformation |

### Mummifier Registration

Mummifiers are registered by filename extension in `BaseMummyContext`. Lookup uses normalized (lowercase) extensions, and first match wins for compound extensions (e.g., `.tar.gz`). The context provides two fallbacks:

- `getDefaultSourceFileMummifier()` → `OpaqueFileMummifier` (copies file unchanged)
- `getDefaultSourceDirectoryMummifier()` → `DirectoryMummifier`

Custom mummifiers can be registered via `GuiseMummy.addFileMummifierType()` before mummification begins.

## Description and Metadata System

Every artifact can have a persistent metadata description stored as a **sidecar file** — a companion file alongside the artifact with additional properties.

### Sidecar File Format

Sidecar files use TURF Properties format (`.tupr` extension). The sidecar extension segment is `-`, making the full compound extension `.-.tupr` (defined by `Mummifier.DESCRIPTION_FILE_SIDECAR_EXTENSION`).

**Source sidecars** are optional author-provided metadata files alongside source content. For `page.xhtml`, the sidecar would be `page.xhtml.-.tupr` in the same directory.

**Target sidecars** are generated during mummification in the description tree. For a target file `target/site/blog/post.html`, the description is `target/site-description/blog/post.html.-.tupr`. The mapping uses `Paths.changeBase()` to rebase from the target tree to the description tree, then appends the sidecar extension.

### Metadata Content

Target descriptions contain:

- All properties from the source (author-provided metadata, frontmatter, HTML `<meta>` tags, EXIF data).
- `content-type` — the media type of the generated content (e.g., `text/html;charset=UTF-8`).
- `content-fingerprint` — SHA-256 hash of the generated target file content.
- `mummy/sourceContentModifiedAt` — source file modification timestamp (for incremental mummification).

### Metadata Loading Pipeline

For each file artifact, `AbstractFileMummifier.loadArtifactDescription()` orchestrates metadata assembly:

1. **Incremental check**: If incremental mode, load the existing target description and check whether the source has changed since the last mummification.
2. **If unchanged**: Reuse the cached target description.
3. **If changed or full mode**: Call the format-specific `loadSourceMetadata()` (which extracts metadata from frontmatter, `<meta>` tags, EXIF, etc.), merge with source sidecar properties (if present), infer types (e.g., `publishedOn` → `LocalDate`), detect post dates from filename patterns, determine media type, and mark as dirty.

### Incremental Mummification

The dirty-flag logic in `AbstractFileMummifier.mummify()` determines whether to regenerate:

- Target file missing or source content changed → regenerate content.
- Description marked `mummy/descriptionDirty` → update description.
- Missing `content-fingerprint` → update description.

On regeneration, the fingerprint is recalculated and the target description is serialized back to the description tree.

## Configuration

Configuration uses a layered fallback system via the Confound framework.

### Configuration Hierarchy

From highest to lowest priority:

1. **CLI overrides** — command-line options.
2. **Site configuration** — `.guise-mummy.turf` (or `.json`, `.xml`) in the site source directory root. Keys are automatically prefixed with `mummy.`.
3. **Project configuration** — `guise-project.turf` (or `.json`, `.xml`) in the project directory.
4. **Default configuration** — built-in defaults from `GuiseMummy.getDefaultConfiguration()`.

### Key Configuration Properties

| Key | Default | Purpose |
|---|---|---|
| `domain` | — | Project base FQDN (absolute, ending with `.`) |
| `site.domain` | (fallback to `domain`) | Canonical site domain |
| `site.altDomains` | — | Alternative domains (collection) |
| `siteSourceDirectory` | `src/site` | Source tree path |
| `siteTargetDirectory` | `target/site` | Target tree path |
| `siteDescriptionTargetDirectory` | `target/site-description` | Description tree path |
| `mummy.page.namesBare` | `false` | Clean URLs: strip `.html` extensions |
| `mummy.collectionContentBaseNames` | `["index"]` | Content filenames for directories |
| `mummy.assetNamePattern` | `\$(.*)` | Asset filename pattern |
| `mummy.veilNamePattern` | `_(.*)` | Veiled filename pattern |
| `mummy.navigationBaseName` | `.navigation` | Navigation file base name |
| `mummy.templateBaseName` | `.template` | Template file base name |
| `mummy.textOutputLineSeparator` | `\n` | Line separator for reproducible builds |

### Deployment Configuration

Deployment is configured via `deploy.dns` (DNS provider) and `deploy.targets` (collection of deployment target sections). Each target section has a type (`S3`, `S3Website`, `CloudFront`) and target-specific properties.

## Lifecycle

Guise Mummy executes six phases in strict sequence. Each phase builds on results from prior phases.

### INITIALIZE

Loads the project and site configurations (merging with fallbacks), creates the `MummyContext`, and registers mummifiers.

### VALIDATE

Checks preconditions: source directory exists, source and target directories don't overlap, domain names are absolute FQDNs, no obsolete configuration keys.

### PLAN

Delegates to `DirectoryMummifier.plan()`, which recursively walks the source tree:

1. The site source directory becomes the root `DirectoryArtifact`.
2. For each child in a directory:
   - A mummifier is selected by filename extension.
   - The target filename is computed, applying post-date extraction (`@YYYY-MM-DD-slug.ext` → `YYYY/MM/DD/slug.html`), asset/veil renaming, bare-name stripping, and extension changes.
   - The child mummifier's `plan()` creates the appropriate `Artifact`.
3. Content artifacts (e.g., `index.xhtml`) are identified per `mummy.collectionContentBaseNames` and subsumed into their parent directory artifact.
4. Directories without a content file (that are not asset trees) receive a phantom `SimpleGeneratedXhtmlArtifact`.
5. The complete tree is wrapped in a `DefaultMummyPlan`, which indexes artifacts by source path (including referent paths for content artifact aliasing) and builds parent/principal mappings.

### MUMMIFY

Recursively invokes each artifact's mummifier. For file artifacts, `AbstractFileMummifier.mummify()` handles incremental checks, content generation, fingerprint calculation, and description serialization. For directories, `DirectoryMummifier.mummify()` creates the target directory and recurses into content and child artifacts.

### PREPARE_DEPLOY

Loads DNS and deployment target configurations from the project configuration. Creates `Route53`, `S3`, `S3Website`, and/or `CloudFront` instances. Calls `prepare()` on each, which provisions or validates infrastructure (buckets, distributions, certificates, hosted zones).

### DEPLOY

Executes deployment: DNS records are created/updated, then each deploy target uploads content and returns a deployment URL. For S3-based targets, this involves walking the artifact plan, mapping each artifact to an S3 key, and uploading content with appropriate metadata (content type, fingerprint). CloudFront distributions are invalidated after upload.

## Context (`MummyContext`)

The `MummyContext` is the runtime service provider available throughout all lifecycle phases. It provides:

- **Configuration** access (merged project + site configuration).
- **Directory paths** for the three trees.
- **Mummifier registry** and lookup by file extension.
- **The plan** (available from the PLAN phase onward).
- **Page source file lookup** — `findPageSourceFile()` for locating page files by base name in a directory or ancestor directories.
- **XML infrastructure** — `newPageDocumentBuilder()` for namespace-aware `DocumentBuilder` instances.
- **Deployment state** — `getDeployDns()` and `getDeployTargets()` (available from PREPARE_DEPLOY onward).

## Two Coordinate Systems

Guise Mummy works with **two fundamentally different coordinate systems**:

1. **Filesystem paths** (`java.nio.file.Path`) — platform-dependent representations of locations in the local filesystem. On Windows these use backslash separators; on Unix, forward slashes. These paths represent where source content _lives_ and where generated output is _written_.

2. **URI paths** (`URIPath`, `URI`) — platform-independent, standard representations following [RFC 3986](https://tools.ietf.org/html/rfc3986). These paths always use forward slashes, support percent-encoding, and carry collection semantics via a trailing slash. These paths represent how resources are _identified and referenced_ on the web.

The codebase never conflates these two systems. Each has its own type, operations, and resolution semantics. Translation between them occurs at well-defined boundaries — and only through `java.nio.file.Path.toUri()` and `java.nio.file.Paths.get(URI)`.

### Translating Between Coordinate Systems

**Filesystem → URI**: The sole mechanism is `Path.toUri()`, which produces a `file:///` URI. This handles platform-specific separators, drive letters, UNC paths, and percent-encoding correctly.

```java
final URI artifactTargetPathUri = artifact.getTargetPath().toUri();
```

After obtaining the `file:///` URI, the code applies `toCollectionURI()` if the artifact is a collection, then operates purely in the URI domain.

**URI → Filesystem**: The sole mechanism is `java.nio.file.Paths.get(URI)`. The pattern is: convert filesystem path to URI, resolve the URI reference against it (in the URI domain), then convert the result back to a filesystem path. The URI domain acts as the mediator for all reference resolution.

```java
final Path referenceSourcePath = Paths.get(contextSourcePath.toUri().resolve(sourceRelativeReference.toURI()));
```

**Why not string manipulation**: Filesystem `Path.toString()` is platform-dependent — backslashes on Windows, forward slashes on Unix. Converting between the two coordinate systems by replacing separators (`replace('\\', '/')`) would bypass percent-encoding, mishandle edge cases (spaces, special characters, UNC paths), and ignore the collection/non-collection distinction. The codebase avoids this entirely.

## Collection Paths and the Trailing-Slash Problem

### The Concept

In the web (RFC 3986), a path ending in `/` denotes a _collection_ — a container of other resources. A path without a trailing slash denotes a _non-collection_ — an individual resource. This distinction is semantically significant because URI reference resolution behaves differently:

- Resolving `image.jpg` against `articles/` yields `articles/image.jpg`
- Resolving `image.jpg` against `articles/page.html` yields `articles/image.jpg` (same parent)
- Resolving `../other` against `articles/` yields `other` (backs up from the collection)

Guise Mummy models this distinction explicitly. A `CollectionArtifact` corresponds to a collection path (trailing slash); a file artifact corresponds to a non-collection path (no trailing slash).

### The Filesystem Impedance Mismatch

Filesystems do not encode the collection/non-collection distinction in their path representation. A directory path `C:\project\target\site\articles` has no trailing separator. When Java converts this to a URI via `Path.toUri()`, the result may or may not have a trailing slash depending on whether the directory exists on disk. Since artifact paths may refer to directories that don't yet exist (during planning), the filesystem cannot be relied on to add the trailing slash.

The codebase solves this with **explicit type knowledge**: wherever a URI is derived from an artifact's filesystem path, the code checks `artifact instanceof CollectionArtifact` and, if true, forces the URI into collection form:

```java
artifact instanceof CollectionArtifact ? toCollectionURI(artifactTargetPathUri) : artifactTargetPathUri
```

`URIs.toCollectionURI(URI)` appends `/` to the URI path if not already present. The corresponding `URIPath` method is `toCollectionURIPath()`.

This pattern appears consistently wherever the codebase crosses from filesystem to URI representation:
- `Artifact.relativizeResourceReference()` — uses `forceCollection` parameter (set from `artifact instanceof CollectionArtifact`)
- `AbstractMummyPlan.relativizeResourceReference()` — validates tree containment (both paths must be absolute and within the same source or target tree) and applies collection forcing
- `PlanDescriber.collectRedirect()` — applies `toCollectionURI()` to the artifact target URI when the artifact is a `CollectionArtifact`, before resolving the `altLocation` reference against it

Note that `S3.planResource()` skips collection artifacts entirely — directories have no corresponding S3 object, so no key is generated for them. The collection forcing matters in reference _calculation_, not in key assignment.

## Resource References

A **resource reference** is a relative URI path (`URIPath`) that identifies a resource without any assumption about the absolute location where the site will ultimately be served. Resource references are always in the URI domain, never in the filesystem domain. They never carry a leading `/`.

This relativity is a deliberate architectural invariant, not an incidental implementation detail. A Guise Mummy site is a self-contained, portable unit: it might later be deployed at the root of a server (`/`), or under a subpath (`/blog/`, `/company/docs/`). The resource references within the site are the same regardless of deployment location. An absolute path (one beginning with `/`) would bake in an assumption about the deployment mount point that the model intentionally avoids.

### Two Relativization Contexts

Resource references are always relative, but the _base_ they are relative to depends on the context of use.

#### Artifact-relative references (inter-page links)

`MummyPlan.referenceInSource()` and `MummyPlan.referenceInTarget()` compute the reference from one artifact to another. These references are **relative to the from-artifact** — the result may include `../` segments to backtrack from child to sibling or parent. This is the standard web relative-reference model: a link in `articles/post.html` to `images/photo.jpg` is `../images/photo.jpg`, which works regardless of where on a server the `articles/` directory resides.

`MummyPlan.findArtifactBySourceRelativeReference()` performs the inverse operation: given an artifact and a relative URI reference, it resolves the reference against the artifact's source path (in the URI domain) and looks up the resulting artifact.

Both of these methods resolve against the **principal artifact** of the referring artifact. For example, a link _from_ `foo/index.html` is calculated against `foo/` (the directory artifact), since the `index.html` content artifact has been subsumed into the directory.

#### Site-root-relative references (deployment and display)

`Artifact.relativizeResourceReference(URI baseUri, Artifact artifact)` computes the reference for an artifact **relative to the site root**. When the base URI is the root artifact's target path URI, the result is a site-root-relative path such as `blog/post.html` or `articles/` — no leading `/`, no `../` (for in-site artifacts).

This is used in deployment (`S3.plan()`, `S3Website.planResource()`) and plan description (`PlanDescriber`). These consumers need a consistent key for each resource within the site, independent of which artifact is "looking at" which.

### The Implementation

`Artifact.relativizeResourceReference(URI, URI, boolean)` is the shared static implementation:

1. Takes base URI, reference URI, and a `forceCollection` flag
2. Applies `toCollectionURI()` if `forceCollection` is true
3. Relativizes using `URIPath.relativize(URI, URI)` — which properly backtracks using `..` segments, unlike Java's `URI.relativize()`

The result is always a `URIPath`, not a `String`.

### `altLocation`

The `mummy/altLocation` property is a **URI path reference** relative to the artifact declaring it (artifact-relative context). It specifies an alternate site location that should redirect to the artifact's actual location.

Processing bridges the two relativization contexts: the artifact-relative input is resolved to an absolute filesystem URI, then re-relativized against the site root to produce a site-root-relative reference. The type chain in `S3Website.planResource()` and `PlanDescriber.collectRedirect()`:

```
Object (URF property value)
  → String
    → URIPath (artifact-relative URI reference)
      → URI (absolute file:/// URI, via resolve against artifact target path URI)
        → URIPath (site-root-relative, via relativize against site root URI)
          → String (S3 key or display path)
```

Each step uses the correct typed operation:
- `URIPath::of` — parses the string as a URI path, not a filesystem path
- `URIs.resolve(artifact.getTargetPath().toUri(), altLocationReference)` — resolves the relative URI reference against the artifact's URI form, producing an absolute `file:///` URI
- `URIPath.relativize(rootTargetPathUri, altLocationUri)` — relativizes against the site root, producing a site-root-relative `URIPath`

At no point is `altLocation` treated as a filesystem path string.

Both sides of a redirect mapping — the alternate location (source) and the artifact's resource reference (destination) — are stored as site-root-relative `URIPath` values.

### Deployment Targets and Absolute Paths

Deployment targets consume site-root-relative resource references and translate them into whatever form the platform requires. The site model itself never produces absolute paths — that is a deployment-target concern resolved at the boundary.

In the S3 deployment:
- **S3 keys** are the `URIPath.toString()` of the site-root-relative reference — no leading `/`. The S3 bucket key namespace happens to be bucket-root-relative, so the site-root-relative form maps directly.
- **S3 object redirect** (`x-amz-website-redirect-location`): the target key is prefixed with `/` and encoded in `preparePutObject()`, because this header value is the literal HTTP `Location` header content, which requires an absolute path per RFC 7231 §7.1.2.
- **S3 routing rules** (`keyPrefixEquals`, `replaceKeyWith`, `replaceKeyPrefixWith`): use the site-root-relative key directly, without a leading `/`, because S3 routing rules match against bucket keys.
- **`guise serve`** (local development server): maps the servlet context at `/` — a deployment-time decision, not part of the model.

The absolute-path form (with leading `/`) appears _only_ at protocol boundaries that require it. It is never stored in the model.

## How the Plan is Built

During the PLAN lifecycle phase, `GuiseMummy` delegates to `DirectoryMummifier.plan()`, which recursively walks the source filesystem tree:

1. The site source directory becomes the root `DirectoryArtifact`'s source path.
2. The site target directory becomes the root `DirectoryArtifact`'s target path.
3. For each child in the source directory:
   - A mummifier is selected based on file extension.
   - The source path is the actual filesystem path.
   - The target filename is computed by `planChildArtifactTargetPath()`, which handles post-date extraction, asset/veil renaming, bare-name stripping, and extension changes.
   - The target path is the parent target directory resolved with the computed target filename.

This is entirely a filesystem operation: `Path.resolve(childTargetFilename)`. URI conversion happens later, at the point of use (link generation, deployment).

## Display and Logging

Throughout the codebase, paths are logged and displayed in their natural representation:

- **Filesystem paths** are displayed as `Path` objects, which render in platform-native form.
- **URI references** are displayed as `URIPath` objects using their `toString()`, which renders the encoded URI path form.

## Key Utilities

### `URIPath` (`com.globalmentor.net.URIPath`)

An immutable value type representing a URI path. Key operations:

| Method | Purpose |
|---|---|
| `URIPath.of(String)` | Parse an encoded URI path string |
| `URIPath.relativize(URI, URI)` | Compute a relative path between two URIs with proper backtracking (fixes JDK-6226081) |
| `URIPath.findRelativePath(URI, URI)` | Like `relativize()`, but returns `Optional` |
| `resolve(URIPath)` | Resolve a relative path against this one |
| `isCollection()` | Whether the path ends in `/` |
| `isSubPath()` | Whether the path is relative and does not backtrack past the origin |
| `toCollectionURIPath()` | Force a trailing `/` |
| `checkRelative()` / `checkAbsolute()` | Precondition validation |
| `normalize()` | Remove `.` and `..` segments |
| `toString()` | The raw encoded path as it would appear in a URI |

### `URIs` (`com.globalmentor.net.URIs`)

Static utilities for URI operations:

| Method | Purpose |
|---|---|
| `toCollectionURI(URI)` | Force a trailing `/` on the URI path |
| `resolve(URI, URI)` | RFC 3986 resolution (fixes Java's RFC 2396 empty-path behavior and UNC path issues) |
| `resolve(URI, URIPath)` | Convenience overload |
| `findRelativePath(URI, URI)` | Compute relative path with backtracking; returns `Optional` |

### `Paths` (`com.globalmentor.io.Paths`)

Static utilities for filesystem `Path` operations:

| Method | Purpose |
|---|---|
| `changeBase(Path, Path, Path)` | Rebase a path from one tree root to another |
| `isSubPath(Path, Path)` | Check containment after normalization |
| `checkArgumentAbsolute(Path)` | Precondition: path is absolute |
| `checkArgumentSubPath(Path, Path)` | Precondition: path is under base |

### `Path.toUri()` / `Paths.get(URI)` (JDK)

The bridge between the two coordinate systems. These are the _only_ mechanisms the codebase uses for filesystem↔URI translation.
