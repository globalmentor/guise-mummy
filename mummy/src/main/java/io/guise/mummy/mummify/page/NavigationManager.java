/*
 * Copyright © 2020 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package io.guise.mummy.mummify.page;

import static com.globalmentor.java.Conditions.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.lines;
import static java.util.stream.Collectors.toList;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.*;

import com.globalmentor.net.*;

import io.clogr.Clogged;
import io.guise.mummy.*;

/**
 * Strategy for loading and sorting navigation.
 * @author Garret Wilson
 */
public class NavigationManager implements Clogged {

	/**
	 * Loads a text navigation list file (e.g. <code>.navigation.list</code>.
	 * <p>
	 * Each line is a reference to an artifact, relative to the _directory_ of the corresponding {{.navigation.lst}} file, just like references in a template are
	 * relative to the original {{.template.*}} file location. Fragments and queries are allowed and maintained.
	 * </p>
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param navigationListFile The path to the file containing the navigation list.
	 * @return The navigation items loaded from the file.
	 * @throws IOException if there is an I/O error loading the navigation list file.
	 */
	public Stream<NavigationItem> loadNavigationList(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact,
			@Nonnull final Path navigationListFile) throws IOException {
		final Path navigationListFileParent = navigationListFile.getParent(); //each line reference is relative to the directory of the navigation file
		checkState(navigationListFileParent != null, "Navigation list file `%s` has no parent.", navigationListFile);
		try (final Stream<String> lines = lines(navigationListFile, UTF_8)) { //trailing empty lines are ignored, as desired
			return lines.<NavigationItem>flatMap(line -> { //map lines to Optional<NavigationItem>, warning if there is no artifact for a reference 
				final URI reference;
				try {
					reference = new URI(line); //assume each line is a path reference _relative to the navigation list file_
				} catch(final URISyntaxException uriSyntaxException) {
					throw new UncheckedIOException(new IOException(String.format("Invalid reference <%s> in navigation file `%s`.", line, navigationListFile)));
				}
				if(reference.isAbsolute()) {
					throw new UncheckedIOException(
							new IOException(String.format("External URI <%s> not currently supported in navigation file `%s`.", reference, navigationListFile)));
				}
				final URIPath referencePath = URIs.getPath(reference);
				if(referencePath == null) {
					throw new UncheckedIOException(
							new IOException(String.format("Navigation reference <%s> in navigation file `%s` does not have a path.", reference, navigationListFile)));
				}
				final Optional<Artifact> foundNavigationArtifact = context.findArtifactBySourceRelativeReference(navigationListFileParent, referencePath);
				if(!foundNavigationArtifact.isPresent()) {
					getLogger().warn("No target artifact found for relative reference `{}` in navigation file `{}`.", referencePath, navigationListFile);
				}
				return foundNavigationArtifact.map(navigationArtifact -> {
					//get the correct relative path to the artifact from the context artifact
					final URIPath contextArtifactRelativeReferencePath = context.relativizeResourceReference(contextArtifact.getSourcePath(),
							navigationArtifact.getSourcePath(), navigationArtifact instanceof CollectionArtifact);
					//change the reference path to get the appropriate href relative to the context artifact
					final String href = URIs.changePath(reference, contextArtifactRelativeReferencePath).toString();
					return DefaultNavigationItem.forHrefTargetingArtifact(href, navigationArtifact);
				}).stream();
			}).collect(toList()).stream(); //(important) collect the artifacts to a list to prevent any exceptions upon stream iteration after method return
		} catch(final UncheckedIOException uncheckedIOException) { //both the the lines() stream and our own checks can throw an unchecked I/O exception
			throw uncheckedIOException.getCause();
		}
	}

}
