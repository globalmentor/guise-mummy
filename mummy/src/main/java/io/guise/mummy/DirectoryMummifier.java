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

import static com.globalmentor.io.Paths.*;
import static com.globalmentor.java.Conditions.*;
import static java.nio.file.Files.*;
import static java.util.Collections.*;
import static java.util.function.Predicate.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

import com.globalmentor.net.ContentType;

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
	 */
	protected Optional<Path> discoverSourceDirectoryContentFile(@Nonnull MummyContext context, @Nonnull final Path sourceDirectory) {
		final Path directoryFile = sourceDirectory.resolve("index.xhtml"); //TODO provide a formal lookup mechanism
		if(Files.isRegularFile(directoryFile)) {
			return Optional.of(directoryFile);
		}
		return Optional.empty();
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns empty; currently directory artifacts do not themselves have target descriptions, but rather rely on the target
	 *           descriptions of any content file.
	 */
	@Override
	protected Optional<UrfResourceDescription> loadTargetDescription(MummyContext context, Path sourcePath) throws IOException {
		return Optional.empty();
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation recursively discovers and describes an artifacts for all its children.
	 */
	@Override
	public Artifact plan(MummyContext context, Path sourcePath) throws IOException {
		checkArgumentDirectory(sourcePath);

		final Path targetDirectory = getArtifactTargetPath(context, sourcePath);

		//discover and plan the directory content file, if present
		final Optional<Path> contentFile = discoverSourceDirectoryContentFile(context, sourcePath);
		final Artifact contentArtifact = contentFile.map(throwingFunction(contentSourceFile -> {
			final SourcePathMummifier contentMummifier = context.findRegisteredMummifierForSourceFile(contentSourceFile).orElseThrow(IllegalStateException::new); //TODO improve error
			return contentMummifier.plan(context, contentSourceFile);
		})).orElseGet(() -> { //if there is no directory content file, create a phantom page content file
			if(context.isVeiled(sourcePath)) { //don't generate content files for veiled directories
				return null;
			}
			final Path phantomContentSourceFile = sourcePath.resolve("index.xhtml"); //TODO get from configuration
			final SourcePathMummifier contentMummifier = context.findRegisteredMummifierForSourceFile(phantomContentSourceFile)
					.orElseThrow(IllegalStateException::new); //TODO improve error
			final UrfObject phantomDescription = new UrfObject();
			final Path directoryFilename = sourcePath.getFileName();
			if(directoryFilename != null) { //set the title to match the directory filename, if present
				phantomDescription.setPropertyValueByHandle(Artifact.PROPERTY_HANDLE_TITLE, directoryFilename.toString());
			}
			phantomDescription.setPropertyValue(Content.TYPE_PROPERTY_TAG, PageMummifier.PAGE_MEDIA_TYPE);
			return new DefaultXhtmlPhantomArtifact(contentMummifier, phantomContentSourceFile,
					contentMummifier.getArtifactTargetPath(context, phantomContentSourceFile), phantomDescription);
		});

		//discover and plan the child artifacts
		final List<Artifact> childArtifacts = new ArrayList<>();
		try (final Stream<Path> childPaths = list(sourcePath).filter(not(context::isIgnore))) {
			childPaths.forEach(throwingConsumer(childSourcePath -> {
				if(!childSourcePath.equals(contentFile.orElse(null))) { //skip the content file TODO create an optional comparison utility method
					final Mummifier childMummifier = context.getMummifierForSourcePath(childSourcePath);
					final Artifact childArtifact = childMummifier.plan(context, childSourcePath);
					childArtifacts.add(childArtifact);
				}
			}));
		}
		return new DirectoryArtifact(this, sourcePath, targetDirectory, contentArtifact, childArtifacts);
	}

	@Override
	public void mummify(final MummyContext context, final Artifact contextArtifact, final Artifact artifact) throws IOException {
		checkArgument(artifact instanceof DirectoryArtifact, "Artifact %s is not a directory artifact.");
		checkArgumentDirectory(artifact.getSourcePath());

		final DirectoryArtifact directoryArtifact = (DirectoryArtifact)artifact;

		//create the directory
		getLogger().debug("created directory: {}", directoryArtifact);
		createDirectories(artifact.getTargetPath());

		//mummify the directory content artifact, if present
		directoryArtifact.getContentArtifact()
				.ifPresent(throwingConsumer(contentArtifact -> contentArtifact.getMummifier().mummify(context, artifact, contentArtifact)));

		//mummify each child artifact
		for(final Artifact childArtifact : directoryArtifact.getChildArtifacts()) {
			childArtifact.getMummifier().mummify(context, childArtifact);
		}
	}

}
