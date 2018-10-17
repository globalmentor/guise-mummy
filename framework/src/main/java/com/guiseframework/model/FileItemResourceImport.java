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

import static java.util.Objects.*;

import com.globalmentor.net.ContentType;

import static com.globalmentor.io.Files.*;

import org.apache.commons.fileupload.FileItem;

/**
 * A resource import that accesses a web file upload through the Apache commons file item interface.
 * @author Garret Wilson
 * @see FileItem
 */
public class FileItemResourceImport implements ResourceImport {

	/** The Apache commons file item representing a file upload. */
	private final FileItem fileItem;

	/** @return The Apache commons file item representing a file upload. */
	protected FileItem getFileItem() {
		return fileItem;
	}

	/** The content type of the resource to be imported. */
	private final ContentType contentType;

	/** The simple name of the resource, or <code>null</code> if the name is not known. */
	private final String simpleName;

	/** Whether the resource has been accessed, either by retrieving its input stream or by storing the resource in a file. */
	private boolean imported = false;

	@Override
	public synchronized boolean isImported() {
		return imported;
	}

	/**
	 * File item constructor.
	 * @param fileItem The Apache commons file item representing a file upload.
	 * @throws NullPointerException if the given file item is <code>null</code>.
	 */
	public FileItemResourceImport(final FileItem fileItem) {
		this.fileItem = requireNonNull(fileItem, "File item cannot be null.");
		contentType = ContentType.create(fileItem.getContentType()); //create a content type object from the file item
		String name = fileItem.getName(); //get the name of the item
		if(name != null) { //if there is a filename
			name = getFilename(name); //make sure it is only a filename and not a complete path
		}
		simpleName = name; //save the filename
	}

	@Override
	public String getName() {
		return getFileItem().getName(); //return the name returned by the the file item
	}

	@Override
	public String getSimpleName() {
		return simpleName; //return the name we saved earlier
	}

	@Override
	public ContentType getContentType() {
		return contentType; //return the pre-made content type
	}

	@Override
	public long getContentLength() {
		return getFileItem().getSize(); //return the size of the file item
	}

	@Override
	public synchronized InputStream getInputStream() throws IOException {
		if(isImported()) { //if the resource is already stored
			throw new IllegalStateException("Resource already stored.");
		}
		imported = true; //show that the resource has been accessed
		return getFileItem().getInputStream(); //get an input stream from the file item
	}

	@Override
	public synchronized void store(final File file) throws IOException {
		if(isImported()) { //if the resource is already stored
			throw new IllegalStateException("Resource already stored.");
		}
		try {
			getFileItem().write(file); //ask the file item to write the resource to the given file
			imported = true; //show that the resource has been accessed
		} catch(final Exception exception) { //if there is an error
			throw exception instanceof IOException ? (IOException)exception : (IOException)new IOException(exception.getMessage()).initCause(exception); //the file item can send back any kind of exception, so if it isn't an IOException, convert it to one 
		}
	}

	@Override
	public String toString() {
		final StringBuilder stringBuilder = new StringBuilder(super.toString()); //create a string builder for constructing the string
		final String name = getName(); //get the name
		if(name != null) { //if a name is available
			stringBuilder.append(' '); //separate the information
			stringBuilder.append(name); //append the name
		}
		final ContentType contentType = getContentType(); //get the content type
		if(contentType != null) { //if a content type is given
			stringBuilder.append(' '); //separate the information
			stringBuilder.append('(').append(contentType).append(')'); //(contentType)
		}
		final long contentLength = getContentLength(); //get the content length
		if(contentLength >= 0) { //if a content length is available
			stringBuilder.append(' '); //separate the information
			stringBuilder.append("length: ").append(contentLength); //length: contentLength
		}
		return stringBuilder.toString(); //return the string we constructed
	}
}
