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
import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.*;

import javax.annotation.*;

import io.clogr.Clogged;
import io.urf.turf.TurfSerializer;

/**
 * Guise static site generator.
 * @author Garret Wilson
 */
public class GuiseMummy implements Clogged {

	/** The string form or namespace of Guise Mummy elements, such as in an XHTML document or as the leading IRI segment of RDFa metadata. */
	public static final String NAMESPACE_STRING = "https://guise.io/name/mummy/";

	/** The namespace of Guise Mummy elements, such as in an XHTML document or as the leading IRI segment of RDFa metadata. */
	public static final URI NAMESPACE = URI.create(NAMESPACE_STRING);

	/** The default mummifier for normal files. */
	private final Mummifier defaultFileMummifier = new OpaqueFileMummifier();

	/** The default mummifier for normal directories. */
	private final Mummifier defaultDirectoryMummifier = new DirectoryMummifier();

	/** The registered mummifiers by supported extensions. */
	private final Map<String, Mummifier> fileMummifiersByExtension = new HashMap<>();

	/**
	 * Registers a mummify for all its supported filename extensions.
	 * @param mummifier The mummifier to register.
	 * @see Mummifier#getSupportedFilenameExtensions()
	 */
	public void registerFileMummifier(@Nonnull final Mummifier mummifier) {
		mummifier.getSupportedFilenameExtensions().forEach(ext -> fileMummifiersByExtension.put(ext, mummifier));
	}

	/** No-args constructor. */
	public GuiseMummy() {
		//register default resource types
		registerFileMummifier(new XhtmlPageMummifier());
	}

	/**
	 * Performs static site generation on a source directory into a target directory.
	 * @param sourceDirectory The root of the site to be mummified.
	 * @param targetDirectory The root directory of the generated static site; will be created if needed.
	 * @throws IllegalArgumentException if the source directory does not exist or is not a directory.
	 * @throws IllegalArgumentException if the source and target directories overlap.
	 * @throws IOException if there is an I/O error generating the static site.
	 */
	public void mummify(@Nonnull final Path sourceDirectory, @Nonnull final Path targetDirectory) throws IOException {
		final Context context = new Context(sourceDirectory, targetDirectory);

		//#plan phase
		final Artifact rootArtifact = new DirectoryMummifier().plan(context, sourceDirectory); //TODO create special SiteMummifier extending DirectoryMummifier
		context.updatePlan(rootArtifact);

		printArtifactDescription(context, rootArtifact);

		//#mummify phase
		rootArtifact.getMummifier().mummify(context, rootArtifact);
	}

	//TODO document
	private void printArtifactDescription(@Nonnull final MummyContext context, @Nonnull final Artifact artifact) { //TODO transfer to CLI
		final TurfSerializer turfSerializer = new TurfSerializer();

		//TODO remove debug code
		getLogger().debug("{} ({})", artifact.getTargetPath(), artifact.getTargetPath().toUri());
		if(artifact.getResourceDescription().hasProperties()) {
			try {
				getLogger().debug("    {}", turfSerializer.serializeDescription(new StringBuilder(), artifact.getResourceDescription()));
			} catch(final IOException ioException) {
				getLogger().error("Error debugging resource description.", ioException);
			}
		}

		context.findParentArtifact(artifact).ifPresent(parent -> getLogger().debug("  parent: {}", parent.getTargetPath()));
		final Collection<Artifact> siblings = context.siblingArtifacts(artifact).collect(toList()); //TODO make debugging calls more efficient, or transfer to describe functionality  
		if(!siblings.isEmpty()) {
			getLogger().debug("  siblings: {}", siblings);
		}
		final Collection<Artifact> children = context.childArtifacts(artifact).collect(toList());
		if(!children.isEmpty()) {
			getLogger().debug("  children: {}", children);
		}

		if(artifact instanceof CollectionArtifact) {
			for(final Artifact childArtifact : ((CollectionArtifact)artifact).getChildArtifacts()) {
				printArtifactDescription(context, childArtifact);
			}
		}
	}

	/**
	 * Mutable mummification context controlled by Guise Mummy.
	 * @author Garret Wilson
	 */
	protected class Context extends BaseMummyContext {

		private final Path siteSourceDirectory;

		@Override
		public Path getSiteSourceDirectory() {
			return siteSourceDirectory;
		}

		private final Path siteTargetDirectory;

		@Override
		public Path getSiteTargetDirectory() {
			return siteTargetDirectory;
		}

		/**
		 * Site source directory constructor.
		 * @param siteSourceDirectory The source directory of the entire site.
		 * @param siteTargetDirectory The base output directory of the site being mummified.
		 * @throws IllegalArgumentException if the source directory does not exist or is not a directory.
		 * @throws IllegalArgumentException if the source and target directories overlap.
		 */
		public Context(@Nonnull final Path siteSourceDirectory, @Nonnull final Path siteTargetDirectory) {
			checkArgumentDirectory(siteSourceDirectory);
			checkArgumentDisjoint(siteSourceDirectory, siteTargetDirectory);
			this.siteSourceDirectory = siteSourceDirectory;
			this.siteTargetDirectory = siteTargetDirectory;
		}

		@Override
		public Mummifier getDefaultSourceFileMummifier() {
			return defaultFileMummifier;
		}

		@Override
		public Mummifier getDefaultSourceDirectoryMummifier() {
			return defaultDirectoryMummifier;
		}

		@Override
		public Optional<Mummifier> findRegisteredMummifierForSourceFile(@Nonnull final Path sourceFile) {
			return extensions(sourceFile.getFileName().toString()).map(fileMummifiersByExtension::get).filter(Objects::nonNull).findFirst();
		}

		/**
		 * {@inheritDoc}
		 * @implSpec This implementation doesn't support registered source directory mummifiers, and will always return {@link Optional#empty()}.
		 */
		@Override
		public Optional<Mummifier> findRegisteredMummifierForSourceDirectory(Path sourceDirectory) {
			return Optional.empty();
		}

	}

}
