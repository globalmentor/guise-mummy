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
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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
	protected Optional<ResourceMummifier> getMummifier(@Nonnull final Path sourcePath) {
		if(isDirectory(sourcePath)) {
			return Optional.of(new DirectoryMummifier()); //TODO use a shared directory mummifier
		}

		//TODO create Paths.extensions
		return extensions(sourcePath.getFileName().toString()).map(mummifiersByExtension::get).filter(Objects::nonNull).findFirst();
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

		//describe phase
		final Artifact rootArtifact = describeSourceDirectory(context, sourceDirectory, targetDirectory);
		printArtifactDescription(rootArtifact);

		//mummify phase
		mummify(context, rootArtifact);

	}

	private void printArtifactDescription(@Nonnull final Artifact artifact) { //TODO transfer to CLI
		System.out.println(artifact.getOutputFile());
		if(artifact instanceof CollectionArtifact) {
			for(final Artifact childArtifact : ((CollectionArtifact)artifact).getChildArtifacts()) {
				printArtifactDescription(childArtifact);
			}
		}
	}

	/**
	 * TODO document
	 * @param context The current mummification context.
	 * @param sourceDirectory The root of the site to be mummified.
	 * @param targetDirectory The root directory of the generated static site; will be created if needed.
	 * @throws IOException if there is an I/O error generating the static site.
	 */
	protected void mummify(@Nonnull Context context, @Nonnull Artifact artifact) throws IOException {
		artifact.getMummifier().mummify(context, artifact);
	}

	//TODO fix private final Map<Path, Artifact> artifactsBySourcePath = new HashMap<>();

	protected Optional<Path> discoverSourceDirectoryContentFile(@Nonnull Context context, @Nonnull final Path sourceDirectory) {
		final Path directoryFile = sourceDirectory.resolve("index.xhtml"); //TODO provide a formal lookup mechanism
		if(Files.isRegularFile(directoryFile)) {
			return Optional.of(directoryFile);
		}
		return Optional.empty();
	}

	/**
	 * Recursively describes a source directory and all its children.
	 * @param context The current mummification context.
	 * @param sourceDirectory The root of the site to be mummified.
	 * @param targetDirectory The root directory of the generated static site; will be created if needed.
	 * @throws IOException if there is an I/O error generating the static site.
	 */
	protected Artifact describeSourceDirectory(@Nonnull Context context, @Nonnull final Path sourceDirectory, @Nonnull final Path targetDirectory)
			throws IOException {
		checkArgument(isDirectory(sourceDirectory), "Source %s does not exist or is not a directory.");

		//TODO probably delegate to the directory mummifier to do the description, allowing special directories with special mummifiers
		final ResourceMummifier directoryMummifier = getMummifier(sourceDirectory)
				.orElseThrow(() -> new IllegalStateException("No directory mummifier registered."));

		//discover and describe the directory content file, if present
		final Optional<Path> contentFile = discoverSourceDirectoryContentFile(context, sourceDirectory);
		final Artifact contentArtifact = contentFile.map(throwingFunction(contentSourceFile -> {
			final ResourceMummifier contentMummifier = getMummifier(contentSourceFile).orElseThrow(IllegalStateException::new);
			final Path contentTargetFile = targetDirectory.resolve(contentSourceFile.getFileName()); //TODO transfer to some common location; maybe during discovery
			final Path contentOutputFile = changeExtension(contentTargetFile, "html"); //switch to generating an HTML file TODO get from mummifier
			return describeSourceFile(contentMummifier, contentSourceFile, contentOutputFile);
		})).orElse(null);

		//discover and describe the child artifacts
		final List<Artifact> childArtifacts = new ArrayList<>();
		try (final Stream<Path> childPaths = list(sourceDirectory).filter(not(this::isIgnore))) {
			childPaths.forEach(throwingConsumer(childSourcePath -> {
				final Path childTargetPath = targetDirectory.resolve(childSourcePath.getFileName().toString()); //TODO transfer to some common location; maybe during discovery
				if(isDirectory(childSourcePath)) {
					childArtifacts.add(describeSourceDirectory(context, childSourcePath, childTargetPath));
				} else if(isRegularFile(childSourcePath)) {
					if(!childSourcePath.equals(contentFile.orElse(null))) { //don't return the directory content file TODO create Optional equals utility method
						getMummifier(childSourcePath).ifPresent(throwingConsumer(mummifier -> {
							final Path childOutputFile = changeExtension(childTargetPath, "html"); //switch to generating an HTML file TODO get from mummifier
							//TODO probably delegate to the directory mummifier to do the description
							childArtifacts.add(describeSourceFile(mummifier, childSourcePath, childOutputFile));
						}));
					}
				} else {
					getLogger().warn("Skipping non-regular file {}.", childSourcePath);
				}
			}));
		}
		return new DirectoryArtifact(directoryMummifier, sourceDirectory, targetDirectory, contentArtifact, childArtifacts);
	}

	protected Artifact describeSourceFile(@Nonnull final ResourceMummifier mummifier, @Nonnull final Path sourceFile, @Nonnull final Path outputFile) {
		//TODO in the future probably just pass the source file, and have a strategy that determines that output file, based on the artifact type
		return new Context.Page(mummifier, sourceFile, outputFile);
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
			public Page(/*TODO fix @Nonnull final URIPath resourceContextPath, final boolean isCollection, */@Nonnull final ResourceMummifier mummifier,
					@Nonnull final Path sourceFile, @Nonnull final Path outputFile) {
				super(/*TODO fix resourceContextPath, isCollection, */mummifier, sourceFile, outputFile);
			}

		}
	}

}
