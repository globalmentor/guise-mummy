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

package dev.guise.mummy;

import static java.util.Collections.*;
import static java.util.Objects.*;

import java.util.*;
import java.util.stream.Stream;

import org.jspecify.annotations.*;

/// A default source file artifact supporting aspects.
/// @author Garret Wilson
class DefaultAspectualSourceFileArtifact extends DefaultSourceFileArtifact implements AspectualArtifact {

	private final Map<String, Artifact> aspectsById;

	/// {@inheritDoc}
	/// @implSpec This implementation delegates to [#aspect(String)].
	@Override
	public Optional<Artifact> findAspect(final String aspectId) {
		return Optional.ofNullable(aspect(aspectId));
	}

	@Override
	public Artifact aspect(final String aspectId) {
		return aspectsById.get(requireNonNull(aspectId));
	}

	@Override
	public Collection<Artifact> getAspects() {
		return aspectsById.values();
	}

	/// {@inheritDoc}
	/// @implSpec This implementation delegates to [#getAspects()], as the only comprised artifacts of this class are the aspect artifacts.
	@Override
	public Stream<Artifact> comprisedArtifacts() {
		return getAspects().stream();
	}

	@Override
	public Collection<Artifact> getSubsumedArtifacts() {
		return emptySet();
	}

	/// Builder constructor.
	/// @param builder The builder specifying the construction parameters.
	/// @throws IllegalArgumentException if the corporeal source file does not exist or is not a regular file.
	protected DefaultAspectualSourceFileArtifact(@NonNull final Builder<?> builder) {
		super(builder);
		this.aspectsById = builder.getAspectArtifacts();
	}

}
