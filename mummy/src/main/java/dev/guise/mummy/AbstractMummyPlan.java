/*
 * Copyright © 2020 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package dev.guise.mummy;

import static com.globalmentor.io.Paths.*;
import static java.util.Objects.*;

import java.nio.file.Path;

import org.jspecify.annotations.*;

import com.globalmentor.net.URIPath;

/// Abstract base plan for mummifying a site.
/// @author Garret Wilson
public abstract class AbstractMummyPlan implements MummyPlan {

	private final Artifact rootArtifact;

	@Override
	public Artifact getRootArtifact() {
		return rootArtifact;
	}

	/// Root artifact constructor.
	/// @param rootArtifact The root artifact of the site, representing the root directory.
	public AbstractMummyPlan(@NonNull final Artifact rootArtifact) {
		this.rootArtifact = requireNonNull(rootArtifact);
	}

	@Override
	public URIPath referenceInSource(final Artifact fromArtifact, final Artifact toArtifact) {
		return relativizeResourceReference(getPrincipalArtifact(fromArtifact).getSourcePath(), toArtifact.getSourcePath(),
				toArtifact instanceof CollectionArtifact);
	}

	@Override
	public URIPath referenceInTarget(final Artifact fromArtifact, final Artifact toArtifact) {
		return relativizeResourceReference(getPrincipalArtifact(fromArtifact).getTargetPath(), toArtifact.getTargetPath(),
				toArtifact instanceof CollectionArtifact);
	}

	/// Relativizes a reference to a target file in the file system against some base path. As a safety measure both paths must be either a subpath of the root
	/// artifact source path, or a subpath of the root artifact target path. The returned reference will be a resource URI path (e.g. appropriate for web
	/// references) relative to the base path.
	/// @apiNote If the reference target path does not yet exist and a forced collection path is not requested, this method may not produce a URI path in the
	///          correct collection form, as it is impossible to know if the target represents a directory.
	/// @param basePath The absolute path against which the reference path with be relativized.
	/// @param referencePath The absolute reference path to relativize.
	/// @param forceCollection `true` if the returned path should be in collection form, ending with a slash, regardless of whether the reference path
	///          is a collection or even exists.
	/// @return The reference path relative to the base path as a URI path.
	/// @throws IllegalArgumentException if the target path and or the base path is not absolute and/or is not within the same source/target tree as per the root
	///           artifact.
	/// @see #getRootArtifact()
	protected URIPath relativizeResourceReference(@NonNull final Path basePath, @NonNull final Path referencePath, final boolean forceCollection) {
		final Path root = isSubPath(getRootArtifact().getSourcePath(), basePath) ? getRootArtifact().getSourcePath() : getRootArtifact().getTargetPath();
		return Artifact.relativizeResourceReference(checkArgumentSubPath(root, checkArgumentAbsolute(basePath)).toUri(),
				checkArgumentSubPath(root, checkArgumentAbsolute(referencePath)).toUri(), forceCollection);
	}

}
