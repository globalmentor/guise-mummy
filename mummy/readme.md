# Guise Mummy

Guise™ Mummy static site generator library and CLI application.

## Overview

Guise Mummy takes a set of source files of various types and generates a static site, analogous to [Jekyll](https://jekyllrb.com/) or [Hugo](https://gohugo.io/). Guise Mummy is extremely fast and cross-platform, as it is based upon the Java Version Machine (JVM). It is standards-based and designed in a way to be flexible and extensible.

### Features

Here are a few of its features, which illustrate how Guise Mummy simply does what you would expect it to do, and does it right.

* An emphasis on [“convention over configuration"](https://en.wikipedia.org/wiki/Convention_over_configuration), with inspiration from [Apache Maven](https://maven.apache.org/). Put your source files in `src/site`, and your site will be generated in `target/site` with no need for configuraiton—unless you want to.
* A normalized view of your site. No more deciding whether to link to `foo/bar/index.html` or to `foo/bar/`; Guise Mummy will normalize all your links to `foo/bar/` (unless you configure otherwise).
* Example-based navigation regeneration. Why create complicated expression-language logic (unless you want to) just to create a navigation menu in your template? Just provide an example menu in the template and Guise Mummy will regenerate the menu for each page, using the correct page names and menu styles, just by looking at your example.
* Fast: your site is generated in parallel using multiple processor cores. (_Upcoming feature._)
* Semantic: change a page property just by updating an HTML or Markdown header in the page source itself. Guise Mummy uses a simple but extensive semantic-based metadata framework, the [Uniform Resource Framework (URF)](https://urf.io/), throughout.
* Complete link relativization. Just put a link you want in the template; Guise Mummy will update the link to still point to the same resource wherever the template is used in your hierarchy.
* An option to generate [clean URLs](https://en.wikipedia.org/wiki/Clean_URL) with no file extensions, such as `products/mousetrap` instead of `products/mousetrap.html`. (_Upcoming feature._) Nevertheless file type such as `text/html` will be maintained, whether for local serving via the CLI or for deploying to a remote hosting service.
* Automated deployment. (_Upcoming feature._) With a simple CLI command you can upload your entire site to a hosting service, such as [Amazon S3](https://aws.amazon.com/s3/). Such deployment will transfer appropriate per-page metadata such as Internet media type, even if clean URL generation was chosen.

### v0.1.0

The initial release of Guise Mummy is completely functional, although the options it supports is not yet complete. Nevertheless it is fully capable of generating a full site with basic capabilities. Its current limitations include:

* Source files must be [XHTML5](https://www.w3.org/TR/html52/introduction.html#html-vs-xhtml) (XML files using the HTML5 vocabulary, not necessarily following any legacy XHTML) using the `.xhtml` extension. _Markdown and plain HTML5 coming soon._
* Configuration files are not yet implemented.
* No expression language or interpolation is yet included.

## Download

Guise Mummy is available in the Maven Central Repository as [io.guise:guise-mummy](https://search.maven.org/search?q=g:io.guise%20AND%20a:guise-mummy). For building the entire Guise ecosystem see the documentation for the parent [Guise project](../).

## Primer

### CLI

Guise Mummy may be used programmatically, but is most easily used by consumers via the [Command-Line Interface (CLI)](../cli/). The following is a very quick overview of the basic CLI commands. Use `guise help` for more details and options.

* `guise clean`
: Deletes everything in the `site/target` directory, relative to the current directory.
* `guise mummify`
: Uses the source files in `src/site` directory and generates a static site in the `target/site` directory.
* `guise serve`
: Starts an HTTP server listening on port `4040` and opens the default browser to test the generated site.

### Source Files

Source files currently must be [XHTML5](https://www.w3.org/TR/html52/introduction.html#html-vs-xhtml) using the `.xhtml` extension. A sample source file may be found in the [Hello World Demo](../demo-hello-world/).

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
* Any directory or file starting with `.` (a [“dotfile”](https://wiki.archlinux.org/index.php/Dotfiles)) is not included in the generated static site, although it may be used to influence site generation. For example a `.template.xhtml` template file will be used as a template (see below), but that template file itself will not appear in the generated site.
* Any directory or file starting with an underscore `_` is considered a “veiled” resource; it _will_ be included in the generated static site, but will not appear in any generated navigation menu. For example an `_assets` directory (containing perhaps `_assets/css/…` and `_assets/js/…`) will still appear in the static site with its contents, but if a template contained a menu for regeneration, the generated menu would _not_ include any sort of “assets” menu item.

### Templates

If you place a template file named `.template.xhtml` in any directory, the outline and style of that template will be used to generate each artifact _in that directory and directories below it_, placing the artifact's title, metadata, and content inside the template. Guise Mummy oriented features are indicated by elements and attributes in the `https://guise.io/name/mummy/` XHTML namespace.

Here's an example of a template that uses the Guise Skeleton CSS framework (_for example only; not yet included in the Guise distribution_) for styling and [Font Awesome](https://fontawesome.com/) for icons; and indicates that the menu should be regenerated automatically using the `mummy:regenerate` attribute. The stylesheet link will be retargeted as appropriate if the template is applied to an artifact in a subdirectory. The navigation menu will be regenerated for each artifact, based upon the other content artifacts and directories (that are not veiled; see above) at each level.

```html
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:mummy="https://guise.io/name/mummy/">
<head>
  <meta charset="UTF-8" />
  <title>Example Template</title>
  <link href="_assets/css/guise-skeleton.min.css" rel="stylesheet" />
  <link href="_assets/css/fontawesome-all.min.css" rel="stylesheet" />
</head>
<body class="guise-skeleton">
  <header>
    <nav class="bar box">
      <a class="concise" href="./">Acme Company</a>
      <input class="toggle" type="checkbox" id="nav-toggle" role="button" />
      <ul class="menu" mummy:regenerate="regenerate">
        <li class="active"><a class="disabled" href="">Foo</a></li>
        <li><a href="about.xhtml">About</a></li>
      </ul>
      <label for="nav-toggle" class="toggle concise"><span class="fas fa-bars"></span></label>
    </nav>
  </header>
<main>
  <!-- artifact content will be placed here -->
</main>
</body>
</html>
```

There is no need for an expression language to mark certain menu items as "active" or "disabled" (although an expression language will be provided in the future for complex tasks). Guise Mummy determines the style for the current active menu item based upon the template menu item with a link to `""` (indicate the artifact “self”). Other menu items will be given styles based upon the first non-self menu item in the template.

## Architecture

### Domain Model

The following are the mail entities in the Guise Mummy domain model

* **Guise Mummy** (class `io.guise.mummy.GuiseMummy`)
: The central class responsible for orchestrating static site generation, referred to as “mummification”.
* **Artifact** (interface `io.guise.mummy.Artifact`)
: Represents a file or directory generated in the static site. _Note: Not all artifacts will have a true representation in the source tree. Some artifacts may be created from a separate list of blog entries, and “phantom” artifacts don't exist in source form at all. The latter include `index.html` files generated in order to create content for directories. Nevertheless, artifacts expose a hypothetical source file during processing, so that links will be correctly processed if any source files link to them in the source tree._ Each artifact has a _description_, a set of properties and values that describe it, using the [Uniform Resource Framework (URF)](https://urf.io/).
* **Mummifier** (interface `io.guise.mummy.Mummifier`)
: The strategy for generating an artifact in the target tree. New strategies can be registered, normally based on source file type. For example, in the future a [LaTeX](https://www.latex-project.org/) mummifier might be associated with the `.tex` source extension. Guise Mummy would automatically delegate to this mummifier to generate static HTML files whenever `*.tex` file were encountered.

### Life Cycle

“Mummification”, or generation of a static site, occurs in several phases:

1. **Validate**
: The configuration and parameters are validated.
2. **Plan**
: Guise Mummy _discovers_ resources in the source tree, and _plans_ mummification by i) choosing the correct mummifier, ii) determining a description for the artifact, and iii) creating an artifact to become part of the mummification plan. _Note that each mummifier controls the process of planning its contents, allowing for custom mummification based upon artifact type. Thus a special calendar directory might have a calendar mummifier that generates a calendar page based upon iCalendar files within that directory, rather than creating HTML pages for the child `*.ics` files._
3. **Mummify**
: Guise Mummy initiates site mummification of the planned site based upon the plan, using the selected mummifiers to generate the artifacts.

The mummification phase can be divided into subphases for generation of a typical HTML file. _Pages are generated by instances of `io.guise.mummy.PageMummifier`. The `io.guise.mummy.AbstractPageMummifier` is typically the base implementation for all page mummifiers. XHTML source files are generated using `io.guise.mummy.XhtmlPageMummifier`._

1. **Load**
: The source file of whatever type is loaded (or created/generated) and normalized as an in-memory XHTML DOM tree (regardless of course format) for further processing.
2. **Apply Template**
: If any `.template.*` file is present in the given directory or any parent directory, the template is applied to the file. This includes i) loading the template as if it were a source artifact, using the appropriate mummifier ii) relocating its links for its new location, iii) substituting the artifact title into the template, iv) substituting the artifact metadata into the template, and v) extracting the main content from the artifact and inserting it into the template. _The template can be a standard `.template.xhtml` file for example; its `<main>` or `<body>` content will be used as a target for the `<main>` or `<body>` content of the artifact. Support for custom content and insertion points will be included in a future release._
3. **Process**
: The normalized XHTML DOM tree is evaluated and transformed appropriately. This includes regenerating sections such as navigation menus, evaluating expressions (_upcoming feature_), and intepolating properties (_upcoming feature_).
4. **Relocate**
: The artifact's links are converted to reflect its new location in the generated site. This includes moving links to their new location, as well as changing the links based upon their target artifacts. For example a source link to `example.tex` would be converted to `example.html`, if a LaTeX mummifier were installed that actually converted the referenced `example.tex` to HTML in the target tree.
5. **Cleanse**
: All Guise Mummy related content (such as attributes or metadata used to direct mummification) is removed from the processed XHTML tree
6. **Save**
: An HTML file is generated from the resulting in-memory XHTML DOM representation.

Note that some mummifiers do not follow all these subphases. For example an `io.guise.mummy.OpaqueFileMummifier` will simply copy the source file unaltered for unrecognized source files. Currently this includes images, CSS, and JavaScript files. (As Guise Mummy grows in sophistication, however, even some of these artifacts may have specialized mummifiers which perform some manipulation if so configured.)

### Design Features

* Normalized artifact processing model. Artifacts are typically normalized to an XHTML5 DOM in-memory representation, regardless of original format. This provides for standardized processing and extensibility. For example template application and menu regeneration doesn't need to know if the content originated as an XHTML file or a Markdown file.
* Each artifact can expose "referent" path names for link normalization. For example by default a directory will expose both a path to `foo/` and a path to`foo/index.xhtml` (based upon the whichever so-called “content artifact” is present to be used  for default content). This way any link to `foo/index.xhtml` can be normalized to `foo/` for concise, semantic links.

## Implementation Notes

* Java's `java.net.URI` relativation algorithm does not support “backtracking”; that is, it can't generare relative links from `example/foo.html` to `example/bar.html`, which would require a backtracking `../bar.html` link. See [JDK-6226081](https://bugs.java.com/bugdatabase/view_bug.do?bug_id=6226081) and [_How to construct a relative path in Java from two absolute paths (or URLs)?_](https://stackoverflow.com/q/204784/421049) The solution is not as trivial, but GlobalMentor implemented a general URI relativization algorithm for use in Guise Mummy, tracked by [JAVA-102](https://globalmentor.atlassian.net/browse/JAVA-102).
* Java's `java.net.URI` class furthermore follows [RFC 2396](https://tools.ietf.org/html/rfc2396) instead of the updated [RFC 3986](https://tools.ietf.org/html/rfc3986). One downside is that its handling of relative URIs conflicts with that of modern browsers and HTML5. For example if a link appears in `foo/bar.html` referencing `""` (the empty string), the referent resource should be the `foo/bar.html` resource itself. Java, however, resolves the target link to the `foo/` parent resource, as [discussed on Stack Overflow](https://stackoverflow.com/a/27644491/421049). Guise Mummy works around this limitation as well.

## Issues

Issues tracked by [JIRA](https://globalmentor.atlassian.net/projects/GUISE).
