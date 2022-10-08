/*
 * Copyright © 2019 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package io.guise.mummy;

import java.util.Collection;

/**
 * An artifact such as a directory that has child artifacts that are candidates for navigation.
 * @apiNote This is a specialization of a composite artifact; an artifact that is considered a "collection" of child artifacts.
 * @author Garret Wilson
 */
public interface CollectionArtifact extends CompositeArtifact {

	/**
	 * Returns the child artifacts that make up the collection.
	 * @apiNote There may be other comprising artifacts not considered children, for example some helper or sidecar files.
	 * @return The child artifacts of this artifact.
	 * @see #comprisedArtifacts()
	 */
	public Collection<Artifact> getChildArtifacts();

}
