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
import static com.globalmentor.net.URIs.*;
import static java.util.Objects.*;

import java.net.URI;
import java.nio.file.*;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.*;

import com.globalmentor.net.URIPath;

/**
 * A plan for mummifying a site.
 * @author Garret Wilson
 */
public interface MummyPlan {

	/** @return The root artifact of the site, representing the root directory. */
	public Artifact getRootArtifact();

	//# artifact lookup

	/**
	 * Returns the parent artifact of some artifact.
	 * @param artifact The artifact for which a parent is to be determined.
	 * @return The parent artifact, if any, of the given artifact.
	 */
	public Optional<Artifact> findParentArtifact(@Nonnull final Artifact artifact);

	/**
	 * Provides the child artifacts of some artifact.
	 * @param artifact The artifact for which children should be returned.
	 * @return The child artifacts, if any, of the given artifact.
	 */
	public default Stream<Artifact> childArtifacts(@Nonnull final Artifact artifact) {
		return requireNonNull(artifact) instanceof CollectionArtifact ? ((CollectionArtifact)artifact).getChildArtifacts().stream() : Stream.empty();
	}

	/**
	 * Provides the artifacts that are siblings to the given artifact. An artifact will not have siblings if it has no parent. If any artifacts are returned, the
	 * returned artifacts <em>will</em> include the given artifact. This means that a single child artifact will return itself as the single sibling artifact.
	 * @param artifact The artifact for which siblings should be returned.
	 * @return The sibling artifacts, if any, of the given artifact, including the given artifact.
	 */
	public default Stream<Artifact> siblingArtifacts(@Nonnull final Artifact artifact) {
		return findParentArtifact(artifact).map(this::childArtifacts).orElse(Stream.empty());
	}

	//## artifact lookup by source reference 

	/**
	 * Retrieves an artifact referred to by a reference source path in the file system.
	 * <p>
	 * The source path of the returned artifact may not actually be equal to given source path. For example, a referent source path of
	 * <code>/foo/bar/index.xhtml</code> might return the artifact for the file that exists at the source path <code>/foo/bar/</code>, because any link to
	 * <code>/foo/bar/index.xhtml</code> is really referring to <code>/foo/bar/</code>; the <code>/foo/bar/index.xhtml</code> file is merely an implementation
	 * detail for storing the content of <code>/foo/bar/</code>.
	 * </p>
	 * @param referenceSourcePath The absolute source path being used as a reference to some artifact.
	 * @return The artifact referred to by a reference source path.
	 * @throws IllegalArgumentException if the given reference path is not absolute.
	 */
	public Optional<Artifact> findArtifactBySourceReference(@Nonnull final Path referenceSourcePath);

	/**
	 * Retrieves an artifact referred to by a URI path source reference relative to some context artifact.
	 * <p>
	 * The source path of the returned artifact may not actually be equal to given source path. For example, a referent source path of
	 * <code>/foo/bar/index.xhtml</code> might return the artifact for the file that exists at the source path <code>/foo/bar/</code>, because any link to
	 * <code>/foo/bar/index.xhtml</code> is really referring to <code>/foo/bar/</code>; the <code>/foo/bar/index.xhtml</code> file is merely an implementation
	 * detail for storing the content of <code>/foo/bar/</code>.
	 * </p>
	 * @param contextArtifact The artifact the relative reference should be resolved against when finding the referent artifact.
	 * @param sourceRelativeReference The relative URI path being used as a reference to some artifact.
	 * @return The artifact referred to by a relative path source reference.
	 * @throws IllegalArgumentException if the given reference path is absolute.
	 */
	public default Optional<Artifact> findArtifactBySourceRelativeReference(@Nonnull final Artifact contextArtifact,
			@Nonnull final URIPath sourceRelativeReference) {
		return findArtifactBySourceRelativeReference(contextArtifact.getSourcePath(), sourceRelativeReference);
	}

	/**
	 * Retrieves an artifact referred to by a URI path source reference relative to some context source path.
	 * <p>
	 * The source path of the returned artifact may not actually be equal to given source path. For example, a referent source path of
	 * <code>/foo/bar/index.xhtml</code> might return the artifact for the file that exists at the source path <code>/foo/bar/</code>, because any link to
	 * <code>/foo/bar/index.xhtml</code> is really referring to <code>/foo/bar/</code>; the <code>/foo/bar/index.xhtml</code> file is merely an implementation
	 * detail for storing the content of <code>/foo/bar/</code>.
	 * </p>
	 * <p>
	 * This method follows <a href="https://tools.ietf.org/html/rfc3986">RFC 3986</a> and resolves a relative reference of the empty string by using the source
	 * relative reference e.g. <code>/foo/bar</code>, not as the parent collection <code>/foo/</code>.
	 * </p>
	 * @param contextSourcePath The source path the relative reference should be resolved against when finding the referent artifact.
	 * @param sourceRelativeReference The relative URI path being used as a reference to some artifact.
	 * @return The artifact referred to by a relative path source reference.
	 * @throws IllegalArgumentException if the context source path is not absolute.
	 * @throws IllegalArgumentException if the given reference path is absolute.
	 */
	public default Optional<Artifact> findArtifactBySourceRelativeReference(@Nonnull final Path contextSourcePath,
			@Nonnull final URIPath sourceRelativeReference) {
		checkArgumentAbsolute(contextSourcePath);
		sourceRelativeReference.checkRelative();
		//Resolve the relative path to the URI form of the context artifact path, and then convert that back to a file system path.
		//Follow RFC 3986 by interpreting resolution to "" as returning the context source path itself, not the collection/directory path.
		final URI sourceRelativeReferenceURI = sourceRelativeReference.toURI();
		final Path referenceSourcePath = sourceRelativeReferenceURI.equals(EMPTY_PATH_URI) ? contextSourcePath
				: Paths.get(contextSourcePath.toUri().resolve(sourceRelativeReference.toURI()));
		return findArtifactBySourceReference(referenceSourcePath);
	}

}
