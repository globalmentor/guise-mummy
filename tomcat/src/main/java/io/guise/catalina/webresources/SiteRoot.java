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
import java.util.Optional;

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

	/**
	 * Returns The context document base as a {@link File}, resolved to the {@link Host} app base as necessary.
	 * @return The current document base if any.
	 * @see Context#getDocBase()
	 * @see Host#getAppBaseFile()
	 */
	protected Optional<File> findDocBaseFile() {
		final Context context = getContext();
		return Optional.ofNullable(context.getDocBase()).map(docBase -> { //resolve to host app base as necessary
			File docBaseFile = new File(docBase);
			if(!docBaseFile.isAbsolute()) {
				docBaseFile = new File(((Host)context.getParent()).getAppBaseFile(), docBaseFile.getPath());
			}
			return docBaseFile;
		});
	}

	@Nullable
	private String descriptionBase;

	/**
	 * Returns the defined root directory of the description metadata tree, which may or may not be the same as the site doc base.
	 * @return The root directory of the description metadata tree, either an absolute pathname or a relative to the {@link Host} app base pathname, or
	 *         <code>null</code> if not defined, indicating that the context doc base should be used.
	 * @see Context#getDocBase()
	 */
	public String getDescriptionBase() {
		return descriptionBase;
	}

	/**
	 * Sets the root directory of the description metadata tree.
	 * @param descriptionBase The root directory of the description metadata tree, either an absolute pathname or a relative to the {@link Host} app base
	 *          pathname.
	 */
	public void setDescriptionBase(@Nonnull final String descriptionBase) {
		this.descriptionBase = requireNonNull(descriptionBase);
	}

	/**
	 * Returns the root directory of the description metadata tree as a {@link File}, which may or may not be the same as the site doc base; or the site doc base
	 * if not defined. The file will be resolved to the {@link Host} app base as necessary.
	 * @implSpec Defaults to the which site doc base.
	 * @return The root directory of the description metadata tree.
	 * @see Host#getAppBaseFile()
	 * @see #findDocBaseFile()
	 */
	public Optional<File> findDescriptionBaseFile() {
		final Context context = getContext();
		return Optional.ofNullable(getDescriptionBase()).map(descriptionBase -> { //resolve to host app base as necessary
			File descriptionBaseFile = new File(descriptionBase);
			if(!descriptionBaseFile.isAbsolute()) {
				descriptionBaseFile = new File(((Host)context.getParent()).getAppBaseFile(), descriptionBaseFile.getPath());
			}
			return descriptionBaseFile;
		}).or(this::findDocBaseFile);
	}

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

	/** Creates the root using the context doc base as the directory for site resources descriptions. */
	public SiteRoot() {
		this((String)null);
	}

	/**
	 * Creates the root with a record of the directory used for descriptions of site resources.
	 * @apiNote The description base which may or may not be the same as the site doc base.
	 * @param descriptionBase The root directory of the description metadata tree, either an absolute pathname or a relative to the {@link Host} app base
	 *          pathname, or <code>null</code> if the the site doc base. should be used.
	 */
	public SiteRoot(@Nullable final String descriptionBase) {
		this.descriptionBase = descriptionBase;
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This implementation returns a specialized {@link SiteDirResourceSet} to serve as a factory for creating specialized Guise-aware file resources.
	 * @see #getDescriptionBase()
	 */
	@Override
	protected WebResourceSet createMainResourceSet() {
		return findDocBaseFile().filter(File::isDirectory).<WebResourceSet>flatMap(docBaseFile -> {
			return findDescriptionBaseFile().map(descriptionBaseFile -> {
				final SiteDirResourceSet siteDirResourceSet = new SiteDirResourceSet(this, ROOT_PATH, docBaseFile.getAbsolutePath(), ROOT_PATH,
						descriptionBaseFile.getAbsolutePath());
				siteDirResourceSet.setDescriptionFileSidecarPrefix(getDescriptionFileSidecarPrefix());
				siteDirResourceSet.setDescriptionFileSidecarExtension(getDescriptionFileSidecarExtension());
				return siteDirResourceSet;
			});
		}).orElseGet(super::createMainResourceSet);
	}

}
