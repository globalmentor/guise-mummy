/*
 * Copyright © 2019 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.mummy.deploy;

import java.util.Set;

import javax.annotation.*;

import io.confound.config.ConfigurationException;
import io.guise.mummy.MummyContext;

/**
 * A deploy target that distributes content to another location. A typical example is a content delivery network (CDN).
 * @author Garret Wilson
 */
public interface ContentDeliveryTarget extends DeployTarget {

	/**
	 * Returns all the possible user agent identifications this content delivery target might use when retrieving content from the origin target.
	 * @apiNote Each of these identification strings is usually used as a value of the <code>User-Agent</code> HTTP header.
	 * @return The set of user agent identifiers, if any, any of which this target may use when retrieving content from the origin.
	 */
	public Set<String> getUserAgentIdentifications();

	/**
	 * Determines the deploy target this target will use for its origin content. The returned deploy target will be one of the deploy target instances recognized
	 * by the given context.
	 * @param context The context of static site generation.
	 * @return The deploy target this target uses for its origin content.
	 * @throws ConfigurationException if the origin target cannot be determined.
	 * @see MummyContext#getDeployTargets()
	 */
	public DeployTarget getOriginTarget(@Nonnull final MummyContext context) throws ConfigurationException;

}
