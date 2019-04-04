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

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import io.urf.model.UrfResourceDescription;

/**
 * Provides information about the artifact produced by mummifying a resource.
 * <p>
 * Artifact equality is determined by target path as given by {@link #getTargetPath()}.
 * </p>
 * @author Garret Wilson
 */
public interface Artifact {

	//#resource description

	/** The property handle of the string naming artifact. */
	public static final String PROPERTY_HANDLE_NAME = "name";
	/** The property handle of the string describing the artifact. */
	public static final String PROPERTY_HANDLE_DESCRIPTION = "description";
	/** The property handle of the string to use as a label, in link text for example. */
	public static final String PROPERTY_HANDLE_LABEL = "label";
	/** The property handle of the title, such as a page title. */
	public static final String PROPERTY_HANDLE_TITLE = "title";

	/** @return The properties and their values describing the artifact. */
	public UrfResourceDescription getResourceDescription();

	//##description

	/**
	 * Looks up the description property in the resource description, returning it as a string if present.
	 * @return The string form of the description, if available.
	 * @see #getResourceDescription()
	 * @see #PROPERTY_HANDLE_DESCRIPTION
	 */
	public default Optional<String> findDescription() {
		return getResourceDescription().findPropertyValueByHandle(PROPERTY_HANDLE_DESCRIPTION).map(Object::toString);
	}

	//##label

	/**
	 * Determines the label to use for the artifact, for example to appear as the text of a link to the artifact.
	 * <p>
	 * This method will always return a value, determined in the following order of priority:
	 * </p>
	 * <ol>
	 * <li>The {@value #PROPERTY_HANDLE_LABEL} property.</li>
	 * <li>The {@value #PROPERTY_HANDLE_NAME} property.</li>
	 * <li>The {@link Path#getFileName()} of the target path of this artifact, with no extension.</li>
	 * </ol>
	 * @return The label determined to be used for this artifact.
	 * @see #findLabel()
	 * @see #findName()
	 * @see #getTargetPath()
	 */
	public default String determineLabel() {
		assert getTargetPath().getFileName() != null : "Artifacts are expected always to have filenames.";
		return findLabel().or(this::findName).orElseGet(() -> removeExtension(getTargetPath().getFileName().toString()));
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

	//##name

	/**
	 * Looks up the name property in the resource description, returning it as a string if present.
	 * @return The string form of the name, if available.
	 * @see #getResourceDescription()
	 * @see #PROPERTY_HANDLE_NAME
	 */
	public default Optional<String> findName() {
		return getResourceDescription().findPropertyValueByHandle(PROPERTY_HANDLE_NAME).map(Object::toString);
	}

	//##title

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

	/** @return The path referring to the source of this artifact, wich may be a file or a directory. */
	public Path getSourcePath();

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

}
