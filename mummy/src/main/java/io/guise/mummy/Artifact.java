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
import java.util.Set;

/**
 * Provides information about the artifact produced by mummifying a resource.
 * <p>
 * Artifact equality is determined by target path as given by {@link #getTargetPath()}.
 * </p>
 * @author Garret Wilson
 */
public interface Artifact {

	/** @return A description of the source resource for this artifact. */
	//TODO implement public UrfResourceDescription getResourceDescription();

	/**
	 * Returns the path to the directory containing the artifact source file. If the artifact source path refers to a directory, this method returns the source
	 * path itself; otherwise this method returns the parent directory.
	 * @return The source directory of the artifact.
	 * @see #getSourcePath()
	 */
	public Path getSourceDirectory();

	/** @return The path referring to the source of this artifact, wich may be a file or a directory. */
	public Path getSourcePath();

	/**
	 * Retrieves the source paths that should be equivalent targets referring to this artifact.
	 * <p>
	 * For example, if a directory <code>/foo/</code> has a content source file of <code>/foo/index.xhtml</code>, both <code>/foo/</code> and
	 * <code>/foo/index.xhtml</code> would refer to this same artifact. In the logical resource model, the <code>/foo/index.xhtml</code> file is an implementation
	 * detail for storing the contents of the <code>/foo/</code> collection.
	 * </p>
	 * @return All source paths that, if referred to by source links, identify this same artifact.
	 */
	public Set<Path> getReferentSourcePaths();

	/** @return The path to the generated artifact in the target tree. */
	public Path getTargetPath();

	/** @return The mummifier responsible for mummifying this artifact. */
	public Mummifier getMummifier();

}
