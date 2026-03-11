# `dev.guise.mummy.deploy.aws`

AWS deployment targets for Guise Mummy static sites.

## Deploy Targets

- **`S3`** — Deploys site artifacts to an [S3](https://aws.amazon.com/s3/) bucket. Handles incremental deployment via fingerprint comparison, bucket creation, and policy configuration. Serves as the base class for `S3Website`.
- **`S3Website`** — Extends `S3` to configure the bucket as an [S3 static website](https://docs.aws.amazon.com/AmazonS3/latest/userguide/WebsiteHosting.html), with index document support and `altLocation` redirect handling via routing rules or object redirects.
- **`CloudFront`** — Sets up a [CloudFront](https://aws.amazon.com/cloudfront/) distribution fronting an `S3Website` deployment, with ACM certificate provisioning and cache invalidation.
- **`Route53`** — Manages DNS records in [Route 53](https://aws.amazon.com/route53/) hosted zones, including alias records for CloudFront distributions.

## S3 Deploy Objects

The S3 deployers use a plan/put/prune lifecycle. During planning, deploy objects are collected in a map keyed by S3 key. During put, objects are uploaded (with optional incremental skipping). During prune, S3 objects not in the map are deleted.

- **`S3DeployObject`** — Interface for an object to be deployed; provides key, content type, content length, fingerprint, and content stream.
- **`AbstractS3DeployObject`** — Base implementation with key-based equality and a content stream provider.
- **`S3ArtifactDeployObject`** — Deploys a Guise Mummy artifact's content to S3.
- **`S3ArtifactRedirectDeployObject`** — An S3 object that redirects one key to another, used for `altLocation` support.

## S3 Key Encoding

S3 object keys produced by the `S3`/`S3Website` deployers are in **percent-encoded** form. The key derivation pipeline is:

1. `artifact.getTargetPath().toUri()` — Java's `Path.toUri()` percent-encodes non-ASCII characters per RFC 3986.
2. `Artifact.relativizeResourceReference(rootUri, artifact)` — produces a `URIPath` relative to the site root.
3. `URIPath.toString()` — returns `URI.getRawPath()`, the percent-encoded form.

For example, a file named `café.html` produces S3 key `caf%C3%A9.html`. These keys match the URI paths that browsers use to reference the resources, which is the form expected by S3 Website's index document and routing rule mechanisms.

## Configuration

- **`AWS`** — Shared configuration constants. `deploy.aws.profile` specifies the AWS credentials profile.
- `S3` reads `region` and `bucket` from the deploy target's local configuration section.
- `S3Website` adds `altBuckets`, `redirectMeans`, and `redirectCountOptimalThreshold`.
- `CloudFront` requires an `S3Website` target to be configured first; it uses the S3 website bucket as the CloudFront origin.
- `Route53` resolves a hosted zone by ID or domain name.
