# [GUISE-230]: Delegate site deployment to Flange environment infrastructure.

## Objective

Enable Guise Mummy to deploy a mummified site to a Flange environment, using Flange's CloudFormation-managed infrastructure (S3 + CloudFront OAC + CloudFront KVS) instead of Guise Mummy's built-in, manual AWS API-based deployment.

## Problem

Guise Mummy's current deployment (`dev.guise.mummy.deploy.aws`) manages AWS infrastructure directly through imperative API calls — creating S3 buckets, configuring website hosting, provisioning CloudFront distributions, requesting ACM certificates, and setting up Route 53 records. This approach has several drawbacks:

- **Infrastructure drift.** Manual API calls have no declarative state to reconcile against. Configuration changes outside the tool go undetected.
- **Brittleness.** The deployment code must handle every possible AWS API state transition, error case, and eventual consistency delay. This is complex, hard to test, and fragile.
- **Legacy architecture.** The current approach uses S3 Website Hosting (public bucket + website endpoint) rather than the modern CloudFront OAC (Origin Access Control) approach, which keeps the bucket private and serves content exclusively through CloudFront.
- **Monolithic coupling.** All infrastructure management is embedded in Guise Mummy itself, duplicating capabilities that Flange now provides as a reusable framework.

## Desired Behavior

A developer who has created a Flange environment with site support (e.g., `flange env create prod --with-site --with-domain example.com --with-web-domain www`) can deploy a Guise Mummy site to that environment by adding two configuration properties to the project's `guise-project.turf`:

1. The Flange environment name (e.g., `prod`).
2. Optionally, the AWS profile to use for credentials.

Running `guise deploy` then:

- Detects the Flange environment name in the project configuration.
- Resolves the environment via Flange's `AwsFlangeEnvironmentManager` (reading CloudFormation exports for the site bucket, distribution ID, and KVS ARN).
- Constructs a `MetadataStrategy` that adapts Guise Mummy's artifact metadata (content type and content fingerprint from the `target/site-description/` sidecar `.-.tupr` files) to the `S3Synchronizer.MetadataStrategy` interface.
- Extracts redirects from the artifact plan's `mummy/altLocation` properties and maps them to Flange's `Map<URIPath, URI>` redirect format.
- Determines the collection content resource name from the project's `mummy.collectionContentBaseNames` configuration and the `mummy.page.namesBare` setting.
- Delegates to `AwsFlangeDeployer.deploySite()` with the site directory, redirects, collection content resource name, resolved environment, and metadata strategy.

If the Flange environment name is **not** configured, `guise deploy` falls through to the existing `DeployTarget`-based deployment (S3/S3Website/CloudFront/Route53), preserving backward compatibility.

## Constraints

- **Flange environment resolution requires CloudFormation exports.** `AwsFlangeEnvironmentManager.resolve()` reads CloudFormation stack exports prefixed with `flange-<env>:`. The environment must have been created with `--with-site` so that `SiteBucketName`, `SiteDistributionId`, and `SiteKeyValueStoreArn` exports exist.
- **`S3Synchronizer` operates on filesystem paths, not artifact trees.** The synchronizer walks `target/site/` via `Files.walk()`. Guise Mummy's metadata strategy must build a `Map<Path, Artifact>` keyed by target filesystem path at construction time, adapting from the artifact tree's source-path index (`artifactsByReferenceSourcePath`). Paths must be consistent with the synchronizer's `toRealPath()` canonicalization.
- **Metadata sidecar files are in a parallel directory tree.** Content type and fingerprint live in `target/site-description/` as `.-.tupr` files (e.g., `index.-.tupr`), not alongside the site content in `target/site/`. The metadata strategy can load these from disk by mapping a site target path to its sidecar counterpart, or consult the in-memory artifact tree directly if the artifact plan is available.
- **Flange does not yet support custom DNS entries or domain redirects.** The existing Guise Mummy deployment supports Route 53 DNS management (`deploy.dns`) and alternative domains that redirect to the canonical domain (`site.altDomains`). These features are not available when deploying via Flange. If the project configuration includes `deploy.dns` or `site.altDomains`, the deployment should emit a warning indicating these features are not supported with Flange deployment.
- **The Flange deployer requires specific AWS SDK clients.** `AwsFlangeDeployer` is `AutoCloseable` and manages its own S3, CloudFront, and CloudFront KVS clients. It provides factory methods `forProfile(Optional<AwsProfile>)` and `forCredentials(...)` for construction.
- **Guise Mummy currently has no AWS profile configuration.** The existing deployment creates AWS clients internally within each deploy target (`S3`, `CloudFront`, `Route53`). A new project-level or deploy-level configuration property for the AWS profile will be needed.

## Acceptance Criteria

- A Guise Mummy project with a Flange environment name configured in `guise-project.turf` deploys to that environment via `guise deploy`.
- The deployed site content in S3 has correct content types, including for extensionless files when `mummy.page.namesBare = true`.
- Redirects declared via `mummy/altLocation` in the site's source files are synchronized to the Flange environment's CloudFront KVS.
- The collection content resource name is correctly derived from the project's `mummy.collectionContentBaseNames` and bare-names configuration.
- CloudFront distribution is invalidated after deployment.
- If the Flange environment name is not configured, existing `DeployTarget`-based deployment continues to work unchanged.
- If `deploy.dns` or `site.altDomains` are configured alongside a Flange environment, the deployment emits a warning that those features are not supported with Flange deployment.
- An AWS profile can optionally be specified in the project configuration for Flange deployment.

## Non-Goals

- Removing or deprecating the existing `DeployTarget`-based deployment mechanism.
- Adding DNS record management to the Flange deployment path (future ticket).
- Adding alternative domain redirect support to the Flange deployment path (future ticket).
- Modifying Flange itself (any needed Flange changes are tracked in the Flange project).
- Supporting Flange service deployment (Lambda, SAM) from Guise Mummy — only static site deployment is in scope.

## Guidance

- **Flange deployer API.** The primary integration point is `AwsFlangeDeployer.deploySite(Path siteDirectory, Map<URIPath, URI> redirects, Optional<String> collectionContentResourceName, AwsFlangeEnvironment env, MetadataStrategy metadataStrategy, Supplier<SiteSynchronizationMonitor> monitorSupplier)` in the `flange-deploy-aws` module. The simpler overloads omit the metadata strategy (using a default extension-based one) and/or the monitor.
- **Metadata strategy adaptation.** [FLANGE-75] introduced the `S3Synchronizer.MetadataStrategy` interface, which returns `Optional<Metadata>` containing `Optional<MediaType>` content type and `Map<Algorithm, Hash>` checksums. A Guise Mummy implementation would read `content-type` and `content-fingerprint` from the artifact's `UrfResourceDescription` (in memory) or the `.-.tupr` sidecar files (on disk). Per [FLANGE-75] minutes, the strategy is called with the absolute filesystem `Path` of files in `target/site/`, so the implementation must map these paths back to artifacts or their sidecar descriptions.
- **Redirect extraction.** The artifact plan already discovers `mummy/altLocation` properties during the PLAN phase. The current S3 deployment extracts these in `S3.planResource()` and produces `S3ArtifactRedirectDeployObject` instances. An analogous extraction pass over the plan would produce the `Map<URIPath, URI>` that Flange expects.
- **Collection content resource name.** Flange's `collectionContentResourceName` corresponds to Guise Mummy's `mummy.collectionContentBaseNames` (default `["index"]`), combined with the appropriate extension (`.html` unless `mummy.page.namesBare = true`, in which case no extension). Flange's KVS-based CloudFront function handles the mapping of directory paths to the content resource.
- **Lifecycle integration.** The Flange deployment path should be invoked during the existing `PREPARE_DEPLOY`/`DEPLOY` lifecycle phases in `GuiseMummy.mummify()`, as an alternative to the `DeployTarget` iteration. The artifact plan and mummified site content are both available at that point.
- **`SiteProject.Type.GUISE_MUMMY`.** Flange's `SiteProject` enum already recognizes Guise Mummy projects by detecting `target/site/`. This enables `flange deploy-site` as an alternative CLI entry point, though the primary integration path for this ticket is `guise deploy`.
- **Orientation.** The deploy lifecycle is orchestrated in `GuiseMummy.mummify()` at [mummy/src/main/java/dev/guise/mummy/GuiseMummy.java](mummy/src/main/java/dev/guise/mummy/GuiseMummy.java). Deploy targets are loaded from configuration sections in the `PREPARE_DEPLOY` phase. The `S3` deploy target in [mummy/src/main/java/dev/guise/mummy/deploy/aws/S3.java](mummy/src/main/java/dev/guise/mummy/deploy/aws/S3.java) contains the current artifact-to-S3-object mapping logic. Redirect handling is in `S3Website` and `S3ArtifactRedirectDeployObject`. The CLI entry point is [cli/src/main/java/dev/guise/cli/GuiseCli.java](cli/src/main/java/dev/guise/cli/GuiseCli.java).
