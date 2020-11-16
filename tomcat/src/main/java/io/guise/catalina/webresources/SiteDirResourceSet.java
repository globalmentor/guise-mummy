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
import static com.globalmentor.io.Paths.*;
import static com.globalmentor.net.URIs.*;
import static java.nio.file.Files.*;
import static java.util.Objects.*;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.*;

import org.apache.catalina.*;
import org.apache.catalina.webresources.*;
import org.apache.juli.logging.*;
import org.apache.tomcat.util.digester.Digester;

import com.globalmentor.io.Filenames;
import com.globalmentor.io.Paths;
import com.globalmentor.java.Objects;

import io.urf.model.*;
import io.urf.turf.TURF;
import io.urf.turf.TurfParser;
import io.urf.vocab.content.Content;

/**
 * Tomcat resource set for a Guise generated site directory.
 * @implSpec This implementation loads the Internet media type dynamically for retrieved resources, stored in metadata in an optional sidecar file for each
 *           file. The sidecar metadata file must be in TURF Properties format. By default it is expected to be stored in a file with the same name in the
 *           description base directory, but with a <code>-.tupr</code> extension added to whatever extension the filename already has. Determination of the
 *           sidecar is configurable using {@link #setDescriptionFileSidecarPrefix(String)} and {@link #setDescriptionFileSidecarExtension(String)}.
 * @author Garret Wilson
 */
public class SiteDirResourceSet extends DirResourceSet {

	private static final Log log = LogFactory.getLog(SiteDirResourceSet.class);

	private File descriptionFileBase; //set when initialized in initInternal()

	/**
	 * Returns the absolute complete path to the base of the description file tree as a {@link File}. Includes the configured description base and the internal
	 * path.
	 * @implNote This method is analogous to {@link #getFileBase()}, but for the description sidecar files.
	 * @return The absolute complete path to the base of the description file tree as a {@link File}; includes the configured description base and the internal
	 *         path.
	 * @see #getDescriptionBase()
	 * @see #getInternalPath()
	 */
	protected final File getDescriptionFileBase() {
		return descriptionFileBase;
	}

	@Nullable
	private String descriptionBase = null;

	/**
	 * Returns the root directory of the description metadata tree, which may or may not be the same as the resource set base.
	 * @return The absolute path of the root directory of the description metadata tree, or <code>null</code> if not defined, indicating that resource set base
	 *         should be used.
	 * @see #getBase()
	 */
	public String getDescriptionBase() {
		return descriptionBase;
	}

	/**
	 * Sets the root directory of the description metadata tree.
	 * @param descriptionBase The absolute path of the root directory of the description metadata tree.
	 */
	public void setDescriptionBase(@Nonnull final String descriptionBase) {
		this.descriptionBase = requireNonNull(descriptionBase);
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

	/**
	 * No-arguments constructor. The description base directory, if not changed before initialization, will be assumed to be be the same as the site base.
	 * @apiNote This constructor is necessary if used with the {@link Digester}.
	 */
	public SiteDirResourceSet() {
		super();
	}

	/**
	 * A Guise-aware site-based set of resources based upon a site directory.
	 * @param root The {@link WebResourceRoot} this new resource set will be added to.
	 * @param webAppMount The path within the web application at which this resource set will be mounted. For example, to add a directory of JARs to a web
	 *          application, the directory would be mounted at <code>"WEB-INF/lib/"</code>.
	 * @param base The absolute path to the directory on the file system from which the resources will be served.
	 * @param internalPath The path within this new resource set where resources will be served from.
	 * @param descriptionBase The absolute path of the root directory of the description metadata tree, which may or may not be the same as the resource set base.
	 *          The internal path will be appended to this path as it is to the base path for the served resources.
	 */
	public SiteDirResourceSet(@Nonnull final WebResourceRoot root, @Nonnull final String webAppMount, @Nonnull final String base,
			@Nonnull final String internalPath, @Nonnull final String descriptionBase) {
		super(root, webAppMount, base, internalPath);
		this.descriptionBase = requireNonNull(descriptionBase);
	}

	@Override
	protected void initInternal() throws LifecycleException {
		super.initInternal();
		final String descriptionBaset = getDescriptionBase();
		//default to the file base if there is no custom description base set
		descriptionFileBase = descriptionBaset != null ? new File(getDescriptionBase(), getInternalPath()) : getFileBase();
		checkType(descriptionFileBase);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec If creating a {@link FileResource}, this detects Guise metadata and uses it to initialize the resource with custom Internet media type
	 *           information.
	 */
	@Override
	public WebResource getResource(String path) {
		checkPath(path);
		final String webAppMount = getWebAppMount();
		final WebResourceRoot root = getRoot();
		if(path.startsWith(webAppMount)) {
			final File file = file(path.substring(webAppMount.length()), false);
			if(file != null && file.exists()) {
				if(file.isDirectory() && !isCollectionPath(path)) {
					path += PATH_SEPARATOR;
				}
				final FileResource fileResource = new FileResource(root, path, file, isReadOnly(), getManifest());

				//load any description sidecar
				final Path filePath = file.toPath();
				final String filename = Paths.findFilename(filePath)
						.orElseThrow(() -> new IllegalArgumentException(String.format("Path %s has no filename.", filePath)));
				final String descriptionFilename = Filenames.addExtension(getDescriptionFileSidecarPrefix() + filename, getDescriptionFileSidecarExtension()); //e.g. `filename.ext.-.tupr`
				final Path descriptionFile = changeBase(filePath.resolveSibling(descriptionFilename), getFileBase().toPath(), getDescriptionFileBase().toPath());
				if(isRegularFile(descriptionFile)) {
					try (final InputStream inputStream = new BufferedInputStream(newInputStream(descriptionFile))) {
						new TurfParser<List<Object>>(new SimpleGraphUrfProcessor()).parseDocument(inputStream, TURF.PROPERTIES_MEDIA_TYPE).stream()
								.flatMap(Objects.asInstances(UrfResourceDescription.class)).findFirst().ifPresentOrElse(description -> {
									//set the MIME type if indicated
									description.findPropertyValue(Content.TYPE_PROPERTY_TAG).ifPresent(contentType -> fileResource.setMimeType(contentType.toString()));
								}, () -> log.warn(String.format("No description found for resource %s in file %s.", file, descriptionFile)));

					} catch(final IOException ioException) {
						log.error(String.format("Error loading resource-specific metadata for resource %s from %s.", file, descriptionFile), ioException);
					}
				}

				return fileResource;

			}
		}
		return super.getResource(path);
	}

}
