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

package io.guise.mummy.mummify.collection;

import static com.globalmentor.io.Filenames.*;
import static com.globalmentor.io.Paths.*;
import static com.globalmentor.java.Conditions.*;
import static io.guise.mummy.GuiseMummy.*;
import static java.nio.file.Files.*;
import static java.util.Collections.*;
import static java.util.function.Predicate.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Stream;

import javax.annotation.*;

import com.globalmentor.html.spec.HTML;
import com.globalmentor.io.Filenames;
import com.globalmentor.net.ContentType;

import io.confound.config.*;
import io.guise.mummy.*;
import io.guise.mummy.mummify.*;
import io.guise.mummy.mummify.page.*;
import io.urf.model.UrfObject;
import io.urf.model.UrfResourceDescription;
import io.urf.vocab.content.Content;

/**
 * Mummifier for directories.
 * @implSpec This mummifier only works with instances of {@link DirectoryArtifact}.
 * @implNote Although the current implementation creates a default phantom content file if one is not present, this implementation will work without a known
 *           content file. This enables future implementations to allow configuration of whether a default content file is used.
 * @implSpec Currently directory artifacts do not themselves have target descriptions, but rather rely on the target descriptions of any content file.
 * @author Garret Wilson
 * @see DirectoryArtifact
 */
public class DirectoryMummifier extends AbstractSourcePathMummifier {

	@Override
	public Set<String> getSupportedFilenameExtensions() {
		return emptySet();
	}

	/**
	 * {@inheritDoc}
	 * @implSpec Directories have no media types of their own, so this version returns no media type.
	 */
	@Override
	public Optional<ContentType> getArtifactMediaType(final MummyContext context, final Path sourcePath) throws IOException {
		return Optional.empty();
	}

	/**
	 * Attempts to discover a "content file" for a source directory, serving as the default content page for the directory.
	 * @apiNote Normally this would be <code>index.xhtml</code> or some other "index" file.
	 * @param context The context of static site generation.
	 * @param sourceDirectory The source directory to be mummified.
	 * @return The path of a source file, if any, to be used as the directory content file.
	 * @see GuiseMummy#CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES
	 */
	protected Optional<Path> discoverSourceDirectoryContentFile(@Nonnull MummyContext context, @Nonnull final Path sourceDirectory) {
		return context.getConfiguration().getCollection(CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES, String.class).stream() //look at each base name
				.flatMap(throwingFunction(baseName -> context.findPageSourceFile(sourceDirectory, baseName).stream())) //try to find a page source file for that name
				.map(Map.Entry::getKey).findFirst(); //for the first one found, return its path
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns empty; currently directory artifacts do not themselves have target descriptions, but rather rely on the target
	 *           descriptions of any content file.
	 */
	@Override
	protected Optional<UrfResourceDescription> loadTargetDescription(final MummyContext context, final Path targetDirectory) throws IOException {
		return Optional.empty();
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation recursively discovers and describes an artifacts for all its children.
	 * @see GuiseMummy#CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES
	 */
	@Override
	public Artifact plan(final MummyContext context, final Path sourceDirectory, final Path targetDirectory) throws IOException {
		checkArgumentDirectory(sourceDirectory);

		//discover and plan the directory content file, if present
		final Optional<Path> contentFile = discoverSourceDirectoryContentFile(context, sourceDirectory);
		final Artifact contentArtifact = contentFile.map(throwingFunction(contentSourceFile -> { //nullable
			final SourcePathMummifier contentMummifier = context.findRegisteredMummifierForSourceFile(contentSourceFile).orElseThrow(IllegalStateException::new); //TODO improve error
			//TODO normalize content filename
			assert contentSourceFile.getFileName() != null;
			final String contentTargetFilename = contentMummifier.planArtifactTargetFilename(context, contentSourceFile.getFileName().toString());
			final Path contentTargetFile = targetDirectory.resolve(contentTargetFilename);
			return contentMummifier.plan(context, contentSourceFile, contentTargetFile);
		})).orElseGet(() -> { //if there is no directory content file, create a phantom page content file
			/*TODO replace with check for assets subtree
			if(context.isVeiled(sourceDirectory)) { //don't generate content files for veiled directories
				return null;
			}
			*/
			final Collection<String> collectionContentBaseNames = context.getConfiguration().getCollection(CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES, String.class);
			if(collectionContentBaseNames.isEmpty()) { //if there are no collection content base names, there can be no no content file
				return null;
			}
			final String phantomContentBaseName = collectionContentBaseNames.iterator().next(); //e.g. "index"
			final String phantomContentSourceFilename = addExtension(phantomContentBaseName, HTML.XHTML_NAME_EXTENSION); //e.g. "index.xhtml"
			final Path phantomContentSourceFile = sourceDirectory.resolve(phantomContentSourceFilename);
			final SourcePathMummifier phantomContentMummifier = context.findRegisteredMummifierForSourceFile(phantomContentSourceFile)
					.orElseThrow(IllegalStateException::new); //TODO improve error
			final String phantomContentTargetFilename = phantomContentMummifier.planArtifactTargetFilename(context, phantomContentSourceFilename);
			final Path phantomContentTargetFile = targetDirectory.resolve(phantomContentTargetFilename);
			final UrfObject phantomDescription = new UrfObject();
			final Path directoryFilename = sourceDirectory.getFileName();
			if(directoryFilename != null) { //set the title to match the directory filename, if present
				phantomDescription.setPropertyValueByHandle(Artifact.PROPERTY_HANDLE_TITLE, directoryFilename.toString());
			}
			phantomDescription.setPropertyValue(Content.TYPE_PROPERTY_TAG, PageMummifier.PAGE_MEDIA_TYPE);
			return new DefaultXhtmlPhantomArtifact(phantomContentMummifier, phantomContentSourceFile, phantomContentTargetFile, phantomDescription);
		});

		//discover and plan the child artifacts
		final List<Artifact> childArtifacts = new ArrayList<>();
		try (final Stream<Path> childPaths = list(sourceDirectory).filter(not(context::isIgnore))) {
			childPaths.forEach(throwingConsumer(childSourcePath -> {
				if(!childSourcePath.equals(contentFile.orElse(null))) { //skip the content file TODO create an optional comparison utility method
					final SourcePathMummifier childMummifier = context.getMummifierForSourcePath(childSourcePath);
					assert childSourcePath.getFileName() != null;
					final Path childTargetPath = planChildArtifactTargetPath(context, targetDirectory, childSourcePath.getFileName().toString(), childMummifier);
					final Artifact childArtifact = childMummifier.plan(context, childSourcePath, childTargetPath);
					childArtifacts.add(childArtifact);
				}
			}));
		}
		return new DirectoryArtifact(this, sourceDirectory, targetDirectory, contentArtifact, childArtifacts);
	}

	/**
	 * Determines the output path for an artifact in the site target directory based upon the source path in the site source directory.
	 * @implSpec This implementation recognizes blog posts and adds an appropriate subdirectory structure for them in the target tree path.
	 * @implSpec This version also recognizes veiled artifacts and renames ("unveils") them as necessary according to the veil name pattern configured with the
	 *           key {@value GuiseMummy#CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN}, both for files and for directories.
	 * @implSpec This implementation delegates to {@link Mummifier#planArtifactTargetFilename(MummyContext, String)} to finalize the filename being used.
	 * @param context The context of static site generation.
	 * @param targetDirectory The target directory of the main artifact this mummifier is mummifying.
	 * @param childSourceFilename A suggested filename; normally the filename of the child file or directory.
	 * @param childMummifier The mummifier that will be used to create the artifact.
	 * @return The path in the site target directory to which the child artifact should be generated.
	 * @throws ConfigurationException if the given veil name pattern specifies more than one matching group.
	 * @see Mummifier#planArtifactTargetFilename(MummyContext, String)
	 * @see PostArtifact#FILENAME_PATTERN
	 * @see GuiseMummy#CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN
	 */
	protected Path planChildArtifactTargetPath(@Nonnull final MummyContext context, @Nonnull Path targetDirectory, @Nonnull final String childSourceFilename,
			@Nonnull final Mummifier childMummifier) {
		//both the target directory and the child target filename will change as it gets processed
		String childTargetFilename = childSourceFilename;

		//posts
		final Matcher postMatcher = PostArtifact.FILENAME_PATTERN.matcher(childTargetFilename);
		if(postMatcher.matches()) {
			final String postYear = postMatcher.group(PostArtifact.FILENAME_PATTERN_YEAR_GROUP);
			final String postMonth = postMatcher.group(PostArtifact.FILENAME_PATTERN_MONTH_GROUP);
			final String postDay = postMatcher.group(PostArtifact.FILENAME_PATTERN_DAY_GROUP);
			final String postFilename = postMatcher.group(PostArtifact.FILENAME_PATTERN_FILENAME_GROUP);
			targetDirectory = targetDirectory.resolve(postYear).resolve(postMonth).resolve(postDay); //YYYY/DD/MM
			childTargetFilename = postFilename;
		}

		//veiled artifacts
		final Pattern veilPattern = context.getConfiguration().getObject(CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN, Pattern.class);
		final Matcher veilMatcher = veilPattern.matcher(childTargetFilename);
		if(veilMatcher.matches()) {
			Configuration.check(veilMatcher.groupCount() <= 1, "Veil name pattern /%s/ configured with key `%s` can have at most one matching group.", veilPattern,
					CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN);
			if(veilMatcher.groupCount() > 0) {
				final String newFilename = veilMatcher.group(1);
				if(newFilename != null) { //rename the file as indicated by the match
					Configuration.check(!Filenames.isSpecialName(newFilename),
							"Veil name pattern /%s/ configured with key `%s` when applied `%s` resulted in forbidden, special name `%s`.", veilPattern,
							CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN, childTargetFilename, newFilename);
					childTargetFilename = newFilename;
				}
			}
		}

		//mummifier filename modifications
		childTargetFilename = childMummifier.planArtifactTargetFilename(context, childTargetFilename);
		return targetDirectory.resolve(childTargetFilename);
	}

	@Override
	public void mummify(final MummyContext context, final Artifact contextArtifact, final Artifact artifact) throws IOException {
		checkArgument(artifact instanceof DirectoryArtifact, "Artifact %s is not a directory artifact.");
		checkArgumentDirectory(artifact.getSourcePath());

		final DirectoryArtifact directoryArtifact = (DirectoryArtifact)artifact;

		//create the directory if it doesn't exist
		final Path targetDirectory = artifact.getTargetPath();
		if(!isDirectory(targetDirectory)) {
			getLogger().debug("Mummified directory artifact {}.", directoryArtifact);
			createDirectories(targetDirectory);
		}

		//mummify the directory content artifact, if present
		directoryArtifact.getContentArtifact()
				.ifPresent(throwingConsumer(contentArtifact -> contentArtifact.getMummifier().mummify(context, artifact, contentArtifact)));

		//mummify each child artifact
		for(final Artifact childArtifact : directoryArtifact.getChildArtifacts()) {
			childArtifact.getMummifier().mummify(context, childArtifact);
		}
	}

}
