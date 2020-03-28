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

package io.guise.mummy.mummify;

import static com.globalmentor.io.Paths.*;
import static java.nio.file.Files.*;
import static java.util.Objects.*;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

import javax.annotation.*;

import io.guise.mummy.Artifact;
import io.guise.mummy.MummyContext;
import io.urf.model.*;
import io.urf.turf.TurfParser;

/**
 * An abstract mummifier to serve as a base class for mummifiers generally.
 * @author Garret Wilson
 */
public abstract class AbstractMummifier implements Mummifier {

	/**
	 * {@inheritDoc}
	 * @implSpec This version merely returns the given filename unmodified.
	 */
	@Override
	public String planArtifactTargetFilename(final MummyContext context, final String filename) {
		return requireNonNull(filename);
	}

	/**
	 * Determines the output file path for an artifact description in the site description target directory for the given artifact.
	 * @implSpec This implementation delegates to {@link #getArtifactDescriptionFile(MummyContext, Path)} using {@link Artifact#getTargetPath()}.
	 * @param context The context of static site generation.
	 * @param artifact The artifact for which a description file should be returned.
	 * @return The path in the site description target directory to which a description may be generated.
	 * @throws IllegalArgumentException if the given source file is not in the target source tree.
	 * @see Artifact#getTargetPath()
	 */
	protected Path getArtifactDescriptionFile(final @Nonnull MummyContext context, final @Nonnull Artifact artifact) {
		return getArtifactDescriptionFile(context, artifact.getTargetPath());
	}

	/**
	 * Determines the output file path for an artifact description in the site description target directory based upon the target path in the site target
	 * directory.
	 * @implSpec The default implementation produces a filename based upon the target path filename, but in the
	 *           {@link MummyContext#getSiteDescriptionTargetDirectory()} directory.
	 * @param context The context of static site generation.
	 * @param targetPath The path in the site source directory.
	 * @return The path in the site description target directory to which a description may be generated.
	 * @throws IllegalArgumentException if the given source file is not in the target source tree.
	 * @see MummyContext#getSiteDescriptionTargetDirectory()
	 */
	protected Path getArtifactDescriptionFile(final @Nonnull MummyContext context, final @Nonnull Path targetPath) {
		return addExtension(changeBase(targetPath, context.getSiteTargetDirectory(), context.getSiteDescriptionTargetDirectory()), "@.turf"); //TODO use constant
	}

	/**
	 * Loads the generated target description if any of a source file.
	 * @param context The context of static site generation.
	 * @param targetPath The path in the site target directory.
	 * @throws IllegalArgumentException if the given source file is not in the site source tree.
	 * @return The generated target description, if present, of the resource being mummified.
	 * @throws IOException if there is an I/O error retrieving the description, including if the metadata is invalid.
	 * @see #getArtifactDescriptionFile(MummyContext, Path)
	 */
	protected Optional<UrfResourceDescription> loadTargetDescription(@Nonnull MummyContext context, @Nonnull final Path targetPath) throws IOException {
		final Path descriptionFile = getArtifactDescriptionFile(context, targetPath);
		if(!isRegularFile(descriptionFile)) {
			return Optional.empty();
		}
		try (final InputStream inputStream = new BufferedInputStream(newInputStream(descriptionFile))) {
			return new TurfParser<List<Object>>(new SimpleGraphUrfProcessor()).parseDocument(inputStream).stream().filter(UrfResourceDescription.class::isInstance)
					.map(UrfResourceDescription.class::cast).findFirst();
		}
	}

}
