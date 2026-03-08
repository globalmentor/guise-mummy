# Guise Mummy Path Representation

This document describes how Guise Mummy represents, translates, and operates on paths — both filesystem paths and site URI paths. It establishes the terminology (the ubiquitous language) used throughout the codebase and explains the conceptual model that keeps path handling consistent.

## Two Coordinate Systems

Guise Mummy works with **two fundamentally different coordinate systems**:

1. **Filesystem paths** (`java.nio.file.Path`) — platform-dependent representations of locations in the local filesystem. On Windows these use backslash separators; on Unix, forward slashes. These paths represent where source content _lives_ and where generated output is _written_.

2. **URI paths** (`URIPath`, `URI`) — platform-independent, standard representations following [RFC 3986](https://tools.ietf.org/html/rfc3986). These paths always use forward slashes, support percent-encoding, and carry collection semantics via a trailing slash. These paths represent how resources are _identified and referenced_ on the web.

The codebase never conflates these two systems. Each has its own type, operations, and resolution semantics. Translation between them occurs at well-defined boundaries — and only through `java.nio.file.Path.toUri()` and `java.nio.file.Paths.get(URI)`.

## Trees

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

## Artifacts and Their Paths

An `Artifact` is the central abstraction: it represents a resource being processed. Every artifact carries two absolute filesystem `Path` values:

- **`getSourcePath()`** — where the source content is (or would be) in the source tree
- **`getTargetPath()`** — where the generated output will be placed in the target tree

For a file artifact (like a page or image), both are file paths. For a `CollectionArtifact` (the only implementation being `DirectoryArtifact`), both are directory paths.

Artifact equality is determined by target path.

### Source Directory vs. Source Path

`Artifact.getSourceDirectory()` returns the _containing directory_ of the artifact source. For a file artifact, this is the parent directory; for a directory artifact, it is the directory itself. This distinction matters because an artifact's source may be a file _within_ a directory, while the directory itself is a separate collection artifact.

`Artifact.isSourcePathFile()` indicates whether the source refers to a file rather than a directory. This exists because during planning, the source path may not yet exist on disk, and the type system needs to capture the distinction without checking the filesystem.

## Artifact Categories

The `Artifact` interface defines a precise vocabulary documented in its Javadoc:

- **Composite artifact** — An artifact potentially composed of other artifacts. Provides `comprisedArtifacts()`, which yields _all_ constituent artifacts (for tree traversal).

- **Collection artifact** — A composite artifact with a _collection IRI path reference_ (one ending in `/`). The canonical implementation is `DirectoryArtifact`. Provides `getChildArtifacts()`, which yields the navigable members (a subset of comprised artifacts).

- **Content artifact** — A subsumed artifact of a directory that represents the directory's content. Historically `index.html`. A content artifact is comprised by its directory but is _not_ a child artifact. In the logical resource model, `foo/index.xhtml` is an implementation detail for storing the content of the `foo/` collection.

- **Subsumed artifact** — An artifact that has been absorbed into another and should not appear as a separate IRI path reference. The content artifact is the archetypal subsumed artifact. A subsumed artifact's _principal artifact_ is the one it has been subsumed into.

- **Principal artifact** — The canonical artifact for IRI path references. An artifact is normally its own principal artifact; a subsumed artifact's principal artifact is the artifact it was subsumed into. For example, `foo/index.html`'s principal artifact is the `foo/` directory artifact.

- **Veiled artifact** — Hidden from default navigation but still accessible if referred to directly. Designated by source filename convention (default: underscore prefix such as `_notes.md`).

- **Asset** — A veiled artifact that additionally will not produce generated pages. Designated by source filename convention (default: dollar-sign prefix such as `$template.html`).

### Comprised vs. Child Artifacts

These terms are distinct. In `DirectoryArtifact`:

- **`comprisedArtifacts()`** returns child artifacts _plus_ the content artifact (if any). Used for exhaustive tree traversal.
- **`getChildArtifacts()`** returns only the navigable collection members. Used for navigation.
- **`getSubsumedArtifacts()`** returns only the content artifact (if any).

## Collection Paths and the Trailing-Slash Problem

### The concept

In the web (RFC 3986), a path ending in `/` denotes a _collection_ — a container of other resources. A path without a trailing slash denotes a _non-collection_ — an individual resource. This distinction is semantically significant because URI reference resolution behaves differently:

- Resolving `image.jpg` against `articles/` yields `articles/image.jpg`
- Resolving `image.jpg` against `articles/page.html` yields `articles/image.jpg` (same parent)
- Resolving `../other` against `articles/` yields `other` (backs up from the collection)

Guise Mummy models this distinction explicitly. A `CollectionArtifact` corresponds to a collection path (trailing slash); a file artifact corresponds to a non-collection path (no trailing slash).

### The filesystem impedance mismatch

Filesystems do not encode the collection/non-collection distinction in their path representation. A directory path `C:\project\target\site\articles` has no trailing separator. When Java converts this to a URI via `Path.toUri()`, the result may or may not have a trailing slash depending on whether the directory exists on disk. Since artifact paths may refer to directories that don't yet exist (during planning), the filesystem cannot be relied on to add the trailing slash.

The codebase solves this with **explicit type knowledge**: wherever a URI is derived from an artifact's filesystem path, the code checks `artifact instanceof CollectionArtifact` and, if true, forces the URI into collection form:

```java
artifact instanceof CollectionArtifact ? toCollectionURI(artifactTargetPathUri) : artifactTargetPathUri
```

`URIs.toCollectionURI(URI)` appends `/` to the URI path if not already present. Its Javadoc notes it is "most useful for working with file systems that are imprecise about distinguishing between collection and non-collection nodes."

The corresponding `URIPath` method is `toCollectionURIPath()`.

This pattern appears consistently wherever the codebase crosses from filesystem to URI representation:
- `AbstractMummyPlan.relativizeResourceReference()` — uses `forceCollection` parameter (set from `toArtifact instanceof CollectionArtifact`)
- `S3.plan()` — applies `toCollectionURI()` when the artifact is a `CollectionArtifact`

### Referent source paths

Because a directory and its content file represent the same logical resource, `Artifact.getReferentSourcePaths()` returns all source filesystem paths that refer to the same artifact. For a `DirectoryArtifact` with a content file, this includes both `foo/` and `foo/index.xhtml`. This allows source-tree link resolution to find the correct artifact regardless of which path form was used in a reference.

## Translating Between Coordinate Systems

### Filesystem → URI

The sole mechanism is `Path.toUri()`, which produces a `file:///` URI. This handles platform-specific separators, drive letters, UNC paths, and percent-encoding correctly.

```java
final URI artifactTargetPathUri = artifact.getTargetPath().toUri();
```

After obtaining the `file:///` URI, the code applies `toCollectionURI()` if the artifact is a collection, then operates purely in the URI domain.

### URI → Filesystem

The sole mechanism is `java.nio.file.Paths.get(URI)`. This appears in `MummyPlan.findArtifactBySourceRelativeReference()`:

```java
final Path referenceSourcePath = Paths.get(contextSourcePath.toUri().resolve(sourceRelativeReference.toURI()));
```

The pattern is: convert filesystem path to URI, resolve the URI reference against it (in the URI domain), then convert the result back to a filesystem path. The URI domain acts as the mediator for all reference resolution.

### Why not string manipulation

Filesystem `Path.toString()` is platform-dependent — backslashes on Windows, forward slashes on Unix. Converting between the two coordinate systems by replacing separators (`replace('\\', '/')`) would bypass percent-encoding, mishandle edge cases (spaces, special characters, UNC paths), and ignore the collection/non-collection distinction. The codebase avoids this entirely.

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

### The implementation

`AbstractMummyPlan.relativizeResourceReference(Path, Path, boolean)` is the shared implementation for both contexts:

1. Takes two absolute filesystem `Path` values (base and reference)
2. Validates both are subpaths of the same tree (source or target)
3. Converts both to URIs via `Path.toUri()`
4. Applies `toCollectionURI()` if the target is a collection
5. Relativizes using `URIPath.relativize(URI, URI)` — which properly backtracks using `..` segments, unlike Java's `URI.relativize()`

The result is always a `URIPath`, not a `String`.

### altLocation

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

Both sides of a redirect mapping — the alternate location (source) and the artifact's resource reference (destination) — are stored as site-root-relative `URIPath` values. The model does not support absolute paths or full URLs as redirect destinations; `URIPath.of()` rejects anything with a scheme, authority, query, or fragment.

### Deployment targets and absolute paths

Deployment targets consume site-root-relative resource references and translate them into whatever form the platform requires. The site model itself never produces absolute paths — that is a deployment-target concern resolved at the boundary.

In the S3 deployment:
- **S3 keys** are the `URIPath.toString()` of the site-root-relative reference — no leading `/`. The S3 bucket key namespace happens to be bucket-root-relative, so the site-root-relative form maps directly.
- **S3 object redirect** (`x-amz-website-redirect-location`): the target key is prefixed with `/` and encoded in `preparePutObject()`, because this header value is the literal HTTP `Location` header content, which requires an absolute path per RFC 7231 §7.1.2.
- **S3 routing rules** (`keyPrefixEquals`, `replaceKeyWith`, `replaceKeyPrefixWith`): use the site-root-relative key directly, without a leading `/`, because S3 routing rules match against bucket keys.
- **`guise serve`** (local development server): maps the servlet context at `/` — a deployment-time decision, not part of the model.

The absolute-path form (with leading `/`) appears _only_ at protocol boundaries that require it. It is never stored in the model.

## How the Plan is Built

During the PLAN lifecycle phase, `GuiseMummy` delegates to `DirectoryMummifier.plan()`, which recursively walks the source filesystem tree:

1. The site source directory becomes the root `DirectoryArtifact`'s source path
2. The site target directory becomes the root `DirectoryArtifact`'s target path
3. For each child in the source directory:
   - A mummifier is selected based on file type
   - The source path is the actual filesystem path
   - The target filename is computed by `planChildArtifactTargetPath()`, which handles post-date extraction, asset/veil renaming, bare-name stripping, and extension changes
   - The target path is the parent target directory resolved with the computed target filename

This is entirely a filesystem operation: `Path.resolve(childTargetFilename)`. URI conversion happens later, at the point of use (link generation, deployment).

## Display and Logging

Throughout the codebase, paths are logged and displayed in their natural representation:

- **Filesystem paths** are displayed as `Path` objects, which render in platform-native form. Log messages like `"Source file \`{}\` uses ..."` receive `artifact.getSourcePath()` directly.
- **URI references** are displayed as `URIPath` objects using their `toString()`, which renders the encoded URI path form.
- **Debug-level logging** occasionally shows both forms: `"{} ({})"` with `artifact.getTargetPath()` and `artifact.getTargetPath().toUri()`.

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

Static utilities for URI operations. Key methods:

| Method | Purpose |
|---|---|
| `toCollectionURI(URI)` | Force a trailing `/` on the URI path |
| `resolve(URI, URI)` | RFC 3986 resolution (fixes Java's RFC 2396 empty-path behavior and UNC path issues) |
| `resolve(URI, URIPath)` | Convenience overload |
| `findRelativePath(URI, URI)` | Compute relative path with backtracking; returns `Optional` |

### `Paths` (`com.globalmentor.io.Paths`)

Static utilities for filesystem `Path` operations. Key methods:

| Method | Purpose |
|---|---|
| `changeBase(Path, Path, Path)` | Rebase a path from one tree root to another |
| `isSubPath(Path, Path)` | Check containment after normalization |
| `checkArgumentAbsolute(Path)` | Precondition: path is absolute |
| `checkArgumentSubPath(Path, Path)` | Precondition: path is under base |

### `Path.toUri()` / `Paths.get(URI)` (JDK)

The bridge between the two coordinate systems. These are the _only_ mechanisms the codebase uses for filesystem↔URI translation.
