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

import static com.globalmentor.io.Paths.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;

import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

import io.urf.model.*;

/**
 * A default source file artifact supporting aspects.
 * @implSpec The current implementation creates aspects duplicating the description of the main artifact.
 * @implNote The main artifact description may have been cached after mummification, so duplicating it for the aspect may result in irrelevant or incorrect
 *           properties. The mummifier will need to take this into consideration, by forcing the description to be set dirty for example. Under the current
 *           implementation, this may be done by removing any {@link io.urf.vocab.content.Content#MODIFIED_AT_PROPERTY_TAG} property when the mummifier knows
 *           for sure that the main artifact needs mummification (assuming aspects generation are controlled solely by the main artifact generation).
 * @author Garret Wilson
 */
class DefaultAspectualSourceFileArtifact extends DefaultSourceFileArtifact implements AspectualArtifact {

	/** The delimiter for appending an aspect ID to a filename. */
	private static final char FILENAME_ASPECT_DELIMITER = '-';

	private final Map<String, Artifact> aspectsById;

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation delegates to {@link #aspect(String)}.
	 */
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

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation delegates to {@link #getAspects()}, as the only comprised artifacts of this class are the aspect artifacts.
	 */
	@Override
	public Stream<Artifact> comprisedArtifacts() {
		return getAspects().stream();
	}

	@Override
	public Collection<Artifact> getSubsumedArtifacts() {
		return emptySet();
	}

	/**
	 * Builder constructor.
	 * @param builder The builder specifying the construction parameters.
	 * @param aspectIds The IDs of the aspects that should be added.
	 * @throws IllegalArgumentException if the corporeal source file does not exist or is not a regular file.
	 */
	protected DefaultAspectualSourceFileArtifact(@Nonnull final Builder<?> builder, @Nonnull final Set<String> aspectIds) {
		super(builder);
		aspectsById = aspectIds.stream().collect(toUnmodifiableMap(identity(), aspectId -> { //create aspects for each aspect ID
			final Path aspectSourcePath = appendFilenameBase(getSourcePath(), FILENAME_ASPECT_DELIMITER + requireNonNull(aspectId)); //e.g. `foo-bar.jpg` -> `foo-bar-preview.jpg`
			final Path aspectTargetPath = appendFilenameBase(getTargetPath(), FILENAME_ASPECT_DELIMITER + requireNonNull(aspectId)); //e.g. `foo-bar.jpg` -> `foo-bar-preview.jpg`
			final UrfResourceDescription aspectResourceDescription = new UrfObject(); //TODO create description copy constructor
			for(final Map.Entry<URI, Object> property : getResourceDescription().getProperties()) { //TODO fix description caching for artifacts somehow; the current logic will set wrong fingerprints, for example
				aspectResourceDescription.setPropertyValue(property.getKey(), property.getValue());
			}
			aspectResourceDescription.setPropertyValue(PROPERTY_TAG_MUMMY_ASPECT, aspectId); //e.g. mummy/aspect="preview"
			return DefaultSourceFileArtifact.builder(getMummifier(), aspectSourcePath, aspectTargetPath).setCorporealSourceFile(getCorporealSourceFile())
					.withDescription(aspectResourceDescription).build(); //TODO set aspect ID
		}));
	}

}
