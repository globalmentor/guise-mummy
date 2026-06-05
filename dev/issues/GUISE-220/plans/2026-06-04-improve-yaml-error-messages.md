# [GUISE-220] Plan: Improve YAML Front Matter Error Messages

## Overview

This plan is monolithic; the work was not decomposable into independent chunks.

- Step 1: Wrap `YamlEngineException` in `loadSourceMetadata()` with context
- Step 2: Add test for the error path

## Step 1: Wrap `YamlEngineException` in `loadSourceMetadata()`

**File:** `mummy/src/main/java/dev/guise/mummy/mummify/page/MarkdownPageMummifier.java`

Add `import org.snakeyaml.engine.v2.exceptions.YamlEngineException;` to the imports (the current wildcard `org.snakeyaml.engine.v2.api.*` does not cover the `exceptions` package).

In `loadSourceMetadata()`, immediately after the null-YAML early return, compute the 1-based file line on which the YAML content begins:

```java
final int yamlStartLine = (int)content.substring(0, matcher.start(MARKDOWN_WITH_YAML_PATTERN_YAML_GROUP)).chars()
        .filter(c -> c == '\n').count() + 1;
```

Then wrap `new Load(LoadSettings.builder().build()).loadFromString(yaml)` in a try/catch:

```java
final Object object;
try {
    object = new Load(LoadSettings.builder().build()).loadFromString(yaml);
} catch(final YamlEngineException yamlEngineException) {
    throw new IOException("Invalid YAML front matter in `%s` (line numbers are relative to line %d): %s"
            .formatted(name, yamlStartLine, yamlEngineException.getLocalizedMessage()), yamlEngineException);
}
```

Under the current pattern the YAML group always starts on line 2 (the opening `---\n` occupies line 1). Computing `yamlStartLine` from the matcher rather than hardcoding it keeps the offset correct if the pattern is revised.

The original `YamlEngineException` is preserved as the cause, so the full SnakeYAML-formatted diagnostic (including its own `in reader, line N, column M:` output) is still accessible via `getCause()`.

## Step 2: Add Test for the Error Path

**File:** `mummy/src/test/java/dev/guise/mummy/mummify/page/MarkdownPageMummifierTest.java`

Add `import org.snakeyaml.engine.v2.exceptions.YamlEngineException;`.

Add a test that calls `loadSourceMetadata()` with inline content whose YAML front matter is syntactically invalid. The behavioral contract under test is that a YAML error is surfaced as an `IOException` whose cause is the original `YamlEngineException`:

```java
/// Tests that a YAML syntax error in front matter is wrapped as an [IOException]
/// with the original [YamlEngineException] preserved as the cause.
/// @see MarkdownPageMummifier#loadSourceMetadata(MummyContext, InputStream, String)
@Test
public void testLoadSourceMetadataInvalidYamlWrapsException() throws IOException {
    final MarkdownPageMummifier mummifier = new MarkdownPageMummifier();
    final String markdown = "---\nfoo: [unclosed\n---\n# Heading\n";
    try (final InputStream inputStream = new ByteArrayInputStream(markdown.getBytes(UTF_8))) {
        final IOException thrown = assertThrows(IOException.class, () ->
                mummifier.loadSourceMetadata(mummyContext, inputStream, "test.md"));
        assertThat(thrown.getCause(), instanceOf(YamlEngineException.class));
    }
}
```

The message text is not asserted: the message format is a diagnostic detail, not an API contract, and the instructions prohibit asserting human-readable error message text. The cause type check is the meaningful behavioral assertion.
