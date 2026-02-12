# Plan: `io.guise` → `dev.guise` Package Rename

## Checklist

- [x] Step 1: Rename directory trees (`io/guise/` → `dev/guise/`) via `git mv` (8 source roots, ~115 files).
- [x] Step 2: Update Java `package` declarations and `import` statements (`io.guise` → `dev.guise`) (~101 Java files).
- [x] Step 3: Update Maven POM files — groupId, properties, URL (5 POMs, 14 occurrences).
- [x] Step 4: Update XML namespace URIs (`guise.io` → `guise.dev`) in Java constants, XHTML files, and CSS (5 files).
- [x] Step 5: Update readme documentation — Maven coordinates, class references, namespace URIs (4 readme files).
- [x] Step 6: Add migration guide section to `mummy/readme.md`.
- [x] Step 7: Verify build and tests.

**Notable items:**
- `target/` directories are build artifacts — `mvn clean` before build verification.
- Resource files (`.properties`, `.xhtml`, `.md`, `.jpg`) have no `io.guise` in their content; only their directory paths change.
- `demo-basic/src/site/_assets/css/guise-skeleton.min.css` has a `guise.io` URL in its banner comment.

---

## Step 1: Rename directory trees

Use `git mv` to rename the top-level `io` directory to `dev` in each source root. Because no other packages live under `io/` in any of these trees, renaming at the `io` → `dev` level is sufficient and preserves everything underneath.

### Source roots to rename

Each command renames the `io` directory to `dev` at the source root level:

| # | Source root | Command |
|---|---|---|
| 1 | `cli/src/main/java/` | `git mv cli/src/main/java/io cli/src/main/java/dev` |
| 2 | `mesh/src/main/java/` | `git mv mesh/src/main/java/io mesh/src/main/java/dev` |
| 3 | `mesh/src/test/java/` | `git mv mesh/src/test/java/io mesh/src/test/java/dev` |
| 4 | `mummy/src/main/java/` | `git mv mummy/src/main/java/io mummy/src/main/java/dev` |
| 5 | `mummy/src/test/java/` | `git mv mummy/src/test/java/io mummy/src/test/java/dev` |
| 6 | `mummy/src/main/resources/` | `git mv mummy/src/main/resources/io mummy/src/main/resources/dev` |
| 7 | `mummy/src/test/resources/` | `git mv mummy/src/test/resources/io mummy/src/test/resources/dev` |
| 8 | `tomcat/src/main/java/` | `git mv tomcat/src/main/java/io tomcat/src/main/java/dev` |

**File counts by module:** cli: 1, mesh: 18, mummy: 80 Java + 1 properties + 6 test resources + 7 test images = 94, tomcat: 2. **Total: ~115 files.**

### Alternative considered: file-by-file `git mv`

Moving individual files would be more granular but far more error-prone and verbose. Since no non-Guise files exist under `io/` in any source root, renaming at the `io` directory level is safe and idiomatic.

---

## Step 2: Update Java package declarations and imports

After the directory rename, all `.java` files still contain `package io.guise.*` and `import io.guise.*` statements. Use a single workspace-wide find-and-replace:

1. **Package declarations:** Replace `package io.guise` with `package dev.guise` in all `.java` files.
2. **Import statements:** Replace `import io.guise` with `import dev.guise` (covers both regular and static imports) in all `.java` files.

These are safe prefix replacements — no other `io.guise` text exists in Java source outside of package/import declarations.

**File count:** ~101 Java files (1 cli + 18 mesh + 80 mummy + 2 tomcat).

### Verification

After replacement, confirm zero remaining occurrences of `package io.guise` or `import io.guise` in any `.java` file.

---

## Step 3: Update Maven POM files

### 3a. GroupId replacements

Replace `<groupId>io.guise</groupId>` with `<groupId>dev.guise</groupId>` in all POM files:

| File | Occurrences | Context |
|---|---|---|
| `pom.xml` | 5 | project groupId (line 12), 4 dependency-management entries (lines 120, 126, 132, 138) |
| `cli/pom.xml` | 3 | parent groupId (line 6), 2 dependencies (lines 38, 43) |
| `mesh/pom.xml` | 1 | parent groupId (line 6) |
| `mummy/pom.xml` | 2 | parent groupId (line 6), guise-mesh dependency (line 68) |
| `tomcat/pom.xml` | 1 | parent groupId (line 6) |

**Total: 12 groupId occurrences across 5 files.**

### 3b. Main class property

In `cli/pom.xml` line 17, replace:
```xml
<exe.main.class>io.guise.cli.GuiseCli</exe.main.class>
```
with:
```xml
<exe.main.class>dev.guise.cli.GuiseCli</exe.main.class>
```

### 3c. Project URL

In `pom.xml` line 19, replace:
```xml
<url>https://guise.io/mummy/</url>
```
with:
```xml
<url>https://guise.dev/mummy/</url>
```

**Total: 14 POM changes across 5 files.**

---

## Step 4: Update XML namespace URIs and web URLs

These are `guise.io` domain references in code, templates, and assets — distinct from the Java package rename.

### 4a. Java namespace constants

| File | Line | Old | New |
|---|---|---|---|
| `mesh/src/main/java/dev/guise/mesh/GuiseMesh.java` | 42 | `"https://guise.io/name/mesh/"` | `"https://guise.dev/name/mesh/"` |
| `mummy/src/main/java/dev/guise/mummy/GuiseMummy.java` | 83 | `"https://guise.io/name/mummy/"` | `"https://guise.dev/name/mummy/"` |

Note: File paths shown use the *post-rename* `dev/guise/` paths from Step 1.

### 4b. XHTML templates and test resources

| File | Line | Old | New |
|---|---|---|---|
| `demo-basic/src/site/.template.xhtml` | 3 | `https://guise.io/name/mummy/` | `https://guise.dev/name/mummy/` |
| `mummy/src/test/resources/dev/guise/mummy/smoke-mesh.xhtml` | 3 | `https://guise.io/name/mesh/` | `https://guise.dev/name/mesh/` |
| `mummy/src/test/resources/dev/guise/mummy/mummify/page/simple-metadata.xhtml` | 5 | `https://guise.io/name/mummy/` | `https://guise.dev/name/mummy/` |

### 4c. CSS asset

| File | Line | Old | New |
|---|---|---|---|
| `demo-basic/src/site/_assets/css/guise-skeleton.min.css` | 3 | `https://guise.io/skeleton/` | `https://guise.dev/skeleton/` |

**Total: 6 substitutions across 5 files (after directory rename).**

---

## Step 5: Update readme documentation

Four readme files reference `io.guise` Maven coordinates and/or class names.

### 5a. `cli/readme.md` (line 7)

Replace `io.guise:guise-cli` and `g:io.guise` in the Maven Central URL with `dev.guise:guise-cli` and `g:dev.guise`.

### 5b. `mesh/readme.md` (line 8)

Replace `io.guise:guise-mesh` and `g:io.guise` with `dev.guise:guise-mesh` and `g:dev.guise`.

### 5c. `tomcat/readme.md` (line 7)

Replace `io.guise:guise-tomcat` and `g:io.guise` with `dev.guise:guise-tomcat` and `g:dev.guise`.

### 5d. `mummy/readme.md`

Multiple references:
- Line 32: Maven coordinate `io.guise:guise-mummy` and Maven Central URL `g:io.guise`.
- Line 68: Namespace URI `https://guise.io/name/mummy/` (already handled conceptually in Step 4, but appears in readme prose).
- Line 75: Namespace URI in XHTML example `https://guise.io/name/mummy/`.
- Lines 109, 111, 113, 127 (×3), 142: Class references `io.guise.mummy.*` → `dev.guise.mummy.*`.

**Total: ~15 substitutions across 4 readme files.** A single find-and-replace of `io.guise` → `dev.guise` in each file handles the Maven coordinates and class references. The `guise.io` → `guise.dev` namespace URI replacements in `mummy/readme.md` lines 68 and 75 are a separate replacement.

---

## Step 6: Add migration guide

Add a "Migration from 0.x" section (or similar) to `mummy/readme.md` documenting the breaking changes for existing Guise sites:

1. **Package rename:** `io.guise` → `dev.guise` in Maven coordinates and Java imports.
2. **Namespace URI change:** `https://guise.io/name/mummy/` → `https://guise.dev/name/mummy/` and `https://guise.io/name/mesh/` → `https://guise.dev/name/mesh/` in all XHTML templates and content files.

The exact placement and wording will be determined during implementation. The key content is:
- The old and new namespace URIs.
- The old and new Maven coordinates.
- A note that XHTML template files (`xmlns:mummy="…"`, `xmlns:mx="…"`) must be updated.

---

## Step 7: Verify build and tests

1. Run `mvn clean` to remove all `target/` directories containing old `io/guise/` class paths.
2. Run `mvn compile test-compile -pl mesh,mummy,tomcat,cli` to verify compilation.
3. Run `mvn test -pl mesh,mummy,tomcat` to verify unit tests.
4. Confirm zero occurrences of `io.guise` in any `src/` file (use VS Code `grep_search`).
5. Confirm zero occurrences of `guise.io` in any source file outside `dev/issues/` (use VS Code `grep_search`).

---

## Execution order rationale

Steps must be performed in order:

1. **Directory rename first** (Step 1) — creates the `dev/guise/` tree so that subsequent text replacements target files at their final paths. Git tracks the renames.
2. **Java source text** (Step 2) — package/import declarations updated after files are in place.
3. **POM files** (Step 3) — independent of Java source but logically grouped after source.
4. **Namespace URIs** (Step 4) — domain-level changes, separate concern from package rename.
5. **Readmes** (Step 5) — documentation updated last.
6. **Migration guide** (Step 6) — new content, written after all references are finalized.
7. **Build verification** (Step 7) — final validation.

### Alternative considered: text replacement before directory rename

An alternative would be to do all text replacements first (while files are still at `io/guise/` paths), then rename directories. This was rejected because:
- File paths used in edit tool calls would become stale after the rename, requiring careful sequencing.
- `git mv` works on the current filesystem state, so it's cleaner to rename first and then edit the files at their final locations.
- The "rename first" approach matches the natural mental model: move, then update references.
