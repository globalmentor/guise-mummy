package com.guiseframework.platform.web;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.event.ProgressListener;
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
		protected String getID() {return id;}

	/**The listener that will be notified when progress is made for a particular platform file upload, or <code>null</code> if no listener has been registered.*/
	private ProgressListener progressListener=null;

		/**@return The listener that will be notified when progress is made for a particular platform file upload, or <code>null</code> if no listener has been registered.*/
		ProgressListener getProgressListener() {return progressListener;}

		/**Sets the listener to be notified when progress is made for a particular platform file upload
		@param progressListener The listener that will be notified when progress is made for a particular platform file upload, or <code>null</code> if no listener should be registered.
		*/
		void setProgressListener(final ProgressListener progressListener) {this.progressListener=progressListener;}

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
}
