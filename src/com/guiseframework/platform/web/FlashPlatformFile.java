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

package com.guiseframework.platform.web;

import static com.globalmentor.java.Objects.*;

import com.globalmentor.model.TaskState;
import com.globalmentor.net.URIPath;
import com.guiseframework.Bookmark;
import com.guiseframework.event.ProgressEvent;
import com.guiseframework.event.ProgressListener;
import com.guiseframework.platform.AbstractPlatformFile;

/**A local file represented by a Flash <code>flash.net.FileReference</code> on the web platform.
Because Flash registers progress listeners on a per-file basis, this file keeps track of a single listener,
available only to web classes (as other upload implementations may not register listeners for individual files).
@author Garret Wilson
*/
public class FlashPlatformFile extends AbstractPlatformFile
{

	/**The Flash file reference list that owns this platform file.*/
	private final FlashFileReferenceList fileReferenceList;

		/**@return The Flash file reference list that owns this platform file.*/
		protected FlashFileReferenceList getFileReferenceList() {return fileReferenceList;}

	/**The ID given to the file by Flash.*/
	private final String id;

		/**@return The ID given to the file by Flash.*/
		public String getID() {return id;}

	/**File reference list, name and size constructor.
	@param fileReferenceList The Flash file reference list that owns this platform file.
	@param id The ID given to the file by Flash.
	@param name The name of the file.
	@param size The size of the file, or -1 if the size is unknown.
	@exception NullPointerException if the given ID, file reference list, and/or name is <code>null</code>.
	*/
	public FlashPlatformFile(final FlashFileReferenceList fileReferenceList, final String id, final String name, final long size)
	{
		super(name, size);	//construct the parent class
		this.id=checkInstance(id, "ID cannot be null.");
		this.fileReferenceList=checkInstance(fileReferenceList, "File reference list cannot be null.");
	}

	/**Uploads the file from the platform.
	@param destinationPath The path representing the destination of the platform file, relative to the application.
	@param destinationBookmark The bookmark to be used in uploading the platform file to the destination path, or <code>null</code> if no bookmark should be used.
	@exception NullPointerException if the given destination path and/or listener is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	@exception IllegalArgumentException if the provided path is absolute.
	@exception IllegalStateException the platform file can no longer be uploaded because, for example, other platform files have since been selected.	
	*/
	public void upload(final URIPath destinationPath, final Bookmark destinationBookmark)
	{
		getFileReferenceList().upload(this, destinationPath, destinationBookmark);	//tell the owner file reference list to upload this file
	}

	/**Cancels the current upload or download.*/
	public void cancel()
	{
		getFileReferenceList().cancel(this);	//tell the owner file reference list to cancel this file transfer
	}

	/**Fires a progress event to all registered progress listeners.
	This method delegates to the super version and is present in this class so that it may be called from the depictor of {@link FlashFileReferenceList}.
	@param state The state of the progress.
	@param transferred The current number of bytes transferred, or <code>-1</code> if not known.
	@param total The total or estimated total bytes to transfer, or <code>-1</code> if not known.
	@exception NullPointerException if the given state is <code>null</code>.
	@see ProgressListener
	@see ProgressEvent
	@see WebFlashFileReferenceListDepictor
	*/
	protected void fireProgressed(final TaskState state, final long transferred, final long total)
	{
		super.fireProgressed(state, transferred, total);	//delegate to the super version
	}

}
