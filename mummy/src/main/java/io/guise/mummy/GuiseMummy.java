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

package io.guise.mummy;

import static com.globalmentor.io.Filenames.*;
import static com.globalmentor.io.Paths.*;
import static com.globalmentor.java.Conditions.*;
import static java.nio.file.Files.*;
import static java.util.function.Predicate.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

import io.clogr.Clogged;

/**
 * Guise static site generator.
 * @author Garret Wilson
 */
public class GuiseMummy implements Clogged {

	/** The registered mummifiers by supported extensions. */
	private final Map<String, ResourceMummifier> mummifiersByExtension = new HashMap<>();

	//TODO document
	protected Optional<ResourceMummifier> getMummifier(@Nonnull final Path resourcePath) {
		//TODO create Paths.extensions
		return extensions(resourcePath.getFileName().toString()).map(mummifiersByExtension::get).filter(Objects::nonNull).findFirst();
	}

	/**
	 * Registers a mummify for all its supported filename extensions.
	 * @param mummifier The mummifier to register.
	 * @see ResourceMummifier#getSupportedFilenameExtensions()
	 */
	public void registerMummifier(@Nonnull final ResourceMummifier mummifier) {
		mummifier.getSupportedFilenameExtensions().forEach(ext -> mummifiersByExtension.put(ext, mummifier));
	}

	/** No-args constructor. */
	public GuiseMummy() {
		//register default resource types
		registerMummifier(new XhtmlMummifier());
	}

	/**
	 * Determines whether a path should be ignored during discovery.
	 * @implSpec This specification currently only ignores dotfiles, for example <code>.git</code> and <code>.gitignore</code>.
	 * @param path The path to check.
	 * @return <code>true</code> if the path should be ignored and excluded from processing.
	 */
	protected boolean isIgnore(@Nonnull final Path path) {
		if(isDotfile(path)) { //ignore dotfiles
			return true;
		}
		return false;
	}

	/**
	 * TODO document
	 * @param sourceDirectory The root of the site to be mummified.
	 * @param targetDirectory The root directory of the generated static site; will be created if needed.
	 * @throws IOException if there is an I/O error generating the static site.
	 */
	public void mummify(@Nonnull final Path sourceDirectory, @Nonnull final Path targetDirectory) throws IOException {
		checkArgument(isDirectory(sourceDirectory), "Source %s does not exist or is not a directory.");
		createDirectories(targetDirectory);

		final MummifyContext context = new MummifyContext() {}; //TODO testing

		//TODO load `.guiseignore` file for each directory

		final Set<Path> childDirectories = new LinkedHashSet<>();

		try (final Stream<Path> resourcePaths = list(sourceDirectory).filter(not(this::isIgnore))) {
			resourcePaths.forEach(resourcePath -> {

				final Path targetPath = targetDirectory.resolve(resourcePath.getFileName()); //TODO transfer to some common location; maybe during discovery

				System.out.println(resourcePath);
				if(isDirectory(resourcePath)) {
					childDirectories.add(resourcePath);
				} else if(isRegularFile(resourcePath)) {
					getMummifier(resourcePath).ifPresent(throwingConsumer(mummifier -> {
						final Path outputPath = changeExtension(targetPath, "html"); //switch to generating an HTML file TODO use constant
						mummifier.mummify(context, resourcePath, outputPath);
					}));
				} else {
					getLogger().warn("Skipping non-regular file {}.", resourcePath);
				}
			});
		}

		//child directories
		for(final Path childDirectory : childDirectories) {
			final Path targetChildDirectory = targetDirectory.resolve(childDirectory.getFileName()); //TODO transfer to some common location; maybe during discovery
			mummify(childDirectory, targetChildDirectory);
		}

	}

}
