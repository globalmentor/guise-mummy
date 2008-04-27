/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.guiseframework.platform;

import com.globalmentor.net.URIPath;
import com.guiseframework.Bookmark;
import com.guiseframework.event.ProgressListenable;

/**A local file on a platform.
@author Garret Wilson
*/
public interface PlatformFile extends ProgressListenable<Long>
{

	/**@return The name of the file.*/
	public String getName();

	/**@return The size of the file, or -1 if the size is unknown.*/
	public long getSize();

	/**Cancels the current upload or download.*/
	public void cancel();

	/**Uploads the file from the platform.
	@param destinationPath The path representing the destination of the platform file, relative to the application.
	@param destinationBookmark The bookmark to be used in uploading the platform file to the destination path, or <code>null</code> if no bookmark should be used.
	@exception NullPointerException if the given destination path and/or listener is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	@exception IllegalArgumentException if the provided path is absolute.
	@exception IllegalStateException the platform file can no longer be uploaded because, for example, other platform files have since been selected.	
	*/
	public void upload(final URIPath destinationPath, final Bookmark destinationBookmark);

}
