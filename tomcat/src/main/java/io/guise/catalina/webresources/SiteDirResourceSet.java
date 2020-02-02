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

import io.urf.model.*;
import io.urf.turf.TurfParser;
import io.urf.vocab.content.Content;

/**
 * Tomcat source set for a Guise generated site directory.
 * @implSpec This version loads Guise generated Internet media type dynamically for retrieved resources.
 * @author Garret Wilson
 */
public class SiteDirResourceSet extends DirResourceSet {

	private static final Log log = LogFactory.getLog(SiteDirResourceSet.class);

	private Path baseDir; //set when initialized in initInternal()

	private final Path descriptionBaseDir;

	/**
	 * A Guise-aware site-based set of resources based upon a site directory.
	 *
	 * @param root The {@link WebResourceRoot} this new resource set will be added to.
	 * @param webAppMount The path within the web application at which this resource set will be mounted. For example, to add a directory of JARs to a web
	 *          application, the directory would be mounted at <code>"WEB-INF/lib/"</code>.
	 * @param base The absolute path to the directory on the file system from which the resources will be served.
	 * @param internalPath The path within this new resource set where resources will be served from.
	 * @param descriptionBaseDir The root directory of the description metadata tree, which may or may not be the same as the site doc base.
	 */
	public SiteDirResourceSet(@Nonnull final WebResourceRoot root, @Nonnull final String webAppMount, @Nonnull final String base,
			@Nonnull final String internalPath, @Nonnull final Path descriptionBaseDir) {
		super(root, webAppMount, base, internalPath);
		this.descriptionBaseDir = requireNonNull(descriptionBaseDir);
	}

	@Override
	protected void initInternal() throws LifecycleException {
		super.initInternal();
		baseDir = getFileBase().toPath();
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
				final Path descriptionFile = addExtension(changeBase(file.toPath(), baseDir, descriptionBaseDir), "@.turf"); //TODO use constant
				if(isRegularFile(descriptionFile)) {
					try (final InputStream inputStream = new BufferedInputStream(newInputStream(descriptionFile))) {
						new TurfParser<List<Object>>(new SimpleGraphUrfProcessor()).parseDocument(inputStream).stream().filter(UrfResourceDescription.class::isInstance)
								.map(UrfResourceDescription.class::cast).findFirst().ifPresentOrElse(description -> {
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
