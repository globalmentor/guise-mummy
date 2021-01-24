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

import java.util.*;

import javax.annotation.*;

/**
 * An artifact potentially containing other <dfn>aspects</dfn> such as a preview image, an abridged book, or sound excerpt.
 * @author Garret Wilson
 */
public interface AspectualArtifact extends CompositeArtifact {

	/**
	 * Returns all the aspects of this artifact.
	 * @return The artifacts representing aspects of this artifact.
	 */
	public Collection<Artifact> getAspects();

	/**
	 * Finds an aspect by its ID.
	 * @param aspectId The ID of the aspect to return.
	 * @return The identified aspect, if one is found.
	 */
	public Optional<Artifact> findAspect(@Nonnull final String aspectId);

	/**
	 * Retrieves an aspect by its ID.
	 * @apiNote This method is provided primarily for MEXL expression and may be removed when MEXL has native support for {@link Optional}.
	 * @implSpec The default implementation delegates to {@link #findAspect(String)}.
	 * @param aspectId The ID of the aspect to return.
	 * @return The identified aspect, or <code>null</code> if no aspect with that ID is found.
	 */
	public default Artifact getAspect(@Nonnull final String aspectId) {
		return findAspect(aspectId).orElse(null);
	}

}
