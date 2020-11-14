/*
 * Copyright © 2019 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import java.io.*;
import java.nio.file.Path;
import java.util.*;

import javax.annotation.*;

import com.globalmentor.java.Objects;

import io.guise.mummy.*;
import io.urf.model.*;
import io.urf.turf.*;

/**
 * Abstract mummifier for generating artifacts based upon a single source file or directory.
 * @author Garret Wilson
 */
public abstract class AbstractSourcePathMummifier extends AbstractMummifier implements SourcePathMummifier {

	/**
	 * Determines the path for an artifact source description sidecar for the given artifact.
	 * @apiNote Whether a source description file is supported depends on the specific mummifier implementation.
	 * @implSpec This implementation delegates to {@link #getArtifactSourceDescriptionFile(MummyContext, Path)} using {@link Artifact#getSourcePath()}.
	 * @param context The context of static site generation.
	 * @param artifact The artifact for which a source description file should be returned.
	 * @return The path in the site source directory where a description sidecar might be found.
	 * @throws IllegalArgumentException if the given source file is not in the target source tree.
	 * @see Artifact#getSourcePath()
	 */
	protected Path getArtifactSourceDescriptionFile(final @Nonnull MummyContext context, final @Nonnull Artifact artifact) {
		return getArtifactSourceDescriptionFile(context, artifact.getTargetPath());
	}

	/**
	 * Determines the path for an artifact source description sidecar based upon the source path.
	 * @apiNote Whether a source description file is supported depends on the specific mummifier implementation.
	 * @implSpec The default implementation produces a filename based upon the source path filename with the {@link Mummifier#DESCRIPTION_FILE_SIDECAR_EXTENSION}
	 *           added.
	 * @param context The context of static site generation.
	 * @param sourcePath The path in the site source directory.
	 * @return The path in the site source directory where a description sidecar might be found.
	 * @throws IllegalArgumentException if the given source file is not in the target source tree.
	 * @see Mummifier#DESCRIPTION_FILE_SIDECAR_EXTENSION
	 */
	protected Path getArtifactSourceDescriptionFile(final @Nonnull MummyContext context, final @Nonnull Path sourcePath) {
		return addFilenameExtension(sourcePath, DESCRIPTION_FILE_SIDECAR_EXTENSION);
	}

	/**
	 * Loads the source description sidecar of an artifact based upon its source path.
	 * @param context The context of static site generation.
	 * @param sourcePath The path in the site target directory (not the path of the target description itself).
	 * @throws IllegalArgumentException if the given source file is not in the site source tree.
	 * @return The generated target description, if present, of the resource being mummified.
	 * @throws IOException if there is an I/O error retrieving the description, including if the metadata is invalid.
	 * @see #getArtifactSourceDescriptionFile(MummyContext, Path)
	 */
	protected Optional<UrfResourceDescription> loadArtifactSourceDescription(@Nonnull MummyContext context, @Nonnull final Path sourcePath) throws IOException {
		final Path descriptionFile = getArtifactSourceDescriptionFile(context, sourcePath);
		if(!isRegularFile(descriptionFile)) {
			return Optional.empty();
		}
		try (final InputStream inputStream = new BufferedInputStream(newInputStream(descriptionFile))) {
			return new TurfParser<List<Object>>(new SimpleGraphUrfProcessor()).parseDocument(inputStream, TURF.PROPERTIES_MEDIA_TYPE).stream()
					.flatMap(Objects.asInstances(UrfResourceDescription.class)).findFirst();
		}
	}

}
