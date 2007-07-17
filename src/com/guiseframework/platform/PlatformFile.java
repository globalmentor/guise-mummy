package com.guiseframework.platform;

import com.guiseframework.Bookmark;
import com.guiseframework.event.ProgressListenable;

/**A local file on a platform.
@author Garret Wilson
*/
public interface PlatformFile extends ProgressListenable
{

	/**@return The name of the file.*/
	public String getName();

	/**@return The size of the file, or -1 if the size is unknown.*/
	public long getSize();

	/**Uploads the file from the platform.
	@param destinationPath The path representing the destination of the platform file, relative to the application.
	@param destinationBookmark The bookmark to be used in uploading the platform file to the destination path, or <code>null</code> if no bookmark should be used.
	@exception NullPointerException if the given destination path and/or listener is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	@exception IllegalArgumentException if the provided path is absolute.
	@exception IllegalStateException the platform file can no longer be uploaded because, for example, other platform files have since been selected.	
	*/
	public void upload(final String destinationPath, final Bookmark destinationBookmark);

}
