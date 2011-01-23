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

package com.guiseframework.model;

import java.io.*;

import com.globalmentor.net.ContentType;

/**An interface to a resource being imported, such as a web file upload.
All resource access methods must be synchronized on the resource import instance.
@author Garret Wilson
*/
public interface ResourceImport
{

	/**@return The name of the resource, which may be, for example, a simple name, a path and filename, or <code>null</code> if the name is not known.
	@see #getSimpleName()
	*/
	public String getName();

	/**Returns the simple name, such as the filename, of the resource.
	The returned string does not include any path information.
	@return The simple name of the resource, or <code>null</code> if the name is not known.
	@see #getName()
	*/
	public String getSimpleName();
	
	/**@return The content type of the resource to be imported, or <code>null</code> if the content type of the resource is not known.*/
	public ContentType getContentType();

	/**@return The length of the resource to be imported, or -1 if the length of the resource is not known.*/
	public long getContentLength();

	/**@return Whether the resource has been accessed, either by retrieving its input stream or by storing the resource in a file.*/
	public boolean isImported();

	/**Retrieves an input stream to the resource.
	There can only be at most a single call to this method or {@link #store(File)}.
	@return An input stream to the resource to be imported.
	@exception IllegalStateException if this resource has already been stored in a file, or an input stream to the resource has already been retrieved.
	@exception IOException if there is an error getting an input stream to the resource.
	*/
	public InputStream getInputStream() throws IOException;

	/**Convenience method for storing the imported resource in a file.
	Depending on the implementation, this may allow greater efficiency than reading from the stream.
	There can only be at most a single call to this method or {@link #getInputStream()}.
	@param file The file to which the resource should be written.
	@exception IllegalStateException if this resource has already been stored in a file, or an input stream to the resource has already been retrieved.
	@exception IOException If there is an error writing the resource to the file.
	*/
	public void store(final File file) throws IOException;

}
