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

import java.net.URI;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.locks.Lock;

import com.globalmentor.net.URIPath;
import com.guiseframework.Bookmark;
import com.guiseframework.GuiseApplication;
import com.guiseframework.event.ValueSelectListener;

/**The platform on which Guise objects are being depicted.
@author Garret Wilson
*/
public interface Platform
{

	/**@return The Guise application running on this platform.*/
	public GuiseApplication getApplication();

	/**@return The user local environment.*/
	public Environment getEnvironment();

	/**Generates a new depict ID unique to this session platform.
	@return A new depict ID unique to this session platform.
	*/
	public long generateDepictID();

	/**Determines the depictor appropriate for the given depicted object.
	A depictor class is located by individually looking up the depicted object class hiearchy for registered depictor classes.
	@param <O> The type of depicted object.
	@param depictedObject The depicted object for which a depictor should be returned.
	@return A depictor to depict the given component, or <code>null</code> if no depictor is registered.
	@exception IllegalStateException if the registered depictor could not be instantiated for some reason.
	*/
	public <O extends DepictedObject> Depictor<? super O> getDepictor(final O depictedObject);

	/**Registers a depicted object so that it can interact with the platform.
	@param depictedObject The depicted object to register.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	*/
	public void registerDepictedObject(final DepictedObject depictedObject);

	/**Unregisters a depicted object so that no longer interacts with the platform.
	@param depictedObject The depicted object to unregister.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	*/
	public void unregisterDepictedObject(final DepictedObject depictedObject);

	/**Retrieves a depicted object that has been registered with the platform by the ID of the depicted object.
	@param depictedObjectID The ID of the depicted object to retrieve.
	@return The registered depicted object with the given ID, or <code>null</code> if there is no depicted object registered with this platform with the given ID.
	*/
	public DepictedObject getDepictedObject(final long depictedObjectID);

	/**@return The client software being used to access Guise on this platform.*/
	public ClientProduct getClientProduct();
	
	/**@return The thread-safe queue of messages to be delivered to the platform.*/
	public Queue<? extends PlatformMessage> getSendMessageQueue();

	/**@return The lock used for exclusive depiction on the platform.*/
	public Lock getDepictLock();

	/**Retrieves information and functionality related to the current depiction.
	@return A context for the current depiction.
	@exception IllegalStateException if no depict context can be returned in the current depiction state.
	*/
	public DepictContext getDepictContext();

	/**Selects one or more files on the platform, using the appropriate selection functionality for the platform.
	@param multiple Whether multiple files should be allowed to be selected.
	@param platformFileSelectListener The listener that will be notified when platform files are selected.
	*/
	public void selectPlatformFiles(final boolean multiple, final ValueSelectListener<Collection<PlatformFile>> platformFileSelectListener);

	/**Sends a resource to the platform.
	@param resourcePath The path of the resource to send, relative to the application.
	@exception NullPointerException if the given path is <code>null</code>.
	@exception IllegalArgumentException if the given string is not a path.
	*/
	public void sendResource(final URIPath resourcePath);

	/**Sends a resource to the platform.
	@param resourceURI The URI of the resource to send, relative to the application.
	@exception NullPointerException if the given URI is <code>null</code>.
	*/
	public void sendResource(final URI resourceURI);

	/**Sends a resource to the platform with the specified bookmark.
	@param resourcePath The path of the resource to send, relative to the application.
	@param bookmark The bookmark at the given path, or <code>null</code> if there is no bookmark.
	@exception NullPointerException if the given path is <code>null</code>.
	*/
	public void sendResource(final URIPath resourcePath, final Bookmark bookmark);

	/**Sends a resource to the platform.
	@param resourceURI The URI of the resource to send, relative to the application.
	@param bookmark The bookmark at the given path, or <code>null</code> if there is no bookmark.
	@exception NullPointerException if the given URI is <code>null</code>.
	*/
	public void sendResource(final URI resourceURI, final Bookmark bookmark);
}
