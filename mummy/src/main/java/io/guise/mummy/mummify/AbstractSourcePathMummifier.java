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
import static io.guise.mummy.GuiseMummy.*;
import static java.nio.file.Files.*;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.*;

import io.confound.config.*;
import io.guise.mummy.GuiseMummy;
import io.guise.mummy.MummyContext;
import io.urf.model.*;
import io.urf.turf.TurfParser;

/**
 * Abstract mummifier for generating artifacts based upon a single source file or directory.
 * @author Garret Wilson
 */
public abstract class AbstractSourcePathMummifier implements SourcePathMummifier {

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation merely changes the base between source and target directory trees.
	 * @implSpec This version also recognizes veiled artifacts and renames them as necessary according to the veil name pattern configured with the key
	 *           {@value GuiseMummy#CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN}, both for files and for directories.
	 * @throws ConfigurationException if the given veil name pattern specifies more than one matching group.
	 * @see MummyContext#getSiteSourceDirectory()
	 * @see MummyContext#getSiteTargetDirectory()
	 * @see GuiseMummy#CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN
	 */
	@Override
	public Path getArtifactTargetPath(@Nonnull MummyContext context, @Nonnull final Path sourcePath) {
		//switch from source to target
		Path targetPath = changeBase(sourcePath, context.getSiteSourceDirectory(), context.getSiteTargetDirectory());
		//perform path transformations
		final Path filename = targetPath.getFileName();
		if(filename != null) {
			final String filenameString = filename.toString();
			//veiled artifacts
			final Pattern veilPattern = context.getConfiguration().getObject(CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN, Pattern.class);
			final Matcher veilMatcher = veilPattern.matcher(filenameString);
			if(veilMatcher.matches()) {
				Configuration.check(veilMatcher.groupCount() <= 1, "Veil name pattern /%s/ configured with key `%s` can have at most one matching group.", veilPattern,
						CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN);
				if(veilMatcher.groupCount() > 0) {
					final String newFilename = veilMatcher.group(1);
					if(newFilename != null) { //rename the file as indicated by the match
						//TODO add more appropriate checks and don't use literals; see https://stackoverflow.com/q/60834114/421049
						Configuration.check(!newFilename.equals(".") && !newFilename.equals(".."),
								"Veil name pattern /%s/ configured with key `%s` when applied `%s` resulted in forbidden, special name `%s`.", veilPattern,
								CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN, filenameString, newFilename);
						targetPath = targetPath.resolveSibling(newFilename);
					}
				}
			}
		}
		return targetPath;
	}

	/**
	 * Loads the generated target description if any of a source file.
	 * @param context The context of static site generation.
	 * @param sourcePath The path in the site source directory.
	 * @throws IllegalArgumentException if the given source file is not in the site source tree.
	 * @return The generated target description, if present, of the resource being mummified.
	 * @throws IOException if there is an I/O error retrieving the description, including if the metadata is invalid.
	 * @see #getArtifactDescriptionFile(MummyContext, Path)
	 */
	protected Optional<UrfResourceDescription> loadTargetDescription(@Nonnull MummyContext context, @Nonnull final Path sourcePath) throws IOException {
		final Path descriptionFile = getArtifactDescriptionFile(context, sourcePath);
		if(!isRegularFile(descriptionFile)) {
			return Optional.empty();
		}
		try (final InputStream inputStream = new BufferedInputStream(newInputStream(descriptionFile))) {
			return new TurfParser<List<Object>>(new SimpleGraphUrfProcessor()).parseDocument(inputStream).stream().filter(UrfResourceDescription.class::isInstance)
					.map(UrfResourceDescription.class::cast).findFirst();
		}
	}

}
