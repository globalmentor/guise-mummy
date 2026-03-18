/*
 * Copyright © 2019 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.guise.mummy.deploy;

import static com.globalmentor.java.Objects.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import org.jspecify.annotations.*;

import dev.guise.mummy.*;

/// A strategy for deploying a site to some other location such as a server.
///
/// # State Model
///
/// A deploy target is **stateful across lifecycle phases**. The expected state flow is:
///
/// 1. **Constructor** — receives immutable configuration (profile, region, bucket names, etc.) derived from the project
///    configuration, and constructs any API clients needed for infrastructure interaction. These values are stored as
///    `final` fields and do not change.
/// 2. **[#prepare(MummyContext)]** — discovers or provisions infrastructure (buckets, certificates, environments).
///    Results that are needed in later phases are cached as instance fields. For example, an ACM certificate ARN
///    resolved during preparation is stored for use during deployment.
/// 3. **[#deploy(MummyContext, Artifact)]** — consumes cached infrastructure state from preparation and reads
///    runtime configuration from [MummyContext#getConfiguration()] on demand. Configuration values are **not** cached
///    from the constructor; `context.getConfiguration()` is called directly where needed.
///
/// @apiNote Implementations should document any fields set during [#prepare(MummyContext)] and their availability
///          guarantees, following the pattern established by `CloudFront.getAcmCertificateArn()`.
/// @author Garret Wilson
public interface DeployTarget {

	/// Retrieves the protocols supported for access by an end-user after deployment.
	/// @apiNote The term "protocol" is used loosely here; more precisely these identify the supported URL schemes.
	/// @apiNote These are not the protocols supported for deployment itself.
	/// @return The supported protocols for end-user access after deployment, in the canonical (lowercase) form of the appropriate URL scheme, such as
	///         `http` or `https`.
	public Set<String> getSupportedProtocols();

	/// Convenience method to retrieve all content delivery targets that deliver content from this deploy target.
	/// @param context The context of static site generation.
	/// @return All [ContentDeliveryTarget] deploy targets that have their origin target set this deploy target.
	/// @see MummyContext#getDeployTargets()
	/// @see ContentDeliveryTarget#getOriginTarget(MummyContext)
	public default Stream<ContentDeliveryTarget> contentDeliveryTargets(@NonNull final MummyContext context) {
		final DeployTarget thisDeployTarget = this;
		return context.getDeployTargets().stream().flatMap(List::stream) //get a stream of deploy targets, if any
				//only look at deploy targets that are content delivery targets with this deploy target as an origin
				.flatMap(asInstances(ContentDeliveryTarget.class)).filter(target -> target.getOriginTarget(context) == thisDeployTarget);
	}

	/// Prepares for deploying a site. This may include provisioning infrastructure (e.g. creating buckets, requesting
	/// certificates) or resolving externally managed infrastructure (e.g. looking up a deployment environment).
	/// @implSpec Implementations may cache discovered infrastructure state as instance fields for use in [#deploy(MummyContext, Artifact)].
	///          Such fields should document that they are available only after successful preparation.
	/// @param context The context of static site generation.
	/// @throws IOException if there is an I/O error during site deployment preparation.
	public void prepare(@NonNull final MummyContext context) throws IOException;

	/// Deploys a site.
	/// @param context The context of static site generation.
	/// @param rootArtifact The root artifact of the site being deployed
	/// @return The URL for accessing the deployed site, if available.
	/// @throws IOException if there is an I/O error during site deployment.
	public Optional<URI> deploy(@NonNull final MummyContext context, @NonNull Artifact rootArtifact) throws IOException;

}
