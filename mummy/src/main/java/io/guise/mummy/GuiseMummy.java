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
import static com.globalmentor.net.URIs.ROOT_PATH;
import static java.nio.file.Files.*;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

import com.globalmentor.net.URIPath;
import com.globalmentor.net.URIs;

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
		final Context context = new Context(sourceDirectory);
		mummify(context, sourceDirectory, targetDirectory);
	}

	/**
	 * TODO document
	 * @param context The current mummification context.
	 * @param sourceDirectory The root of the site to be mummified.
	 * @param targetDirectory The root directory of the generated static site; will be created if needed.
	 * @throws IOException if there is an I/O error generating the static site.
	 */
	protected void mummify(@Nonnull Context context, @Nonnull final Path sourceDirectory, @Nonnull final Path targetDirectory) throws IOException {
		checkArgument(isDirectory(sourceDirectory), "Source %s does not exist or is not a directory.");
		createDirectories(targetDirectory);

		//TODO use or delete: final URI contextRoot = sourceDirectory.toUri();

		//TODO load `.guiseignore` file for each directory

		final Set<Path> childDirectories = new LinkedHashSet<>();

		try (final Stream<Path> resourcePaths = list(sourceDirectory).filter(not(this::isIgnore))) {
			resourcePaths.forEach(resourcePath -> {

				final boolean isCollection = resourcePath.getFileName().toString().equals("index.xhtml"); //TODO allow configuration; decide whether to use resource or artifact to determine
				final URIPath resourceContextPath;
				{
					final URI siteSourceUri = context.getSiteSourceDirectory().toUri();
					final URI resourceUri = resourcePath.toUri();
					final URI relativeResourcePath = siteSourceUri.relativize(resourceUri);
					final URIPath nonnormalizedContextPath = new URIPath(ROOT_PATH + relativeResourcePath.toString()); //TODO create URIPath method for context path manipulation
					//normalize the "index" input resource to be synonymous with its collection
					resourceContextPath = isCollection ? nonnormalizedContextPath.getCurrentLevel() : nonnormalizedContextPath;
				}

				final Path targetPath = targetDirectory.resolve(resourcePath.getFileName()); //TODO transfer to some common location; maybe during discovery

				System.out.println(resourcePath);
				if(isDirectory(resourcePath)) {
					childDirectories.add(resourcePath);
				} else if(isRegularFile(resourcePath)) {
					getMummifier(resourcePath).ifPresent(throwingConsumer(mummifier -> {

						final Path outputFile = changeExtension(targetPath, "html"); //switch to generating an HTML file TODO use constant

						final Page page = new Context.Page(resourceContextPath, isCollection, resourcePath, outputFile);
						context.setArtifact(page); //TODO unset the artifact later?

						System.out.println("*** Ready to mummify: " + page);
						mummifier.mummify(context, resourcePath, outputFile);
					}));
				} else {
					getLogger().warn("Skipping non-regular file {}.", resourcePath);
				}
			});
		}

		//child directories
		for(final Path childDirectory : childDirectories) {
			final Path targetChildDirectory = targetDirectory.resolve(childDirectory.getFileName()); //TODO transfer to some common location; maybe during discovery
			mummify(context, childDirectory, targetChildDirectory);
		}

	}

	/**
	 * Mutable mummification context controlled by Guise Mummy.
	 * @author Garret Wilson
	 */
	protected static class Context implements MummifyContext {

		private final Path siteSourceDirectory;

		@Override
		public Path getSiteSourceDirectory() {
			return siteSourceDirectory;
		}

		/**
		 * Site source directory constructor.
		 * @param siteSourceDirectory The source directory of the entire site.
		 */
		public Context(@Nonnull final Path siteSourceDirectory) {
			this.siteSourceDirectory = requireNonNull(siteSourceDirectory);
		}

		private Artifact artifact;

		@Override
		public Artifact getArtifact() {
			return artifact;
		}

		/**
		 * Sets the current artifact, such as a {@link Page}, being mummified.
		 * @param artifact The current artifact being mummified.
		 */
		public void setArtifact(@Nonnull final Artifact artifact) {
			this.artifact = requireNonNull(artifact);
		}

		//TODO document
		protected static class Page extends AbstractPage {

			/**
			 * Source resource context path constructor.
			 * @param resourceContextPath The absolute path of the resource, relative to the site context.
			 * @param sourceFile The file containing the source of this artifact.
			 * @param outputFile The file where the artifact will be generated.
			 * @throws IllegalArgumentException if the given context path is not absolute.
			 */
			public Page(@Nonnull final URIPath resourceContextPath, final boolean isCollection, @Nonnull final Path sourceFile, @Nonnull final Path outputFile) {
				super(resourceContextPath, isCollection, sourceFile, outputFile);
			}

		}

	}

}
