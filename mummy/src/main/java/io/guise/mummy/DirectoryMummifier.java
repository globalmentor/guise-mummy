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

import static com.globalmentor.java.Conditions.*;
import static java.nio.file.Files.*;
import static java.util.Collections.*;
import static java.util.function.Predicate.not;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * Mummifier for directories.
 * @author Garret Wilson
 */
public class DirectoryMummifier extends AbstractSourcePathMummifier {

	@Override
	public Set<String> getSupportedFilenameExtensions() {
		return emptySet();
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
	 * @implSpec This implementation recursively discovers and describes an artifacts for all its children.
	 */
	@Override
	public Artifact plan(MummyContext context, Path sourcePath) throws IOException {
		checkArgument(isDirectory(sourcePath), "Source %s does not exist or is not a directory.");

		final Path targetDirectory = getArtifactTargetPath(context, sourcePath);

		//discover and plan the directory content file, if present
		final Optional<Path> contentFile = discoverSourceDirectoryContentFile(context, sourcePath);
		final Artifact contentArtifact = contentFile.map(throwingFunction(contentSourceFile -> {
			final Mummifier contentMummifier = context.getMummifier(contentSourceFile).orElseThrow(IllegalStateException::new); //TODO improve error
			return contentMummifier.plan(context, contentSourceFile);
		})).orElse(null);

		//discover and plan the child artifacts
		final List<Artifact> childArtifacts = new ArrayList<>();
		try (final Stream<Path> childPaths = list(sourcePath).filter(not(context::isIgnore))) {
			childPaths.forEach(throwingConsumer(childSourcePath -> {
				if(!childSourcePath.equals(contentFile.orElse(null))) { //skip the content file TODO create an optional comparison utility method
					final Optional<Mummifier> childMummifier = context.getMummifier(childSourcePath);
					childMummifier.ifPresent(throwingConsumer(mummifier -> {
						final Artifact childArtifact = mummifier.plan(context, childSourcePath);
						childArtifacts.add(childArtifact);
					}));
				}
			}));
		}
		;
		return new DirectoryArtifact(this, sourcePath, targetDirectory, contentArtifact, childArtifacts);
	}

	@Override
	public void mummify(final MummyContext context, final Artifact contextArtifact, final Artifact artifact) throws IOException {
		checkArgument(artifact instanceof DirectoryArtifact, "Artifact %s is not a directory artifact.");
		checkArgument(isDirectory(artifact.getSourcePath()), "Source path %s does not exist or is not a directory.");

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
