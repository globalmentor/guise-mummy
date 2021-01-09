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
import static com.globalmentor.io.Files.*;
import static com.globalmentor.io.Paths.*;
import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.util.Optionals.*;
import static io.guise.mummy.Artifact.PROPERTY_HANDLE_TITLE;
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
import io.urf.model.*;
import io.urf.vocab.content.Content;

/**
 * Mummifier for directories.
 * @implSpec This mummifier only works with instances of {@link DirectoryArtifact}.
 * @implNote Although the current implementation creates a default phantom content file if one is not present, this implementation will work without a known
 *           content file. This enables future implementations to allow configuration of whether a default content file is used.
 * @implNote This implementation does not have a means for determining if a phantom content file (currently hard-coded in {@link DefaultXhtmlPhantomArtifact})
 *           or its description (loaded from {@link #getArtifactSourceDescriptionFile(MummyContext, Path)}) has changed; therefore the target description file
 *           for a phantom content file is always written.
 * @implNote This implementation does not currently generate a target description file using {@link #getArtifactTargetDescriptionFile(MummyContext, Artifact)}
 *           or {@link #getArtifactTargetDescriptionFile(MummyContext, Path)}, relying instead on the description file for the content file, if any.
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
	 * @implSpec This implementation returns the {@link Mummifier#DESCRIPTION_FILE_SIDECAR_EXTENSION} itself as the filename but in the form of a dotfile, inside
	 *           the directory being mummified.
	 */
	@Override
	protected Path getArtifactSourceDescriptionFile(final MummyContext context, final Path sourceDirectory) {
		return sourceDirectory.resolve(DOTFILE_PREFIX + DESCRIPTION_FILE_SIDECAR_EXTENSION);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns the {@link Mummifier#DESCRIPTION_FILE_SIDECAR_EXTENSION} itself as the filename, inside the target path but in the
	 *           {@link MummyContext#getSiteDescriptionTargetDirectory()} directory.
	 */
	@Override
	protected Path getArtifactTargetDescriptionFile(final MummyContext context, final Path targetDirectory) {
		return changeBase(targetDirectory, context.getSiteTargetDirectory(), context.getSiteDescriptionTargetDirectory())
				.resolve(DESCRIPTION_FILE_SIDECAR_EXTENSION);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation recursively discovers and describes an artifacts for all its children.
	 * @see GuiseMummy#CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES
	 */
	@Override
	public Artifact plan(final MummyContext context, final Path sourceDirectory, final Path targetDirectory) throws IOException {
		checkArgumentDirectory(sourceDirectory);
		final boolean isAssetSourceDirectoryTree = isAssetSourcePath(context, sourceDirectory, true); //see if this subtree is for assets

		//discover and plan the directory content file, if present
		final Optional<Path> discoveredContentFile = discoverSourceDirectoryContentFile(context, sourceDirectory);
		final Artifact contentArtifact = discoveredContentFile.map(throwingFunction(contentSourceFile -> { //nullable
			final Path sourceDescriptionFile = getArtifactSourceDescriptionFile(context, sourceDirectory); //find out where to look for a source description
			if(exists(sourceDescriptionFile)) {
				getLogger().warn("Directory `{}` will use description of its content file `{}`; the source description file `{}` will be ignored.", sourceDirectory,
						contentSourceFile, sourceDescriptionFile);
			}
			final SourcePathMummifier contentMummifier = context.findRegisteredMummifierForSourceFile(contentSourceFile).orElseThrow(IllegalStateException::new); //TODO improve error
			final String contentSourceFilename = findFilename(contentSourceFile).orElseThrow(IllegalStateException::new);
			//normalize the content filename to the first of the recognized base names
			final String normalizedContentFilename = context.getConfiguration().getCollection(CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES, String.class).stream()
					.findFirst().map(contentBaseName -> Filenames.changeBase(contentSourceFilename, contentBaseName)).orElse(contentSourceFilename);
			final String contentTargetFilename = contentMummifier.planArtifactTargetFilename(context, normalizedContentFilename);
			final Path contentTargetFile = targetDirectory.resolve(contentTargetFilename);
			return contentMummifier.plan(context, contentSourceFile, contentTargetFile);
		})).orElseGet(throwingSupplier(() -> { //if there is no directory content file, create a phantom page content file
			if(isAssetSourceDirectoryTree) { //don't generate content files for asset trees
				return null;
			}
			final Collection<String> collectionContentBaseNames = context.getConfiguration().getCollection(CONFIG_KEY_MUMMY_COLLECTION_CONTENT_BASE_NAMES,
					String.class);
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
			final UrfResourceDescription phantomDescription = loadArtifactSourceDescription(context, sourceDirectory).orElseGet(() -> new UrfObject());
			if(!phantomDescription.hasPropertyValueByHandle(PROPERTY_HANDLE_TITLE)) { //if any source description did not contain a title, add a default one
				final Path directoryFilename = sourceDirectory.getFileName();
				if(directoryFilename != null) { //set the title to match the directory filename, if present
					phantomDescription.setPropertyValueByHandle(Artifact.PROPERTY_HANDLE_TITLE, directoryFilename.toString());
				}
			}
			phantomDescription.setPropertyValue(Content.TYPE_PROPERTY_TAG, PageMummifier.PAGE_MEDIA_TYPE);
			//TODO do we need to set the description dirty so that the description will get serialized?
			return new DefaultXhtmlPhantomArtifact(phantomContentMummifier, phantomContentSourceFile, phantomContentTargetFile, phantomDescription);
		}));

		//discover and plan the child artifacts
		final Pattern assetNamePattern = context.getConfiguration().getObject(CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN, Pattern.class);
		final List<Artifact> childArtifacts = new ArrayList<>();
		try (final Stream<Path> childPaths = list(sourceDirectory).filter(not(context::isIgnore))) {
			childPaths.forEach(throwingConsumer(childSourcePath -> {
				if(!isPresentAndEquals(discoveredContentFile, childSourcePath)) { //skip the content file, if any
					final SourcePathMummifier registeredChildMummifier = context.getMummifierForSourcePath(childSourcePath);
					assert childSourcePath.getFileName() != null;
					final String childSourceFilename = childSourcePath.getFileName().toString();
					//for assets or paths an an asset tree, don't mummify any pages
					final SourcePathMummifier childMummifier;
					if((registeredChildMummifier instanceof PageMummifier) && (isAssetSourceDirectoryTree || assetNamePattern.matcher(childSourceFilename).matches())) {
						childMummifier = context.getDefaultSourcePathMummifier(childSourcePath);
					} else {
						childMummifier = registeredChildMummifier;
					}
					final Path childTargetPath = planChildArtifactTargetPath(context, targetDirectory, childSourceFilename, childMummifier, isAssetSourceDirectoryTree);
					final Artifact childArtifact = childMummifier.plan(context, childSourcePath, childTargetPath);

					//TODO add error handling here with a better error

					childArtifacts.add(childArtifact);
				}
			}));
		}
		return new DirectoryArtifact(this, sourceDirectory, targetDirectory, contentArtifact, childArtifacts);
	}

	/**
	 * Indicates whether the given source path is an <dfn>asset</dfn> for which no page should be generated. Ancestor paths are not checked.
	 * @implSpec This implementation delegates to {@link #isAssetSourcePath(MummyContext, Path, boolean)} without checking for ancestors.
	 * @param context The context of static site generation.
	 * @param sourcePath The source path to check.
	 * @return <code>true</code> if the source path is an asset.
	 * @see GuiseMummy#CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN
	 * @deprecated To be deleted if not needed
	 */
	@Deprecated
	protected boolean isAssetSourcePath(@Nonnull final MummyContext context, @Nonnull final Path sourcePath) {
		return isAssetSourcePath(context, sourcePath, false);
	}

	/**
	 * Indicates whether the given source path is an <dfn>asset</dfn> for which no page should be generated.
	 * @implSpec This implementation considers an asset any source path the source filename of which, or if <var>checkAncestors</var> is enabled the filename of
	 *           any parent source directory of which (within the site), matches the pattern configured under the
	 *           {@value GuiseMummy#CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN} key. For example with a pattern of <code>/\$(.+)/</code>, both
	 *           <code>…/$foo/bar.txt</code> and <code>…/foo/$bar.txt</code> would be considered assets.
	 * @param context The context of static site generation.
	 * @param sourcePath The source path to check.
	 * @param checkAncestors Indicates whether parent paths should also be checked, up to the site source path.
	 * @return <code>true</code> if the source path is an asset.
	 * @throws IllegalArgumentException if the given source path is not inside the site source directory when checking ancestors.
	 * @see GuiseMummy#CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN
	 */
	protected boolean isAssetSourcePath(@Nonnull final MummyContext context, @Nonnull final Path sourcePath, final boolean checkAncestors) {
		final Pattern assetNamePattern = context.getConfiguration().getObject(CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN, Pattern.class);
		final Path siteSourceDirectory = context.getSiteSourceDirectory();
		Path currentSourcePath = sourcePath;
		while(!currentSourcePath.equals(siteSourceDirectory)) {
			if(assetNamePattern.matcher(currentSourcePath.getFileName().toString()).matches()) {
				return true;
			}
			if(!checkAncestors) {
				break;
			}
			currentSourcePath = currentSourcePath.getParent();
			checkArgument(currentSourcePath != null, "Source path `%s` was not inside site source directory `%s`.", sourcePath, siteSourceDirectory);
		}
		return false;
	}

	/**
	 * Indicates whether the given source name is an <dfn>asset</dfn> for which no page should be generated.
	 * @param context The context of static site generation.
	 * @param sourceName The source name to check.
	 * @return <code>true</code> if the source name is an asset.
	 * @see GuiseMummy#CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN
	 * @deprecated To be deleted if not needed
	 */
	@Deprecated
	protected boolean isAssetSourceName(@Nonnull final MummyContext context, @Nonnull final String sourceName) {
		final Pattern assetNamePattern = context.getConfiguration().getObject(CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN, Pattern.class);
		return assetNamePattern.matcher(sourceName).matches();
	}

	/**
	 * Determines the output path for an artifact in the site target directory based upon the source path in the site source directory.
	 * @implSpec This implementation recognizes blog posts and adds an appropriate subdirectory structure for them in the target tree path.
	 * @implSpec This version recognizes asset artifacts and renames them as necessary according to the asset name pattern configured with the key
	 *           {@value GuiseMummy#CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN}, both for files and for directories. If the source directory is in an asset tree, or if
	 *           the child filename itself indicates an asset, no hierarchy-related filename changes (e.g. unveiling or post renaming) will be made, except an
	 *           asset in a non-asset tree will be renamed.
	 * @implSpec This version recognizes veiled artifacts and renames ("unveils") them as necessary according to the veil name pattern configured with the key
	 *           {@value GuiseMummy#CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN}, both for files and for directories.
	 * @implSpec This implementation delegates to {@link Mummifier#planArtifactTargetFilename(MummyContext, String)} to finalize the filename being used.
	 * @param context The context of static site generation.
	 * @param targetDirectory The target directory of the main artifact this mummifier is mummifying.
	 * @param childSourceFilename A suggested filename; normally the filename of the child file or directory.
	 * @param childMummifier The mummifier that will be used to create the artifact.
	 * @param isAssetSourceDirectoryTree Whether the source directory is an asset or is in an asset tree.
	 * @return The path in the site target directory to which the child artifact should be generated.
	 * @throws ConfigurationException if the given veil name pattern specifies more than one matching group.
	 * @see Mummifier#planArtifactTargetFilename(MummyContext, String)
	 * @see PostArtifact#FILENAME_PATTERN
	 * @see GuiseMummy#CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN
	 * @see GuiseMummy#CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN
	 */
	protected Path planChildArtifactTargetPath(@Nonnull final MummyContext context, @Nonnull Path targetDirectory, @Nonnull final String childSourceFilename,
			@Nonnull final Mummifier childMummifier, final boolean isAssetSourceDirectoryTree) {
		//both the target directory and the child target filename will change as it gets processed
		String childTargetFilename = childSourceFilename;
		if(!isAssetSourceDirectoryTree) { //don't make hierarchy-related filename changes in an assets tree

			//assets
			final Pattern assetPattern = context.getConfiguration().getObject(CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN, Pattern.class);
			final Matcher assetMatcher = assetPattern.matcher(childTargetFilename);
			if(assetMatcher.matches()) {
				Configuration.check(assetMatcher.groupCount() <= 1, "Asset name pattern /%s/ configured with key `%s` can have at most one matching group.",
						assetPattern, CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN);
				if(assetMatcher.groupCount() > 0) {
					final String newFilename = assetMatcher.group(1);
					if(newFilename != null) { //rename the file as indicated by the match
						Configuration.check(!Filenames.isSpecialName(newFilename),
								"Asset name pattern /%s/ configured with key `%s` when applied `%s` resulted in forbidden, special name `%s`.", assetPattern,
								CONFIG_KEY_MUMMY_ASSET_NAME_PATTERN, childTargetFilename, newFilename);
						childTargetFilename = newFilename;
					}
				}
			} else { //assets don't get any further filename hierarchy-related filename changes

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
					Configuration.check(veilMatcher.groupCount() <= 1, "Veil name pattern /%s/ configured with key `%s` can have at most one matching group.",
							veilPattern, CONFIG_KEY_MUMMY_VEIL_NAME_PATTERN);
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
			}
		}

		//mummifier filename modifications
		childTargetFilename = childMummifier.planArtifactTargetFilename(context, childTargetFilename);
		return targetDirectory.resolve(childTargetFilename);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation saves the description description if modified by calling {@link #saveTargetDescription(MummyContext, Artifact)}.
	 */
	@Override
	public void mummify(final MummyContext context, final Artifact artifact) throws IOException {
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
		directoryArtifact.findContentArtifact().ifPresent(throwingConsumer(contentArtifact -> {
			contentArtifact.getMummifier().mummify(context, contentArtifact);
			//Note that if a content file was once but no longer present, an orphaned content file
			//description would be left, but the ways these circumstances could come about are
			//largely theoretical (e.g. converting a source directory to an assets tree and
			//renaming the target tree to match). 
		}));

		//mummify each child artifact
		for(final Artifact childArtifact : directoryArtifact.getChildArtifacts()) {
			childArtifact.getMummifier().mummify(context, childArtifact);
		}
	}

}
