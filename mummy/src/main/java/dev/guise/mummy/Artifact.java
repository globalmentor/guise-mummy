/*
 * Copyright © 2019 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.guise.mummy;

import static com.globalmentor.io.Filenames.*;
import static dev.guise.mummy.GuiseMummy.*;

import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import dev.guise.mummy.mummify.Mummifier;
import io.urf.model.UrfResourceDescription;

/// Provides information about the artifact produced by mummifying a resource.
///
/// Artifact equality is determined by target path as given by [#getTargetPath()].
///
/// There are several semantic categories of artifacts:
///
/// - **assets**: A set of artifacts that have been designated as *veiled* and which additionally will not result in pages generated.
/// - **composite artifact**: An artifact potentially composed of other artifacts.
/// - **collection artifact**: A type of *composite artifact* with a collection IRI path reference, i.e. one ending in a forward slash such as `…/widgets/`. The archetypal collection artifact implementation is a directory.
/// - **content artifact**: A special *subsumed artifact* of a directory that serves to represent its content. Historically the content artifact was `index.html`. Note that a content artifact is *not* a *child artifact* of its directory.
/// - **corporeal artifact**: An artifact that potentially contains content, such as a [CorporealSourceArtifact].
/// - **principal artifact**: An artifact that should be used as the canonical source and target for IRI path references. An artifact is normally its own principal artifact unless it is a *subsumed artifact* in which case the principal artifact is the one it is subsumed into and which should be used for IRI path references.
/// - **subsumed artifact**: An artifact that has been subsumed into another one one and should not be visible as separate IRI path references. The archetypal subsumed artifact is the *content artifact* (historically `index.html`) of a directory.
/// - **veiled artifact**: A resource that is available to be served to the user agent if access directly, but is not part of the normal navigation tree. Veiled artifacts are usually designated by some indication in the source filename, such as an underscore prefix.
///
/// @author Garret Wilson
public interface Artifact {

	//# resource description

	//## properties
	//### general properties

	/// The property handle of the artist identifier, such as "Jane Doe". Typically used for images.
	public static final String PROPERTY_HANDLE_ARTIST = "artist";
	/// The property handle of the author identifier, such as "Jane Doe". Typically used for pages.
	public static final String PROPERTY_HANDLE_AUTHOR = "author";
	/// The property handle of the [java.time.Instant] the work was originally created, but not necessarily published. For images this would be the time the
	/// original photo was taken.
	public static final String PROPERTY_HANDLE_CREATED_AT = "createdAt";
	/// The property handle of the copyright message, such as "Copyright © 2020 GlobalMentor, Inc. All Rights Reserved.".
	public static final String PROPERTY_HANDLE_COPYRIGHT = "copyright";
	/// The property handle of the string describing the artifact.
	public static final String PROPERTY_HANDLE_DESCRIPTION = "description";
	/// The icon associated with the artifact, used in a navigation link for example, in `group/name` form (e.g.
	/// `fas/fa-home` or `material-icons/home`).
	public static final String PROPERTY_HANDLE_ICON = "icon";
	/// The property handle of the string to use as a label, in navigation link text for example.
	public static final String PROPERTY_HANDLE_LABEL = "label";
	/// The property handle of the string naming artifact.
	public static final String PROPERTY_HANDLE_NAME = "name";
	/// The property handle specifying the [java.time.LocalDate] of publication.
	public static final String PROPERTY_HANDLE_PUBLISHED_ON = "publishedOn";
	/// The property handle of the title, such as a page title.
	public static final String PROPERTY_HANDLE_TITLE = "title";

	//### Guise Mummy properties

	/// The property tag of the `mummy/aspect` property for indicating that the artifact is an aspect (e.g. `"preview"`) of the resource.
	/// @see AspectualArtifact
	public static final URI PROPERTY_TAG_MUMMY_ASPECT = NAMESPACE.resolve("aspect");
	/// The property tag of the `mummy/altLocation` property for indicating an alternate (redirect) name.
	public static final URI PROPERTY_TAG_MUMMY_ALT_LOCATION = NAMESPACE.resolve("altLocation");
	/// The property tag of the `mummy/order` property for indicating e.g. navigation order.
	public static final URI PROPERTY_TAG_MUMMY_ORDER = NAMESPACE.resolve("order");
	/// The default Mummy order.
	/// @see #PROPERTY_TAG_MUMMY_ORDER
	public static final long MUMMY_ORDER_DEFAULT = 0;
	/// A [Boolean] value indicating whether the target description itself has been changed (or is new) and needs serializing. This is a transient property
	/// and is not normally persisted.
	/// @apiNote This property is used for incremental mummification; it is usually not appropriate to use in custom properties.
	public static final URI PROPERTY_TAG_MUMMY_DESCRIPTION_DIRTY = NAMESPACE.resolve("descriptionDirty");
	/// The [java.time.Instant] the source content was last modified.
	/// @apiNote This property is used for incremental mummification; it is usually not appropriate to use in custom properties.
	public static final URI PROPERTY_TAG_MUMMY_SOURCE_CONTENT_MODIFIED_AT = NAMESPACE.resolve("sourceContentModifiedAt");
	/// The property tag of the `mummy/template` for specifying a template path, relative to the source path.
	public static final URI PROPERTY_TAG_MUMMY_TEMPLATE = NAMESPACE.resolve("template");

	/// Returns the properties and their values describing the artifact.
	/// @return The properties and their values describing the artifact.
	public UrfResourceDescription getResourceDescription();

	//## description

	/// Looks up the description property in the resource description, returning it as a string if present.
	/// @return The string form of the description, if available.
	/// @see #getResourceDescription()
	/// @see #PROPERTY_HANDLE_DESCRIPTION
	public default Optional<String> findDescription() {
		return getResourceDescription().findPropertyValueByHandle(PROPERTY_HANDLE_DESCRIPTION).map(Object::toString);
	}

	//## label

	/// Determines the label to use for the artifact, for example to appear as the text of a link to the artifact.
	///
	/// This method will always return a value, determined in the following order of priority:
	///
	/// 1. The {@value #PROPERTY_HANDLE_LABEL} property.
	/// 2. The {@value #PROPERTY_HANDLE_NAME} property.
	/// 3. The {@value #PROPERTY_HANDLE_TITLE} property.
	/// 4. The [Path#getFileName()] of the target path of this artifact, with no extension.
	///
	/// @return The label determined to be used for this artifact.
	/// @see #findLabel()
	/// @see #findName()
	/// @see #findTitle()
	/// @see #getTargetPath()
	public default String determineLabel() {
		assert getTargetPath().getFileName() != null : "Artifacts are expected always to have filenames.";
		return findLabel().or(this::findName).or(this::findTitle).orElseGet(() -> removeExtension(getTargetPath().getFileName().toString()));
	}

	/// Looks up the label property in the resource description, returning it as a string if present.
	/// @return The string form of the label, if available.
	/// @see #getResourceDescription()
	/// @see #PROPERTY_HANDLE_LABEL
	public default Optional<String> findLabel() {
		return getResourceDescription().findPropertyValueByHandle(PROPERTY_HANDLE_LABEL).map(Object::toString);
	}

	//## name

	/// Looks up the name property in the resource description, returning it as a string if present.
	/// @return The string form of the name, if available.
	/// @see #getResourceDescription()
	/// @see #PROPERTY_HANDLE_NAME
	public default Optional<String> findName() {
		return getResourceDescription().findPropertyValueByHandle(PROPERTY_HANDLE_NAME).map(Object::toString);
	}

	//## title

	/// Determines the title to use for the artifact.
	///
	/// This method will always return a value, determined in the following order of priority:
	///
	/// 1. The {@value #PROPERTY_HANDLE_TITLE} property.
	/// 2. The {@value #PROPERTY_HANDLE_NAME} property.
	/// 3. The [Path#getFileName()] of the target path of this artifact, with no extension.
	///
	/// @return The label determined to be used for this artifact.
	/// @see #findName()
	/// @see #findTitle()
	/// @see #getTargetPath()
	public default String determineTitle() {
		assert getTargetPath().getFileName() != null : "Artifacts are expected always to have filenames.";
		return findTitle().or(this::findName).orElseGet(() -> removeExtension(getTargetPath().getFileName().toString()));
	}

	/// Looks up the title property in the resource description, returning it as a string if present.
	/// @return The string form of the title, if available.
	/// @see #getResourceDescription()
	/// @see #PROPERTY_HANDLE_TITLE
	public default Optional<String> findTitle() {
		return getResourceDescription().findPropertyValueByHandle(PROPERTY_HANDLE_TITLE).map(Object::toString);
	}

	/// Returns the path to the directory containing the artifact source file. If the artifact source path refers to a directory, this method returns the source
	/// path itself; otherwise this method returns the parent directory.
	/// @return The source directory of the artifact.
	/// @see #getSourcePath()
	public Path getSourceDirectory();

	/// Returns the path to the source of the artifact in the source tree.
	/// @apiNote Depending on the artifact implementation, the source path is not guaranteed to exist.
	/// @apiNote This method and all methods in this interface related to a source path in a file system may be moved eventually to [SourcePathArtifact].
	/// @return The path referring to the source of this artifact, which may be a file or a directory.
	public Path getSourcePath();

	/// Indicates whether the source path refers to a file as opposed to a directory. This implies that its source path is distinct from the source directory and
	/// that its source path will provide some independent filename.
	/// @apiNote This method is useful during mummification because a source path may not actually exist, making it difficult to ascertain the status.
	/// @return `true` if the source path represents a file and not a directory.
	/// @see #getSourcePath()
	/// @see #getSourceDirectory()
	public boolean isSourcePathFile();

	/// Retrieves the source paths that should be equivalent targets referring to this artifact.
	///
	/// For example, if a directory `/foo/` has a content source file of `/foo/index.xhtml`, both `/foo/` and
	/// `/foo/index.xhtml` would refer to this same artifact. In the logical resource model, the `/foo/index.xhtml` file is an implementation
	/// detail for storing the contents of the `/foo/` collection.
	///
	/// @return All source paths that, if referred to by source links, identify this same artifact.
	public Set<Path> getReferentSourcePaths();

	/// Returns the path to the generated artifact in the target tree.
	/// @return The path to the generated artifact in the target tree.
	public Path getTargetPath();

	/// Returns the mummifier responsible for mummifying this artifact.
	/// @return The mummifier responsible for mummifying this artifact.
	public Mummifier getMummifier();

	/// Indicates whether the artifact represents that something was "posted" or published on some date, with a hierarchical URI path indicating the post date.
	/// @return `true` if this artifact is a post.
	public boolean isPost();

	/// Indicates whether the artifact would normally be part of site navigation.
	///
	/// A navigable artifact may still be veiled, however, by filename conventions or other settings.
	///
	/// @return `true` if the artifact should be part of default navigation.
	public boolean isNavigable();

}
