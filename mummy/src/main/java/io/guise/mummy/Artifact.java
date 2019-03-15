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

import java.nio.file.Path;

import com.globalmentor.net.URIPath;

import io.urf.model.UrfResourceDescription;

/**
 * Provides information about the artifact produced by mummifying a resource.
 * @author Garret Wilson
 */
public interface Artifact {

	/**
	 * Returns the path of the artifact source resource in the context of the site.
	 * <p>
	 * For example if the site context root is <code>file:/home/user/site/</code> and the source resource is located at
	 * <code>file:/home/user/site/foo/bar/example.xhtml</code>, this method will return <code>/foo/bar/example.xhtml</code>.
	 * </p>
	 * @return The absolute path of the source resource relative to the site context.
	 */
	public URIPath getResourceContextPath();

	/** @return A description of the source resource for this artifact. */
	//TODO implement public UrfResourceDescription getResourceDescription();

	/** @return The actual file containing the source of this artifact. */
	public Path getSourceFile();

	/** @return The actual file generated for this artifact. */
	public Path getOutputFile();

}
