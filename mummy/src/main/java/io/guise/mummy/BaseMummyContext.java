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

import static com.globalmentor.io.Paths.*;
import static java.nio.file.Files.*;
import static java.util.Objects.*;

import java.nio.file.Path;
import java.util.*;

import javax.annotation.*;

/**
 * Abstract base implementation of a mummification context with common default functionality.
 * @author Garret Wilson
 */
public abstract class BaseMummyContext implements MummyContext {

	/**
	 * {@inheritDoc}
	 * @implSpec This specification currently ignores dotfiles, for example <code>.git</code> and <code>.gitignore</code>; as well as non-regular files.
	 */
	public boolean isIgnore(final Path sourcePath) {
		if(isDotfile(sourcePath)) { //ignore dotfiles
			return true;
		}
		if(!isRegularFile(sourcePath) && !isDirectory(sourcePath)) { //TODO add option to traverse symbolic links
			return true;
		}
		return false;
	}

	private final Map<Artifact, Artifact> parentArtifactsByArtifact = new HashMap<>();

	@Override
	public Optional<Artifact> findParentArtifact(final Artifact artifact) {
		return Optional.ofNullable(parentArtifactsByArtifact.get(requireNonNull(artifact)));
	}

	private final Map<Path, Artifact> artifactsByReferenceSourcePath = new HashMap<>();

	@Override
	public Optional<Artifact> findArtifactBySourceReference(final Path referenceSourcePath) {
		return Optional.ofNullable(artifactsByReferenceSourcePath.get(checkArgumentAbsolute(referenceSourcePath)));
	}

	/**
	 * Recursively updates the mummification plan for the given artifact. Parent artifacts are updated in the map, for example.
	 * @param artifact The artifact the plan of which to update.
	 */
	protected void updatePlan(@Nonnull final Artifact artifact) {
		requireNonNull(artifact);
		artifact.getReferentSourcePaths().forEach(referenceSourcePath -> artifactsByReferenceSourcePath.put(referenceSourcePath, artifact));
		if(artifact instanceof CollectionArtifact) {
			for(final Artifact childArtifact : ((CollectionArtifact)artifact).getChildArtifacts()) {
				parentArtifactsByArtifact.put(childArtifact, artifact); //map the parent to the child
				updatePlan(childArtifact); //recursively update the plan for the children
			}
		}
	}

}
