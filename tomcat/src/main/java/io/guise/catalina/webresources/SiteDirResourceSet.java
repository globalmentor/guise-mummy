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

import java.io.File;

import org.apache.catalina.*;
import org.apache.catalina.webresources.*;

public class SiteDirResourceSet extends DirResourceSet {

	/**
	 * A Guise-aware site-based set of resources based upon a site directory.
	 *
	 * @param root The {@link WebResourceRoot} this new resource set will be added to.
	 * @param webAppMount The path within the web application at which this resource set will be mounted. For example, to add a directory of JARs to a web
	 *          application, the directory would be mounted at <code>"WEB-INF/lib/"</code>.
	 * @param base The absolute path to the directory on the file system from which the resources will be served.
	 * @param internalPath The path within this new resource set where resources will be served from.
	 */
	public SiteDirResourceSet(WebResourceRoot root, String webAppMount, String base, String internalPath) {
		super(root, webAppMount, base, internalPath);
	}

	/**
	 * {@inheritDoc}
	 * @implSpec This version creates a specialized {@link SiteFileResource} which detects Guise metadata and uses it to indicate custom Internet media type
	 *           information.
	 * @implNote This implementation duplicates almost all the code from the {@link DirResourceSet#getResource(String)} implementation of this method because the
	 *           {@link DirResourceSet} implementation provides no way to plug in a file resource factory.
	 * @see SiteFileResource
	 */
	@Override
	public WebResource getResource(String path) {
		checkPath(path);
		String webAppMount = getWebAppMount();
		WebResourceRoot root = getRoot();
		if(path.startsWith(webAppMount)) {
			File f = file(path.substring(webAppMount.length()), false);
			if(f == null) {
				return new EmptyResource(root, path);
			}
			if(!f.exists()) {
				return new EmptyResource(root, path, f);
			}
			if(f.isDirectory() && path.charAt(path.length() - 1) != '/') {
				path = path + '/';
			}
			return new SiteFileResource(root, path, f, isReadOnly(), getManifest());
		} else {
			return new EmptyResource(root, path);
		}
	}

}
