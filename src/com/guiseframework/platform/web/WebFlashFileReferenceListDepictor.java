package com.guiseframework.platform.web;

import java.util.*;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.event.ProgressListener;
import com.garretwilson.util.*;
import com.guiseframework.Bookmark;
import com.guiseframework.platform.PlatformEvent;
import com.guiseframework.platform.PlatformFile;

/**A web depictor for a Flash <code>flash.net.FileReferenceList</code>.
@author Garret Wilson
*/
public class WebFlashFileReferenceListDepictor extends AbstractWebDepictor<FlashFileReferenceList> implements FlashFileReferenceList.Depictor<FlashFileReferenceList>
{

	/**The web commands for controlling audio.*/
	public enum FlashFileReferenceCommand implements WebCommand
	{
		/**The command to allow the user to browse browse.*/
		FILE_BROWSE;
	}
	
	/**The property for specifying whether multiple files should be selected.*/
	public final static String MULTIPLE_PROPERTY="multiple";

	/**Requests that the user be presented with a dialog to browse.*/
	@SuppressWarnings("unchecked")
	public void browse()
	{
		getPlatform().getSendEventQueue().add(new WebCommandEvent<FlashFileReferenceCommand>(getDepictedObject(), FlashFileReferenceCommand.FILE_BROWSE,
				new NameValuePair<String, Object>(MULTIPLE_PROPERTY, Boolean.TRUE)));	//send a file browse command to the platform TODO fix single/multiple
	}

	/**Initiates file uploads.
	@param destinationPath The path representing the destination of the platform files, relative to the application.
	@param destinationBookmark The bookmark to be used in uploading the platform files to the destination path, or <code>null</code> if no bookmark should be used.
	@param progressListener The listener that will be notified when progress is made for a particular platform file upload.
	@param platformFiles Thet platform files to upload.
	@exception NullPointerException if the given destination path and/or listener is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	@exception IllegalArgumentException if the provided path is absolute.
	@exception ClassCastException of one or more of the platform files is not a {@link FlashPlatformFile}.
	@exception IllegalStateException if one or more of the specified platform files can no longer be uploaded because, for example, other platform files have since been selected.	
	*/
	public void upload(final String destinationPath, final Bookmark destinationBookmark, final ProgressListener progressListener, final PlatformFile... platformFiles)
	{
		
	}	

	/**Processes an event from the platform.
	@param event The event to be processed.
	@exception IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
	*/
	public void processEvent(final PlatformEvent event)
	{
		if(event instanceof WebChangeEvent)	//if a property changed
		{
			final WebChangeEvent webChangeEvent=(WebChangeEvent)event;	//get the web change event
			final FlashFileReferenceList flashFileReferenceList=getDepictedObject();	//get the depicted object
			if(webChangeEvent.getDepictedObject()!=flashFileReferenceList)	//if the event was meant for another depicted object
			{
				throw new IllegalArgumentException("Depict event "+event+" meant for depicted object "+webChangeEvent.getDepictedObject());
			}
			final Map<String, Object> properties=webChangeEvent.getProperties();	//get the new properties
			final List<Map<String, Object>> fileReferences=(List<Map<String, Object>>)asInstance(properties.get("fileReferences"), List.class);	//get the new file references, if any
			if(fileReferences!=null)	//if file references were given
			{
				final List<FlashPlatformFile> platformFileList=new ArrayList<FlashPlatformFile>(fileReferences.size());	//create a new list to store the platform files
				for(final Map<String, Object> fileReference:fileReferences)	//for each file reference
				{
					platformFileList.add(new FlashPlatformFile(flashFileReferenceList, (String)fileReference.get("id"), (String)fileReference.get("name"), ((Number)fileReference.get("size")).longValue()));
				}
				flashFileReferenceList.setPlatformFiles(platformFileList);	//tell the file reference list which platform files it now has
			}			
		}
	}
}
