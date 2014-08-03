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

import com.globalmentor.model.NameValuePair;
import com.globalmentor.model.TaskState;
import static com.globalmentor.net.URIs.*;
import com.globalmentor.util.*;
import com.guiseframework.platform.*;

/**
 * An abstract depictor for a collector of platform files for the web platform.
 * @author Garret Wilson
 */
public class AbstractWebPlatformFileCollectorDepictor extends AbstractWebDepictor<PlatformFileCollector> implements
		PlatformFileCollector.Depictor<PlatformFileCollector> {

	/** The web commands for controlling the platform file collector. */
	public enum WebPlatformFileCollectorCommand implements WebPlatformCommand {
		/**
		 * The command to allow the user to browse to select a file. parameters: <code>{@value #MULTIPLE_PROPERTY}:"<var>multiple</var>"}</code>
		 */
		FILE_BROWSE,

		/**
		 * The command to cancel a transfer. parameters: <code>{{@value #ID_PROPERTY}:"<var>fileReferenceID</var>"}</code>
		 */
		FILE_CANCEL,

		/**
		 * The command to initiate an upload. parameters:
		 * <code>{{@value #ID_PROPERTY}:"<var>fileReferenceID</var>", {@value #DESTINATION_URI_PROPERTY}:<var>destinationURI</var>}</code>
		 */
		FILE_UPLOAD;

		/** The property for specifying the destination URI of a file upload. */
		public final static String DESTINATION_URI_PROPERTY = "destinationURI";
		/** The property for specifying the ID of a file. */
		public final static String ID_PROPERTY = "id";
		/** The property for specifying whether multiple files should be selected. */
		public final static String MULTIPLE_PROPERTY = "multiple";

	}

	/** The concurrent map of web platform files mapped to the IDs assigned to them, either by Flash or (for Google Gears) by Guise. */
	private final Map<String, WebPlatformFile> idPlatformFileMap = new ConcurrentHashMap<String, WebPlatformFile>();

	/**
	 * Retrieves a platform file by the ID assigned to it.
	 * @param id The ID assigned to the platform file.
	 * @return The specified platform file, or <code>null</code> if there is no platforom file with the given ID.
	 * @throws NullPointerException if the given ID is <code>null</code>.
	 */
	public WebPlatformFile getPlatformFile(final String id) {
		return idPlatformFileMap.get(checkInstance(id, "Platform file ID cannot be null."));
	}

	/** Requests that the user be presented with a dialog to browse. */
	@SuppressWarnings("unchecked")
	public void browse() {
		getPlatform().getSendMessageQueue().add(
				new WebCommandDepictEvent<WebPlatformFileCollectorCommand>(getDepictedObject(), WebPlatformFileCollectorCommand.FILE_BROWSE,
						new NameValuePair<String, Object>(WebPlatformFileCollectorCommand.MULTIPLE_PROPERTY, Boolean.TRUE))); //send a file browse command to the platform TODO fix single/multiple
	}

	/**
	 * Cancels a platform file upload or download.
	 * @param platformFile Thet platform file to cancel.
	 * @throws NullPointerException if the given platform file is <code>null</code>.
	 * @throws IllegalStateException the specified platform file can no longer be canceled because, for example, other platform files have since been selected.
	 */
	@SuppressWarnings("unchecked")
	public void cancel(final PlatformFile platformFile) {
		getPlatform().getSendMessageQueue().add(
				new WebCommandDepictEvent<WebPlatformFileCollectorCommand>(getDepictedObject(), WebPlatformFileCollectorCommand.FILE_CANCEL, //send a file cancel command to the platform
						new NameValuePair<String, Object>(WebPlatformFileCollectorCommand.ID_PROPERTY, ((WebPlatformFile)platformFile).getID()))); //send the ID of the file
	}

	/**
	 * Initiates a platform file upload.
	 * @param platformFile Thet platform file to upload.
	 * @param destinationURI The URI representing the destination of the platform file, either absolute or relative to the application.
	 * @throws NullPointerException if the given platform file and/or destination URI is <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	public void upload(final PlatformFile platformFile, final URI destinationURI) {
		final URI resolvedDestinationURI = getSession().getApplication().resolveURI(destinationURI); //resolve the destination URI
		//add an identification of the Guise session to the URI if needed, as Flash 8 on FireFox sends the wrong HTTP session ID cookie value TODO transfer to a Flash-only version if we can
		final URI sessionedDestinationURI = appendQueryParameter(resolvedDestinationURI, WebPlatform.GUISE_SESSION_UUID_URI_QUERY_PARAMETER, getSession().getUUID()
				.toString());
		getPlatform().getSendMessageQueue().add(
				new WebCommandDepictEvent<WebPlatformFileCollectorCommand>(getDepictedObject(), WebPlatformFileCollectorCommand.FILE_UPLOAD, //send a file upload command to the platform
						new NameValuePair<String, Object>(WebPlatformFileCollectorCommand.ID_PROPERTY, ((WebPlatformFile)platformFile).getID()), //send the ID of the file
						new NameValuePair<String, Object>(WebPlatformFileCollectorCommand.DESTINATION_URI_PROPERTY, sessionedDestinationURI))); //indicate the destination
	}

	/**
	 * Processes an event from the platform.
	 * @param event The event to be processed.
	 * @throws IllegalArgumentException if the given event is a relevant {@link DepictEvent} with a source of a different depicted object.
	 */
	public void processEvent(final PlatformEvent event) {
		if(event instanceof WebChangeDepictEvent) { //if a property changed
			final WebChangeDepictEvent webChangeEvent = (WebChangeDepictEvent)event; //get the web change event
			final PlatformFileCollector platformFileCollector = getDepictedObject(); //get the depicted object
			if(webChangeEvent.getDepictedObject() != platformFileCollector) { //if the event was meant for another depicted object
				throw new IllegalArgumentException("Depict event " + event + " meant for depicted object " + webChangeEvent.getDepictedObject());
			}
			final Map<String, Object> properties = webChangeEvent.getProperties(); //get the new properties
			final List<Map<String, Object>> fileReferences = (List<Map<String, Object>>)asInstance(properties.get("fileReferences"), List.class); //get the new file references, if any
			if(fileReferences != null) { //if file references were given
				idPlatformFileMap.clear(); //clear the map of platform files TODO fix race condition, perhaps by adding read/write locks; it is very unlikely that this class would be used in such as way as to create race conditions, however, as most of the time the file references of a file reference list will be updated at long intervals  
				final List<WebPlatformFile> platformFileList = new ArrayList<WebPlatformFile>(fileReferences.size()); //create a new list to store the platform files
				for(final Map<String, Object> fileReference : fileReferences) { //for each file reference
					final String id = (String)fileReference.get("id");
					final WebPlatformFile platformFile = new WebPlatformFile(platformFileCollector, id, (String)fileReference.get("name"),
							((Number)fileReference.get("size")).longValue());
					platformFileList.add(platformFile);
					idPlatformFileMap.put(platformFile.getID(), platformFile); //map the platform file with the ID assigned to it
				}
				platformFileCollector.setPlatformFiles(platformFileList); //tell the file reference list which platform files it now has
			}
			final String taskStateString = asInstance(properties.get("taskState"), String.class); //get the task state, if reported TODO use a constant
			final Number transferred = asInstance(properties.get("transferred"), Number.class); //get the bytes transferred, if reported TODO use a constant
			if(taskStateString != null && transferred != null) { //if we have progress
				final TaskState taskState = getSerializedEnum(TaskState.class, taskStateString); //get the task state
				final Number total = asInstance(properties.get("total"), Number.class); //get the total bytes to transfer, if any
				final String webPlatformFileID = asInstance(properties.get("id"), String.class); //get the ID of the platform file
				final WebPlatformFile platformFile = webPlatformFileID != null ? getPlatformFile(webPlatformFileID) : null; //get the platform file identified
				if(platformFile != null) { //if we know the platform file
					platformFile.fireProgressed(taskState, transferred.longValue(), total != null ? total.longValue() : -1); //update the file progress
				}
			}
		}
	}

}
