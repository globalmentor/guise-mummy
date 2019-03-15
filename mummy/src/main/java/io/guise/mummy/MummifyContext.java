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

package io.guise.mummy;

import java.net.URI;
import java.nio.file.Path;

/**
 * Provides information about context of static site generation.
 * @author Garret Wilson
 */
public interface MummifyContext {

	/**
	 * Returns some URI indicating the root of the current context, that is, the site source directory. All resource context paths are interpreted relative to
	 * this root.
	 * @apiNote This method will typically but not necessarily return the URI form of the site source directory.
	 * @return The URI that represents the root of the current context.
	 */
	public default URI getRoot() {
		return getSiteSourceDirectory().toUri();
	}

	/**
	 * Returns the base directory of the entire site source, representing the root of the context.
	 * @apiNote This is analogous to Maven's <code>${project.basedir}/src/site</code> directory.
	 * @return The base directory of the site being mummified.
	 */
	public Path getSiteSourceDirectory();

	/** @return The absolute URI indicating the root URI of this context, that is, this site. */
	//TODO decide what to do with this: public URI getRootUri();

	/** @return The current artifact, such as a {@link Page}, being mummified. */
	public Artifact getArtifact();

	//TODO public UrfObject getResourceDescription(path)

}
