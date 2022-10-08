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

package io.guise.mummy.deploy;

import static com.globalmentor.java.Objects.*;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

import io.guise.mummy.*;

/**
 * A strategy for deploying a site to some other location such as a server.
 * @author Garret Wilson
 */
public interface DeployTarget {

	/**
	 * Retrieves the protocols supported for access by an end-user after deployment.
	 * @apiNote The term "protocol" is used loosely here; more precisely these identify the supported URL schemes.
	 * @apiNote These are not the protocols supported for deployment itself.
	 * @return The supported protocols for end-user access after deployment, in the canonical (lowercase) form of the appropriate URL scheme, such as
	 *         <code>http</code> or <code>https</code>.
	 */
	public Set<String> getSupportedProtocols();

	/**
	 * Convenience method to retrieve all content delivery targets that deliver content from this deploy target.
	 * @param context The context of static site generation.
	 * @return All {@link ContentDeliveryTarget} deploy targets that have their origin target set this deploy target.
	 * @see MummyContext#getDeployTargets()
	 * @see ContentDeliveryTarget#getOriginTarget(MummyContext)
	 */
	public default Stream<ContentDeliveryTarget> contentDeliveryTargets(@Nonnull final MummyContext context) {
		final DeployTarget thisDeployTarget = this;
		return context.getDeployTargets().stream().flatMap(List::stream) //get a stream of deploy targets, if any
				//only look at deploy targets that are content delivery targets with this deploy target as an origin
				.flatMap(asInstances(ContentDeliveryTarget.class)).filter(target -> target.getOriginTarget(context) == thisDeployTarget);
	}

	/**
	 * Prepares for deploying a site. This may include configuring a server, for example.
	 * @param context The context of static site generation.
	 * @throws IOException if there is an I/O error during site deployment preparation.
	 */
	public void prepare(@Nonnull final MummyContext context) throws IOException;

	/**
	 * Deploys a site.
	 * @param context The context of static site generation.
	 * @param rootArtifact The root artifact of the site being deployed
	 * @return The URL for accessing the deployed site, if available.
	 * @throws IOException if there is an I/O error during site deployment.
	 */
	public Optional<URI> deploy(@Nonnull final MummyContext context, @Nonnull Artifact rootArtifact) throws IOException;

}
