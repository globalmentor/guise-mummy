# Guise Mummy

Guise™ Mummy static site generator library and CLI application.

## Overview

Guise Mummy takes source files of various types — XHTML, Markdown, images, and arbitrary assets — and generates a deployable static site. Analogous to tools like [Jekyll](https://jekyllrb.com/) or [Hugo](https://gohugo.io/), Guise Mummy is cross-platform and standards-based, built on the Java Virtual Machine (JVM). It is designed to simply do what you would expect it to do, and do it right.

### Features

* **Convention over configuration**, with inspiration from [Apache Maven](https://maven.apache.org/). Put your source files in `src/site/`, and your site will be generated in `target/site/` — no configuration required, unless you want it.
* **Multiple source formats.** Write pages in [XHTML5](https://www.w3.org/TR/html52/introduction.html#html-vs-xhtml) (`.xhtml`) or [Markdown](https://commonmark.org/) (`.md`). Images, CSS, JavaScript, and other files are included automatically.
* **A normalized view of your site.** No more deciding whether to link to `foo/bar/index.html` or to `foo/bar/`; Guise Mummy will normalize all your links to `foo/bar/`. With [clean URLs](https://en.wikipedia.org/wiki/Clean_URL) enabled, even the `.html` extension disappears — `products/mousetrap` instead of `products/mousetrap.html` — while preserving content type metadata for both local serving and remote deployment.
* **Complete link relativization.** Put a link in a template and Guise Mummy will update it to still point to the same resource wherever the template is used in your hierarchy, including backtracking across sibling directories.
* **Example-based navigation.** Provide a sample navigation menu in your template — Guise Mummy will regenerate it for each page, applying the correct page names and active/inactive styles, just by looking at your example. No expression language required for this common task.
* **Expression language.** For more complex needs, [Guise Mesh](#expression-language-guise-mesh) provides a full expression language (MEXL, backed by [Apache Commons JEXL](https://commons.apache.org/proper/commons-jexl/)) with iteration, attribute mutation, text replacement, and string interpolation — all expressed as standard XML attributes, keeping your templates valid XHTML.
* **Semantic metadata.** Change a page property by updating a YAML front-matter header in Markdown or an HTML `<meta>` element in XHTML. Guise Mummy uses a simple but extensive semantic-based metadata framework, the [Uniform Resource Framework (URF)](https://urf.io/), throughout.
* **Automated deployment.** A single `guise deploy` command uploads your site to [Amazon S3](https://aws.amazon.com/s3/) with [CloudFront](https://aws.amazon.com/cloudfront/) CDN distribution and [Route 53](https://aws.amazon.com/route53/) DNS management, preserving per-page metadata such as content type along the way.
* **Incremental mummification.** Only changed pages are regenerated on subsequent builds, with content fingerprints tracked for each artifact.

## Download

Guise Mummy is available in the Maven Central Repository as [dev.guise:guise-mummy](https://search.maven.org/search?q=g:dev.guise%20AND%20a:guise-mummy). For building the entire Guise ecosystem see the documentation for the parent [Guise Mummy project](../).

## Getting Started

### CLI

Guise Mummy is most easily used via the [Command-Line Interface (CLI)](../cli/). Use `guise help` for details and options.

| Command | Description |
|---|---|
| `guise validate` | Validates the project configuration. |
| `guise clean` | Removes the site target and description directories. |
| `guise plan` | Discovers source files and plans mummification; prints the plan by default. |
| `guise mummify` | Generates the static site from source files. |
| `guise prepare-deploy` | Generates the site and provisions deployment infrastructure, without deploying. |
| `guise deploy` | Generates the site and deploys it to the configured hosting service. |
| `guise serve` | Starts an HTTP server on port `4040` to browse the generated site. |

Each command runs all prerequisite lifecycle phases automatically. For example `guise deploy` validates, plans, mummifies, and prepares deployment before deploying.

Common options include `--full` / `-f` to force a full build instead of incremental, `--browse` / `-b` to open a browser after serving or deploying, and `--port` / `-p` to set the server port for `guise serve`.

### Source Files

Source files go in `src/site/`. Guise Mummy determines how to process each file based on its extension.

**XHTML** (`.xhtml`) — The native format. Pages are well-formed XML using the HTML5 vocabulary.

```html
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>Hello, World!</title>
</head>
<body>
  <p>Hello, World!</p>
</body>
</html>
```

**Markdown** (`.md`, `.markdown`) — Pages with optional YAML front matter for metadata. Links to other source files (e.g. `about.md`) work naturally and are retargeted during generation.

```markdown
---
title: Welcome
description: The home page.
author: Jane Smith
---
# Welcome

Read more [about](about.md) this site.
```

**Other files** — Images, CSS, JavaScript, and any other files are copied to the target site as-is, with content type metadata preserved.

#### File Naming Conventions

* **Dotfiles** — Any file or directory starting with `.` is excluded from the generated site but may influence generation. For example `.template.xhtml` serves as a template (see [Templates](#templates)) but does not itself appear in the output.
* **Veiled files** — A file or directory starting with `_` (underscore) _is_ included in the generated site but hidden from generated navigation menus. For example an `_assets/` directory containing `_assets/css/` and `_assets/js/` will still appear in the target site, but no "assets" menu item will be generated.
* **Assets** — A file or directory starting with `$` (dollar sign) is included in the target site but excluded from both navigation and page processing. The asset designation suppresses page generation only — files that would otherwise be processed by a page mummifier (Markdown, XHTML, HTML) are instead handled by the default file mummifier. Other registered mummifiers (such as the image mummifier) still apply normally; for example, a large image in an asset directory may still be scaled.

### Templates

Place a template file named `.template.xhtml` in any directory to apply it to every page _in that directory and its subdirectories_. The template provides the page skeleton — header, footer, navigation, stylesheets — while the page provides the content. Guise Mummy merges them by replacing the template's `<main>` content (or `<article>`, or `<body>` if neither is present) with the page's content.

Guise Mummy features are controlled through elements and attributes in the `https://guise.dev/name/mummy/` XHTML namespace.

```html
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:mummy="https://guise.dev/name/mummy/">
<head>
  <meta charset="UTF-8" />
  <title>Example Template</title>
  <link href="_assets/css/site.css" rel="stylesheet" />
</head>
<body>
  <header>
    <nav>
      <ul mummy:regenerate="regenerate">
        <li class="active"><a class="disabled" href="">Foo</a></li>
        <li><a href="about.xhtml">About</a></li>
      </ul>
    </nav>
  </header>
  <main>
    <!-- page content will be placed here -->
  </main>
</body>
</html>
```

#### Navigation Regeneration

The `mummy:regenerate` attribute on a `<ul>` or `<ol>` inside a `<nav>` element tells Guise Mummy to regenerate the list for each page. Guise Mummy infers the active and inactive menu item styles from the template's example items:

* A link with `href=""` (the empty string) marks the **active/self** item template — the style applied to the current page's menu entry.
* The first item with a non-empty (or absent) `href` marks the **inactive** item template — the style applied to all other menu entries.

No expression language is needed to mark menu items as "active" or "disabled". Guise Mummy determines this automatically.

#### Template Link Retargeting

Stylesheet links, script references, and other resource URLs in the template are automatically retargeted when the template is applied to pages in subdirectories. A `<link href="_assets/css/site.css" …/>` in a root template becomes `../_assets/css/site.css` for a page one level deep, and so on.

### Expression Language (Guise Mesh)

For dynamic content beyond template application, Guise Mummy includes [Guise Mesh](../mesh/) — a DOM-based expression language using the `https://guise.dev/name/mesh/` namespace (prefix `mx`). Guise Mesh uses MEXL (Mesh Expression Language), backed by [Apache Commons JEXL 3](https://commons.apache.org/proper/commons-jexl/).

#### Available Variables

| Variable | Content |
|---|---|
| `plan` | The site plan — access to all artifacts and the site structure |
| `artifact` | The current artifact being processed |
| `page` | The current page's metadata description (URF resource description) |

#### Attributes

| Attribute | Purpose |
|---|---|
| `mx:each` | Iterates over a collection, cloning the host element for each item |
| `mx:item-var` | Names the iteration item variable (default: `it`) |
| `mx:index-var` | Names the iteration index variable (default: `i`) |
| `mx:iter-var` | Names the iteration state variable (default: `iter`), exposing `.first`, `.last`, `.current`, `.index` |
| `mx:text` | Replaces the element's text content with the expression result |
| `mx:attr-*name*` | Sets attribute *name* on the element; `false` removes it, `true` sets it as a boolean attribute |

#### String Interpolation

Any non-Mesh attribute value or text node may contain `^{expression}` interpolation markers. For example `<span>Hello, ^{page.author}!</span>` substitutes the page's author metadata.

## Concepts

The following vocabulary is used throughout Guise Mummy's documentation, logging, and APIs. See the [architecture document](architecture.md) for the full treatment.

* **Artifact** — A resource being processed: either a file (page, image, script) or a directory. Every artifact has a source path and a target path.
* **Collection artifact** — An artifact representing a directory. Its path ends in `/` in the URI domain.
* **Content artifact** — The file that provides default content for a directory (typically `index.xhtml` or `index.md`). It is subsumed into its parent directory artifact and does not appear separately in navigation.
* **Mummifier** — The processing strategy for generating an artifact. Mummifiers are registered by filename extension and can be extended.
* **Three trees** — Guise Mummy operates on three parallel directory trees: the **source tree** (`src/site/`), the **target tree** (`target/site/`), and the **description tree** (`target/site-description/`) where artifact metadata sidecars are stored.
* **Lifecycle phases** — Site generation proceeds through six phases in order: **Initialize**, **Validate**, **Plan**, **Mummify**, **Prepare Deploy**, and **Deploy**.

## How Mummification Works

### Lifecycle

When you run `guise mummify` (or any command that includes mummification), Guise Mummy executes a sequence of phases:

1. **Initialize** — Loads project and site configuration, creates the runtime context, and registers mummifiers for each supported file type.
2. **Validate** — Checks preconditions: the source directory exists, source and target directories don't overlap, domain names are well-formed.
3. **Plan** — Walks the source tree recursively, selects a mummifier for each file based on its extension, computes target filenames (applying date extraction, clean-URL renaming, and extension changes), and builds a complete site plan.
4. **Mummify** — Generates the target site, invoking each artifact's mummifier. Pages go through the [page processing pipeline](#page-processing-pipeline); other files are copied or optimized as appropriate.
5. **Prepare Deploy** — Loads deployment configuration and provisions infrastructure (S3 buckets, CloudFront distributions, Route 53 hosted zones).
6. **Deploy** — Uploads content with metadata (content type, fingerprint) and invalidates CDN caches.

### Page Processing Pipeline

For page artifacts (XHTML, Markdown), `AbstractPageMummifier` orchestrates the following pipeline:

1. **Load** — The source file is loaded and parsed into an in-memory XHTML DOM, regardless of its original format. A Markdown file becomes an XHTML DOM just as an `.xhtml` file does.
2. **Normalize** — The DOM is tidied: named `<meta>` elements are removed (they are regenerated later during Ascribe).
3. **Apply Template** — If a `.template.*` file is present in the page's directory or any ancestor directory, it is loaded, its links are relocated to the page's source location, and the page's content is merged into the template's `<main>` (or `<article>`, or `<body>`).
4. **Mesh** — [Guise Mesh](#expression-language-guise-mesh) expressions are evaluated: `mx:each` iteration, `mx:attr-*` attribute mutations, `mx:text` replacements, and `^{…}` string interpolation.
5. **Process** — Registered widgets are dispatched and `mummy:regenerate` navigation lists are rebuilt.
6. **Relocate** — All link references (`href`, `src`, etc.) are retargeted from source paths to target paths. A link to `example.md` becomes `example.html`; relative paths are recalculated for the artifact's target location.
7. **Cleanse** — All Guise Mummy namespace elements and attributes (`mummy:*`, `xmlns:mummy`) are stripped from the output.
8. **Ascribe** — Artifact metadata is written back into the HTML as `<meta>` elements (title, author, generator, generation timestamp).
9. **Save** — The final XHTML DOM is serialized as an HTML5 document.

Not all mummifiers follow this pipeline. A `GenericFileMummifier` (the default fallback) copies unrecognized file types (CSS, JavaScript, fonts, etc.) to the target unchanged, determining media type from the filename extension. `OpaqueFileMummifier` does the same without media type detection. Image mummifiers may perform optimization without DOM processing.

### Design Highlights

* **Format-independent processing.** All page content — whether originally XHTML or Markdown — is normalized to an XHTML5 DOM before template application, expression evaluation, and link processing. Template application and navigation regeneration work identically regardless of the original source format. A new source format only needs to implement the Load step; every subsequent step works automatically.
* **Transparent link normalization.** Each artifact can expose multiple _referent paths_ — alternative source paths that all resolve to the same artifact. A directory exposes both `foo/` and `foo/index.xhtml` (or `foo/index.md`), so any link to `foo/index.xhtml` is normalized to `foo/` for concise, semantic URLs.
* **Portable resource references.** Links within the generated site are always relative — never absolute. The site can be deployed at a server root (`/`) or under any subpath (`/blog/`, `/docs/`) without modification. The absolute-path form appears only at protocol boundaries that require it (such as HTTP redirect headers).

## Configuration

Guise Mummy uses a layered configuration system. Higher-priority sources override lower ones:

1. **CLI options** — Command-line flags override everything.
2. **Site configuration** — A `.guise-mummy.turf` file in the site source directory root. Properties are automatically prefixed with `mummy.`.
3. **Project configuration** — A `guise-project.turf` file in the project directory.
4. **Defaults** — Built-in conventions.

A typical project configuration:

```turf
*GuiseProject:
  mummy:
    page:
      namesBare = true
    ;
  ;
  deploy:
    targets = [
      * S3:
        region = "us-east-1"
        bucket = "example.com"
      ;
    ]
  ;
;
```

### Common Configuration Properties

| Property | Default | Description |
|---|---|---|
| `domain` | — | The project's base fully-qualified domain name |
| `site.domain` | (falls back to `domain`) | The canonical site domain |
| `mummy.page.namesBare` | `false` | Enable clean URLs (strip `.html` extensions) |
| `mummy.templateBaseName` | `.template` | Base name for template files |
| `mummy.collectionContentBaseNames` | `["index"]` | Base names recognized as directory content files |
| `mummy.veilNamePattern` | `_(.*)` | Regex pattern for veiled files (default: underscore prefix) |
| `mummy.assetNamePattern` | `\$(.*)` | Regex pattern for asset files (default: dollar-sign prefix) |
| `mummy.textOutputLineSeparator` | `\n` | Line separator for reproducible output |

See the [architecture document](architecture.md) for the full configuration reference, deployment configuration, and the description/metadata system.

## Migration

### Migrating to 0.6.x from 0.5.x

The Guise domain has moved from `guise.io` to `guise.dev`. This release includes the following breaking changes:

- **Maven coordinates:** The groupId has changed from `io.guise` to `dev.guise`. Update all `<groupId>` references in your POM files accordingly (e.g. `dev.guise:guise-mummy` instead of `io.guise:guise-mummy`).
- **XML namespace URIs:** The Guise Mummy and Guise Mesh XHTML namespace URIs have changed:
  - `https://guise.io/name/mummy/` → `https://guise.dev/name/mummy/`
  - `https://guise.io/name/mesh/` → `https://guise.dev/name/mesh/`

  Update all `xmlns:mummy="…"` and `xmlns:mx="…"` declarations in your `.template.xhtml` and other XHTML content files.

## Issues

Issues tracked by [JIRA](https://globalmentor.atlassian.net/projects/GUISE).
