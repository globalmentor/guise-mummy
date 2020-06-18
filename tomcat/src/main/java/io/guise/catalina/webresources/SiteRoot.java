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

package io.guise.catalina.webresources;

import static com.globalmentor.io.Filenames.*;
import static com.globalmentor.net.URIs.*;
import static java.util.Objects.*;

import java.io.File;
import java.nio.file.Path;

import javax.annotation.*;

import org.apache.catalina.*;
import org.apache.catalina.webresources.*;

import io.urf.turf.TURF;

/**
 * Guise Tomcat site root.
 * @author Garret Wilson
 * @implSpec This implementation uses a {@link SiteDirResourceSet} configured to load the Internet media type dynamically for retrieved resources, stored in
 *           metadata in an optional sidecar file for each file. The identification of each sidecar file is configurable using
 *           {@link #setDescriptionFileSidecarPrefix(String)} and {@link #setDescriptionFileSidecarExtension(String)}, which will be used to configure
 *           {@link SiteDirResourceSet}. See that class for more details.
 * @see SiteDirResourceSet
 */
public class SiteRoot extends StandardRoot {

	private final Path descriptionBaseDir;

	private String descriptionFileSidecarPrefix = "";

	/**
	 * Retrieves the filename prefix to add to a file to discover its description sidecar file, if any.
	 * @implSpec Defaults to the empty string (i.e. no prefix).
	 * @return The description file sidecar filename prefix.
	 * @see #getDescriptionFileSidecarExtension()
	 */
	public String getDescriptionFileSidecarPrefix() {
		return descriptionFileSidecarPrefix;
	}

	/**
	 * Sets the filename prefix to add to a file to discover its description sidecar file, if any.
	 * @implSpec Defaults to the empty string (i.e. no prefix).
	 * @param prefix The filename prefix to use to discover description sidecar files.
	 * @see #setDescriptionFileSidecarExtension(String)
	 */
	public void setDescriptionFileSidecarPrefix(@Nonnull final String prefix) {
		descriptionFileSidecarPrefix = requireNonNull(prefix);
	}

	private String descriptionFileSidecarExtension = addExtension("-", TURF.PROPERTIES_FILENAME_EXTENSION);

	/**
	 * Retrieves the filename prefix to use to discover a file's description sidecar file, if any.
	 * @implSpec Defaults to <code>-.tupr</code>.
	 * @return The description file sidecar filename extension.
	 * @see #getDescriptionFileSidecarPrefix()
	 */
	public String getDescriptionFileSidecarExtension() {
		return descriptionFileSidecarExtension;
	}

	/**
	 * Sets the filename prefix to use to discover a file's description sidecar file, if any.
	 * @implSpec Defaults to <code>-.tupr</code>.
	 * @param extension The filename extension to use to discover description sidecar files.
	 * @see #setDescriptionFileSidecarPrefix(String)
	 */
	public void setDescriptionFileSidecarExtension(@Nonnull final String extension) {
		descriptionFileSidecarExtension = requireNonNull(extension);
	}

	/**
	 * Creates the root with a record of the directory used for descriptions of site resources.
	 * @param descriptionBaseDir The root directory of the description metadata tree, which may or may not be the same as the site doc base.
	 */
	public SiteRoot(@Nonnull final Path descriptionBaseDir) {
		this.descriptionBaseDir = requireNonNull(descriptionBaseDir);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns a specialized {@link SiteDirResourceSet} to serve as a factory for creating specialized Guise-aware file resources.
	 */
	@Override
	protected WebResourceSet createMainResourceSet() {
		final Context context = getContext();
		final String docBase = context.getDocBase();
		if(docBase != null) {
			File baseFile = new File(docBase);
			if(!baseFile.isAbsolute()) {
				baseFile = new File(((Host)context.getParent()).getAppBaseFile(), baseFile.getPath());
			}
			if(baseFile.isDirectory()) {
				final SiteDirResourceSet siteDirResourceSet = new SiteDirResourceSet(this, ROOT_PATH, baseFile.getAbsolutePath(), ROOT_PATH, descriptionBaseDir);
				siteDirResourceSet.setDescriptionFileSidecarPrefix(getDescriptionFileSidecarPrefix());
				siteDirResourceSet.setDescriptionFileSidecarExtension(getDescriptionFileSidecarExtension());
				return siteDirResourceSet;
			}
		}
		return super.createMainResourceSet();
	}

}
