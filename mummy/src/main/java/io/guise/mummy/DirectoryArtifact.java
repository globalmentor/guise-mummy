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

import static com.globalmentor.collections.Sets.*;

import java.nio.file.Path;
import java.util.*;

import javax.annotation.*;

/**
 * Abstract implementation of a collection artifact.
 * @author Garret Wilson
 */
public class DirectoryArtifact extends AbstractArtifact implements CollectionArtifact {

	private final Artifact contentArtifact;

	/** @return The optional internal artifact representing the content of this directory, such as <code>index.xhtml</code>. */
	public Optional<Artifact> getContentArtifact() {
		return Optional.ofNullable(contentArtifact);
	}

	private final Collection<Artifact> childArtifacts;

	@Override
	public Collection<Artifact> getChildArtifacts() {
		return childArtifacts;
	}

	/**
	 * Source resource context path constructor.
	 * @param resourceContextPath The absolute path of the resource, relative to the site context.
	 * @param sourceFile The file containing the source of this artifact.
	 * @param outputFile The file where the artifact will be generated.
	 * @param contentArtifact The internal artifact representing the content of this directory, such as <code>index.xhtml</code>, or <code>null</code> if there is
	 *          no content artifact.
	 * @param childArtifacts The child artifacts of this artifact.
	 * @throws IllegalArgumentException if the given context path is not absolute.
	 */
	public DirectoryArtifact(/*TODO fix @Nonnull final URIPath resourceContextPath, */@Nonnull final Path sourceFile, @Nonnull final Path outputFile,
			@Nullable Artifact contentArtifact, @Nonnull Collection<Artifact> childArtifacts) {
		//TODO add precondition to ensure this is a directory?
		super(sourceFile, outputFile);
		this.contentArtifact = contentArtifact;
		this.childArtifacts = Set.copyOf(childArtifacts);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This version returns the default reference source files and the content artifact, if any.
	 * @see #getContentArtifact()
	 */
	@Override
	public Set<Path> getReferentSourceFiles() {
		final Set<Path> defaultReferenceSourceFiles = super.getReferentSourceFiles();
		//add the content artifact to referent source files if present
		return getContentArtifact().map(contentArtifact -> immutableSetOf(defaultReferenceSourceFiles, contentArtifact.getSourceFile()))
				.orElse(defaultReferenceSourceFiles);
	}

}
