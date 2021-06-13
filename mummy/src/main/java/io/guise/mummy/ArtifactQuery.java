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

import javax.annotation.*;

import com.globalmentor.net.URIPath;

/**
 * A means for querying artifacts.
 * <p>
 * A query requires an initial specification of the source of annotations by using a <code>from…()</code> method such as {@link #fromLevelOf(Artifact)}.
 * </p>
 * @apiNote An artifact query could be considered a query builder; its methods mutate the query, and a call to {@link #iterator()} actually performs the query.
 * @author Garret Wilson
 */
public interface ArtifactQuery extends Iterable<Artifact> {

	//from

	/**
	 * Initially queries children of a given artifact.
	 * @param artifact The artifact for which children should be queried.
	 * @return This artifact query.
	 */
	public ArtifactQuery fromChildrenOf(@Nonnull Artifact artifact);

	/**
	 * Initially queries children of an artifact referred to by a URI path source reference relative to some other artifact.
	 * @param artifact The artifact for which children should be queried.
	 * @param artifact The artifact the relative reference should be resolved against when finding the referent artifact.
	 * @param sourceRelativeReference The relative URI path being used as a reference to some artifact.
	 * @throws IllegalArgumentException if the given reference path is absolute.
	 * @return This artifact query.
	 */
	public ArtifactQuery fromChildrenOf(@Nonnull Artifact artifact, @Nonnull final URIPath sourceRelativeReference);

	/**
	 * Initially queries children of an artifact referred to by a URI path source reference relative to some other artifact.
	 * @apiNote This is a convenience method for {@link #fromChildrenOf(Artifact, URIPath)} for MEXL query construction.
	 * @implSpec The default implementation delegates to {@link #fromChildrenOf(Artifact, URIPath)}.
	 * @param artifact The artifact the relative reference should be resolved against when finding the referent artifact.
	 * @param sourceRelativeReference The relative URI path being used as a reference to some artifact.
	 * @throws IllegalArgumentException if the given string is not a valid reference path or is absolute.
	 * @return This artifact query.
	 */
	public default ArtifactQuery fromChildrenOf(@Nonnull Artifact artifact, @Nonnull final String sourceRelativeReference) {
		return fromChildrenOf(artifact, URIPath.of(sourceRelativeReference));
	}

	/**
	 * Initially queries siblings of a given artifact. An artifact will not have siblings if it has no parent. If any artifacts are included, the returned
	 * artifacts <em>will</em> include the given artifact. This means that a single child artifact would include only itself as the single sibling artifact.
	 * @param artifact The artifact for which siblings should be queried.
	 * @return This artifact query.
	 */
	public ArtifactQuery fromSiblingsOf(@Nonnull Artifact artifact);

	/**
	 * Initially queries siblings of an artifact referred to by a URI path source reference relative to some other artifact. An artifact will not have siblings if
	 * it has no parent. If any artifacts are included, the returned artifacts <em>will</em> include the given artifact. This means that a single child artifact
	 * would include only itself as the single sibling artifact.
	 * @param artifact The artifact the relative reference should be resolved against when finding the referent artifact.
	 * @param sourceRelativeReference The relative URI path being used as a reference to some artifact.
	 * @throws IllegalArgumentException if the given reference path is absolute.
	 * @return This artifact query.
	 */
	public ArtifactQuery fromSiblingsOf(@Nonnull Artifact artifact, @Nonnull final URIPath sourceRelativeReference);

	/**
	 * Initially queries siblings of an artifact referred to by a URI path source reference relative to some other artifact. An artifact will not have siblings if
	 * it has no parent. If any artifacts are included, the returned artifacts <em>will</em> include the given artifact. This means that a single child artifact
	 * would include only itself as the single sibling artifact.
	 * @apiNote This is a convenience method for {@link #fromSiblingsOf(Artifact, URIPath)} for MEXL query construction.
	 * @implSpec The default implementation delegates to {@link #fromSiblingsOf(Artifact, URIPath)}.
	 * @param artifact The artifact the relative reference should be resolved against when finding the referent artifact.
	 * @param sourceRelativeReference The relative URI path being used as a reference to some artifact.
	 * @throws IllegalArgumentException if the given string is not a valid reference path or is absolute.
	 * @return This artifact query.
	 */
	public default ArtifactQuery fromSiblingsOf(@Nonnull Artifact artifact, @Nonnull final String sourceRelativeReference) {
		return fromSiblingsOf(artifact, URIPath.of(sourceRelativeReference));
	}

	/**
	 * Initially queries artifacts at the same level of the given artifact. The given artifact itself may be included, depending on whether the artifact is a
	 * stand-in for the main resource (e.g. as the index artifact is for a directory), in which case it will not be included. If the artifact itself represents a
	 * level such as a directory, it children will be returned.
	 * @apiNote This method allows querying of artifacts at a certain level without needing to worry whether the given artifact is subsumed, such as
	 *          <code>index.html</code>, and standing in for the collection.
	 * @param artifact The artifact for which artifacts at the same level should be queried.
	 * @return This artifact query.
	 */
	public ArtifactQuery fromLevelOf(@Nonnull Artifact artifact);

	/**
	 * Initially queries artifacts at the same level of some artifact referred to by a URI path source reference relative to some artifact. The resolved artifact
	 * itself may be included, depending on whether the artifact is a stand-in for the main resource (e.g. as the index artifact is for a directory), in which
	 * case it will not be included. If the resolved artifact itself represents a level such as a directory, it children will be returned.
	 * @apiNote This method allows querying of artifacts at a certain level without needing to worry whether the given artifact is subsumed, such as
	 *          <code>index.html</code>, and standing in for the collection.
	 * @param artifact The artifact the relative reference should be resolved against when finding the referent artifact.
	 * @param sourceRelativeReference The relative URI path being used as a reference to some artifact.
	 * @throws IllegalArgumentException if the given reference path is absolute.
	 * @return This artifact query.
	 */
	public ArtifactQuery fromLevelOf(@Nonnull Artifact artifact, @Nonnull final URIPath sourceRelativeReference);

	/**
	 * Initially queries artifacts at the same level of an artifact referred to by a URI path source reference relative to some other artifact. The resolved
	 * artifact itself may be included, depending on whether the artifact is a stand-in for the main resource (e.g. as the index artifact is for a directory), in
	 * which case it will not be included. If the resolved artifact itself represents a level such as a directory, it children will be returned.
	 * @apiNote This method allows querying of artifacts at a certain level without needing to worry whether the given artifact is subsumed, such as
	 *          <code>index.html</code>, and standing in for the collection.
	 * @apiNote This is a convenience method for {@link #fromLevelOf(Artifact, URIPath)} for MEXL query construction.
	 * @implSpec The default implementation delegates to {@link #fromLevelOf(Artifact, URIPath)}.
	 * @param artifact The artifact the relative reference should be resolved against when finding the referent artifact.
	 * @param sourceRelativeReference The relative URI path being used as a reference to some artifact.
	 * @throws IllegalArgumentException if the given string is not a valid reference path or is absolute.
	 * @return This artifact query.
	 */
	public default ArtifactQuery fromLevelOf(@Nonnull Artifact artifact, @Nonnull final String sourceRelativeReference) {
		return fromLevelOf(artifact, URIPath.of(sourceRelativeReference));
	}

	//filter

	/**
	 * Filters artifacts by base content type.
	 * @param contentTypeMatch A base content type matching string, supporting the wildcard <code>*</code> character for the subtype, such as
	 *          <code>image/jpeg</code> or <code>image/*</code>.
	 * @return This artifact query.
	 * @throws IllegalStateException if the query has not yet been initialized with an artifact source.
	 */
	public ArtifactQuery filterContentType(@Nonnull final CharSequence contentTypeMatch);

	//order by

	/**
	 * Indicates that artifacts should be returned in ascending order of their target filenames, in addition to previously specified orders if any.
	 * @apiNote This filename ordering is irrespective of target path; that is, only the filename itself will be examined.
	 * @return This artifact query.
	 * @see Artifact#getTargetPath()
	 */
	public ArtifactQuery orderByName();

	/**
	 * Reverses the last specified ordering.
	 * @apiNote Calling this method multiple times will switch the ordering each time. For example calling this method twice subsequently will result in the same
	 *          ordering as before the calls.
	 * @throws IllegalStateException if no ordering has been indicated previously.
	 * @return This artifact query.
	 */
	public ArtifactQuery reversedOrder();

}
