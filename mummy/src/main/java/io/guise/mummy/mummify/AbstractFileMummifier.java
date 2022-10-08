/*
 * Copyright © 2019-2020 GlobalMentor, Inc. <https://www.globalmentor.com/>
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

package io.guise.mummy.mummify;

import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.util.Optionals.*;
import static io.guise.mummy.Artifact.*;
import static java.nio.file.Files.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.*;
import java.net.URI;
import java.nio.file.Path;
import java.time.*;
import java.util.*;
import java.util.regex.*;

import javax.annotation.*;

import io.guise.mummy.*;
import io.urf.model.*;
import io.urf.vocab.content.Content;

/**
 * Abstract mummifier for generating artifacts based upon a single source file.
 * @implSpec This type of mummifier only works with {@link CorporealSourceArtifact}s. If some other type of artifact is used, some mummification methods will
 *           throw a {@link ClassCastException}.
 * @implSpec This implementation recognizes posts based upon {@link SourcePathArtifact#POST_FILENAME_PATTERN} and adds an appropriate
 *           {@link Artifact#PROPERTY_HANDLE_PUBLISHED_ON} property to the description.
 * @implNote This implementation does not yet support source description files using {@link #getArtifactSourceDescriptionFile(MummyContext, Artifact)} or
 *           {@link #getArtifactSourceDescriptionFile(MummyContext, Path)}.
 * @author Garret Wilson
 */
public abstract class AbstractFileMummifier extends AbstractSourcePathMummifier {

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation loads the description using {@link #loadArtifactDescription(MummyContext, Path, Path)} and then creates a new artifact using
	 *           {@link #createArtifact(MummyContext, Path, Path, UrfResourceDescription)}.
	 */
	@Override
	public Artifact plan(final MummyContext context, final Path sourceFile, final Path targetFile) throws IOException {
		getLogger().trace("Planning artifact for source file `{}` ...", sourceFile);
		final UrfResourceDescription description = loadArtifactDescription(context, sourceFile, targetFile);
		return createArtifact(context, sourceFile, targetFile, description);
	}

	/**
	 * Creates an artifact of the appropriate type for this mummifier.
	 * @implSpec The default implementation returns an instance of {@link DefaultSourceFileArtifact}.
	 * @param context The context of static site generation.
	 * @param sourceFile The file containing the source of this artifact.
	 * @param outputFile The file where the artifact will be generated.
	 * @param description The description of the artifact.
	 * @return An artifact describing the resource to be mummified.
	 * @throws IOException if there is an I/O error during planning.
	 */
	protected Artifact createArtifact(@Nonnull final MummyContext context, @Nonnull final Path sourceFile, @Nonnull final Path outputFile,
			@Nonnull final UrfResourceDescription description) throws IOException {
		return new DefaultSourceFileArtifact(this, sourceFile, outputFile, description);
	}

	/**
	 * Determines the description for the given artifact based upon its source file and related files.
	 * @implSpec If incremental mummification is enabled via {@link MummyContext#isIncremental()}, this implementation loads the last generated target description
	 *           and uses that. If full mummification is turned on or the source content has been modified, it loads source metadata anew using
	 *           {@link #loadSourceMetadata(MummyContext, Path)}.
	 * @param context The context of static site generation.
	 * @param sourceFile The file containing the source of this artifact in the site source directory.
	 * @param targetFile The target path in the site target directory for the artifact.
	 * @return A description of the resource being mummified.
	 * @throws IOException if there is an I/O error retrieving the description, including if the metadata is invalid.
	 * @see #loadArtifactTargetDescription(MummyContext, Path)
	 * @see #loadSourceMetadata(MummyContext, Path)
	 * @see MummyContext#isIncremental()
	 * @see MummyContext#isFull()
	 */
	protected UrfResourceDescription loadArtifactDescription(@Nonnull MummyContext context, @Nonnull final Path sourceFile, @Nonnull final Path targetFile)
			throws IOException {
		final Optional<Instant> sourceModifiedAt = exists(sourceFile) ? Optional.of(getLastModifiedTime(sourceFile).toInstant()) : Optional.empty();
		final Optional<UrfResourceDescription> cachedDescription;
		if(context.isIncremental()) {
			//we'll load the target description if we can, and see if we can use it
			cachedDescription = loadArtifactTargetDescription(context, targetFile).filter(description -> {
				//check the source content modified timestamp, and discard the target description if the source content has changed at all
				final boolean sourceContentDirty = description.findPropertyValue(PROPERTY_TAG_MUMMY_SOURCE_CONTENT_MODIFIED_AT)
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
				getLogger().debug("Using previously generated target description to describe source file `{}`.", sourceFile);
				return true;
			});
		} else { //full mummification
			cachedDescription = Optional.empty();
		}
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
			//add the publication date for posts
			final Path filename = sourceFile.getFileName();
			if(filename != null) {
				final Matcher postMatcher = SourcePathArtifact.POST_FILENAME_PATTERN.matcher(filename.toString());
				if(postMatcher.matches()) { //if this is a post, add a `publishedOn` property if there isn't one already
					if(!description.hasPropertyValueByHandle(PROPERTY_HANDLE_PUBLISHED_ON)) {
						final String postYear = postMatcher.group(SourcePathArtifact.POST_FILENAME_PATTERN_YEAR_GROUP);
						final String postMonth = postMatcher.group(SourcePathArtifact.POST_FILENAME_PATTERN_MONTH_GROUP);
						final String postDay = postMatcher.group(SourcePathArtifact.POST_FILENAME_PATTERN_DAY_GROUP);
						//the regex will guarantee that these are parsable as integers 
						final LocalDate publishedOn = LocalDate.of(Integer.parseInt(postYear), Integer.parseInt(postMonth), Integer.parseInt(postDay));
						description.setPropertyValueByHandle(PROPERTY_HANDLE_PUBLISHED_ON, publishedOn);
					}
				}
			}
			//add the content type
			getArtifactMediaType(context, sourceFile).ifPresent(mediaType -> description.setPropertyValue(Content.TYPE_PROPERTY_TAG, mediaType));
			//add the source modification timestamp, if any
			sourceModifiedAt.ifPresent(instant -> description.setPropertyValue(PROPERTY_TAG_MUMMY_SOURCE_CONTENT_MODIFIED_AT, instant));
			description.setPropertyValue(PROPERTY_TAG_MUMMY_DESCRIPTION_DIRTY, true); //we created a new description, so the description needs to be persisted
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
	protected abstract List<Map.Entry<URI, Object>> loadSourceMetadata(@Nonnull MummyContext context, @Nonnull Path sourceFile) throws IOException;

	/**
	 * {@inheritDoc}
	 * @apiNote This method cannot be overridden, as it performs necessary checks for incremental mummification. To implement file mummification,
	 *          {@link #mummifyFile(MummyContext, CorporealSourceArtifact)} should be overridden instead.
	 * @implSpec If incremental mummification is enabled via {@link MummyContext#isIncremental()}, this version checks the the timestamp of the target file, and
	 *           delegates to {@link #mummifyFile(MummyContext, CorporealSourceArtifact)} if the file needs regenerated.
	 * @implSpec This implementation saves the description description if modified by calling {@link #saveTargetDescription(MummyContext, Artifact)}.
	 * @throws ClassCastException if the given artifact is not an instance of {@link CorporealSourceArtifact}.
	 * @see Content#MODIFIED_AT_PROPERTY_TAG
	 * @see Artifact#PROPERTY_TAG_MUMMY_DESCRIPTION_DIRTY
	 * @see MummyContext#isIncremental()
	 * @see MummyContext#isFull()
	 */
	@Override
	public final void mummify(@Nonnull final MummyContext context, @Nonnull Artifact artifact) throws IOException {
		getLogger().trace("Mummifying file artifact {} ...", artifact);
		final Path targetFile = artifact.getTargetPath();
		final UrfResourceDescription description = artifact.getResourceDescription();
		final Optional<Instant> oldTargetModifiedAt;
		final boolean targetContentDirty;
		if(context.isIncremental()) {
			oldTargetModifiedAt = exists(targetFile) ? Optional.of(getLastModifiedTime(targetFile).toInstant()) : Optional.empty();
			targetContentDirty = description.findPropertyValue(Content.MODIFIED_AT_PROPERTY_TAG)
					.map(modifiedAt -> !isPresentAndEquals(oldTargetModifiedAt, modifiedAt))
					//if there is no timestamp, we consider the content dirty
					.orElse(true);
		} else { //full mummification
			targetContentDirty = true;
			oldTargetModifiedAt = Optional.empty(); //no need to check the old target modification timestamp if we're doing full mummification
		}
		//produce target file if dirty
		final Instant newTargetModifiedAt;
		if(targetContentDirty) {
			final Path parentDirectory = targetFile.getParent();
			if(parentDirectory != null && !exists(parentDirectory)) { //ensure parent directories exist, as artifact children may specify files several layers deep, e.g. blog posts 
				createDirectories(parentDirectory);
			}
			mummifyFile(context, (CorporealSourceArtifact)artifact);
			checkState(exists(targetFile), "Mummification of artifact source file `%s` did not produce target file `%s`.", artifact.getSourcePath(), targetFile);
			getLogger().debug("Mummified file artifact {}.", artifact);
			newTargetModifiedAt = getLastModifiedTime(targetFile).toInstant();
		} else {
			getLogger().debug("Using previously generated target file `{}`.", targetFile);
			newTargetModifiedAt = oldTargetModifiedAt
					.orElseThrow(() -> new AssertionError("If the old target timestamp was not present, the target content should have been marked as dirty."));
		}
		//produce description file if dirty
		final boolean targetDescriptionDirty = targetContentDirty //checking content dirtiness inherently covers a missing or out of date target timestamp
				//no need to check existence; if the description file didn't exist, the description should have been marked as dirty
				|| description.hasPropertyValue(PROPERTY_TAG_MUMMY_DESCRIPTION_DIRTY)
				//require a fingerprint property (checking content dirtiness takes care of outdated fingerprint) 
				|| !description.hasPropertyValue(Content.FINGERPRINT_PROPERTY_TAG);
		if(targetDescriptionDirty) {
			description.setPropertyValue(Content.MODIFIED_AT_PROPERTY_TAG, newTargetModifiedAt); //update the target file timestamp
			description.setPropertyValue(Content.FINGERPRINT_PROPERTY_TAG, FINGERPRINT_ALGORITHM.digest(targetFile)); //update the target fingerprint
			description.removeProperty(PROPERTY_TAG_MUMMY_DESCRIPTION_DIRTY); //remove the description dirty flag, if any
			try {
				saveTargetDescription(context, artifact);
			} catch(final IOException ioException) {
				//If there is any I/O error saving the description, set its dirty flag for completeness
				//(although its usefulness at this point is questionable).
				description.setPropertyValue(PROPERTY_TAG_MUMMY_DESCRIPTION_DIRTY, true);
				throw ioException;
			}
		} else {
			getLogger().debug("Using previously generated target description file `{}`.", getArtifactTargetDescriptionFile(context, artifact));
		}
	}

	/**
	 * Invariably mummifies a resource to a file in the presence of a context artifact, which may or may not be the same as the artifact itself. Mummification is
	 * always performed, regardless of the state of metadata.
	 * @param context The context of static site generation.
	 * @param artifact The artifact being generated.
	 * @throws IOException if there is an I/O error during mummification.
	 */
	protected abstract void mummifyFile(@Nonnull final MummyContext context, @Nonnull CorporealSourceArtifact artifact) throws IOException;

}
