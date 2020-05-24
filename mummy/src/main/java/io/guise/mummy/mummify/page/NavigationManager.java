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

import static com.globalmentor.io.Filenames.*;
import static com.globalmentor.io.Files.*;
import static com.globalmentor.io.Paths.*;
import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.java.Objects.*;
import static io.guise.mummy.GuiseMummy.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static org.zalando.fauxpas.FauxPas.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

import javax.annotation.*;

import com.globalmentor.java.Objects;
import com.globalmentor.net.*;
import com.globalmentor.text.Text;
import com.globalmentor.util.stream.Streams;

import io.clogr.Clogged;
import io.guise.mummy.*;
import io.urf.model.SimpleGraphUrfProcessor;
import io.urf.model.UrfResourceDescription;
import io.urf.turf.TURF;
import io.urf.turf.TurfParser;

/**
 * Strategy for loading and sorting navigation.
 * @author Garret Wilson
 */
public class NavigationManager implements Clogged {

	/** The set of filename extensions for supported navigation files, in order of precedence. */
	private static final Set<String> SUPPORTED_NAVIGATION_FILE_EXTENSIONS = Stream.of(TURF.FILENAME_EXTENSION, Text.LST_NAME_EXTENSION)
			.collect(collectingAndThen(toCollection(LinkedHashSet::new), Collections::unmodifiableSet));

	/**
	 * Loads a a supported text navigation file such as <code>.navigation.lst</code>.
	 * <p>
	 * This method searches up the directory hierarchy and loads the first supported navigation definition file with the configured navigation base filename.
	 * </p>
	 * @implSpec This implementation currently supports the following navigation definition formats:
	 *           <ul>
	 *           <li>Text list.</li>
	 *           <li>TURF</li>
	 *           </ul>
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @return The navigation items loaded from the file.
	 * @throws IOException if there is an I/O error loading the navigation list file.
	 * @see GuiseMummy#CONFIG_KEY_MUMMY_NAVIGATION_BASE_NAME
	 */
	public Optional<Stream<NavigationItem>> loadNavigation(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact) throws IOException {
		final String navigationBaseName = context.getConfiguration().getString(CONFIG_KEY_MUMMY_NAVIGATION_BASE_NAME);
		final Set<String> navigationFilenames = SUPPORTED_NAVIGATION_FILE_EXTENSIONS.stream().map(ext -> addExtension(navigationBaseName, ext))
				.collect(collectingAndThen(toCollection(LinkedHashSet::new), Collections::unmodifiableSet));
		return findAncestorFileByName(contextArtifact.getSourceDirectory(), navigationFilenames, Files::isRegularFile, context.getSiteSourceDirectory())
				.map(throwingFunction(navigationFile -> {
					switch(findFilenameExtension(navigationFile).orElseThrow(IllegalStateException::new)) {
						case Text.LST_NAME_EXTENSION:
							return loadNavigationList(context, contextArtifact, navigationFile);
						case TURF.FILENAME_EXTENSION:
							return loadNavigationTurf(context, contextArtifact, navigationFile);
						default:
							throw new AssertionError(String.format("Unrecognized navigation file type: `%s`.", navigationFile));
					}
				}));
	}

	/**
	 * Loads a text navigation list file (e.g. <code>.navigation.lst</code>). Each line is a reference to an artifact, relative to the <em>directory</em> of the
	 * corresponding navigation list file. Fragments and queries are allowed and maintained.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param navigationFile The path to the file containing the navigation list.
	 * @return The navigation items loaded from the file.
	 * @throws IOException if there is an I/O error loading the navigation list file.
	 */
	public Stream<NavigationItem> loadNavigationList(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Path navigationFile)
			throws IOException {
		final Path navigationListFileParent = navigationFile.getParent(); //each line reference is relative to the directory of the navigation file
		checkState(navigationListFileParent != null, "Navigation list file `%s` has no parent.", navigationFile);
		try (final Stream<String> lines = lines(navigationFile, UTF_8)) { //trailing empty lines are ignored, as desired
			return lines.<NavigationItem>flatMap(line -> { //map lines to Optional<NavigationItem>, warning if there is no artifact for a reference
				final Optional<NavigationItem> foundNavigationItem = createNavigationItemFromReference(context, contextArtifact, navigationFile, line, null,
						emptyList());
				if(!foundNavigationItem.isPresent()) {
					getLogger().warn("No target artifact found for relative reference `{}` in navigation file `{}`.", line, navigationFile);
				}
				return foundNavigationItem.stream();
			}).collect(toList()).stream(); //(important) collect the artifacts to a list to prevent any exceptions upon stream iteration after method return
		} catch(final IllegalArgumentException illegalArgumentException) {
			throw new IOException(illegalArgumentException.getLocalizedMessage(), illegalArgumentException);
		} catch(final UncheckedIOException uncheckedIOException) { //possibly thrown by `lines()`
			throw uncheckedIOException.getCause();
		}
	}

	/**
	 * Creates a navigation item from the given reference. If the reference is relative, finds a target navigation artifact from the reference. Relative
	 * references are considered to be relative relative to the <em>directory</em> of the given navigation file. Fragments and queries are allowed and maintained.
	 * @implSpec This implementation delegates to {@link #createNavigationItemFromReference(MummyContext, Artifact, Path, URI, UrfResourceDescription, List)}.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param navigationFile The path to the file containing the navigation reference.
	 * @param navigationReference The navigation reference, which is an absolute URI or a reference to an artifact relative to the navigation file parent.
	 * @param description The description of the navigation item, or <code>null</code> if no description is available.
	 * @param navigation The navigation subordinate to this navigation item; may be empty.
	 * @return A navigation item for the reference, which will be empty if no target artifact could be found for the reference.
	 * @throws IllegalArgumentException if the given navigation file has no parent.
	 * @throws IllegalArgumentException if the reference is an invalid URI or URI reference path.
	 * @throws IllegalArgumentException if the reference is an absolute URI and there is no description or the description does does not indicate a label.
	 */
	protected Optional<NavigationItem> createNavigationItemFromReference(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact,
			@Nonnull final Path navigationFile, @Nonnull final String navigationReference, @Nullable UrfResourceDescription description,
			@Nonnull final List<NavigationItem> navigation) throws IllegalArgumentException {
		final URI navigationReferenceURI;
		try {
			navigationReferenceURI = new URI(navigationReference); //assume the reference is _relative to the navigation file_
		} catch(final URISyntaxException uriSyntaxException) {
			throw new IllegalArgumentException(String.format("Invalid reference <%s> in navigation file `%s`.", navigationReference, navigationFile));
		}
		return createNavigationItemFromReference(context, contextArtifact, navigationFile, navigationReferenceURI, description, navigation);
	}

	/**
	 * Creates a navigation item from the given reference. If the reference is relative, finds a target navigation artifact from the reference. Relative
	 * references are considered to be relative relative to the <em>directory</em> of the given navigation file. Fragments and queries are allowed and maintained.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param navigationFile The path to the file containing the navigation reference.
	 * @param navigationReference The navigation reference, which is an absolute URI or a reference to an artifact relative to the navigation file parent.
	 * @param description The description of the navigation item, or <code>null</code> if no description is available.
	 * @param navigation The navigation subordinate to this navigation item; may be empty.
	 * @return A navigation item for the reference, which will be empty if no target artifact could be found for the reference.
	 * @throws IllegalArgumentException if the given navigation file has no parent.
	 * @throws IllegalArgumentException if the reference is an invalid URI reference path.
	 * @throws IllegalArgumentException if the reference is an absolute URI and there is no description or the description does does not indicate a label.
	 * 
	 */
	protected Optional<NavigationItem> createNavigationItemFromReference(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact,
			@Nonnull final Path navigationFile, @Nonnull final URI navigationReference, @Nullable UrfResourceDescription description,
			@Nonnull final List<NavigationItem> navigation) throws IllegalArgumentException {
		final Path navigationListFileParent = navigationFile.getParent(); //each line reference is relative to the directory of the navigation file
		checkArgument(navigationListFileParent != null, "Navigation list file `%s` has no parent.", navigationFile);
		//external reference
		if(navigationReference.isAbsolute()) {
			checkArgument(description != null, "External URI navigation reference <%s> requires a description containing a label.", navigationReference);
			return Optional.of(DefaultNavigationItem.forReference(navigationReference.toString(), description, navigation));
		}
		//internal reference
		final URIPath navigationReferencePath = URIs.findURIPath(navigationReference)
				.orElseThrow(() -> new IllegalArgumentException(String.format("Navigation reference <%s> does not have a path.", navigationReference)));
		final Optional<Artifact> foundNavigationArtifact = context.findArtifactBySourceRelativeReference(navigationListFileParent, navigationReferencePath);
		if(!foundNavigationArtifact.isPresent()) {
			getLogger().warn("No target artifact found for relative reference `{}`.", navigationReferencePath);
		}
		return foundNavigationArtifact.map(navigationArtifact -> {
			//get the correct relative path to the artifact from the context artifact
			final URIPath contextArtifactRelativeReferencePath = context.relativizeResourceReference(contextArtifact.getSourcePath(),
					navigationArtifact.getSourcePath(), navigationArtifact instanceof CollectionArtifact);
			//change the reference path to get the appropriate href relative to the context artifact
			final String href = URIs.changePath(navigationReference, contextArtifactRelativeReferencePath).toString();
			return description != null ? DefaultNavigationItem.forArtifactReference(href, description, navigationArtifact, navigation)
					: DefaultNavigationItem.forArtifactReference(href, navigationArtifact, navigation);
		});
	}

	/**
	 * Loads a navigation definition file in TURF format (e.g. <code>.navigation.turf</code>). Each item can be one of the following:
	 * <ul>
	 * <li>A string, in which case the value is interpreted as a reference to an artifact, relative to the navigation file, identical to
	 * {@link #loadNavigationList(MummyContext, Artifact, Path)}.</li>
	 * <li>An object description which supports, in addition to {@value NavigationItem#PROPERTY_HANDLE_HREF}, the same descriptive properties an artifact would:
	 * <ul>
	 * <li>{@value NavigationItem#PROPERTY_HANDLE_HREF}: Either a string path to an artifact as in {@link #loadNavigationList(MummyContext, Artifact, Path)}, or a
	 * URL link to an external page.</li>
	 * <li>{@value Artifact#PROPERTY_HANDLE_LABEL}: The label for the navigation item, overriding any of the artifact.</li>
	 * <li>{@value Artifact#PROPERTY_HANDLE_ICON}: The icon of the navigation item, overriding any of the artifact.</li>
	 * <li>{@value NavigationItem#PROPERTY_HANDLE_NAVIGATION}: A property containing a nested navigation list.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * <p>
	 * The following is an example navigation definition:
	 * </p>
	 * <pre><code>
	 * [
	 *   "./"
	 *   "#quick-start"
	 *   "about.xhtml"
	 *   "help/"
	 *   * :
	 *     label="Documentation"
	 *     icon="fas/fa-book"
	 *     href = "doc/"
	 *   ;
	 *   * :
	 *     label="Source"
	 *     icon="fas/fa-file-code"
	 *       navigation=[
	 *       "foo/"
	 *       "bar/"
	 *     ]
	 *   ;
	 * ]
	 * </code></pre>
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param navigationFile The path to the file containing the navigation definition.
	 * @return The navigation items loaded from the file.
	 * @throws IOException if there is an I/O error loading the navigation file.
	 */
	public Stream<NavigationItem> loadNavigationTurf(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact, @Nonnull final Path navigationFile)
			throws IOException {
		final List<?> navigationObjects;
		try (final InputStream inputStream = new BufferedInputStream(newInputStream(navigationFile))) {
			navigationObjects = new TurfParser<List<Object>>(new SimpleGraphUrfProcessor()).parseDocument(inputStream).stream()
					//ensure there is only one root object
					.reduce(Streams.toFindOnly(
							() -> new UncheckedIOException(new IOException(String.format("Navigation file `%s` cannot contain more than one root object.", navigationFile)))))
					//make sure the root object is a list
					.flatMap(Objects.asInstance(List.class)).orElseThrow(() -> new UncheckedIOException(
							new IOException(String.format("Navigation file `%s` must contain a single list to describe navigation.", navigationFile))));
		} catch(final UncheckedIOException uncheckedIOException) {
			throw uncheckedIOException.getCause();
		}
		try {
			return navigationItemsFromUrfList(context, contextArtifact, navigationFile, navigationObjects);
		} catch(final IllegalArgumentException illegalArgumentException) {
			throw new IOException(String.format("Error in navigation file `%s`: %s", illegalArgumentException.getLocalizedMessage()), illegalArgumentException);
		}
	}

	/**
	 * Converts a list of objects from a TURF graph to navigation items. The objects adhere to the model described in
	 * {@link #loadNavigationTurf(MummyContext, Artifact, Path)}.
	 * @param context The context of static site generation.
	 * @param contextArtifact The artifact in which context the artifact is being generated, which may or may not be the same as the artifact being generated.
	 * @param navigationFile The path to the file containing the navigation definition.
	 * @param navigationObjects The list of navigation objects from the URF model.
	 * @return The navigation items loaded from the file.
	 * @throws IllegalArgumentException if the navigation objects are invalid or incorrect. The error message will not indicate the navigation file.
	 */
	protected Stream<NavigationItem> navigationItemsFromUrfList(@Nonnull MummyContext context, @Nonnull final Artifact contextArtifact,
			@Nonnull final Path navigationFile, @Nonnull final List<?> navigationObjects) throws IllegalArgumentException {
		return navigationObjects.stream().flatMap(navObject -> { //convert the parsed objects to navigation items
			if(navObject instanceof CharSequence) { //e.g. "about.xhtml#history" or <https://www.example.com/> (currently unsupported) 
				final Optional<NavigationItem> navItem = createNavigationItemFromReference(context, contextArtifact, navigationFile, navObject.toString(), null,
						emptyList());
				if(!navItem.isPresent()) {
					getLogger().warn("No target artifact found for relative reference `{}` in navigation file `{}`.", navObject, navigationFile);
				}
				return navItem.stream();
			} else if(navObject instanceof URI) { //e.g. <https://www.example.com/> (not currently supported; will throw exception for us)
				final Optional<NavigationItem> navItem = createNavigationItemFromReference(context, contextArtifact, navigationFile, (URI)navObject, null, emptyList());
				if(!navItem.isPresent()) {
					getLogger().warn("No target artifact found for relative reference `{}` in navigation file `{}`.", navObject, navigationFile);
				}
				return navItem.stream();
			} else if(navObject instanceof UrfResourceDescription) {
				final UrfResourceDescription navDescription = (UrfResourceDescription)navObject;
				final List<NavigationItem> navigation = navDescription.findPropertyValueByHandle(NavigationItem.PROPERTY_HANDLE_NAVIGATION)
						.flatMap(asInstance(List.class)).map(nav -> navigationItemsFromUrfList(context, contextArtifact, navigationFile, nav).collect(toUnmodifiableList()))
						.orElse(emptyList());
				return navDescription.findPropertyValueByHandle(NavigationItem.PROPERTY_HANDLE_HREF).map(href -> {
					if(href instanceof CharSequence) { //e.g. "about.xhtml#history" or <https://www.example.com/> (currently unsupported) 
						final Optional<NavigationItem> navItem = createNavigationItemFromReference(context, contextArtifact, navigationFile, href.toString(),
								navDescription, navigation);
						if(!navItem.isPresent()) {
							getLogger().warn("No target artifact found for relative reference `{}` in navigation file `{}`.", href, navigationFile);
						}
						return navItem.stream();
					} else if(href instanceof URI) { //e.g. <https://www.example.com/> (not currently supported; will throw exception for us)
						final Optional<NavigationItem> navItem = createNavigationItemFromReference(context, contextArtifact, navigationFile, (URI)href, navDescription,
								navigation);
						if(!navItem.isPresent()) {
							getLogger().warn("No target artifact found for relative reference `{}` in navigation file `{}`.", href, navigationFile);
						}
						return navItem.stream();
					} else {
						throw new IllegalArgumentException(String.format("Unsupported href `%s`.", href));
					}
				}).orElse(Stream.of(DefaultNavigationItem.fromDescription(navDescription, navigation))); //if no reference, just create an item from the description
			} else {
				throw new IllegalArgumentException(String.format("Unsupported object `%s`.", navObject));
			}
		});
	}

}
