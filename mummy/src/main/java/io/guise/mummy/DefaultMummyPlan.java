/*
 * Copyright © 2020 GlobalMentor, Inc. <http://www.globalmentor.com/>
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
import static java.util.Objects.*;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

/**
 * Default plan for mummifying a site.
 * @author Garret Wilson
 */
public class DefaultMummyPlan implements MummyPlan {

	private final Artifact rootArtifact;

	@Override
	public Artifact getRootArtifact() {
		return rootArtifact;
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
	 * Recursively initializes the mummification plan for the given artifact. Parent artifacts are updated in the map, for example.
	 * @param artifact The artifact the plan of which to update.
	 */
	private void initialize(@Nonnull final Artifact artifact) {
		requireNonNull(artifact);
		artifact.getReferentSourcePaths().forEach(referenceSourcePath -> artifactsByReferenceSourcePath.put(referenceSourcePath, artifact));
		if(artifact instanceof CollectionArtifact) {
			for(final Artifact childArtifact : ((CollectionArtifact)artifact).getChildArtifacts()) {
				parentArtifactsByArtifact.put(childArtifact, artifact); //map the parent to the child
				initialize(childArtifact); //recursively update the plan for the children
			}
		}
	}

	/**
	 * Root artifact constructor.
	 * @param rootArtifact The root artifact of the site, representing the root directory.
	 */
	public DefaultMummyPlan(@Nonnull final Artifact rootArtifact) {
		this.rootArtifact = requireNonNull(rootArtifact);
		initialize(rootArtifact);
	}

	@Override
	public ArtifactQuery queryArtifacts() {
		return new DefaultArtifactQuery();
	}

	/**
	 * Artifact query implementation using this plan.
	 */
	protected class DefaultArtifactQuery extends BaseArtifactQuery {

		/**
		 * {@inheritDoc}
		 * @implNote This implementation does not currently work with a detail artifact of a main context artifact, such as an index file of a directory.
		 */
		@Override
		public ArtifactQuery fromChildrenOf(final Artifact artifact) {
			setStream(childArtifacts(artifact));
			return this;
		}

		/**
		 * {@inheritDoc}
		 * @implNote This implementation does not currently work with a detail artifact of a main context artifact, such as an index file of a directory.
		 */
		@Override
		public ArtifactQuery fromSiblingsOf(final Artifact artifact) {
			setStream(siblingArtifacts(artifact));
			return this;
		}

		@Override
		public ArtifactQuery fromLevelOf(final Artifact artifact) {
			//return the children of the artifact for this artifact's source directory (either its parent directory or itself if it represents a directory)
			setStream(findArtifactBySourceReference(artifact.getSourceDirectory()).map(DefaultMummyPlan.this::childArtifacts).orElse(Stream.empty()));
			return this;
		}

	}

}
