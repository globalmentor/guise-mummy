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

import java.io.IOException;

import javax.annotation.*;

import io.guise.mummy.*;

/**
 * A strategy for deploying a site to some other location such as a server.
 * @author Garret Wilson
 */
public interface Deployer {

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
	 * @throws IOException if there is an I/O error during site deployment.
	 */
	public void deploy(@Nonnull final MummyContext context, @Nonnull Artifact rootArtifact) throws IOException;

}
