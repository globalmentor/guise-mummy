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
import static com.globalmentor.net.URIs.*;

import java.net.URI;
import java.nio.file.*;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.*;

import com.globalmentor.net.UriPath;

/// A plan for mummifying a site.
/// @author Garret Wilson
public interface MummyPlan {

	//# artifact lookup

	/// Returns the root artifact of the site, representing the root directory.
	/// @return The root artifact of the site, representing the root directory.
	public Artifact getRootArtifact();

	/// Returns the principal artifact for an artifact.
	/// @apiNote The principal artifact is almost always the artifact itself, unless the artifact is a subsumed artifact (e.g. the "index" content file for a
	///          directory), in which case the principal artifact will be the artifact it has been subsumed into.
	/// @param artifact The artifact for which the principal artifact is to be determined.
	/// @return The principal artifact of record for the given artifact.
	public Artifact getPrincipalArtifact(@NonNull final Artifact artifact);

	/// Returns the parent artifact of some artifact.
	///
	/// The determination is made in terms of the principal artifact of that given. For example, if `foo/index.html` is given, the parent artifact of
	/// `foo/` will be returned.
	/// @apiNote Normally the parent artifact will be a [CollectionArtifact].
	/// @param artifact The artifact for which a parent is to be found.
	/// @return The parent artifact, if any, of the given artifact.
	/// @see #getPrincipalArtifact(Artifact)
	public Optional<Artifact> findParentArtifact(@NonNull final Artifact artifact);

	/// Provides the child artifacts of some artifact.
	///
	/// The determination is made in terms of the principal artifact of that given. For example, if `foo/index.html` is given, the child artifacts of
	/// `foo/` will be returned.
	/// @implSpec The default implementation returns the child artifacts of the artifact if it is a [CollectionArtifact].
	/// @param artifact The artifact for which children should be returned.
	/// @return The child artifacts, if any, of the given artifact.
	/// @see #getPrincipalArtifact(Artifact)
	/// @see CollectionArtifact#getChildArtifacts()
	public default Stream<Artifact> childArtifacts(@NonNull final Artifact artifact) {
		final Artifact principalArtifact = getPrincipalArtifact(artifact);
		return principalArtifact instanceof CollectionArtifact collectionArtifact ? collectionArtifact.getChildArtifacts().stream() : Stream.empty();
	}

	/// Provides the artifacts that are siblings to the given artifact. An artifact will not have siblings if it has no parent. If any artifacts are returned, the
	/// returned artifacts *will* include the given artifact. This means that a single child artifact will return itself as the single sibling artifact.
	///
	/// The determination is made in terms of the principal artifact of that given. For example, if `foo/index.html` is given, the sibling artifacts of
	/// `foo/` will be returned.
	/// @param artifact The artifact for which siblings should be returned.
	/// @return The sibling artifacts, if any, of the given artifact, including the principal artifact of that given.
	/// @see #getPrincipalArtifact(Artifact)
	public default Stream<Artifact> siblingArtifacts(@NonNull final Artifact artifact) {
		return findParentArtifact(artifact).map(this::childArtifacts).orElse(Stream.empty());
	}

	//## artifact lookup by source reference 

	/// Retrieves an artifact referred to by a reference source path in the file system.
	///
	/// The source path of the returned artifact may not actually be equal to given source path. For example, a referent source path of
	/// `/foo/bar/index.xhtml` might return the artifact for the file that exists at the source path `/foo/bar/`, because any link to
	/// `/foo/bar/index.xhtml` is really referring to `/foo/bar/`; the `/foo/bar/index.xhtml` file is merely an implementation
	/// detail for storing the content of `/foo/bar/`.
	/// @param referenceSourcePath The absolute source path being used as a reference to some artifact.
	/// @return The artifact referred to by a reference source path.
	/// @throws IllegalArgumentException if the given reference path is not absolute.
	public Optional<Artifact> findArtifactBySourceReference(@NonNull final Path referenceSourcePath);

	/// Retrieves an artifact referred to by a URI path source reference relative to some artifact.
	///
	/// The returned artifact will always be a principal artifact. Thus the source path of the returned artifact may not actually be equal to given source path.
	/// For example, a referent source path of `/foo/bar/index.xhtml` might return the artifact for the file that exists at the source path
	/// `/foo/bar/`, because any link to `/foo/bar/index.xhtml` is really referring to `/foo/bar/`; the
	/// `/foo/bar/index.xhtml` file is merely an implementation detail for storing the content of `/foo/bar/`.
	///
	/// This method ensures that the reference link is calculated against the principal resource of the given artifact, which may not be the given artifact itself.
	/// This allows links from `/foo/index.html` to be calculated against `/foo/`, for example.
	/// @param artifact The artifact the relative reference should be resolved against when finding the referent artifact.
	/// @param sourceRelativeReference The relative URI path being used as a reference to some artifact.
	/// @return The artifact referred to by a relative path source reference.
	/// @throws IllegalArgumentException if the given reference path is absolute.
	/// @see #getPrincipalArtifact(Artifact)
	public default Optional<Artifact> findArtifactBySourceRelativeReference(@NonNull final Artifact artifact, @NonNull final UriPath sourceRelativeReference) {
		return findArtifactBySourceRelativeReference(getPrincipalArtifact(artifact).getSourcePath(), sourceRelativeReference);
	}

	/// Retrieves an artifact referred to by a URI path source reference relative to some source path.
	///
	/// The returned artifact will always be a principal artifact. Thus the source path of the returned artifact may not actually be equal to given source path.
	/// For example, a referent source path of `/foo/bar/index.xhtml` might return the artifact for the file that exists at the source path
	/// `/foo/bar/`, because any link to `/foo/bar/index.xhtml` is really referring to `/foo/bar/`; the
	/// `/foo/bar/index.xhtml` file is merely an implementation detail for storing the content of `/foo/bar/`.
	///
	/// This method follows [RFC 3986](https://tools.ietf.org/html/rfc3986) and resolves a relative reference of the empty string by using the source
	/// relative reference e.g. `/foo/bar`, not as the parent collection `/foo/`.
	/// @apiNote If possible [#findArtifactBySourceRelativeReference(Artifact, UriPath)] should be used to ensure that the reference is calculated from the
	///          principal artifact as the referring artifact.
	/// @param contextSourcePath The source path the relative reference should be resolved against when finding the referent artifact.
	/// @param sourceRelativeReference The relative URI path being used as a reference to some artifact.
	/// @return The artifact referred to by a relative path source reference.
	/// @throws IllegalArgumentException if the context source path is not absolute.
	/// @throws IllegalArgumentException if the given reference path is absolute.
	public default Optional<Artifact> findArtifactBySourceRelativeReference(@NonNull final Path contextSourcePath,
			@NonNull final UriPath sourceRelativeReference) {
		checkArgumentAbsolute(contextSourcePath);
		UriPath.checkArgumentRelative(sourceRelativeReference);
		//Resolve the relative path to the URI form of the context artifact path, and then convert that back to a file system path.
		//Follow RFC 3986 by interpreting resolution to "" as returning the context source path itself, not the collection/directory path.
		final URI sourceRelativeReferenceURI = sourceRelativeReference.toUri();
		final Path referenceSourcePath = sourceRelativeReferenceURI.equals(EMPTY_PATH_URI) ? contextSourcePath
				: Paths.get(contextSourcePath.toUri().resolve(sourceRelativeReference.toUri()));
		return findArtifactBySourceReference(referenceSourcePath);
	}

	/// Creates a general query for artifacts in the plan.
	/// @return An artifact query for subsequent configuration and execution.
	public ArtifactQuery queryArtifacts();

	//# traversal

	/// Walks the plan's artifact tree, visiting each artifact in depth-first pre-order.
	///
	/// @implSpec The default implementation delegates to [ArtifactTreeWalker#walk(Artifact, ArtifactTreeWalker.Visitor)]
	/// with this plan's [root artifact][#getRootArtifact()].
	/// @param visitor The visitor to invoke for each artifact.
	/// @see ArtifactTreeWalker#walk(Artifact, ArtifactTreeWalker.Visitor)
	public default void walk(final ArtifactTreeWalker.Visitor visitor) {
		ArtifactTreeWalker.walk(getRootArtifact(), visitor);
	}

	//# generation

	/// Returns a resource reference from one artifact to another in the source tree. The returned reference will be a relative URI path appropriate to be used as
	/// a web reference.
	///
	/// This method ensures that the reference link is calculated against the principal resource of the given artifact, which may not be the given artifact itself.
	/// This allows links from `/foo/index.html` to be calculated against `/foo/`, for example.
	/// @param fromArtifact The artifact the reference path should be relativized against.
	/// @param toArtifact The artifact being referred to.
	/// @return The resource reference path from the first given artifact to the second given artifact in the terms of the source tree.
	/// @see #getPrincipalArtifact(Artifact)
	/// @see Artifact#getSourcePath()
	public UriPath referenceInSource(@NonNull final Artifact fromArtifact, @NonNull final Artifact toArtifact);

	/// Returns a resource reference from one artifact to another in the target tree. The returned reference will be a relative URI path appropriate to be used as
	/// a web reference.
	///
	/// This method ensures that the reference link is calculated against the principal resource of the given artifact, which may not be the given artifact itself.
	/// This allows links from `/foo/index.html` to be calculated against `/foo/`, for example.
	/// @param fromArtifact The artifact the reference path should be relativized against.
	/// @param toArtifact The artifact being referred to.
	/// @return The resource reference path from the first given artifact to the second given artifact in the terms of the target tree.
	/// @see #getPrincipalArtifact(Artifact)
	/// @see Artifact#getTargetPath()
	public UriPath referenceInTarget(@NonNull final Artifact fromArtifact, @NonNull final Artifact toArtifact);

	//# MEXL

	/// Returns a resource reference from one artifact to another in the source tree. The returned reference will be a relative URI path appropriate to be used as
	/// a web reference.
	/// @apiNote This is a MEXL convenience method that functions identically to [#referenceInSource(Artifact, Artifact)].
	/// @param fromArtifact The artifact the reference path should be relativized against.
	/// @param toArtifact The artifact being referred to.
	/// @return The resource reference path from the first given artifact to the second given artifact in the terms of the source tree.
	/// @see #referenceInSource(Artifact, Artifact)
	public default UriPath reference(@NonNull final Artifact fromArtifact, @NonNull final Artifact toArtifact) {
		return referenceInSource(fromArtifact, toArtifact);
	}

}
