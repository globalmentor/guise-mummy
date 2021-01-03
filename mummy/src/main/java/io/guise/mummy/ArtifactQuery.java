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

/**
 * A means for querying artifacts.
 * <p>
 * A query requires an initial specification of the source of annotations by using a <code>from…()</code> method such as {@link #fromLevelOf(Artifact)}.
 * </p>
 * @apiNote An artifact query could be considered a query builder; its methods mutate the query, and a call to {@link #iterator()} actually performs the query.
 * @author Garret Wilson
 */
public interface ArtifactQuery extends Iterable<Artifact> {

	/**
	 * Initially queries children of a given artifact.
	 * @param artifact The artifact for which children should be queried.
	 * @return This artifact query.
	 */
	public ArtifactQuery fromChildrenOf(@Nonnull Artifact artifact);

	/**
	 * Initially queries siblings of a given artifact. An artifact will not have siblings if it has no parent. If any artifacts are included, the returned
	 * artifacts <em>will</em> include the given artifact. This means that a single child artifact would include only itself as the single sibling artifact.
	 * @param artifact The artifact for which siblings should be queried.
	 * @return This artifact query.
	 */
	public ArtifactQuery fromSiblingsOf(@Nonnull Artifact artifact);

	/**
	 * Initially queries artifacts at the same level of the given artifact. The given artifact itself may be included, depending on whether the artifact is a
	 * stand-in for the main resource (e.g. as the index artifact is for a directory), in which case it will not be included. If the artifact itself represents a
	 * level such as a directory, it children will be returned.
	 * @param artifact The artifact for which artifacts at the same level should be queried.
	 * @return This artifact query.
	 */
	public ArtifactQuery fromLevelOf(@Nonnull Artifact artifact);

}
