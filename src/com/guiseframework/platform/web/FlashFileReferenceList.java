package com.guiseframework.platform.web;

import static com.garretwilson.lang.ClassUtilities.*;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.*;

import com.garretwilson.event.ProgressListener;
import com.garretwilson.lang.ObjectUtilities;
import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.Bookmark;
import com.guiseframework.audio.Audio;
import com.guiseframework.event.*;
import com.guiseframework.model.Notification;
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

	/**Initiates a platform file upload.
	@param platformFile Thet platform file to upload.
	@param destinationPath The path representing the destination of the platform file, relative to the application.
	@param destinationBookmark The bookmark to be used in uploading the platform file to the destination path, or <code>null</code> if no bookmark should be used.
	@exception NullPointerException if the given platform file and/or destination path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	@exception IllegalArgumentException if the provided path is absolute.
	@exception IllegalStateException the specified platform file can no longer be uploaded because, for example, other platform files have since been selected.	
	*/
	public void upload(final FlashPlatformFile platformFile, final String destinationPath, final Bookmark destinationBookmark)
	{
		if(!getPlatformFiles().contains(platformFile))	//if this list no longer knows about this platform file
		{
			throw new IllegalStateException("Platform file "+platformFile+" no longer available for upload; perhaps other platform files have since been selected.");
		}
		final String resolvedDestinationPath=getSession().getApplication().resolvePath(destinationPath);	//resolve the destination path
		final URI destinationURI=URI.create(destinationBookmark!=null ? resolvedDestinationPath+destinationBookmark.toString() : resolvedDestinationPath);	//construct a destination URI
		getDepictor().upload(platformFile, destinationURI);	//tell the depictor to initiate the platform file upload
	}	

		//TODO del all this; now uses platform

	/**Selects a group of platform files and notifies all listeners that the files have been selected.
	This method is called by the associated depictor and should normally not be called directly by an application.
	@param platformFiles The platform files selected.
	*/
	public void selectPlatformFiles(final List<PlatformFile> platformFiles)
	{
		firePlatformFilesSelected(platformFiles);	//fire an event to notify that the files have been selected
	}

	/**Adds a platform file select listener.
	@param platformFileSelectListener The file select listener to add.
	*/
	public void addFileSelectListener(final ValueSelectListener<Collection<PlatformFile>> platformFileSelectListener)
	{
		getEventListenerManager().add(ValueSelectListener.class, platformFileSelectListener);	//add the listener
	}

	/**Removes a platform file select listener.
	@param platformFileSelectListener The file select listener to remove.
	*/
	public void removeActionListener(final ValueSelectListener<Collection<PlatformFile>> platformFileSelectListener)
	{
		getEventListenerManager().remove(ValueSelectListener.class, platformFileSelectListener);	//remove the listener
	}

	/**@return all registered platform file select listeners.*/
	@SuppressWarnings("unchecked")
	protected Iterable<ValueSelectListener<Collection<PlatformFile>>> getPlatformFileSelectListeners()
	{
		return (Iterable<ValueSelectListener<Collection<PlatformFile>>>)(Object)getEventListenerManager().getListeners(ValueSelectListener.class);
	}
	
	/**Fires a platform file select event to all registered platform file select listeners.
	This method delegates to {@link #firePlatformFilesSelected(ValueEvent)}.
	@param platformFiles The platform files selected.
	@see ValueSelectListener
	@see ValueEvent
	*/
	protected void firePlatformFilesSelected(final Collection<PlatformFile> platformFiles)
	{
		final EventListenerManager eventListenerManager=getEventListenerManager();	//get event listener support
		if(eventListenerManager.hasListeners(ValueSelectListener.class))	//if there are value select listeners registered
		{
			firePlatformFilesSelected(new ValueEvent<Collection<PlatformFile>>(this, platformFiles));	//create and fire a new platform file select event
		}
	}

	/**Fires a given value select event to all registered value select listeners.
	@param platformFileEvent The action event to fire.
	*/
	protected void firePlatformFilesSelected(final ValueEvent<Collection<PlatformFile>> platformFileSelectEvent)
	{
		for(final ValueSelectListener<Collection<PlatformFile>> platformFileSelectListener:getPlatformFileSelectListeners())	//for each platform file select listener
		{
			platformFileSelectListener.valueSelected(platformFileSelectEvent);	//dispatch the value select event to the listener
		}
	}


	/**The custom depictor type for this depicted object class.
	@author Garret Wilson
	@param <F> The type of file reference list to be depicted.
	*/
	public interface Depictor<F extends FlashFileReferenceList> extends com.guiseframework.platform.Depictor<F>
	{

		/**Requests that user be displayed a dialog for browsing files.*/
		public void browse();

		/**Initiates a platform file upload.
		@param platformFile Thet platform file to upload.
		@param destinationURI The URI representing the destination of the platform file, relative to the application.
		@exception NullPointerException if the given platform file and/or destination URI is <code>null</code>.
		@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
		@exception IllegalArgumentException if the provided path is absolute.
		*/
		public void upload(final FlashPlatformFile platformFile, final URI destinationURI);

	}

}
