package com.guiseframework.platform.web;

import java.util.*;
import static java.util.Collections.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Objects.*;

import com.garretwilson.net.URIPath;
import com.guiseframework.Bookmark;
import com.guiseframework.platform.*;

/**Representation of a Flash <code>flash.net.FileReferenceList</code> on the web platform.
The installed depictor must be of the specialized type {@link Depictor}.
This class depends on {@link FlashPlatformFile}.
@author Garret Wilson
*/
public class FlashFileReferenceList extends AbstractDepictedObject
{

	/**The bound property of the selected platform files.*/
	public final static String PLATFORM_FILES_PROPERTY=getPropertyName(FlashFileReferenceList.class, "platformFiles");

	/**@return The depictor for this object.*/
	@SuppressWarnings("unchecked")
	public Depictor<? extends FlashFileReferenceList> getDepictor() {return (Depictor<? extends FlashFileReferenceList>)super.getDepictor();} 

	/**The concurrent map of Flash platform files mapped to the IDs assigned to them by Flash.*/
	private final Map<String, FlashPlatformFile> idPlatformFileMap=new ConcurrentHashMap<String, FlashPlatformFile>();

		/**Retrieves a platform file by the ID assigned to it by Flash.
		@param id The ID assigned to the platform file by Flash.
		@return The specified platform file, or <code>null</code> if there is no platforom file with the given ID.
		@exception NullPointerException if the given ID is <code>null</code>.
		*/
		public FlashPlatformFile getPlatformFile(final String id) {return idPlatformFileMap.get(checkInstance(id, "Flash platform file ID cannot be null."));}

	/**The selected platform files.*/
	private List<FlashPlatformFile> platformFiles=emptyList();

		/**@return The selected platform files.*/
		public List<FlashPlatformFile> getPlatformFiles() {return platformFiles;}

		/**Sets the platform files.
		This is a bound property.
		@param newPlatformFiles The new selected platform files.
		@see #PLATFORM_FILES_PROPERTY
		@exception NullPointerException if the given platform files is <code>null</code>. 
		*/
		public void setPlatformFiles(final List<FlashPlatformFile> newPlatformFiles)
		{
			if(platformFiles!=checkInstance(newPlatformFiles, "Platform files cannot be null."))
			{
				final List<FlashPlatformFile> oldPlatformFiles=platformFiles;	//get the old value
				platformFiles=newPlatformFiles;	//actually change the value
				idPlatformFileMap.clear();	//clear the map of platform files TODO fix race condition, perhaps by adding read/write locks; it is very unlikely that this class would be used in such as way as to create race conditions, however, as most of the time the file references of a file reference list will be updated at long intervals  
				for(final FlashPlatformFile platformFile:newPlatformFiles)	//for each platform file
				{
					idPlatformFileMap.put(platformFile.getID(), platformFile);	//map the platform file with the ID assigned to it by Flash
				}
				firePropertyChange(PLATFORM_FILES_PROPERTY, oldPlatformFiles, newPlatformFiles);	//indicate that the value changed
			}
		}

	/**Default constructor.*/
	public FlashFileReferenceList()
	{
	}

	/**Requests that the user be presented a dialog for browsing files.*/
	public void browse()
	{
		getDepictor().browse();	//tell the depictor to start
	}

	/**Cancels a platform file upload or download.
	@param platformFile Thet platform file to cancel.
	@exception NullPointerException if the given platform file is <code>null</code>.
	@exception IllegalStateException the specified platform file can no longer be canceled because, for example, other platform files have since been selected.	
	*/
	public void cancel(final FlashPlatformFile platformFile)
	{
		if(!getPlatformFiles().contains(platformFile))	//if this list no longer knows about this platform file
		{
			throw new IllegalStateException("Platform file "+platformFile+" no longer available for cancel; perhaps other platform files have since been selected.");
		}
		getDepictor().cancel(platformFile);	//tell the depictor to cancel the platform file
	}	

	/**Initiates a platform file upload.
	@param platformFile Thet platform file to upload.
	@param destinationPath The path representing the destination of the platform file, relative to the application.
	@param destinationBookmark The bookmark to be used in uploading the platform file to the destination path, or <code>null</code> if no bookmark should be used.
	@exception NullPointerException if the given platform file and/or destination path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	@exception IllegalArgumentException if the provided path is absolute.
	@exception IllegalStateException the specified platform file can no longer be uploaded because, for example, other platform files have since been selected.	
	*/
	public void upload(final FlashPlatformFile platformFile, final URIPath destinationPath, final Bookmark destinationBookmark)
	{
		if(!getPlatformFiles().contains(platformFile))	//if this list no longer knows about this platform file
		{
			throw new IllegalStateException("Platform file "+platformFile+" no longer available for upload; perhaps other platform files have since been selected.");
		}
		getDepictor().upload(platformFile, destinationPath, destinationBookmark);	//tell the depictor to initiate the platform file upload
	}	

	/**The custom depictor type for this depicted object class.
	@author Garret Wilson
	@param <F> The type of file reference list to be depicted.
	*/
	public interface Depictor<F extends FlashFileReferenceList> extends com.guiseframework.platform.Depictor<F>
	{

		/**Requests that user be displayed a dialog for browsing files.*/
		public void browse();

		/**Cancels a platform file upload or download.
		@param platformFile Thet platform file to cancel.
		@exception NullPointerException if the given platform file is <code>null</code>.
		@exception IllegalStateException the specified platform file can no longer be canceled because, for example, other platform files have since been selected.	
		*/
		public void cancel(final FlashPlatformFile platformFile);

		/**Initiates a platform file upload.
		@param platformFile Thet platform file to upload.
		@param destinationURI The URI representing the destination of the platform file, relative to the application.
		@exception NullPointerException if the given platform file and/or destination path is <code>null</code>.
		@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
		@exception IllegalArgumentException if the provided path is absolute.
		*/
		public void upload(final FlashPlatformFile platformFile, final URIPath destinationPath, final Bookmark destinationBookmark);

	}

}
