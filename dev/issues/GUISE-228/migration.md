# GUISE-228 Migration Guide

This guide covers migrating from the `guise.io` domain to the `guise.dev` domain. The migration affects namespace URIs embedded in source content and cached build metadata; it does not change site content or structure.

## Site Projects

A Guise Mummy site project requires updating namespace URIs in XHTML content files and clearing (or updating) the cached site description metadata. The project file and Markdown content do not require changes.

### Quick Reference

| Search | Replace | Scope |
|--------|---------|-------|
| `https://guise.io/name/mummy/` | `https://guise.dev/name/mummy/` | XHTML content and templates |
| `https://guise.io/name/mesh/` | `https://guise.dev/name/mesh/` | XHTML content and templates (if Guise Mesh is used) |

### XHTML Content and Templates

XHTML source files (including `.template.xhtml` files) declare Guise namespace URIs in `xmlns:` attributes on `<html>` or `<head>` elements. Update all occurrences:

- Guise Mummy: `xmlns:mummy="https://guise.io/name/mummy/"` → `xmlns:mummy="https://guise.dev/name/mummy/"`.
- Guise Mesh: `xmlns:mx="https://guise.io/name/mesh/"` → `xmlns:mx="https://guise.dev/name/mesh/"`.

The prefix name (e.g. `mummy`, `guise-mummy`, `mx`) is site-specific and does not change — only the namespace URI value changes. A site may use a non-standard prefix such as `xmlns:guise-mummy=` instead of `xmlns:mummy=`; in all cases only the URI on the right-hand side needs updating.

Property values such as `<meta property="guise-mummy:order" content="3" />` use the prefix, not the full URI, so they do not need modification once the `xmlns:` declaration is corrected.

### Markdown Content

Markdown front matter uses short prefixes (e.g. `mummy:order: 3`) resolved at runtime against predefined vocabulary mappings. Because the prefix resolution was updated in the code, **Markdown files do not require migration**.

### Project File

The `guise-project.turf` file uses bare type tags (`*GuiseProject:`, `*S3:`) and bare configuration keys (e.g. `mummy:`, `namesBare`), all resolved by the TURF parser at runtime. **The project file does not require migration.**

### Site Description Cache

The `target/site-description/` directory contains cached per-artifact metadata in TUPR format (`*.@.turf`). These files embed the Guise Mummy namespace URI in their URF namespace declarations, e.g.:

```
space-mummy = <https://guise.io/name/mummy/>
```

After the domain rename, property lookups (e.g. `mummy/altLocation`) resolve against the new namespace `https://guise.dev/name/mummy/`, so properties stored under the old namespace will not be found. This affects any functionality that reads cached metadata, including redirect detection and incremental build change detection.

**Option A — Clean rebuild (recommended).** Delete the `target/site-description/` directory (or run a clean build). The next build regenerates all description files with the correct namespace.

**Option B — In-place migration.** If preserving cached metadata is important (e.g. to avoid a full rebuild on a large site), apply the namespace URI replacement across all `.@.turf` files in `target/site-description/`:

- `https://guise.io/name/mummy/` → `https://guise.dev/name/mummy/`

> **Note:** Migrating the description files is optional if the site is cleaned. A clean build regenerates them from the source content, which is the authoritative source of truth. The description cache is a build optimization, not a persistent store.

## Software Projects (Maven Dependencies)

For Java projects that depend on Guise Mummy as a library, update the Maven coordinates in `pom.xml`:

- `<groupId>io.guise</groupId>` → `<groupId>dev.guise</groupId>`.

Artifact IDs (e.g. `guise-mummy`) are unchanged.
