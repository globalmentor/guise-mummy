package com.guiseframework.platform.web;

import java.net.URI;
import java.util.*;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.*;
import com.garretwilson.util.*;

import com.guiseframework.platform.PlatformEvent;

/**A web depictor for a Flash <code>flash.net.FileReferenceList</code>.
@author Garret Wilson
*/
public class WebFlashFileReferenceListDepictor extends AbstractWebDepictor<FlashFileReferenceList> implements FlashFileReferenceList.Depictor<FlashFileReferenceList>
{

	/**The web commands for controlling audio.*/
	public enum FlashFileReferenceCommand implements WebCommand
	{
		/**The command to allow the user to browse to select a file.
		parameters: <code>{multiple:"<var>multiple</var>"}</code>
		*/
		FILE_BROWSE,

		/**The command to initiate an upload.
		parameters: <code>{id:"<var>fileReferenceID</var>", destinationURI:<var>destinationURI</var>}</code>
		*/
		FILE_UPLOAD;
	}
	
	/**The property for specifying the destination URI of a file upload.*/
	public final static String DESTINATION_URI_PROPERTY="destinationURI";
	/**The property for specifying the ID of a file.*/
	public final static String ID_PROPERTY="id";
	/**The property for specifying whether multiple files should be selected.*/
	public final static String MULTIPLE_PROPERTY="multiple";

	/**Requests that the user be presented with a dialog to browse.*/
	@SuppressWarnings("unchecked")
	public void browse()
	{
		getPlatform().getSendEventQueue().add(new WebCommandEvent<FlashFileReferenceCommand>(getDepictedObject(), FlashFileReferenceCommand.FILE_BROWSE,
				new NameValuePair<String, Object>(MULTIPLE_PROPERTY, Boolean.TRUE)));	//send a file browse command to the platform TODO fix single/multiple
	}

	/**Initiates a platform file upload.
	@param platformFile Thet platform file to upload.
	@param destinationURI The URI representing the destination of the platform file, relative to the application.
	@exception NullPointerException if the given platform file and/or destination URI is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	@exception IllegalArgumentException if the provided path is absolute.
	@exception ClassCastException if the current platform is not an {@link HTTPServletWebPlatform}.
	*/
	public void upload(final FlashPlatformFile platformFile, final URI destinationURI)
	{
			//add an identification of the Guise session to the URI if needed, as Flash 8 on FireFox sends the wrong HTTP session ID cookie value
		final URI sessionedDestinationURI=appendQueryParameters(destinationURI, new NameValuePair<String, String>(HTTPServletGuiseSessionManager.GUISE_SESSION_UUID_PARAMETER, getSession().getUUID().toString()));
		getPlatform().getSendEventQueue().add(new WebCommandEvent<FlashFileReferenceCommand>(getDepictedObject(), FlashFileReferenceCommand.FILE_UPLOAD,	//send a file upload command to the platform
				new NameValuePair<String, Object>(ID_PROPERTY, platformFile.getID()),	//send the ID of the file
				new NameValuePair<String, Object>(DESTINATION_URI_PROPERTY, sessionedDestinationURI)));	//indicate the destination
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
			final Number transferred=asInstance(properties.get("transferred"), Number.class);	//get the bytes transferred, if reported TODO use a constant
			if(transferred!=null)	//if we have progress
			{
				final Number total=asInstance(properties.get("total"), Number.class);	//get the total bytes to transfer
				final String flashPlatformFileID=asInstance(properties.get("id"), String.class);	//get the ID of the platform file
				final FlashPlatformFile platformFile=flashPlatformFileID!=null ? flashFileReferenceList.getPlatformFile(flashPlatformFileID) : null;	//get the platform file identified
				if(platformFile!=null)	//if we know the platform file
				{
					platformFile.fireProgressed(transferred.longValue(), total!=null ? total.longValue() : -1);	//update the file progress
				}
			}
		}
	}

}