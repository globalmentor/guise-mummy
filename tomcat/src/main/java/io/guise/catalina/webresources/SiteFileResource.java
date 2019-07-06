
package io.guise.catalina.webresources;

import java.io.File;
import java.util.jar.Manifest;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.FileResource;

/**
 * A Guise-aware site resource based upon a file on the file system. This implementation recognizes Guise metadata sidecar files for determining Internet media
 * types.
 * @author Garret Wilson
 */
public class SiteFileResource extends FileResource {

	/**
	 * 
	 * @param root The {@link WebResourceRoot} this new resource will be added to.
	 * @param webAppMount The path within the web application at which this resource will be mounted.
	 * @param resource The file backing this resource.
	 * @param readOnly Whether the resource is read-only.
	 * @param manifest The JAR manifest if any.
	 */
	public SiteFileResource(final WebResourceRoot root, final String webAppPath, final File resource, final boolean readOnly, final Manifest manifest) {
		super(root, webAppPath, resource, readOnly, manifest);
	}

}
