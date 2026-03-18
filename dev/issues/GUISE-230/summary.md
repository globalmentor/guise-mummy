# [GUISE-230] Summary: Delegate site deployment to Flange environment infrastructure.

## Objective

Enable Guise Mummy to deploy a mummified site to a Flange-managed environment (S3 + CloudFront OAC + CloudFront KVS) instead of its legacy imperative AWS API-based deployment, while preserving backward compatibility with existing deploy targets.

## Approach

The work proceeded in three phases: prerequisite fixes to Guise Mummy's internal model, construction of the `FlangeWebSite` deploy target, and resolution of encoding issues surfaced by the integration.

### Prerequisite fixes

Several latent issues in the existing codebase had to be addressed before the Flange integration could be built on a sound foundation.

**Real-path guarantee.** Artifact paths were derived from configuration on every access, with no guarantee of canonical form. A new `AbstractMummyContext` layer was introduced between `MummyContext` and `BaseMummyContext`, taking the three site directories (source, target, description) as constructor parameters validated via `checkArgumentRealPath()`. This ensures path consistency for Flange's filesystem-walking `S3Synchronizer`, which compares paths by identity.

**S3 collection artifact semantics.** The legacy `S3.plan()` walked subsumed content artifacts as independent entities, causing redirect targets to point to `/foo/index` instead of the canonical `/foo/`. The walker was rewritten to process collection artifacts as model entities and subsumed artifacts as storage details. Five integration tests were added.

**S3 key encoding.** `Artifact.relativizeResourceReference().toString()` leaked URI percent-encoding artifacts (spaces as `%20`, `#` as `%23`) into S3 object keys, producing keys that differed from canonical filesystem names. Three call sites across `S3.java` and `S3Website.java` were fixed to use `toDecodedString()`. A design investigation document (`designs/s3-key-encoding.md`) captures the analysis and the deductive proof that the round-trip is contractually guaranteed.

### `FlangeWebSite` deploy target

The deploy target was built in layered chunks:

1. **`ArtifactTreeWalker`** — a reusable depth-first walker with subsumption status reporting, replacing manual recursive traversal. `MummyPlan.walk()` convenience method added.

2. **`PlanSummary`/`PlanDescriber` refactoring** — `PlanDescriber` was moved to `dev.guise.mummy.plan`, split into `summarize()` (producing a `PlanSummary` record) and `writeTo()` (formatting), and enhanced with a `summarize(Visitor)` overload for piggybacking additional tree-walk logic. This transformed it from a display-only utility into a reusable plan analysis service.

3. **Collection content resource name consolidation** — derivation of the normalized collection content resource name was extracted to `GuiseMummy.findCollectionContentResourceName()`, replacing three independent inline derivations in `S3Website`, `GuiseCli`, and the new `FlangeWebSite`. A latent bug in `GuiseCli` (registering all configured base names as Tomcat welcome files, when only the normalized name matches target files) was fixed in the same pass.

4. **`FlangeWebSite` itself** — placed in `dev.guise.mummy.deploy.flange` (platform-agnostic package, not `deploy.aws`). Constructor reads Flange environment name and optional AWS profile from project configuration. `prepare()` resolves the environment via `AwsFlangeEnvironmentManager`. `deploy()` reuses `PlanDescriber.summarize(Visitor)` to collect both a `PlanSummary` (with redirect entries) and an artifact content-path index in a single tree walk, then delegates to `AwsFlangeDeployer.deploySite()`. `ArtifactMetadataStrategy` adapts artifact content types and SHA-256 fingerprints to Flange's `S3Synchronizer.MetadataStrategy` interface.

5. **`GenericFileMummifier`** — a new mummifier extending `OpaqueFileMummifier` with an extension-to-media-type map consolidated from three redundant sources (AWS SDK `Mimetype`, Flange's Mozilla-sourced list, and `GuiseCli`'s Tomcat-sourced list). Wired as the default mummifier, so deployers receive correct content types for opaque files (web fonts, modern image formats, etc.) without guessing at deploy time.

### Encoding resolution

Live testing revealed that `URIPath.toString()` returned literal non-ASCII characters instead of percent-encoded form for non-ASCII artifact paths, causing Flange redirect KVS keys to silently fail. Two failing tests were written to confirm the bug. The root cause was in `globalmentor-core`'s `URIPath` class, which stored URIs without normalizing encoding. The fix was delivered as a broader `URIPath` → `UriPath` revamp in `globalmentor-core` (separate ticket), establishing percent-encoding normalization as a class invariant. Guise Mummy was then migrated across 19 files, with the two tests re-enabled and passing.

### Operational fixes during integration testing

**S3 checksum sync (Flange bug).** Flange's `S3Synchronizer` built its `HeadObjectRequest` without `checksumMode(ChecksumMode.ENABLED)`, so S3 never returned stored SHA-256 checksums. Every sync check concluded "no checksum; synchronization needed," re-uploading all objects on every deploy. Fixed in Flange; confirmed that subsequent deploys correctly skip unchanged artifacts.

**AWS SDK debug noise (GUISE-107).** `--debug` flooded output with AWS SDK wire logging. A provisional CLI-specific `logback.xml` was added pinning `software.amazon.awssdk`, `org.apache.http`, and `io.netty` loggers to INFO. Committed under [GUISE-107] for later evaluation.

## Key design decisions

- **Deploy target plugin, not top-level fork.** `FlangeWebSite` is registered as a `DeployTarget` (configured as `* Flange:` in `deploy.targets`), preserving the plugin architecture and enabling coexistence with legacy targets.
- **DNS coexistence.** The legacy `Route53` deploy target discovers Flange-created hosted zones and upserts records into them. The initially planned DNS incompatibility warning was removed after empirical confirmation of seamless interop.
- **Metadata resolution deferred to lookup time.** `FlangeWebSite`'s manifest indexes all artifacts by target path (including subsumed content artifacts) and uses `MummyPlan.getPrincipalArtifact()` at `findMetadata()` time to resolve them to their owning directory. This differs from `S3`'s plan-time pre-resolution but suits Flange's filesystem-driven deployment model.

## Deferred work

- **Asset transformation bypass.** The "asset" designation only suppresses page generation; image mummifiers still transform large assets. A design was articulated (record asset status on `Artifact`; suppress `mummifyFile()` in `AbstractFileMummifier.mummify()` for assets) but deferred. See `todo - Asset Semantics Refinement.md`.
- **`S3`/`S3Website` walker refactor.** The legacy deployers should adopt `ArtifactTreeWalker` to replace their manual recursive traversal. See `todo - S3 Walker Refactor.md`.
- **`TextFileMummifier`.** `GenericFileMummifier` maps `.txt` → `text/plain` without charset detection. A future mummifier could detect encoding and produce `text/plain; charset=utf-8`.
- **Shared logback configuration.** The CLI-specific `logback.xml` duplicates the base `globalmentor-application` configuration. The logger pins should eventually be promoted to the shared config. Tracked in [GUISE-107].

## Documentation

An architecture document (`mummy/architecture.md`) was written covering the mummifier model, artifact lifecycle, subsumption semantics, and deploy target integration. The repository readme was rewritten. The S3 key encoding investigation is documented in `designs/s3-key-encoding.md`.
