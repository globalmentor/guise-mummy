# Plan: Consolidate Collection Content Resource Name Derivation

Move `deriveCollectionContentResourceName()` from `FlangeWebSite` to `GuiseMummy`, where the configuration keys it interprets are defined. Update all consumer sites to use the consolidated method. Fix the `GuiseCli` bug that registers all configured base names as Tomcat welcome files instead of only the normalized one.

## Overview

This plan is a single chunk — all steps are additive or modify only internal call sites within one compilation unit boundary (the `guise-mummy` multi-module project). Every intermediate step compiles.

- Step 1: Add `findCollectionContentResourceName()` to `GuiseMummy` with API docs.
- Step 2: Add tests in `GuiseMummyTest`.
- Step 3: Update `FlangeWebSite` — delegate to `GuiseMummy`, remove local method.
- Step 4: Update `FlangeWebSiteTest` — redirect tests to `GuiseMummy`.
- Step 5: Update `S3Website` — replace inline derivation with call to `GuiseMummy`.
- Step 6: Update `GuiseCli` — replace multi-name stream with single call (bug fix).

**Not in scope:** The two `DirectoryMummifier` production sites (lines 130 and 144) are *producers* — they normalize content filenames during planning via `planArtifactTargetFilename()`, a different operation from the post-planning *derivation* that consumers perform. They remain unchanged.

---

## Naming

### Considered alternatives

- **`deriveCollectionContentResourceName()`** — the current name. "Derive" accurately describes the operation (composing two config keys), but the existing `GuiseMummy` methods follow a `find*` naming convention for `Optional`-returning methods.
- **`findConfiguredCollectionContentResourceName()`** — matches `findConfiguredDomain()`, `findConfiguredSiteDomain()`, etc. But those methods directly retrieve and validate a single config value; this one *composes* two config values (`collectionContentBaseNames` + `page.namesBare`) into a derived result. "Configured" slightly mischaracterizes the operation.
- **`findCollectionContentResourceName()`** — uses `find` for the `Optional` return (per project convention), drops "Configured" to signal that this is a derivation from configuration rather than a direct lookup. "Collection content resource name" precisely describes the result.

**Chosen:** `findCollectionContentResourceName(Configuration)`. The `find` prefix is consistent with the other `Optional`-returning methods on `GuiseMummy`, and dropping "Configured" distinguishes it from direct lookups.

---

## Step 1: Add `findCollectionContentResourceName()` to `GuiseMummy`

### Location

`mummy/src/main/java/dev/guise/mummy/GuiseMummy.java`, in the `//## mummy configuration` section, immediately after `CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES` (line 198).

### Implementation

```java
/// Determines the normalized collection content resource name from the mummification configuration.
///
/// During mummification, collection (directory) content files are normalized to use the first entry from
/// [#CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES] as the base name, with the page filename extension
/// [PageMummifier#PAGE_FILENAME_EXTENSION] appended unless bare names are enabled via
/// [PageMummifier#CONFIG_KEY_MUMMY_PAGE_NAMES_BARE]. This method derives that normalized filename from
/// the configuration without consulting the artifact tree.
///
/// @apiNote This is used by deploy targets and serving infrastructure to configure their "index document"
///          or "welcome file" settings. The value reflects a contract between mummification (which produces
///          collection content files with this name) and deployment (which must tell the serving
///          infrastructure what filename to expect).
/// @param configuration The mummification configuration.
/// @return The collection content resource name (e.g. `"index.html"` or `"index"`), or empty if no
///         collection content base names are configured.
/// @see #CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES
/// @see PageMummifier#CONFIG_KEY_MUMMY_PAGE_NAMES_BARE
/// @see PageMummifier#PAGE_FILENAME_EXTENSION
public static Optional<String> findCollectionContentResourceName(final Configuration configuration) {
    final Collection<String> collectionContentBaseNames = configuration.getCollection(
            CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES, String.class);
    if(collectionContentBaseNames.isEmpty()) {
        return Optional.empty();
    }
    final String baseName = collectionContentBaseNames.iterator().next();
    final boolean isNameBare = configuration.findBoolean(
            PageMummifier.CONFIG_KEY_MUMMY_PAGE_NAMES_BARE).orElse(false);
    return Optional.of(isNameBare ? baseName : addExtension(baseName, PageMummifier.PAGE_FILENAME_EXTENSION));
}
```

Requires adding `import static com.globalmentor.io.Filenames.*` if not already present. Verify `PageMummifier` is already imported or accessible.

---

## Step 2: Add tests in `GuiseMummyTest`

### Location

`mummy/src/test/java/dev/guise/mummy/GuiseMummyTest.java`, at end of file before closing brace.

### Tests

Move the four assertions from `FlangeWebSiteTest.testDeriveCollectionContentResourceName()`, retargeting them to `GuiseMummy.findCollectionContentResourceName()`. These cover:

1. Default index with HTML extension (`"index"` → `"index.html"`)
2. Bare names (`"index"` + bare=true → `"index"`)
3. Custom base name (`"default"` → `"default.html"`)
4. Empty config → `Optional.empty()`

Group these in a single test method with descriptive assertion reason strings, under a `//## mummy collection content` section heading.

---

## Step 3: Update `FlangeWebSite`

### Changes

Remove the `deriveCollectionContentResourceName()` method entirely (lines 187–204, including the `//## collection content resource name` section comment).

The call site in `FlangeWebSite.deploy()` (Chunk 2, not yet implemented) will call `GuiseMummy.findCollectionContentResourceName(configuration)` instead — already available via `import static dev.guise.mummy.GuiseMummy.*`.

---

## Step 4: Update `FlangeWebSiteTest`

### Changes

Remove the `testDeriveCollectionContentResourceName()` method and its section heading (lines 237–252). The tests now live in `GuiseMummyTest`.

---

## Step 5: Update `S3Website`

### Location

`mummy/src/main/java/dev/guise/mummy/deploy/aws/S3Website.java`, lines 393–400.

### Change

Replace the inline derivation:

```java
//set the index document, if any, based upon the collection content base name
final Collection<String> collectionContentBaseNames = configuration.getCollection(CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES, String.class);
if(!collectionContentBaseNames.isEmpty()) {
    final String indexDocumentBaseName = collectionContentBaseNames.iterator().next(); //e.g. "index" (mummification should have normalized to use the first one)
    final boolean isNameBare = configuration.findBoolean(PageMummifier.CONFIG_KEY_MUMMY_PAGE_NAMES_BARE).orElse(false);
    final String indexDocumentSuffix = isNameBare ? indexDocumentBaseName : addExtension(indexDocumentBaseName, PAGE_FILENAME_EXTENSION);
    final IndexDocument indexDocument = IndexDocument.builder().suffix(indexDocumentSuffix).build();
    websiteConfigurationBuilder.indexDocument(indexDocument);
}
```

With:

```java
//set the index document, if any, based upon the collection content resource name
findCollectionContentResourceName(configuration).ifPresent(indexDocumentSuffix -> {
    websiteConfigurationBuilder.indexDocument(IndexDocument.builder().suffix(indexDocumentSuffix).build());
});
```

Already has `import static dev.guise.mummy.GuiseMummy.*`.

---

## Step 6: Update `GuiseCli` (bug fix)

### Location

`cli/src/main/java/dev/guise/cli/GuiseCli.java`, lines 392–396.

### Bug

The current code registers *all* configured base names (e.g. `"index"`, `"default"`) as Tomcat welcome files, applying the `.html` extension to each. But Tomcat serves from the *target* directory where mummification has already normalized all collection content to use the *first* base name. The additional welcome files will never match anything.

### Change

Replace:

```java
//set up the collection content filenames (i.e "welcome files") such as `index`/`index.html`
final boolean isPageNameBare = projectConfiguration.findBoolean(PageMummifier.CONFIG_KEY_MUMMY_PAGE_NAMES_BARE).orElse(false);
projectConfiguration.getCollection(CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES, String.class).stream()
        .map(baseName -> isPageNameBare ? baseName : addExtension(baseName, PageMummifier.PAGE_FILENAME_EXTENSION)) //e.g. "index" or "index.html"
        .forEach(context::addWelcomeFile);
```

With:

```java
//set the collection content welcome file (e.g. `index.html` or `index` for bare names)
findCollectionContentResourceName(projectConfiguration).ifPresent(context::addWelcomeFile);
```

Already has `import static dev.guise.mummy.GuiseMummy.*`. The `PageMummifier` import may become unused if this was its only use site; verify and remove if so.
