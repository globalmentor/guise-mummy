# Plan: Guarantee Real Paths for Artifact Source and Target Paths

Ensure that all `Artifact` source and target paths in Guise Mummy are real (canonical) paths — normalized segments, canonical filesystem case — by deriving real paths at the system boundary and documenting the guarantee in API contracts.

## Overview

This is a single monolithic chunk — all steps are additive within the `mummy` module.

- Step 1: Derive real paths in `GuiseMummy.createProject()` for the project directory and all CLI-supplied override directories
- Step 2: Introduce `AbstractMummyContext` — caches site directories as constructor parameters, validates they are in real-path form via `checkArgumentRealPath()`, and addresses the existing TODO on `MummyContext.getRoot()`
- Step 3: Derive real paths in `GuiseMummy.initialize()` and pass them to the context constructor
- Step 4: Document the real-path guarantee on `GuiseProject.getDirectory()`
- Step 5: Document the real-path guarantee on `Artifact.getSourcePath()` and `Artifact.getTargetPath()`
- Step 6: Update `MummyContext` directory method docs; remove `default` implementations (moved to `AbstractMummyContext`)
- Step 7: Update test context classes (`FakeMummyContext`, `DummyMummyContext`); delete `StubMummyContext`
- Step 8: Add post-creation real-path verification at the start of the MUMMIFY phase

**Notable:** No changes needed in the CLI. The CLI already passes `projectDirectory.toAbsolutePath()` to `createProject()`, and `createProject()` is the single gateway where project directory canonicalization is applied.

**Prerequisites (met):**

- `Paths.deriveRealPath(Path, LinkOption...)` in `globalmentor-core`. Walks up the ancestor chain until it finds an existing path, canonicalizes that ancestor via `toRealPath()`, then reattaches the non-existent tail segments via `resolve()`. If the full path exists, it is equivalent to `toRealPath()`. If no ancestor exists, it throws `NoSuchFileException`.
- `Paths.checkArgumentRealPath(Path, LinkOption...)` in `globalmentor-core`. Validates that a path is in real-path form by comparing it against `deriveRealPath()`. Throws `IllegalArgumentException` if they differ (including case differences on case-insensitive platforms). I/O errors from `deriveRealPath()` propagate as `IOException`.

---

## Analysis

### Where paths enter the system

All paths flow through a single gateway: `GuiseMummy.createProject()`. The CLI commands (`validate`, `clean`, `plan`, `mummify`, `prepareDeploy`, `deploy`, `serve`) all call `createProject(projectDirectory.toAbsolutePath(), ...)`. The project directory comes from either:

- The command-line `<project>` parameter (picocli converts to `Path`)
- `getWorkingDirectory()` → `System.getProperty("user.dir")`

Both produce absolute paths but neither guarantees canonical case on case-insensitive filesystems (Windows). These user-supplied paths have the **highest risk** of carrying wrong-case segments — the shell preserves whatever case the user typed in `cd`.

### How paths propagate

1. `createProject()` stores the project directory in `DefaultGuiseProject` (via `checkArgumentAbsolute(projectDirectory).normalize()`).
2. Configuration paths (`siteSourceDirectory`, `siteTargetDirectory`, `siteDescriptionTargetDirectory`) come from three sources, all stored as absolute paths:
   - **Default paths:** computed as `projectDirectory.resolve("target/site")` etc. — canonical if the project directory is canonical.
   - **CLI-supplied absolute paths:** override the config via `createProject()` parameters. Currently `.normalize()`d. These are user-supplied and carry the same case risk as the project directory.
   - **Config-file paths:** could be relative (resolved against project dir) or absolute. Absolute paths from config files carry the same risk.
3. `MummyContext.getSiteTargetDirectory()` currently retrieves the path from configuration on every access via `getProject().getDirectory().resolve(configPath)`. Since the config value is already absolute, `Path.resolve(absolutePath)` returns it unchanged — the project directory is irrelevant. This means CLI/config override paths bypass any project directory canonicalization entirely.
4. `DirectoryMummifier.plan()` receives the source and target root directories and recursively builds the artifact tree by `resolve()`-ing filenames from `Files.list()` (source side) and computed filenames (target side). Child segments are guaranteed correct: source filenames come from `Files.list()` (on-disk case), target filenames are deterministic transformations of source filenames.

**Key insight:** The three site directories (source, target, description target) are essential to the identity of the context — the entire artifact tree is rooted in them. The context should not derive them from configuration; it should receive them as constructor parameters already in canonical form. This decouples the context from configuration key knowledge, makes the contract explicit (the constructor validates via `checkArgumentRealPath()`), and allows test code to construct contexts with any real path without requiring a fully-populated configuration. The `MummyContext` default methods that re-derive directory paths on every access from configuration are replaced by cached fields. This also addresses the existing `//TODO move to some abstract base class` on `MummyContext.getRoot()`.

### Why `Paths.deriveRealPath()` instead of `Path.toRealPath()`

The target directory typically does not exist at project creation time. By design, the PLAN phase is read-only — it computes target paths without creating anything. The MUMMIFY phase creates target directories on demand (`DirectoryMummifier.mummify()` line 337). A plan-only execution (e.g. `--describe-plan`) must not create filesystem side effects.

`Path.toRealPath()` requires the entire path to exist, so it cannot be called on a not-yet-created target directory. `Paths.deriveRealPath()` (from `globalmentor-core`) handles this by walking up the ancestor chain until it finds an existing path, canonicalizing that ancestor, and reattaching the non-existent tail segments. This provides uniform canonicalization for all paths at project creation time without requiring that they exist.

### Why `NOFOLLOW_LINKS`

Guise Mummy already ignores symlinks during source discovery: `BaseMummyContext.isIgnore()` rejects anything that is not `isRegularFile()` or `isDirectory()` (line 109), which excludes symlinks. Using `NOFOLLOW_LINKS` is consistent: we want canonical case and normalized segments without resolving symlinks, because symlinks are not part of Mummy's traversal model.

On Windows, `toRealPath(NOFOLLOW_LINKS)` still canonicalizes case (the primary concern). On Unix, it normalizes path segments. On both, it verifies the path exists.

### Why post-creation verification

Even after deriving real paths, a race condition could cause on-disk case to differ from the computed path — for example, if the target directory is created by an external process between PLAN and MUMMIFY with different case. Since `createDirectories()` on a case-insensitive filesystem treats `TARGET` and `target` as the same, it would not create a new directory, leaving the on-disk case diverged from the computed path. A verification check immediately after creation catches this.

---

## Step 1: Derive real paths in `GuiseMummy.createProject()`

### Project directory (line 526)

Current:

```java
projectDirectory = checkArgumentAbsolute(projectDirectory).normalize();
```

New:

```java
projectDirectory = deriveRealPath(checkArgumentAbsolute(projectDirectory), NOFOLLOW_LINKS);
```

This replaces `.normalize()` with `deriveRealPath(..., NOFOLLOW_LINKS)`, which subsumes normalization (removes `.` and `..`), canonicalizes filesystem case for all existing segments, and preserves non-existent tail segments as-is. The project directory must exist, so this is equivalent to `toRealPath()` in practice — but `deriveRealPath()` is used for consistency with the rest of the plan.

The `IOException` from `deriveRealPath()` is already covered by `createProject()`'s `throws` clause.

Static import `com.globalmentor.io.Paths.deriveRealPath` and `java.nio.file.LinkOption.NOFOLLOW_LINKS`.

### `getDefaultConfiguration()` (line 555)

**Leave as `.normalize()`.** This method is a pure default-values factory that builds default paths from `projectDirectory.resolve(...)`. Since `createProject()` canonicalizes `projectDirectory` before calling `getDefaultConfiguration(projectDirectory)`, the `.normalize()` here is a no-op — but changing it to `deriveRealPath()` would require adding `IOException` to the method's signature for no benefit.

### CLI-supplied override directories (lines 538–546)

Current:

```java
if(siteSourceDirectory != null) {
    userSettings.put(PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY, checkArgumentAbsolute(siteSourceDirectory).normalize());
}
if(siteTargetDirectory != null) {
    userSettings.put(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY, checkArgumentAbsolute(siteTargetDirectory).normalize());
}
if(siteDescriptionTargetDirectory != null) {
    userSettings.put(PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY, checkArgumentAbsolute(siteDescriptionTargetDirectory).normalize());
}
```

New:

```java
if(siteSourceDirectory != null) {
    userSettings.put(PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY, deriveRealPath(checkArgumentAbsolute(siteSourceDirectory), NOFOLLOW_LINKS));
}
if(siteTargetDirectory != null) {
    userSettings.put(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY, deriveRealPath(checkArgumentAbsolute(siteTargetDirectory), NOFOLLOW_LINKS));
}
if(siteDescriptionTargetDirectory != null) {
    userSettings.put(PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY, deriveRealPath(checkArgumentAbsolute(siteDescriptionTargetDirectory), NOFOLLOW_LINKS));
}
```

`deriveRealPath()` handles all cases uniformly: if the full path exists, it canonicalizes completely; if only ancestors exist (typical for target directories), it canonicalizes the existing prefix and preserves the tail.

CLI-supplied override paths must be canonicalized here because `AbstractMummyContext` (Step 2) only *validates* that the paths it receives are in real-path form — it does not derive them. Config-file paths are canonicalized in `initialize()` (Step 3).

---

## Step 2: Introduce `AbstractMummyContext`

Create a new abstract class `AbstractMummyContext` inserted between `MummyContext` and `BaseMummyContext`. This class receives the three site directories as constructor parameters and caches them as immutable fields. It validates that each path is in real-path form via `checkArgumentRealPath()`. It does not access configuration at all — the caller is responsible for resolving the paths from whatever source (configuration, defaults, CLI) and deriving their real paths before constructing the context.

This addresses the existing `MummyContext.getRoot()` TODO: `//TODO move to some abstract base class; if it is configurable, it shouldn't have a default implementation`.

### New class hierarchy

```
MummyContext (interface — abstract method declarations, no default directory methods)
  └─ AbstractMummyContext (abstract — caches validated directories, implements getRoot())
       └─ BaseMummyContext (abstract — mummifier registration, isIgnore(), etc.)
            └─ GuiseMummy.Context (concrete — stores site configuration, plan, deploy state)
```

### `AbstractMummyContext` implementation

```java
/// Abstract base implementation of [MummyContext] that caches site directory paths.
///
/// The site source, target, and description target directories are essential to the identity of the
/// context — the entire artifact tree is rooted in them. They are received as constructor parameters
/// and stored as immutable fields. The constructor validates that each path is in real-path form
/// via [Paths#checkArgumentRealPath(Path, LinkOption...)].
/// @implNote The constructor performs filesystem I/O to validate the real-path precondition.
/// @author Garret Wilson
public abstract class AbstractMummyContext implements MummyContext {

    private final GuiseProject project;
    private final URI root;
    private final Path siteSourceDirectory;
    private final Path siteTargetDirectory;
    private final Path siteDescriptionTargetDirectory;

    /// Constructor.
    /// @param project The Guise project.
    /// @param siteSourceDirectory The base directory of the site source, in real-path form.
    /// @param siteTargetDirectory The output directory of the site, in real-path form.
    /// @param siteDescriptionTargetDirectory The output directory of the site description, in real-path form.
    /// @throws IllegalArgumentException if any directory path is not in real-path form.
    /// @throws IOException if an I/O error occurs during real-path validation.
    protected AbstractMummyContext(@NonNull final GuiseProject project,
            @NonNull final Path siteSourceDirectory,
            @NonNull final Path siteTargetDirectory,
            @NonNull final Path siteDescriptionTargetDirectory) throws IOException {
        this.project = requireNonNull(project);
        this.siteSourceDirectory = checkArgumentRealPath(siteSourceDirectory, NOFOLLOW_LINKS);
        this.siteTargetDirectory = checkArgumentRealPath(siteTargetDirectory, NOFOLLOW_LINKS);
        this.siteDescriptionTargetDirectory = checkArgumentRealPath(siteDescriptionTargetDirectory, NOFOLLOW_LINKS);
        this.root = this.siteSourceDirectory.toUri();
    }

    @Override
    public GuiseProject getProject() { return project; }

    @Override
    public URI getRoot() { return root; }

    @Override
    public Path getSiteSourceDirectory() { return siteSourceDirectory; }

    @Override
    public Path getSiteTargetDirectory() { return siteTargetDirectory; }

    @Override
    public Path getSiteDescriptionTargetDirectory() { return siteDescriptionTargetDirectory; }

}
```

### Changes to `BaseMummyContext`

- Change `extends` from nothing to `extends AbstractMummyContext`
- Remove `implements MummyContext` (inherited from `AbstractMummyContext`)
- Remove the `project` field and `getProject()` override (moved to `AbstractMummyContext`)
- Update constructor to take the three directory paths and pass them to `super()`
- Constructor now throws `IOException` (from `checkArgumentRealPath()`)

### Changes to `GuiseMummy.Context`

- Update `super()` call to pass the three resolved directory paths (see Step 3 for how they are resolved)
- Constructor now throws `IOException` — already compatible since `initialize()` declares `throws IOException`

### Changes to `MummyContext` interface

- Remove `default` implementations of `getRoot()`, `getSiteSourceDirectory()`, `getSiteTargetDirectory()`, `getSiteDescriptionTargetDirectory()` — they become abstract method declarations
- Remove the `//TODO move to some abstract base class` comment on `getRoot()`
- Move `@implSpec` documentation to `AbstractMummyContext` implementations

---

## Step 3: Derive real paths in `GuiseMummy.initialize()`

`GuiseMummy.initialize()` is where the `Context` is constructed. This is where directory paths are resolved from configuration and canonicalized before being passed to the context constructor.

Currently `initialize()` resolves the site source directory from the *project* configuration at the top of the method (to locate the `.guise-mummy` config file), but does not resolve the target directories.

### Site source directory

The existing resolution at the top of `initialize()`:

```java
final Path siteSourceDirectory = project.getDirectory().resolve(project.getConfiguration().getPath(GuiseMummy.PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY));
```

becomes:

```java
final Path siteSourceDirectory = deriveRealPath(
        project.getDirectory().resolve(project.getConfiguration().getPath(GuiseMummy.PROJECT_CONFIG_KEY_SITE_SOURCE_DIRECTORY)),
        NOFOLLOW_LINKS);
```

This happens *before* the mummy configuration is loaded, because the site source directory is needed to locate the `.guise-mummy` config file. The `.guise-mummy` file cannot relocate its own parent directory, so the value from the project configuration is authoritative. The derived-real path is then reused when constructing the `Context`.

### Target and description-target directories

After the mummy configuration has been loaded and merged, derive the two remaining directories:

```java
final Path siteTargetDirectory = deriveRealPath(
        projectDirectory.resolve(mummyConfiguration.getPath(PROJECT_CONFIG_KEY_SITE_TARGET_DIRECTORY)),
        NOFOLLOW_LINKS);
final Path siteDescriptionTargetDirectory = deriveRealPath(
        projectDirectory.resolve(mummyConfiguration.getPath(PROJECT_CONFIG_KEY_SITE_DESCRIPTION_TARGET_DIRECTORY)),
        NOFOLLOW_LINKS);
final Context context = new Context(project, mummyConfiguration, siteSourceDirectory, siteTargetDirectory, siteDescriptionTargetDirectory);
```

The `mummyConfiguration` here is the merged configuration that already incorporates the CLI overrides (canonicalized in Step 1), the config file, and the defaults — in that priority order.

The `IOException` from `deriveRealPath()` is already covered by `initialize()`'s `throws` clause.

---

## Step 4: Document `GuiseProject.getDirectory()`

### Current doc

```java
/// Returns the project directory.
///
/// This is usually where the project configuration file (if any) is stored.
/// @return The project directory,
public Path getDirectory();
```

### Updated doc

```java
/// Returns the project directory.
///
/// This is usually where the project configuration file (if any) is stored.
/// The returned path is absolute and canonical (real path): normalized segments, no symbolic links in the path,
/// and filesystem-native case on case-insensitive systems.
/// @return The absolute canonical project directory.
/// @see Path#toRealPath(LinkOption...)
public Path getDirectory();
```

---

## Step 5: Document `Artifact` path guarantees

### `Artifact.getSourcePath()`

Current:

```java
/// Returns the path to the source of the artifact in the source tree.
/// @apiNote Depending on the artifact implementation, the source path is not guaranteed to exist.
/// @apiNote This method and all methods in this interface related to a source path in a file system may be moved eventually to [SourcePathArtifact].
/// @return The path referring to the source of this artifact, which may be a file or a directory.
public Path getSourcePath();
```

Updated:

```java
/// Returns the path to the source of the artifact in the source tree.
///
/// The returned path is absolute and canonical: derived from the project directory's real path
/// with filesystem-native case for all segments.
/// @apiNote Depending on the artifact implementation, the source path is not guaranteed to exist.
/// @apiNote This method and all methods in this interface related to a source path in a file system may be moved eventually to [SourcePathArtifact].
/// @return The absolute canonical path referring to the source of this artifact, which may be a file or a directory.
public Path getSourcePath();
```

### `Artifact.getTargetPath()`

Current:

```java
/// Returns the path to the generated artifact in the target tree.
/// @return The path to the generated artifact in the target tree.
public Path getTargetPath();
```

Updated:

```java
/// Returns the path to the generated artifact in the target tree.
///
/// The returned path is absolute and canonical: derived from the project directory's real path
/// with filesystem-native case for all segments.
/// @return The absolute canonical path to the generated artifact in the target tree.
public Path getTargetPath();
```

### `Artifact.getSourceDirectory()`

Current:

```java
/// Returns the path to the directory containing the artifact source file. If the artifact source path refers to a directory, this method returns the source
/// path itself; otherwise this method returns the parent directory.
/// @return The source directory of the artifact.
/// @see #getSourcePath()
public Path getSourceDirectory();
```

Updated:

```java
/// Returns the path to the directory containing the artifact source file. If the artifact source path refers to a directory, this method returns the source
/// path itself; otherwise this method returns the parent directory.
/// @return The absolute canonical source directory of the artifact.
/// @see #getSourcePath()
public Path getSourceDirectory();
```

---

## Step 6: Update `MummyContext` interface directory methods

The `default` implementations of `getRoot()`, `getSiteSourceDirectory()`, `getSiteTargetDirectory()`, and `getSiteDescriptionTargetDirectory()` are removed from the `MummyContext` interface. They become abstract method declarations. The concrete implementations move to `AbstractMummyContext` (Step 2). The `@implSpec` documentation moves to the `AbstractMummyContext` implementations.

### `getSiteSourceDirectory()`

Current (interface, `default`):

```java
/// Returns the base directory of the entire site source, representing the root of the context.
/// @implSpec The default implementation retrieves the value for key `siteSourceDirectory` from the configuration and
///           resolves it against the project directory.
/// @apiNote This is analogous to Maven's `${project.basedir}/src/site` directory.
/// @return The base directory of the site being mummified.
/// @see GuiseProject#getDirectory()
public default Path getSiteSourceDirectory() {
```

Updated (interface, abstract):

```java
/// Returns the base directory of the entire site source, representing the root of the context.
///
/// The returned path is absolute and canonical, derived from the project directory's real path.
/// @apiNote This is analogous to Maven's `${project.basedir}/src/site` directory.
/// @return The absolute canonical base directory of the site being mummified.
/// @see GuiseProject#getDirectory()
public Path getSiteSourceDirectory();
```

### `getSiteTargetDirectory()`

Current (interface, `default`):

```java
/// Returns the output directory of the entire site, representing the root of the context.
/// @implSpec The default implementation retrieves the value for key `siteTargetDirectory` from the configuration and
///           resolves it against the project directory.
/// @apiNote This is analogous to Maven's `${project.build.directory}` directory.
/// @return The base output directory of the site being mummified.
/// @see GuiseProject#getDirectory()
public default Path getSiteTargetDirectory() {
```

Updated (interface, abstract):

```java
/// Returns the output directory of the entire site, representing the root of the context.
///
/// The returned path is absolute and canonical, derived from the project directory's real path.
/// @apiNote This is analogous to Maven's `${project.build.directory}` directory.
/// @return The absolute canonical base output directory of the site being mummified.
/// @see GuiseProject#getDirectory()
public Path getSiteTargetDirectory();
```

### `getSiteDescriptionTargetDirectory()`

Updated similarly — remove `default`, remove `@implSpec`, add canonical path statement, make abstract.

### `getRoot()`

Current:

```java
public default URI getRoot() { //TODO move to some abstract base class; if it is configurable, it shouldn't have a default implementation
    return getSiteSourceDirectory().toUri();
}
```

Updated (interface, abstract):

```java
/// Returns some URI indicating the root of the current context, that is, the site source directory.
/// @return The URI that represents the root of the current context.
public URI getRoot();
```

---

## Step 7: Update test context classes

### `FakeMummyContext` (extends `BaseMummyContext`)

Update constructor to take the three directory paths and pass them to `super()`. Constructor now throws `IOException`.

Test call sites:

- `DirectoryMummifierTest`: already uses `new DefaultGuiseProject(tempDir)` with a real `@TempDir` path, and calls `plan()` which needs real directories. Derive the three paths from the project's default configuration.
- `XhtmlPageMummifierTest`, `MarkdownPageMummifierTest`: use `Configuration.empty()` and never access directory methods. Pass `getWorkingDirectory()` for all three directory paths — it exists and is a real path. The paths are validated by `checkArgumentRealPath()` but never otherwise used.

### `DummyMummyContext` (implements `MummyContext` directly)

Add concrete implementations of the four now-abstract methods (`getRoot()`, `getSiteSourceDirectory()`, `getSiteTargetDirectory()`, `getSiteDescriptionTargetDirectory()`) that throw `UnsupportedOperationException`. These tests (`DefaultImageMummifierTest`, `BaseImageMummifierTest`) never call directory methods.

### `StubMummyContext`

**Delete.** This class implements `MummyContext` directly and is never referenced outside its own file — dead code.

---

## Step 8: Post-creation real-path verification in MUMMIFY phase

At the start of the MUMMIFY phase in `GuiseMummy.mummify()`, after the root artifact's target directory is created, verify that the on-disk real path matches the computed target path. This catches race conditions or pre-existing directories with wrong case.

### Location: `GuiseMummy.mummify()` (line 311)

Currently:

```java
//# mummify phase
if(phase.compareTo(LifeCyclePhase.MUMMIFY) >= 0) {
    getLogger().info("Mummify phase: {}", LifeCyclePhase.MUMMIFY); //TODO i18n
    rootArtifact.getMummifier().mummify(context, rootArtifact);
}
```

New:

```java
//# mummify phase
if(phase.compareTo(LifeCyclePhase.MUMMIFY) >= 0) {
    getLogger().info("Mummify phase: {}", LifeCyclePhase.MUMMIFY); //TODO i18n
    final Path siteTargetDirectory = context.getSiteTargetDirectory();
    createDirectories(siteTargetDirectory);
    final Path realSiteTargetDirectory = siteTargetDirectory.toRealPath(NOFOLLOW_LINKS);
    if(!realSiteTargetDirectory.equals(siteTargetDirectory)) {
        throw new IOException("Site target directory real path `%s` does not match expected path `%s`.".formatted(realSiteTargetDirectory, siteTargetDirectory));
    }
    rootArtifact.getMummifier().mummify(context, rootArtifact);
}
```

This creates the root target directory (idempotent if it already exists), then verifies that `toRealPath()` on the now-existing directory matches the path computed during PLAN. The subsequent `DirectoryMummifier.mummify()` call will also call `createDirectories()` on the same path, which is harmless since it already exists.

**Note:** This check is only for the site target root. Subdirectories created during mummification use filenames derived from source `Files.list()` and mummifier transformations — they are correct by construction.

---

## Files Modified

| File | Change |
|---|---|
| `mummy/…/GuiseMummy.java` | `deriveRealPath(…, NOFOLLOW_LINKS)` for project dir + CLI overrides in `createProject()`; derive + pass directory paths in `initialize()`; post-creation verification in `mummify()` |
| `mummy/…/AbstractMummyContext.java` | **New file.** Caches site directories as constructor params; validates via `checkArgumentRealPath()` |
| `mummy/…/BaseMummyContext.java` | Extend `AbstractMummyContext`; remove `project` field; update constructor to take three directory paths and throw `IOException` |
| `mummy/…/MummyContext.java` | Remove `default` directory methods (moved to `AbstractMummyContext`); remove `getRoot()` TODO; update docs |
| `mummy/…/GuiseProject.java` | Document real-path guarantee on `getDirectory()` |
| `mummy/…/Artifact.java` | Document real-path guarantee on `getSourcePath()`, `getTargetPath()`, `getSourceDirectory()` |
| `mummy/…test…/FakeMummyContext.java` | Update constructor to take three directory paths; propagate `throws IOException` |
| `mummy/…test…/DummyMummyContext.java` | Add `UnsupportedOperationException` stubs for the four now-abstract directory methods |
| `mummy/…test…/StubMummyContext.java` | **Delete.** Dead code — never referenced. |
| `mummy/…test…/DirectoryMummifierTest.java` | Update `FakeMummyContext` construction to pass derived directory paths |
| `mummy/…test…/XhtmlPageMummifierTest.java` | Update `FakeMummyContext` construction to pass `getWorkingDirectory()` for directory paths |
| `mummy/…test…/MarkdownPageMummifierTest.java` | Update `FakeMummyContext` construction to pass `getWorkingDirectory()` for directory paths |

---

## Alternatives Considered

### Canonicalize in `DefaultGuiseProject` constructor

**Rejected.** Would add `IOException` to the constructor signature. `DefaultGuiseProject` is used in test code with `@TempDir` paths. Canonicalization belongs at the system boundary, not in the domain object.

### Canonicalize in CLI commands before `createProject()`

**Rejected.** Makes the guarantee CLI-dependent. Other entry points (tests, embedded usage, future Maven plugin) wouldn't benefit. `createProject()` is the shared gateway.

### Canonicalize each derived path individually (source dir, target dir, each artifact path)

**Rejected.** Over-engineering. All derived paths descend from the base directories by `resolve()` with simple relative segments. Child segments are correct by construction: source filenames come from `Files.list()` (on-disk case), target filenames are deterministic transformations.

### Use `toRealPath()` without `NOFOLLOW_LINKS`

**Rejected.** Without `NOFOLLOW_LINKS`, symlinks in the project directory path would be resolved to their targets. Guise Mummy already ignores symlinks during source traversal (`BaseMummyContext.isIgnore()` skips non-regular-file/non-directory entries). Using `NOFOLLOW_LINKS` is consistent: we canonicalize case and segments without changing symlink semantics that the rest of the system doesn't support.

### Conditional `toRealPath()` with `.normalize()` fallback for non-existent paths

**Rejected.** The original approach: call `toRealPath()` if the path exists, fall back to `.normalize()` otherwise. This produces a path that is sometimes canonical and sometimes not — the API can't honestly guarantee the result. `Paths.deriveRealPath()` solves this uniformly by canonicalizing the existing ancestor portion and preserving non-existent tail segments.

### Canonicalize only the project directory (not override directories)

**Rejected.** CLI-supplied override directories (e.g. `--site-target-directory`) are stored as absolute paths. When `MummyContext.getSiteTargetDirectory()` calls `resolve()` with an absolute config path, `resolve()` returns the absolute path unchanged — the canonical project directory is irrelevant. Override paths carry raw shell case and have the same (or higher) risk of case mismatch as the project directory itself.

### Canonicalize all paths in `createProject()` only (without context-level handling)

**Rejected.** Applying `deriveRealPath()` to CLI-supplied override directories in `createProject()` covers only two of three configuration sources. Config-file paths loaded from TURF bypass `createProject()`'s parameter handling entirely. A second canonicalization point is needed in `initialize()` after configuration merging.

### Have `AbstractMummyContext` resolve directories from `Configuration`

**Rejected (revised away).** The initial design had `AbstractMummyContext` take a `Configuration` parameter and resolve the three directory config keys internally. This creates two problems: (1) it couples the context to configuration key knowledge — the context shouldn't know which keys hold the directories; (2) tests that don't need directories would be forced to provide a fully-populated configuration or face `MissingConfigurationKeyException` at construction time, because the resolution is eager. Taking the paths as constructor parameters instead decouples the context from configuration and allows test code to pass any real path without a configuration.

### Canonicalize in the `MummyContext` default accessors (on every access)

**Rejected.** `getSiteSourceDirectory()` etc. are called repeatedly during planning and mummification. Performing `deriveRealPath()` (which involves filesystem I/O) on every access is wasteful and semantically wrong — these are accessors, not initializers. The directories should be fixed at construction time, not re-derived on each call.
