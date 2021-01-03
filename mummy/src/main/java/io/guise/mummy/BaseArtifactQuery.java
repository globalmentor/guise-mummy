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

import static com.globalmentor.java.Conditions.*;
import static java.util.Objects.*;

import java.util.Iterator;
import java.util.stream.Stream;

import javax.annotation.*;

/**
 * Base functionality for implementing an artifact query.
 * @author Garret Wilson
 */
public abstract class BaseArtifactQuery implements ArtifactQuery {

	private Stream<Artifact> stream = null;

	/**
	 * Sets the stream to provide the source of artifacts for later filtering an other operations. The stream can only be set once for the query.
	 * @param stream The stream source of artifacts.
	 * @throws IllegalStateException if the stream has already been set.
	 */
	protected void setStream(@Nonnull final Stream<Artifact> stream) {
		checkState(this.stream == null, "Query already initialized with artifact source stream.");
		this.stream = requireNonNull(stream);
	}

	/**
	 * Executes the query and returns an iterator to the artifacts.
	 * @implSpec This implementation does not yet support querying all artifacts; setting an initial set of artifacts via {@link #setStream(Stream)} is required.
	 * @throws IllegalStateException if the query has not yet been initialized with an artifact source stream.
	 */
	@Override
	public Iterator<Artifact> iterator() {
		checkState(this.stream != null, "Query not initialized with an artifact source stream.");
		return stream.iterator();
	}

}
