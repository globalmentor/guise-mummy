# Guise Mesh

Guise™ Mesh is a DOM-based template transformation engine for XML and HTML documents. It evaluates expressions, interpolates text, and transforms elements using attributes in the `mx:` namespace (`https://guise.dev/name/mesh/`). The engine processes a W3C DOM tree in place, expanding iterations, replacing text, mutating attributes, and interpolating embedded expressions.

## Relationship to Guise Mummy

Guise Mesh is published as a standalone module (`dev.guise:guise-mesh`) and does not depend on Guise Mummy. In Guise Mummy, the page mummifier sets up a `MeshContext` with variables such as `page` (the page's URF resource description), `artifact` (the artifact being generated), and `plan` (the site plan), then calls `GuiseMesh.meshDocument()` to process the page template. But Guise Mesh has no knowledge of these types — it operates purely on the DOM and whatever variables the caller provides.

Any application that works with XML or HTML DOM trees can use Guise Mesh as a lightweight template engine by adding the `guise-mesh` dependency and calling the API directly.

## Architecture

The engine is composed of three pluggable layers:

- **Expression evaluation** — The `MexlEvaluator` interface defines the strategy for evaluating Mesh Expression Language (MEXL) expressions against a `MeshContext`. The default implementation, `JexlMexlEvaluator`, delegates to [Apache Commons JEXL](https://commons.apache.org/proper/commons-jexl/) with custom support for URF resource description property access. A different expression engine can be substituted by implementing `MexlEvaluator`.
- **Interpolation** — The `MeshInterpolator` interface handles embedded expression replacement within text. The default implementation, `DefaultMeshInterpolator`, uses the delimiter syntax `^{expression}` and supports multiple expressions per string.
- **Transformation** — The `GuiseMesh` class orchestrates the evaluation and interpolation layers to process `mx:` attributes on DOM elements.

## Template Attributes

| Attribute | Purpose | Example |
|---|---|---|
| `mx:each` | Iterates over a collection, cloning the element for each item. | `<li mx:each="items">` |
| `mx:item-var` | Names the iteration item variable (default: `it`). | `<li mx:each="items" mx:item-var="product">` |
| `mx:index-var` | Names the iteration index variable (default: `i`). | `mx:index-var="idx"` |
| `mx:iter-var` | Names the iteration state variable (default: `iter`), a `MeshIterator` with `current`, `index`, `first`, and `last` properties. | `mx:iter-var="loop"` |
| `mx:text` | Replaces the element's text content with the expression result. | `<h1 mx:text="page.title">Placeholder</h1>` |
| `mx:attr-*` | Sets or removes a non-namespaced attribute. `true` sets the attribute to its own name (HTML boolean attribute idiom); `false` or empty removes it; other values set the attribute text. | `<input mx:attr-disabled="isLocked"/>` |

Text and attribute values also support inline interpolation using `^{expression}` syntax, e.g. `href="products/^{product.slug}"`.

## Getting Started

### Dependency

Add the Maven dependency:

```xml
<dependency>
  <groupId>dev.guise</groupId>
  <artifactId>guise-mesh</artifactId>
  <version>0.6.0-SNAPSHOT</version>
</dependency>
```

### Basic Usage

1. **Create a context** with the variables your template will reference:

   ```java
   MeshContext context = MeshContext.create(Map.of(
       "title", "Hello World",
       "items", List.of("alpha", "beta", "gamma")));
   ```

2. **Parse your template** into a W3C DOM `Document` (using any standard XML/HTML parser).

3. **Mesh the document:**

   ```java
   new GuiseMesh().meshDocument(context, document);
   ```

   The document is transformed in place.

### Expression Permissions

By default, MEXL expressions can access JDK types (e.g. `Map`, `String`, `List`) and URF resource description properties. If your templates reference application-specific types injected into the context, you must declare them when constructing `GuiseMesh`:

```java
GuiseMesh mesh = new GuiseMesh(
    Set.of(Product.class, Category.class), // classes accessible by exact type
    Set.of());                             // packages accessible with sub-packages
```

Classes are matched exactly; packages permit all classes within them (including sub-packages). Both mechanisms can be combined. URF resource descriptions always work without additional permissions because they are resolved through a custom property resolver.

The primary entry points are `GuiseMesh.meshDocument()` for full-document processing and `GuiseMesh.meshElement()` for processing a subtree. The `MeshContext` supports nested scopes via `nestScope()`, which is used internally during iteration and can also be used by callers to provide block-scoped variables.

## Download

Guise Mesh is available in the Maven Central Repository as [dev.guise:guise-mesh](https://search.maven.org/search?q=g:dev.guise%20AND%20a:guise-mesh).

## Issues

Issues tracked by [JIRA](https://globalmentor.atlassian.net/projects/GUISE).

