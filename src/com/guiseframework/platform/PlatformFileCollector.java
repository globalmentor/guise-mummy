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

import java.util.*;
import static java.util.Collections.*;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Objects.*;

import com.globalmentor.net.URIPath;
import com.guiseframework.Bookmark;

/**Encapsulation of a list of platform files and a way to collect them.
The installed depictor must be of the specialized type {@link Depictor}.
@author Garret Wilson
*/
public class PlatformFileCollector extends AbstractDepictedObject
{

	/**The bound property of the selected platform files.*/
	public final static String PLATFORM_FILES_PROPERTY=getPropertyName(PlatformFileCollector.class, "platformFiles");

	/**@return The depictor for this object.*/
	@SuppressWarnings("unchecked")
	public Depictor<? extends PlatformFileCollector> getDepictor() {return (Depictor<? extends PlatformFileCollector>)super.getDepictor();} 

	/**The selected platform files.*/
	private List<? extends PlatformFile> platformFiles=emptyList();

		/**@return The selected platform files.*/
		public List<? extends PlatformFile> getPlatformFiles() {return platformFiles;}

		/**Sets the platform files.
		This is a bound property.
		This method is called by the platform; it should never be called directly from an application.
		@param newPlatformFiles The new selected platform files.
		@see #PLATFORM_FILES_PROPERTY
		@exception NullPointerException if the given platform files is <code>null</code>. 
		*/
		public void setPlatformFiles(final List<? extends PlatformFile> newPlatformFiles)
		{
			if(platformFiles!=checkInstance(newPlatformFiles, "Platform files cannot be null."))
			{
				final List<? extends PlatformFile> oldPlatformFiles=platformFiles;	//get the old value
				platformFiles=newPlatformFiles;	//actually change the value
				firePropertyChange(PLATFORM_FILES_PROPERTY, oldPlatformFiles, newPlatformFiles);	//indicate that the value changed
			}
		}

	/**Default constructor.*/
	public PlatformFileCollector()
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
	public void cancel(final PlatformFile platformFile)
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
	public void upload(final PlatformFile platformFile, final URIPath destinationPath, final Bookmark destinationBookmark)
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
	public interface Depictor<F extends PlatformFileCollector> extends com.guiseframework.platform.Depictor<F>
	{

		/**Requests that user be displayed a dialog for browsing files.*/
		public void browse();

		/**Cancels a platform file upload or download.
		@param platformFile Thet platform file to cancel.
		@exception NullPointerException if the given platform file is <code>null</code>.
		@exception IllegalStateException the specified platform file can no longer be canceled because, for example, other platform files have since been selected.	
		*/
		public void cancel(final PlatformFile platformFile);

		/**Initiates a platform file upload.
		@param platformFile Thet platform file to upload.
		@param destinationURI The URI representing the destination of the platform file, relative to the application.
		@exception NullPointerException if the given platform file and/or destination path is <code>null</code>.
		@exception IllegalArgumentException if the provided path specifies a URI scheme (i.e. the URI is absolute) and/or authority.
		@exception IllegalArgumentException if the provided path is absolute.
		*/
		public void upload(final PlatformFile platformFile, final URIPath destinationPath, final Bookmark destinationBookmark);

	}

}
