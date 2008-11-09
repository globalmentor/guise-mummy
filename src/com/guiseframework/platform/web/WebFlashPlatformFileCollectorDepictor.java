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

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.globalmentor.java.Enums.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.net.URIs.*;


import com.globalmentor.model.TaskState;
import com.globalmentor.net.URIPath;
import com.globalmentor.util.*;
import com.guiseframework.Bookmark;
import com.guiseframework.platform.PlatformFileCollector;
import com.guiseframework.platform.PlatformEvent;
import com.guiseframework.platform.PlatformFile;

/**A web depictor for a Flash <code>flash.net.FileReferenceList</code>.
@author Garret Wilson
*/
public class WebFlashPlatformFileCollectorDepictor extends AbstractWebDepictor<PlatformFileCollector> implements PlatformFileCollector.Depictor<PlatformFileCollector>
{

	/**The web commands for controlling audio.*/
	public enum FlashFileReferenceCommand implements WebPlatformCommand
	{
		/**The command to allow the user to browse to select a file.
		parameters: <code>{@value #MULTIPLE_PROPERTY}:"<var>multiple</var>"}</code>
		*/
		FILE_BROWSE,

		/**The command to cancel a transfer.
		parameters: <code>{{@value #ID_PROPERTY}:"<var>fileReferenceID</var>"}</code>
		*/
		FILE_CANCEL,

		/**The command to initiate an upload.
		parameters: <code>{{@value #ID_PROPERTY}:"<var>fileReferenceID</var>", {@value #DESTINATION_URI_PROPERTY}:<var>destinationURI</var>}</code>
		*/
		FILE_UPLOAD;

		/**The property for specifying the destination URI of a file upload.*/
		public final static String DESTINATION_URI_PROPERTY="destinationURI";
		/**The property for specifying the ID of a file.*/
		public final static String ID_PROPERTY="id";
		/**The property for specifying whether multiple files should be selected.*/
		public final static String MULTIPLE_PROPERTY="multiple";

	}

	/**The concurrent map of Flash platform files mapped to the IDs assigned to them by Flash.*/
	private final Map<String, FlashPlatformFile> idPlatformFileMap=new ConcurrentHashMap<String, FlashPlatformFile>();

		/**Retrieves a platform file by the ID assigned to it by Flash.
		@param id The ID assigned to the platform file by Flash.
		@return The specified platform file, or <code>null</code> if there is no platforom file with the given ID.
		@exception NullPointerException if the given ID is <code>null</code>.
		*/
		public FlashPlatformFile getPlatformFile(final String id) {return idPlatformFileMap.get(checkInstance(id, "Flash platform file ID cannot be null."));}

	/**Requests that the user be presented with a dialog to browse.*/
	@SuppressWarnings("unchecked")
	public void browse()
	{
		getPlatform().getSendMessageQueue().add(new WebCommandDepictEvent<FlashFileReferenceCommand>(getDepictedObject(), FlashFileReferenceCommand.FILE_BROWSE,
				new NameValuePair<String, Object>(FlashFileReferenceCommand.MULTIPLE_PROPERTY, Boolean.TRUE)));	//send a file browse command to the platform TODO fix single/multiple
	}

	/**Cancels a platform file upload or download.
	@param platformFile Thet platform file to cancel.
	@exception NullPointerException if the given platform file is <code>null</code>.
	@exception IllegalStateException the specified platform file can no longer be canceled because, for example, other platform files have since been selected.	
	*/
	@SuppressWarnings("unchecked")
	public void cancel(final PlatformFile platformFile)
	{
		getPlatform().getSendMessageQueue().add(new WebCommandDepictEvent<FlashFileReferenceCommand>(getDepictedObject(), FlashFileReferenceCommand.FILE_CANCEL,	//send a file cancel command to the platform
				new NameValuePair<String, Object>(FlashFileReferenceCommand.ID_PROPERTY, ((FlashPlatformFile)platformFile).getID())));	//send the ID of the file
	}	

	/**Initiates a platform file upload.
	@param platformFile Thet platform file to upload.
	@param destinationURI The URI representing the destination of the platform file, relative to the application.
	@exception NullPointerException if the given platform file and/or destination path is <code>null</code>.
	@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
	@exception IllegalArgumentException if the provided path is absolute.
	*/
	@SuppressWarnings("unchecked")
	public void upload(final PlatformFile platformFile, final URIPath destinationPath, final Bookmark destinationBookmark)
	{
		final URIPath resolvedDestinationPath=getSession().getApplication().resolvePath(destinationPath.checkRelative());	//resolve the destination path
		final URI destinationURI=destinationBookmark!=null ? URI.create(resolvedDestinationPath.toString()+destinationBookmark.toString()) : resolvedDestinationPath.toURI();	//construct a destination URI
			//add an identification of the Guise session to the URI if needed, as Flash 8 on FireFox sends the wrong HTTP session ID cookie value
		final URI sessionedDestinationURI=appendQueryParameters(destinationURI, new NameValuePair<String, String>(WebPlatform.GUISE_SESSION_UUID_URI_QUERY_PARAMETER, getSession().getUUID().toString()));
		getPlatform().getSendMessageQueue().add(new WebCommandDepictEvent<FlashFileReferenceCommand>(getDepictedObject(), FlashFileReferenceCommand.FILE_UPLOAD,	//send a file upload command to the platform
				new NameValuePair<String, Object>(FlashFileReferenceCommand.ID_PROPERTY, ((FlashPlatformFile)platformFile).getID()),	//send the ID of the file
				new NameValuePair<String, Object>(FlashFileReferenceCommand.DESTINATION_URI_PROPERTY, sessionedDestinationURI)));	//indicate the destination
	}	

	/**Processes an event from the platform.
	@param event The event to be processed.
	@exception IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
	*/
	public void processEvent(final PlatformEvent event)
	{
		if(event instanceof WebChangeDepictEvent)	//if a property changed
		{
			final WebChangeDepictEvent webChangeEvent=(WebChangeDepictEvent)event;	//get the web change event
			final PlatformFileCollector flashFileReferenceList=getDepictedObject();	//get the depicted object
			if(webChangeEvent.getDepictedObject()!=flashFileReferenceList)	//if the event was meant for another depicted object
			{
				throw new IllegalArgumentException("Depict event "+event+" meant for depicted object "+webChangeEvent.getDepictedObject());
			}
			final Map<String, Object> properties=webChangeEvent.getProperties();	//get the new properties
			final List<Map<String, Object>> fileReferences=(List<Map<String, Object>>)asInstance(properties.get("fileReferences"), List.class);	//get the new file references, if any
			if(fileReferences!=null)	//if file references were given
			{
				idPlatformFileMap.clear();	//clear the map of platform files TODO fix race condition, perhaps by adding read/write locks; it is very unlikely that this class would be used in such as way as to create race conditions, however, as most of the time the file references of a file reference list will be updated at long intervals  
				final List<FlashPlatformFile> platformFileList=new ArrayList<FlashPlatformFile>(fileReferences.size());	//create a new list to store the platform files
				for(final Map<String, Object> fileReference:fileReferences)	//for each file reference
				{
					final String id=(String)fileReference.get("id");
					final FlashPlatformFile platformFile=new FlashPlatformFile(flashFileReferenceList, id, (String)fileReference.get("name"), ((Number)fileReference.get("size")).longValue());
					platformFileList.add(platformFile);
					idPlatformFileMap.put(platformFile.getID(), platformFile);	//map the platform file with the ID assigned to it by Flash
				}
				flashFileReferenceList.setPlatformFiles(platformFileList);	//tell the file reference list which platform files it now has
			}			
			final String taskStateString=asInstance(properties.get("taskState"), String.class);	//get the task state, if reported TODO use a constant
			final Number transferred=asInstance(properties.get("transferred"), Number.class);	//get the bytes transferred, if reported TODO use a constant
			if(taskStateString!=null && transferred!=null)	//if we have progress
			{
				final TaskState taskState=getSerializedEnum(TaskState.class, taskStateString);	//get the task state
				final Number total=asInstance(properties.get("total"), Number.class);	//get the total bytes to transfer, if any
				final String flashPlatformFileID=asInstance(properties.get("id"), String.class);	//get the ID of the platform file

				final FlashPlatformFile platformFile=flashPlatformFileID!=null ? getPlatformFile(flashPlatformFileID) : null;	//get the platform file identified
				if(platformFile!=null)	//if we know the platform file
				{
					platformFile.fireProgressed(taskState, transferred.longValue(), total!=null ? total.longValue() : -1);	//update the file progress
				}
			}
		}
	}

}
