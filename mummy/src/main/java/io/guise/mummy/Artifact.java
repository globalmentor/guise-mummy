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
import static io.guise.mummy.GuiseMummy.*;

import java.net.URI;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import io.guise.mummy.mummify.Mummifier;
import io.urf.model.UrfResourceDescription;

/**
 * Provides information about the artifact produced by mummifying a resource.
 * <p>
 * Artifact equality is determined by target path as given by {@link #getTargetPath()}.
 * </p>
 * <p>
 * There are several semantic categories of artifacts:
 * </p>
 * <dl>
 * <dt><dfn>assets</dfn></dt>
 * <dd>A set of artifacts that have been designated as <dfn>veiled</dfn> and which additionally will not result in pages generated.</dd>
 * <dt><dfn>composite artifact</dfn></dt>
 * <dd>An artifact potentially composed of other artifacts.</dd>
 * <dt><dfn>collection artifact</dfn></dt>
 * <dd>A type of <dfn>composite artifact</dfn> with a collection IRI path reference, i.e. one ending in a forward slash such as <code>…/widgets/</code>. The
 * archetypal collection artifact implementation is a directory.</dd>
 * <dt><dfn>content artifact</dfn></dt>
 * <dd>A special <dfn>subsumed artifact</dfn> of a directory that serves to represent its content. Historically the content artifact was
 * <code>index.html</code>. Note that a content artifact is <em>not</em> a <dfn>child artifact</dfn> of its directory.</dd>
 * <dt><dfn>corporeal artifact</dfn></dt>
 * <dd>An artifact that potentially contains content, such as a {@link CorporealSourceArtifact}.</dd>
 * <dt><dfn>principal artifact</dfn></dt>
 * <dd>An artifact that should be used as the canonical source and target for IRI path references. An artifact is normally its own principal artifact unless it
 * is a <dfn>subsumed artifact</dfn> in which case the principal artifact is the one it is subsumed into and which should be used for IRI path references.</dd>
 * <dt><dfn>subsumed artifact</dfn></dt>
 * <dd>An artifact that has been subsumed into another one one and should not be visible as separate IRI path references. The archetypal subsumed artifact is
 * the <dfn>content artifact</dfn> (historically <code>index.html</code>) of a directory.</dd>
 * <dt><dfn>veiled artifact</dfn></dt>
 * <dd>A resource that is available to be served to the user agent if access directly, but is not part of the normal navigation tree. Veiled artifacts are
 * usually designated by some indication in the source filename, such as an underscore prefix.</dd>
 * </dl>
 * @author Garret Wilson
 */
public interface Artifact {

	//# resource description

	//## properties
	//### general properties

	/** The property handle of the copyright message, such as "Copyright © 2020 GlobalMentor, Inc. All Rights Reserved.". */
	public static final String PROPERTY_HANDLE_COPYRIGHT = "copyright";
	/** The property handle of the string describing the artifact. */
	public static final String PROPERTY_HANDLE_DESCRIPTION = "description";
	/**
	 * The icon associated with the artifact, used in a navigation link for example, in <code><var>group</var>/<var>name</var></code> form (e.g.
	 * <code>fas/fa-home</code> or <code>material-icons/home</code>).
	 */
	public static final String PROPERTY_HANDLE_ICON = "icon";
	/** The property handle of the string to use as a label, in navigation link text for example. */
	public static final String PROPERTY_HANDLE_LABEL = "label";
	/** The property handle of the string naming artifact. */
	public static final String PROPERTY_HANDLE_NAME = "name";
	/** The property handle specifying the {@link LocalDate} of publication. */
	public static final String PROPERTY_HANDLE_PUBLISHED_ON = "publishedOn";
	/** The property handle of the title, such as a page title. */
	public static final String PROPERTY_HANDLE_TITLE = "title";

	//### Guise Mummy properties

	/**
	 * The property tag of the <code>mummy/aspect</code> property for indicating that the artifact is an aspect (e.g. <code>"preview"</code>) of the resource.
	 * @see AspectualArtifact
	 */
	public static final URI PROPERTY_TAG_MUMMY_ASPECT = NAMESPACE.resolve("aspect");
	/** The property tag of the <code>mummy/altLocation</code> property for indicating an alternate (redirect) name. */
	public static final URI PROPERTY_TAG_MUMMY_ALT_LOCATION = NAMESPACE.resolve("altLocation");
	/** The property tag of the <code>mummy/order</code> property for indicating e.g. navigation order. */
	public static final URI PROPERTY_TAG_MUMMY_ORDER = NAMESPACE.resolve("order");
	/**
	 * The default Mummy order.
	 * @see #PROPERTY_TAG_MUMMY_ORDER
	 */
	public static final long MUMMY_ORDER_DEFAULT = 0;
	/**
	 * A {@link Boolean} value indicating whether the target description itself has been changed (or is new) and needs serializing. This is a transient property
	 * and is not normally persisted.
	 * @apiNote This property is used for incremental mummification; it is usually not appropriate to use in custom properties.
	 */
	public static final URI PROPERTY_TAG_MUMMY_DESCRIPTION_DIRTY = NAMESPACE.resolve("descriptionDirty");
	/**
	 * The {@link Instant} the source content was last modified.
	 * @apiNote This property is used for incremental mummification; it is usually not appropriate to use in custom properties.
	 */
	public static final URI PROPERTY_TAG_MUMMY_SOURCE_CONTENT_MODIFIED_AT = NAMESPACE.resolve("sourceContentModifiedAt");
	/** The property tag of the <code>mummy/template</code> for specifying a template path, relative to the source path. */
	public static final URI PROPERTY_TAG_MUMMY_TEMPLATE = NAMESPACE.resolve("template");

	/** @return The properties and their values describing the artifact. */
	public UrfResourceDescription getResourceDescription();

	//## description

	/**
	 * Looks up the description property in the resource description, returning it as a string if present.
	 * @return The string form of the description, if available.
	 * @see #getResourceDescription()
	 * @see #PROPERTY_HANDLE_DESCRIPTION
	 */
	public default Optional<String> findDescription() {
		return getResourceDescription().findPropertyValueByHandle(PROPERTY_HANDLE_DESCRIPTION).map(Object::toString);
	}

	//## label

	/**
	 * Determines the label to use for the artifact, for example to appear as the text of a link to the artifact.
	 * <p>
	 * This method will always return a value, determined in the following order of priority:
	 * </p>
	 * <ol>
	 * <li>The {@value #PROPERTY_HANDLE_LABEL} property.</li>
	 * <li>The {@value #PROPERTY_HANDLE_NAME} property.</li>
	 * <li>The {@value #PROPERTY_HANDLE_TITLE} property.</li>
	 * <li>The {@link Path#getFileName()} of the target path of this artifact, with no extension.</li>
	 * </ol>
	 * @return The label determined to be used for this artifact.
	 * @see #findLabel()
	 * @see #findName()
	 * @see #findTitle()
	 * @see #getTargetPath()
	 */
	public default String determineLabel() {
		assert getTargetPath().getFileName() != null : "Artifacts are expected always to have filenames.";
		return findLabel().or(this::findName).or(this::findTitle).orElseGet(() -> removeExtension(getTargetPath().getFileName().toString()));
	}

	/**
	 * Looks up the label property in the resource description, returning it as a string if present.
	 * @return The string form of the label, if available.
	 * @see #getResourceDescription()
	 * @see #PROPERTY_HANDLE_LABEL
	 */
	public default Optional<String> findLabel() {
		return getResourceDescription().findPropertyValueByHandle(PROPERTY_HANDLE_LABEL).map(Object::toString);
	}

	//## name

	/**
	 * Looks up the name property in the resource description, returning it as a string if present.
	 * @return The string form of the name, if available.
	 * @see #getResourceDescription()
	 * @see #PROPERTY_HANDLE_NAME
	 */
	public default Optional<String> findName() {
		return getResourceDescription().findPropertyValueByHandle(PROPERTY_HANDLE_NAME).map(Object::toString);
	}

	//## title

	/**
	 * Determines the title to use for the artifact.
	 * <p>
	 * This method will always return a value, determined in the following order of priority:
	 * </p>
	 * <ol>
	 * <li>The {@value #PROPERTY_HANDLE_TITLE} property.</li>
	 * <li>The {@value #PROPERTY_HANDLE_NAME} property.</li>
	 * <li>The {@link Path#getFileName()} of the target path of this artifact, with no extension.</li>
	 * </ol>
	 * @return The label determined to be used for this artifact.
	 * @see #findName()
	 * @see #findTitle()
	 * @see #getTargetPath()
	 */
	public default String determineTitle() {
		assert getTargetPath().getFileName() != null : "Artifacts are expected always to have filenames.";
		return findTitle().or(this::findName).orElseGet(() -> removeExtension(getTargetPath().getFileName().toString()));
	}

	/**
	 * Looks up the title property in the resource description, returning it as a string if present.
	 * @return The string form of the title, if available.
	 * @see #getResourceDescription()
	 * @see #PROPERTY_HANDLE_TITLE
	 */
	public default Optional<String> findTitle() {
		return getResourceDescription().findPropertyValueByHandle(PROPERTY_HANDLE_TITLE).map(Object::toString);
	}

	/**
	 * Returns the path to the directory containing the artifact source file. If the artifact source path refers to a directory, this method returns the source
	 * path itself; otherwise this method returns the parent directory.
	 * @return The source directory of the artifact.
	 * @see #getSourcePath()
	 */
	public Path getSourceDirectory();

	/**
	 * Returns the path to the source of the artifact in the source tree.
	 * @apiNote Depending on the artifact implementation, the source path is not guaranteed to exist.
	 * @apiNote This method and all methods in this interface related to a source path in a file system may be moved eventually to {@link SourcePathArtifact}.
	 * @return The path referring to the source of this artifact, which may be a file or a directory.
	 */
	public Path getSourcePath();

	/**
	 * Indicates whether the source path refers to a file as opposed to a directory. This implies that its source path is distinct from the source directory and
	 * that its source path will provide some independent filename.
	 * @apiNote This method is useful during mummification because a source path may not actually exist, making it difficult to ascertain the status.
	 * @return <code>true</code> if the source path represents a file and not a directory.
	 * @see #getSourcePath()
	 * @see #getSourceDirectory()
	 */
	public boolean isSourcePathFile();

	/**
	 * Retrieves the source paths that should be equivalent targets referring to this artifact.
	 * <p>
	 * For example, if a directory <code>/foo/</code> has a content source file of <code>/foo/index.xhtml</code>, both <code>/foo/</code> and
	 * <code>/foo/index.xhtml</code> would refer to this same artifact. In the logical resource model, the <code>/foo/index.xhtml</code> file is an implementation
	 * detail for storing the contents of the <code>/foo/</code> collection.
	 * </p>
	 * @return All source paths that, if referred to by source links, identify this same artifact.
	 */
	public Set<Path> getReferentSourcePaths();

	/** @return The path to the generated artifact in the target tree. */
	public Path getTargetPath();

	/** @return The mummifier responsible for mummifying this artifact. */
	public Mummifier getMummifier();

	/**
	 * Indicates whether the artifact represents that something was "posted" or published on some date, with a hierarchical URI path indicating the post date.
	 * @return <code>true</code> if this artifact is a post.
	 */
	public boolean isPost();

	/**
	 * Indicates whether the artifact would normally be part of site navigation.
	 * <p>
	 * A navigable artifact may still be veiled, however, by filename conventions or other settings.
	 * </p>
	 * @return <code>true</code> if the artifact should be part of default navigation.
	 */
	public boolean isNavigable();

}
