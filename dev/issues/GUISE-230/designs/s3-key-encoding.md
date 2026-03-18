# S3 Object Key Encoding: Guise Mummy vs. Flange

This document definitively answers the question: **what form do S3 object keys take when produced by Guise Mummy's `S3`/`S3Website` deployers and Flange's `S3Synchronizer`?** The answer was established through empirical investigation (14 tests on OpenJDK 25 / Windows, since removed); the fix is validated by `S3WebsitePlanIT`.

## Executive Summary

| System | S3 key for file `café/index` | S3 key for `my file.html` | S3 key for `section#2.html` |
|---|---|---|---|
| **Guise Mummy** (before fix) | `café/index` ✓ | `my%20file.html` ✗ | `section%232.html` ✗ |
| **Guise Mummy** (after fix) | `café/index` ✓ | `my file.html` ✓ | `section#2.html` ✓ |
| **Flange** | `café/index` ✓ | `my file.html` ✓ | `section#2.html` ✓ |

**Non-ASCII characters**: Both systems produce **identical** decoded Unicode keys. There is **no encoding divergence** for non-ASCII filenames. This was verified for Latin (`café`) and CJK (`東京`, Japanese: "Eastern Capital").

**URI-significant characters** (spaces, `#`, `?`, `%`): Guise Mummy's pre-fix code derived the S3 key via `URIPath.toString()`, which returns `URI.getRawPath()` — the inconsistently encoded form that `Path.toUri()` produces. This leaked percent-encoding artifacts into S3 keys for these characters (the "before fix" row above). The fix (now applied) uses `toDecodedString()` instead, which returns `URI.getPath()` — the fully decoded canonical filesystem name (the "after fix" row). See §The Central Deviation and Fix.

**The previous claim** in the architecture document and minutes that Guise Mummy produces percent-encoded S3 keys (e.g., `caf%C3%A9`) **was incorrect**. Both systems produce decoded Unicode keys for non-ASCII characters.

## S3 Key Identity Principle

An S3 object key is an **opaque canonical name**, not a URI path. The string passed to `PutObjectRequest.builder().key(...)` is stored verbatim as the object's key in S3. The AWS SDK handles HTTP-level percent-encoding transparently as a transport concern: the SDK encodes the key for the HTTP request path, S3 decodes the HTTP request path to recover the original key, and the stored key is identical to the string the caller provided. This round-trip is invisible to the caller.

Therefore the correct value to pass to `PutObject` is the **canonical resource name** — the actual characters that identify the resource. For a file named `café.html` the key is `café.html`; for a file named `my file.html` the key is `my file.html` (with a literal space). Passing a URI-encoded form like `caf%C3%A9.html` or `my%20file.html` would store an object whose key literally contains percent characters — a different object from the one the canonical name identifies.

## Guise Mummy Key Derivation Pipeline

Before the fix, the S3 key was derived in `S3.plan()` via:

```java
final String s3Key = Artifact.relativizeResourceReference(rootTargetPathUri, contentArtifact).toString();
```

This one line conceals a multi-step transformation. Here is each step, traced empirically for a file at `target/site/café/index`:

### Step 1: `rootArtifact.getTargetPath().toUri()` — the root base URI

```
Input:  Path C:\...\target\site\
Output: URI  file:///C:/.../target/site/   (ASCII-only, no encoding issues)
```

The root target path is typically all ASCII, so encoding is not relevant here. `toCollectionURI()` ensures the trailing `/`.

### Step 2: `contentArtifact.getTargetPath().toUri()` — the content file URI

```
Input:  Path C:\...\target\site\café\index
Output: URI  file:///C:/.../target/site/café/index
```

**This is the critical step.** On OpenJDK 25 / Windows, `Path.toUri()` produces a URI whose `getRawPath()` contains literal non-ASCII characters — they are **not** percent-encoded. The `é` character (U+00E9) appears literally, not as `%C3%A9`.

**Why**: Java's `WindowsUriSupport.toUri()` constructs the URI string and passes it through `new URI(String)` (the single-string constructor). This constructor stores the string as the "raw" form without performing percent-encoding on non-ASCII characters. `java.net.URI` is lenient: while RFC 3986 requires non-ASCII characters to be percent-encoded, `URI.create()` silently accepts them. This is a well-known `java.net.URI` quirk documented in the [FLANGE-88] URIPath Revamp TODO.

**Exception — URI-significant characters**: `Path.toUri()` **does** percent-encode characters that are syntactically required for URI validity:
- Space → `%20` (a literal space would fail `URI.create()` parsing)
- `#` → `%23` (fragment delimiter)
- `?` → `%3F` (query delimiter)
- `%` → `%25` (percent-encoding delimiter itself)

Non-ASCII characters are syntactically tolerated by `URI.create()` (they don't cause a parse failure), so they pass through unencoded.

This means the URI produced by `Path.toUri()` is **inconsistently encoded**: some characters are percent-encoded, others are not. The `URIPath` constructed from this URI inherits this inconsistency.

### Step 3: `Artifact.relativizeResourceReference(rootTargetPathUri, contentArtifact)`

This calls `URIPath.relativize(sourceURI, targetURI)`, which delegates to `URIs.findRelativePath()`, which ultimately calls Java's `URI.relativize()`.

```
Input:  base   = file:///C:/.../target/site/     (getRawPath: /C:/.../target/site/)
        target = file:///C:/.../target/site/café/index  (getRawPath: /C:/.../target/site/café/index)
Output: relative URI with getRawPath() = "café/index"
```

`URI.relativize()` produces a relative URI by string-matching the common prefix of the two paths. Since both inputs have decoded non-ASCII in their raw paths, the relative result also has decoded non-ASCII. The raw path of the relative URI is `café/index`.

The result is wrapped in a `URIPath` via `URIPath.fromURI(relativeURI)`.

### Step 4: `URIPath.toString()` — the final S3 key

```java
public String toString() {
    return uri.getRawPath();
}
```

Since the underlying URI's raw path is `café/index` (decoded), `toString()` returns `café/index`.

**Final S3 key: `café/index`** — decoded Unicode.

**However**, this was only accidentally correct for non-ASCII characters. For filenames containing spaces, `#`, `?`, or `%`, the key contained the percent-encoded form (e.g. `my%20file.html` instead of `my file.html`), violating the §S3 Key Identity Principle.

### The Central Deviation and Fix

The deviation was at a single call site in `S3.plan()`, which originally read:

```java
final String s3Key = Artifact.relativizeResourceReference(rootTargetPathUri, contentArtifact).toString();
```

`URIPath.toString()` returns `uri.getRawPath()` — the inconsistently encoded form. The fix (now applied) uses `toDecodedString()` instead, which returns `uri.getPath()` — the fully decoded form that recovers the canonical filesystem name:

```java
final String s3Key = Artifact.relativizeResourceReference(rootTargetPathUri, contentArtifact).toDecodedString();
```

`URI.getPath()` is the defined inverse of whatever encoding `Path.toUri()` performed. This is not merely an empirical observation — it is deductively guaranteed by the JDK API contracts:

1. **`Path.toUri()` must produce a valid `java.net.URI`.** The only character-transformation mechanism available within `java.net.URI` is percent-encoding. Characters that would break URI parsing (space, `#`, `?`, `%`) must be percent-encoded; non-ASCII characters may optionally be encoded. No other transformation is possible.
2. **`URI.getPath()` is contractually defined** as: "equal to `getRawPath()` except that all sequences of escaped octets are decoded."
3. **Percent-encoding is its own inverse.** Characters the provider encoded are decoded back by `getPath()`. Characters the provider left unencoded pass through `getPath()` unchanged. Either way, original characters are recovered.

This means `getRawPath()` is encoding-strategy-dependent — a provider that eagerly encodes non-ASCII would produce `caf%C3%A9` while the current default provider produces `café` — but **`getPath()` converges** to the decoded form regardless. For the S3 key pipeline, `URI.relativize()` preserves this property: both input URIs come from the same provider (same encoding strategy), so the raw-path prefix match succeeds, and `getPath()` on the relative result recovers the original segment characters.

The round-trip is safe for all character classes, including filenames with literal percent sequences. For example, a file literally named `caf%C3%A9.html` on disk:
- `Path.toUri()` encodes `%` as `%25`, producing `caf%25C3%25A9.html` in the raw path
- `URI.getPath()` decodes `%25` back to `%`, recovering the original `caf%C3%A9.html`
- The canonical filename is preserved — `getPath()` does **not** double-decode

This was empirically verified across 20 character classes including spaces, `#`, `%`, non-ASCII Latin, CJK, and mixed combinations (`testPathToUriGetPathRoundTripForSpecialCharacters`). In every case, `toDecodedString()` produces a key identical to Flange's `objectKeyFromPath()` output.

**Caveats:**

- **Default provider only.** The JDK guarantees `Path.of(p.toUri()).equals(p.toAbsolutePath())` only for the default filesystem provider. The deductive argument above still applies to any provider that uses percent-encoding within a standard `java.net.URI` — which is the only mechanism available — but the JDK explicitly declines to guarantee round-trip behavior for non-default providers (e.g. in-memory test filesystems, database-backed filesystems). Code that may handle non-default `Path` instances should be aware of this.
- **Relative path segments only.** The convergence applies to segment content (the characters between `/` delimiters). The root form (drive letters, UNC authority, leading slash conventions) is provider-specific. For S3 key derivation, only the relative segments matter.
- **Separator normalization.** `getPath()` returns segments delimited by `/`, not the platform separator. This is correct for S3 keys but means `toUri().getPath()` is not a general substitute for `Path.toString()`.

### Information Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│ Filesystem                                                              │
│   C:\...\target\site\café\index                                         │
└───────────────┬─────────────────────────────────────────────────────────┘
                │
                ▼  Path.toUri()
┌─────────────────────────────────────────────────────────────────────────┐
│ java.net.URI                                                            │
│   scheme: file                                                          │
│   getRawPath(): /C:/.../target/site/café/index   ← NOT percent-encoded  │
│   getPath():    /C:/.../target/site/café/index   ← same (nothing to     │
│                                                     decode)             │
│                                                                         │
│   NOTE: for `my file.html`, getRawPath() = `my%20file.html`            │
│         and getPath() = `my file.html` — they diverge!                  │
└───────────────┬─────────────────────────────────────────────────────────┘
                │
                ▼  URI.relativize(baseUri, targetUri)
┌─────────────────────────────────────────────────────────────────────────┐
│ java.net.URI (relative)                                                 │
│   getRawPath(): café/index   ← preserves decoded form from input        │
└───────────────┬─────────────────────────────────────────────────────────┘
                │
                ▼  URIPath.fromURI(relativeURI)
┌─────────────────────────────────────────────────────────────────────────┐
│ URIPath                                                                 │
│   toString():        café/index  (returns uri.getRawPath())             │
│   toDecodedString(): café/index  (returns uri.getPath() — same here,   │
│                      but differs for URI-significant chars like spaces) │
└───────────────┬─────────────────────────────────────────────────────────┘
                │
                ▼  .toDecodedString() — the canonical resource name
┌─────────────────────────────────────────────────────────────────────────┐
│ AWS SDK v2: PutObjectRequest.builder().key("café/index")                │
│   The key is the canonical resource name (see §S3 Key Identity          │
│   Principle). The SDK handles HTTP-level encoding transparently;        │
│   S3 stores the object under key "café/index" verbatim.                 │
└─────────────────────────────────────────────────────────────────────────┘
```

## Flange Key Derivation Pipeline

Flange's `S3Synchronizer.uploadFile()` at aws-s3-support/src/main/java/dev/flange/aws/s3/support/S3Synchronizer.java derives S3 keys via:

```java
final Path relativePath = rootDirectory.relativize(file);
final String s3Key = keyPrefix + objectKeyFromPath(relativePath);
```

Where `objectKeyFromPath()` (in `dev.flange.aws.s3.def.S3`) iterates over path name components and joins them with `/`:

```java
public static String objectKeyFromPath(final Path relativePath) {
    final StringJoiner keyJoiner = new StringJoiner(String.valueOf(PATH_SEPARATOR));
    for(int i = 0; i < relativePath.getNameCount(); i++) {
        keyJoiner.add(relativePath.getName(i).toString());
    }
    return keyJoiner.toString();
}
```

### Step-by-Step Trace

For a file at `C:\...\target\site\café\index` with root `C:\...\target\site\`:

```
1. rootDirectory.relativize(file)
   Input:  root = C:\...\target\site\
           file = C:\...\target\site\café\index
   Output: Path café\index

2. objectKeyFromPath(relativePath)
   Iterates: getName(0).toString() = "café"
             getName(1).toString() = "index"
   Joins with '/': "café/index"

Final S3 key: "café/index" — decoded Unicode, same as Guise Mummy.
```

### Information Flow Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│ Filesystem                                                              │
│   C:\...\target\site\café\index                                         │
└───────────────┬─────────────────────────────────────────────────────────┘
                │
                ▼  rootDirectory.relativize(file)
┌─────────────────────────────────────────────────────────────────────────┐
│ java.nio.file.Path (relative)                                           │
│   café\index   (platform separator, decoded characters from filesystem) │
└───────────────┬─────────────────────────────────────────────────────────┘
                │
                ▼  objectKeyFromPath(relativePath)
                   getName(i).toString() for each segment, joined with '/'
┌─────────────────────────────────────────────────────────────────────────┐
│ String                                                                  │
│   "café/index"   (forward slashes, decoded filesystem names)            │
└───────────────┬─────────────────────────────────────────────────────────┘
                │
                ▼  keyPrefix + result  (keyPrefix is "" for root sync)
┌─────────────────────────────────────────────────────────────────────────┐
│ AWS SDK v2: PutObjectRequest.builder().key("café/index")                │
│   The key is the canonical resource name (see §S3 Key Identity          │
│   Principle). The SDK handles HTTP-level encoding transparently;        │
│   S3 stores the object under key "café/index" verbatim.                 │
└─────────────────────────────────────────────────────────────────────────┘
```

## Why Previous Analysis Was Wrong

The previous analysis (documented in the architecture document and minutes) claimed:

> `S3Website` stores S3 object keys in percent-encoded form (e.g. `caf%C3%A9.html` for `café.html`) because the key derivation pipeline flows through `Path.toUri()` → `Artifact.relativizeResourceReference()` → `URIPath.toString()` (which returns `URI.getRawPath()`).

The reasoning was:

1. `Path.toUri()` produces a `file:` URI → **assumed** `getRawPath()` is percent-encoded.
2. `URIPath.toString()` returns `uri.getRawPath()` → **assumed** this returns percent-encoded output.

Both assumptions were wrong because of a subtlety in `java.net.URI`:

- The `URI` class has a dual representation: a "raw" (encoded) form and a "decoded" form.
- `getRawPath()` returns the "raw" form, which the documentation describes as the percent-encoded form.
- **However**, if the URI was constructed from a string containing literal non-ASCII characters (which `URI.create()` silently accepts), then the "raw" form IS the un-encoded string. `getRawPath()` returns whatever was stored, whether properly encoded or not.
- `Path.toUri()` on Windows constructs the URI string with literal non-ASCII characters, so `getRawPath()` returns them literally.

The critical insight is: **`getRawPath()` does not mean "percent-encoded path"**. It means "path as stored in the URI's internal raw representation". If the URI was constructed without encoding, `getRawPath()` returns the un-encoded form.

This is the same defect described in the [FLANGE-88] URIPath Revamp TODO, §1 "No input validation on `of()`":

> `URI.create()` silently accepts non-ASCII characters without percent-encoding them. So `URIPath.of("café")` succeeds, and `toString()` returns `"café"` — not a valid RFC 3986 path. There is no enforcement of the documented contract.

## The Encoding Leak Bug

`Path.toUri()` percent-encodes characters that are syntactically required for URI validity (spaces, `#`, `?`, `%`). When `URIPath.toString()` (which returns `getRawPath()`) is used to derive the S3 key, these encoding artifacts leak into the key. The divergence is not limited to spaces:

| Filesystem name | Guise Mummy key (`.toString()`) | Correct key (`.toDecodedString()`) | Flange key |
|---|---|---|---|
| `my file.html` | `my%20file.html` | `my file.html` | `my file.html` |
| `section#2.html` | `section%232.html` | `section#2.html` | `section#2.html` |
| `100%.html` | `100%25.html` | `100%.html` | `100%.html` |
| `café.html` | `café.html` | `café.html` | `café.html` |

Per the §S3 Key Identity Principle, only the canonical name (the `.toDecodedString()` / Flange form) is correct. The `.toString()` form stores objects whose keys contain literal percent characters — different objects, unreachable via normal browser requests. This was a bug in Guise Mummy's key derivation pipeline, fixed in [GUISE-230].

## The Redirect Location Header: A Separate Pipeline

`S3Website.preparePutObject()` uses a **different** encoding pipeline for the HTTP `Location` header in S3 redirect objects:

```java
builder.websiteRedirectLocation(URIPath.encode(ROOT_PATH + redirectDeployObject.getRedirectTargetKey()));
```

`URIPath.encode()` is an explicit RFC 3986 encoding function that **does** percent-encode non-ASCII characters. So the redirect location for a key `café/` becomes `/caf%C3%A9/` — properly percent-encoded for use as an HTTP `Location` header value (per RFC 9110 §7.1.2).

This is correct and separate from the S3 object key. The redirect location is an HTTP header value that must be a valid URI; the S3 key is an opaque canonical name (see §S3 Key Identity Principle).

## How S3 Resolves HTTP Requests to Object Keys

The §S3 Key Identity Principle establishes that `PutObject` stores the canonical resource name verbatim. The retrieval side is the complement: when a browser sends `GET /caf%C3%A9/index HTTP/1.1`, S3 must map the percent-encoded HTTP request path back to the canonical object key. Standard HTTP semantics require the server to decode the request URI's percent-encoded path before identifying the resource. S3 follows this convention: the request for `/caf%C3%A9/index` resolves to key `café/index`.

The full round-trip for both S3 Website Hosting and CloudFront OAC:

1. **Upload**: The deployer passes canonical name `café/index` to `PutObject`. S3 stores the object under key `café/index`.
2. **Browser request**: User navigates to `https://example.com/café/index`. The browser percent-encodes the path per RFC 3986: `GET /caf%C3%A9/index HTTP/1.1`.
3. **S3 key resolution**: S3 (whether accessed directly via S3 Website Hosting or indirectly via CloudFront OAC) decodes the request path `/caf%C3%A9/index` → `café/index`, and matches the stored object.

**Caveat**: The retrieval-side behavior described above is based on standard HTTP semantics and AWS documentation, not on empirical testing in this investigation. The investigation tests verified only the key generation pipeline. The request-path-to-key resolution has not been tested against a live S3 bucket.

## Implications for GUISE-230

### No S3 Key Encoding Divergence for Non-ASCII

The original concern that motivated the investigation — that Guise Mummy and Flange produce different S3 keys for non-ASCII filenames — is unfounded. Both produce decoded Unicode keys. The Flange deployment integration does not need any key encoding translation for non-ASCII content.

### Space Divergence Is a Bug

Guise Mummy produced `my%20file.html` as the S3 key for a file named `my file.html`, while Flange produces `my file.html`. Per the §S3 Key Identity Principle, the canonical name `my file.html` (with a literal space) is correct; the percent-encoded form `my%20file.html` creates an S3 object whose key contains literal `%20` characters — a different object, unreachable via normal browser requests. This bug extended to all characters that `Path.toUri()` encodes (`#`, `?`, `%`) — see §The Encoding Leak Bug. The fix (now applied) uses `toDecodedString()` instead of `toString()` at the S3 key derivation site (see §The Central Deviation and Fix).

### Architecture Document and Minutes Need Correction

The following claim in the minutes (2026-03-10 **Finding**) is incorrect:

> `S3Website` stores S3 object keys in percent-encoded form (e.g. `caf%C3%A9.html` for `café.html`) because the key derivation pipeline flows through `Path.toUri()` → `Artifact.relativizeResourceReference()` → `URIPath.toString()` (which returns `URI.getRawPath()`).

The architecture document has been updated to reflect the fix.

### URIPath Revamp (FLANGE-88) Remains Important

The underlying `URIPath` defects documented in the [FLANGE-88] TODO are real and important — `URIPath.of()` lacks input validation, `Path.toUri()` creates non-RFC-compliant URIs, `fromURI(URI)` needs to validate and normalize inconsistently encoded URIs, and `equals()` doesn't normalize percent-encoding. These defects cause a practical encoding divergence for URI-significant characters (spaces, `#`, `?`, `%`) — the encoding leak bug documented in §The Encoding Leak Bug. The immediate fix (now applied) uses `toDecodedString()` instead of `toString()` (see §The Central Deviation and Fix). The [FLANGE-88] revamp is a broader correctness effort that would ensure `URIPath` produces well-formed RFC 3986 paths throughout, eliminating the inconsistent encoding at its source rather than compensating for it at the point of use.

## Test Evidence

All findings were supported by investigation tests in `S3KeyEncodingInvestigationIT` (since removed; the fix is validated by `S3WebsitePlanIT`):

| Test | Assertion |
|---|---|
| `testPathToUriDoesNotPercentEncodeNonAscii` | `Path.toUri().getRawPath()` contains `café`, not `caf%C3%A9` |
| `testJavaUriRelativizePreservesDecodedForm` | `URI.relativize()` output `getRawPath()` = `café/index` |
| `testUrisFindRelativePathProducesDecodedForm` | `URIs.findRelativePath()` output = `café/index` |
| `testFullGuiseMummyKeyDerivation` | End-to-end Guise key = `café/index` |
| `testFlangeSynchronizerKeyDerivation` | Flange key = `café/index` |
| `testGuiseMummyVsFlangeKeyConsistency` | **Both keys are identical** |
| `testCjkCharacterKeyDerivation` | CJK (`東京`, Japanese: "Eastern Capital"): both keys identical |
| `testSpaceInFilenameKeyDerivation` | Spaces: Guise = `my%20file.html`, Flange = `my file.html` — **diverge** |
| `testUriPathOfWithDecodedInputStoresAsIs` | `URIPath.of("café/index").toString()` = `café/index` |
| `testUriPathOfWithEncodedInputPreservesEncoding` | `URIPath.of("caf%C3%A9/index").toString()` = `caf%C3%A9/index` |
| `testUriPathEncodeProducesPercentEncoded` | `URIPath.encode("café/index")` = `caf%C3%A9/index` |
| `testLiteralPercentInFilenameFullPipeline` | File `caf%C3%A9.html`: `toDecodedString()` = `caf%C3%A9.html` (preserves literal `%`), matches Flange |
| `testLiteralPercentAndSpaceInFilename` | File `my 100%.html`: `toDecodedString()` = `my 100%.html`, matches Flange |
| `testPathToUriGetPathRoundTripForSpecialCharacters` | 20 character classes (space, `#`, `@`, `!`, `$`, `&`, `'`, `()`, `+`, `,`, `;`, `=`, `{}`, `[]`, `~`, `%`, mixed specials, Latin, CJK, combined): `URI.getPath()` and `toDecodedString()` recover the original filename in every case, matching the Flange key |

Tested on: OpenJDK 25+36-LTS (Temurin), Windows.

[GUISE-230]: ../
