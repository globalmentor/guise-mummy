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

package io.guise.mummy.mummify.collection;

import static com.globalmentor.collections.Sets.*;
import static java.util.Collections.*;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

import io.guise.mummy.*;
import io.guise.mummy.mummify.Mummifier;
import io.urf.model.UrfResourceDescription;

/**
 * Concrete implementation of a collection artifact based upon a file system directory.
 * @author Garret Wilson
 */
public class DirectoryArtifact extends AbstractArtifact implements CollectionArtifact {

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation delegates to {@link #getSourcePath()}.
	 */
	@Override
	public Path getSourceDirectory() {
		return getSourcePath();
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation always returns <code>false</code>.
	 */
	@Override
	public final boolean isSourcePathFile() {
		return false;
	}

	private final Artifact contentArtifact;

	/** @return The optional internal artifact representing the content of this directory, such as <code>index.xhtml</code>. */
	public Optional<Artifact> findContentArtifact() {
		return Optional.ofNullable(contentArtifact);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns the description of the content artifact, if any; otherwise, an empty description.
	 */
	@Override
	public UrfResourceDescription getResourceDescription() {
		return findContentArtifact().map(Artifact::getResourceDescription).orElse(UrfResourceDescription.EMPTY);
	}

	private final Collection<Artifact> childArtifacts;

	@Override
	public Collection<Artifact> getChildArtifacts() {
		return childArtifacts;
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns all the child artifacts along with the content artifact, if any.
	 * @see #getChildArtifacts()
	 * @see #findContentArtifact()
	 */
	@Override
	public Stream<Artifact> comprisedArtifacts() {
		return Stream.concat(getChildArtifacts().stream(), findContentArtifact().stream());
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns the content artifact, if any.
	 * @see #findContentArtifact()
	 */
	@Override
	public Collection<Artifact> getSubsumedArtifacts() {
		return findContentArtifact().map(Set::of).orElse(emptySet());
	}

	/**
	 * Constructor.
	 * @param mummifier The mummifier responsible for generating this artifact.
	 * @param sourceDirectory The directory serving as the source of this artifact.
	 * @param targetDirectory The directory where the artifact will be generated.
	 * @param contentArtifact The internal artifact representing the content of this directory, such as <code>index.xhtml</code>, or <code>null</code> if there is
	 *          no content artifact.
	 * @param childArtifacts The child artifacts of this artifact.
	 */
	public DirectoryArtifact(@Nonnull final Mummifier mummifier, @Nonnull final Path sourceDirectory, @Nonnull final Path targetDirectory,
			@Nullable Artifact contentArtifact, @Nonnull Collection<Artifact> childArtifacts) {
		//TODO add precondition to ensure this is a directory?
		super(mummifier, sourceDirectory, targetDirectory);
		this.contentArtifact = contentArtifact;
		this.childArtifacts = Set.copyOf(childArtifacts);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This version returns the default reference source paths and the source path to the content artifact, if any.
	 * @see #findContentArtifact()
	 */
	@Override
	public Set<Path> getReferentSourcePaths() {
		final Set<Path> defaultReferenceSourcePaths = super.getReferentSourcePaths();
		//add the content artifact, if present, to the referent source paths
		return findContentArtifact().map(contentArtifact -> immutableSetOf(defaultReferenceSourcePaths, contentArtifact.getSourcePath()))
				.orElse(defaultReferenceSourcePaths);
	}

	@Override
	public boolean isNavigable() {
		return true;
	}

}
