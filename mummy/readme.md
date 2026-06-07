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
* **Automated deployment.** A single `guise deploy` command builds your site and uploads it to the cloud — for example to [Amazon S3](https://aws.amazon.com/s3/) with [CloudFront](https://aws.amazon.com/cloudfront/) CDN distribution and [Route 53](https://aws.amazon.com/route53/) DNS management — preserving per-page metadata such as content type along the way.
* **Incremental mummification.** Only changed pages are regenerated on subsequent builds, with content fingerprints tracked for each artifact.

## Download

Guise Mummy is available in the Maven Central Repository as [dev.guise:guise-mummy](https://search.maven.org/search?q=g:dev.guise%20AND%20a:guise-mummy). For building the entire Guise ecosystem see the documentation for the parent [Guise Mummy project](../).

## Getting Started

### Project Layout

A Guise Mummy project follows a conventional directory structure:

```
my-site/
├── guise-project.turf        # project configuration (optional)
└── src/
    └── site/                 # source tree — your authored content
        ├── .template.xhtml   # shared page template (optional)
        ├── index.md          # home page
        ├── about.md
        └── images/
```

Source files go under `src/site/` — pages are usually written in Markdown — and the generated site appears under `target/site/`. Only `src/site/` is required; project configuration and templates are optional.

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
* **Veiled files** — A file or directory whose name starts with `_` (underscore) _is_ included in the generated site but hidden from generated navigation menus. The underscore is removed in the output name, so a `_drafts/` directory is published as `drafts/` — reachable in the site, but with no menu item generated for it.
* **Assets** — A file or directory whose name starts with `$` (dollar sign) is included in the site but excluded from _both_ navigation and page processing. Like the veil marker, the `$` is removed in the output name, so a `$assets/` directory is published as `assets/`. The asset designation only suppresses page generation: a file that a page mummifier would process (Markdown, XHTML, HTML) is instead copied as-is, while other mummifiers still apply — a large image in an asset directory may still be scaled, for instance. Assets are the usual home for stylesheets, scripts, and fonts.

### Page Metadata

A page carries metadata that drives its title, navigation entry, ordering, and templating. Set it in YAML front matter (Markdown) or `<meta>` elements (XHTML).

| Property | Purpose |
|---|---|
| `title` | The page title; used for `<title>` and as the default navigation label. |
| `label` | Overrides the navigation label; falls back to `title` when absent. |
| `icon` | An icon reference for the page, used in generated navigation. |
| `mummy:order` | Orders the page among its siblings in computed navigation. |
| `mummy:template` | The template to apply; an empty value opts the page out (see [Templates](#templates)). |

In Markdown, properties appear in the front matter:

```markdown
---
title: About Us
label: About
mummy:order: 20
---
```

In XHTML, simple properties use `<meta name="…">`, while properties in the Mummy namespace use `<meta property="…">`:

```html
<meta name="label" content="About" />
<meta property="mummy:order" content="20" />
```

In XHTML a Mummy-namespace name written in `kebab-case` is converted to `camelCase`, so `<meta property="mummy:alt-location" …>` sets the `mummy/altLocation` property.

### Templates

Place a template file named `.template.xhtml` in any directory to apply it to every page _in that directory and its subdirectories_. The template provides the page skeleton — header, footer, navigation, stylesheets — while each page provides only its content.

Guise Mummy selects the page's content by taking its `<main>` element, or `<article>` if there is no `<main>`, or the whole `<body>` if neither is present. That content is merged into the matching element of the template. **Only the selected element is carried over — anything outside it, such as a page-specific header placed as a sibling of `<main>`, is discarded.** Content must therefore live inside the page's content element to survive templating.

Guise Mummy features are controlled through elements and attributes in the `https://guise.dev/name/mummy/` XHTML namespace.

```html
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:mummy="https://guise.dev/name/mummy/">
<head>
  <meta charset="UTF-8" />
  <title>Example Template</title>
  <link href="$assets/css/site.css" rel="stylesheet" />
</head>
<body>
  <header>
    <nav>
      <ul mummy:regenerate="regenerate">
        <li class="active"><a class="disabled" href="">Foo</a></li>
        <li><a href="about.md">About</a></li>
      </ul>
    </nav>
  </header>
  <main>
    <!-- page content will be placed here -->
  </main>
</body>
</html>
```

#### Opting Out of Templating

A page that already carries its own complete chrome can opt out of templating by declaring an empty template:

```html
<meta property="mummy:template" content="" />
```

An empty value resolves to the page's own source path, which Guise Mummy treats as "this page is its own template" — so no ancestor template is applied and the page is emitted as authored. A page with no `mummy:template` property is templated normally by the nearest ancestor `.template.xhtml`.

#### Template Link Retargeting

Resource URLs in the template — stylesheet links, script and image references — are relative to the template, and Guise Mummy retargets them to each consuming page's location. The template's `href="$assets/css/site.css"` is emitted as `assets/css/site.css` for a root-level page and `../assets/css/site.css` for a page one directory deep, relativized per page (the `$` asset marker is removed in the output, as described under [Internal Links](#internal-links)).

### Navigation

A template's navigation menu is regenerated for each page from the site's structure, so the menu stays current as pages are added or removed.

#### Regenerating a Menu

Mark a `<ul>` or `<ol>` inside a `<nav>` with `mummy:regenerate="regenerate"` and provide one or two example items; Guise Mummy rebuilds the list for each page, inferring the styles from the examples:

* A link with `href=""` (the empty string) marks the **active/self** item — the style applied to the current page's own entry.
* The first item with a non-empty (or absent) `href` marks the **inactive** item — the style applied to every other entry.

No expression language is needed to mark items active or inactive; Guise Mummy determines this automatically. If menu items carry icons, place an empty `<i></i>` inside an item's link to mark where the icon goes — Guise Mummy fills it with the linked page's `icon`, or removes it when the page has none.

#### Defining Navigation

By default the regenerated menu is computed for each page from its own section — the parent collection and the sibling pages at that level, ordered by the `mummy:order` property and then by name. To control a menu explicitly, place a `.navigation.lst` file in a directory, naming one artifact per line in menu order:

```
./
about/
products/
contact.md
```

A navigation file **replaces** the computed menu entirely, governing both order and membership: an artifact omitted from the file is left out of the menu, though it remains reachable by link. Navigation is resolved per page from the nearest navigation file found searching upward to the site root, so a single `.navigation.lst` at `src/site/` governs the whole site, while one in a subdirectory overrides it for that subtree. A richer `.navigation.turf` form (which takes precedence when both exist) allows external links, custom labels, and icons; a `.navigation+.lst` (note the `+`) _appends_ to the inherited menu rather than replacing it.

After adding, moving, or editing a navigation file, rebuild fully (`guise mummify --full`): incremental builds do not always detect that a changed navigation file has left other pages' menus stale.

### Internal Links

Write links between pages as references to the **source** file, including its extension — `about.md`, `../products/index.xhtml`, `images/photo.jpg`. Guise Mummy resolves each relative reference to its source artifact and rewrites it to that artifact's output URL, relative to the page being generated, applying clean-URL stripping and other renaming automatically. You never write the output form yourself, and a link keeps working wherever a shared template places it in the hierarchy.

This retargeting applies to **relative** references only. Root-absolute references (`/about/`) and full URLs (`https://…`) pass through untouched — they are neither resolved against the source tree nor validated — so relative references are preferred for internal links. A relative reference that matches no source artifact is left unchanged and logged as a warning. See the [architecture document](architecture.md) for the underlying source-to-target model.

### Redirects

To make an alternate location redirect to a page, declare the `mummy/altLocation` property on the page itself — the canonical target — as a reference relative to that page. In XHTML:

```html
<meta property="mummy:alt-location" content="../old-name" />
```

or in Markdown front matter:

```markdown
mummy:altLocation: ../old-name
```

At deployment, the configured deploy target generates an HTTP redirect from the alternate location to the page. Redirects materialize at deploy time only; a local build has nothing to redirect.

### Blogs

Guise Mummy has lightweight support for date-based posts. A source file whose name begins with a date — `@YYYY-MM-DD-slug.md` — is recognized as a post and generates a dated output path (`YYYY/MM/DD/slug`). To present a series of posts as summaries in reverse-chronological order, add a directory widget to the listing page:

```html
<mummy:directory archetype="blog" />
```

The widget lists the posts in its directory; the `blog` archetype sorts them newest-first. No scaffolding is needed in advance — the listing fills in as posts are added.

### Expression Language (Guise Mesh)

For dynamic content beyond template application, Guise Mummy includes [Guise Mesh](../mesh/) — a DOM-based expression language using the `https://guise.dev/name/mesh/` namespace (prefix `mx`). Guise Mesh uses MEXL (Mesh Expression Language), backed by [Apache Commons JEXL 3](https://commons.apache.org/proper/commons-jexl/).

#### Available Variables

| Variable | Content |
|---|---|
| `plan` | The site plan — access to all artifacts and the site structure |
| `artifact` | The current artifact being processed |
| `page` | The current page's metadata description (URF resource description) |

#### Attributes

Guise Mesh transforms a page through `mx:`-namespaced attributes — `mx:each` to repeat an element for each item of a collection, `mx:text` to replace an element's text from an expression, `mx:attr-*` to set attributes, and `mx:content-as` to control whether an element's text is interpolated or taken literally. Because these are ordinary XML attributes, the template stays valid XHTML. For example, to repeat a list item for each product:

```html
<li mx:each="products"><a href="^{it.url}" mx:text="it.name">Product</a></li>
```

See the [Guise Mesh documentation](../mesh/) for the full attribute reference.

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

Each page — whether authored as XHTML or Markdown — is first parsed into an in-memory XHTML DOM, then carried through an ordered sequence: the ancestor template is applied, [Guise Mesh](#expression-language-guise-mesh) expressions are evaluated, `mummy:regenerate` navigation menus are rebuilt, links are retargeted from source to output paths, Guise Mummy markup is stripped, metadata is written back as `<meta>` elements, and the result is serialized as HTML5. The order matters: templating runs before link retargeting, which is why a link authored in a template resolves correctly for every page that uses it. The [architecture document](architecture.md) describes each step in full.

Not every file is a page. Stylesheets, scripts, fonts, and other files are copied to the target unchanged (with their media type detected from the extension), and images may be optimized — none of these go through the DOM pipeline above.

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

## Deployment

Deployment targets are configured in the `deploy` section of `guise-project.turf`, and `guise deploy` builds the site and pushes it to each configured target, preserving per-page metadata such as content type. Guise Mummy can deploy to [Amazon S3](https://aws.amazon.com/s3/) — as a plain bucket or an S3 static website — front it with a [CloudFront](https://aws.amazon.com/cloudfront/) distribution, and manage DNS with [Route 53](https://aws.amazon.com/route53/). It can also deploy to a [Flange](https://flange.dev/)-managed environment, which provisions an S3 and CloudFront site with built-in redirect support.

See the [architecture document](architecture.md) for the deployment lifecycle and target configuration.

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
