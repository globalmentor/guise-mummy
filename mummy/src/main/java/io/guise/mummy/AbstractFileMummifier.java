/*
 * Copyright © 2019-2020 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import static com.globalmentor.util.Optionals.*;
import static io.guise.mummy.Artifact.*;
import static java.nio.file.Files.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

import javax.annotation.*;

import io.urf.model.*;
import io.urf.turf.TurfParser;
import io.urf.vocab.content.Content;

/**
 * Abstract mummifier for generating artifacts based upon a single source file.
 * @author Garret Wilson
 */
public abstract class AbstractFileMummifier extends AbstractSourcePathMummifier {

	/**
	 * Loads the generated target description if any of a source file.
	 * @param context The context of static site generation.
	 * @param sourcePath The path in the site source directory.
	 * @throws IllegalArgumentException if the given source file is not in the site source tree.
	 * @return The generated target description, if present, of the resource being mummified.
	 * @throws IOException if there is an I/O error retrieving the description, including if the metadata is invalid.
	 * @see #getArtifactDescriptionPath(MummyContext, Path)
	 */
	protected Optional<UrfResourceDescription> loadTargetDescription(@Nonnull MummyContext context, @Nonnull final Path sourcePath) throws IOException {
		final Path descriptionFile = getArtifactDescriptionPath(context, sourcePath);
		if(!isRegularFile(descriptionFile)) {
			return Optional.empty();
		}
		try (final InputStream inputStream = new BufferedInputStream(newInputStream(descriptionFile))) {
			return new TurfParser<List<Object>>(new SimpleGraphUrfProcessor()).parseDocument(inputStream).stream().filter(UrfResourceDescription.class::isInstance)
					.map(UrfResourceDescription.class::cast).findFirst();
		}
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation loads the description using {@link #loadDescription(MummyContext, Path)} and then creates a new artifact using
	 *           {@link #createArtifact(Path, Path, UrfResourceDescription)}.
	 */
	@Override
	public Artifact plan(final MummyContext context, final Path sourceFile) throws IOException {
		final UrfResourceDescription description = loadDescription(context, sourceFile);
		return createArtifact(sourceFile, getArtifactTargetPath(context, sourceFile), description);
	}

	/**
	 * Creates an artifact of the appropriate type for this mummifier.
	 * @param sourceFile The file containing the source of this artifact.
	 * @param outputFile The file where the artifact will be generated.
	 * @param description The description of the artifact.
	 * @return An artifact describing the resource to be mummified.
	 * @throws IOException if there is an I/O error during planning.
	 */
	protected abstract Artifact createArtifact(@Nonnull final Path sourceFile, @Nonnull final Path outputFile, @Nonnull final UrfResourceDescription description)
			throws IOException;

	/**
	 * Determines the description for the given artifact based upon its source file and related files.
	 * @implSpec This default loads the last generated target description as used that unless the source content has been modified, in which case it loads source
	 *           metadata anew using {@link #loadSourceMetadata(MummyContext, Path)}.
	 * @param context The context of static site generation.
	 * @param sourceFile The file containing the source of this artifact in the site source directory.
	 * @return A description of the resource being mummified.
	 * @throws IOException if there is an I/O error retrieving the description, including if the metadata is invalid.
	 * @see #loadTargetDescription(MummyContext, Path)
	 * @see #loadSourceMetadata(MummyContext, Path)
	 */
	protected UrfResourceDescription loadDescription(@Nonnull MummyContext context, @Nonnull final Path sourceFile) throws IOException {
		final Optional<Instant> sourceModifiedAt = exists(sourceFile) ? Optional.of(getLastModifiedTime(sourceFile).toInstant()) : Optional.empty();
		//we'll load the target description if we can, and see if we can use it
		final Optional<UrfResourceDescription> cachedDescription = loadTargetDescription(context, sourceFile).filter(description -> {
			//check the source content modified timestamp, and discard the target description if the source content has changed at all
			final boolean sourceContentDirty = description.findPropertyValue(PROPERTY_TAG_SOURCE_MODIFIED_AT)
					//Check the timestamp against the actual file timestamp. We can compare them directly without using a range
					//because presumably we got both values from the same file, so they should be exactly the same.
					//Note also that the source modified timestamp may not be present if there is no source file; in that case assume we can't use
					//a cached target description (because we wouldn't know when it is dirty with no way to compare it with the source file).
					//Thus we will assume that the artifact needs regenerating.
					//In the future we may have a way for the mummifier to detect if a generated resource needs regenerating, even without a source file.
					//This will be important for entirely generated artifacts such as blogs. (In those cases, though, a modified mummifier will be needed
					//to get source from an alternate location, so this logic will need to be further refactored to support that.)
					.map(modifiedAt -> !isPresentAndEquals(sourceModifiedAt, modifiedAt))
					//if there is no timestamp, we consider the content dirty
					.orElse(true);
			if(sourceContentDirty) {
				return false;
			}
			//TODO check source description sidecar timestamp
			getLogger().debug("Using cached target description for source file `{}`.", sourceFile);
			return true;
		});
		return cachedDescription.orElseGet(throwingSupplier(() -> {
			final UrfObject description = new UrfObject();
			//load source metadata
			loadSourceMetadata(context, sourceFile).forEach(meta -> {
				final URI propertyTag = meta.getKey();
				final Object propertyValue = meta.getValue();
				if(!description.hasPropertyValue(propertyTag)) { //the first property wins
					description.setPropertyValue(propertyTag, propertyValue);
				}
			});
			//TODO load any description sidecar			
			//add the content type
			getArtifactMediaType(context, sourceFile).ifPresent(mediaType -> description.setPropertyValue(Content.TYPE_PROPERTY_TAG, mediaType));
			//add the source modification timestamp, if any
			sourceModifiedAt.ifPresent(instant -> description.setPropertyValue(PROPERTY_TAG_SOURCE_MODIFIED_AT, instant));
			description.setPropertyValue(PROPERTY_TAG_DIRTY, true); //we created a new description, so the resource needs to be generated
			return description;
		})); //TODO add a way to make this immutable?
	}

	/**
	 * Loads any metadata stored in the source file itself, if applicable.
	 * @apiNote This method ignores any metadata stored in related files such as sidecar files.
	 * @param context The context of static site generation.
	 * @param sourceFile The source file to be mummified.
	 * @return Metadata stored in the source file being mummified, consisting of resolved URI tag names and values. The name-value pairs may have duplicate names.
	 * @throws IOException if there is an I/O error retrieving the metadata.
	 */
	protected abstract List<Map.Entry<URI, Object>> loadSourceMetadata(@Nonnull MummyContext context, @Nonnull final Path sourceFile) throws IOException;

}
