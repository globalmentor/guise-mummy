package com.guiseframework.model;

import java.io.*;

import javax.mail.internet.ContentType;

import static com.garretwilson.io.ContentTypeUtilities.*;
import static com.garretwilson.io.FileConstants.*;
import static com.garretwilson.lang.ObjectUtilities.*;

import org.apache.commons.fileupload.FileItem;

/**A resource import that accesses a web file upload through the Apache commons file item interface.
@author Garret Wilson
@see FileItem
*/
public class FileItemResourceImport implements ResourceImport
{

	/**The Apache commons file item representing a file upload.*/
	private final FileItem fileItem;

		/**@return The Apache commons file item representing a file upload.*/
		protected FileItem getFileItem() {return fileItem;}

	/**The content type of the resource to be imported.*/
	private final ContentType contentType;

	/**The simple name of the resource, or <code>null</code> if the name is not known.*/
	private final String simpleName;

	/**Whether the resource has been accessed, either by retrieving its input stream or by storing the resource in a file.*/
	private boolean imported=false;

		/**@return Whether the resource has been accessed, either by retrieving its input stream or by storing the resource in a file.*/
		public synchronized boolean isImported() {return imported;}

	/**File item constructor.
	@param fileItem The Apache commons file item representing a file upload.
	@exception NullPointerException if the given file item is <code>null</code>.
	*/
	public FileItemResourceImport(final FileItem fileItem)
	{
		this.fileItem=checkInstance(fileItem, "File item cannot be null.");
		contentType=createContentType(fileItem.getContentType());	//create a content type object from the file item
		String name=fileItem.getName();	//get the name of the item
		if(name!=null)	//if there is a filename
		{
			final int pathSeparatorIndex=name.lastIndexOf(FILE_PATH_SEPARATOR_CHARACTERS);	//see if there are any file path separator characters (unfortunately, this will strip away any backslash found in a Unix filename if the entire path was sent, but it's better to have a too-short filename in a rare case than one that includes the full Windows path)
			if(pathSeparatorIndex>=0)	//if there is a path separator
			{
				name=name.substring(pathSeparatorIndex+1);	//remove everything but what comes after the last path separator
			}
		}
		simpleName=name;	//save the filename
	}

	/**@return The name of the resource, which may be, for example, a simple name, a path and filename, or <code>null</code> if the name is not known.
	@see #getSimpleName()
	*/
	public String getName()
	{
		return getFileItem().getName();	//return the name returned by the the file item
	}

	/**Returns the simple name, such as the filename, of the resource.
	The returned string does not include any path information.
	@return The simple name of the resource, or <code>null</code> if the name is not known.
	@see #getName()
	*/
	public String getSimpleName()
	{
		return simpleName;	//return the name we saved earlier
	}
	
	/**@return The content type of the resource to be imported, or <code>null</code> if the content type of the resource is not known.*/
	public ContentType getContentType()
	{
		return contentType;	//return the pre-made content type
	}

	/**@return The length of the resource to be imported, or -1 if the length of the resource is not known.*/
	public long getContentLength()
	{
		return getFileItem().getSize();	//return the size of the file item
	}

	/**Retrieves an input stream to the resource.
	There can only be at most a single call to this method or {@link #store(File)}.
	@return An input stream to the resource to be imported.
	@exception IllegalStateException if this resource has already been stored in a file, or an input stream to the resource has already been retrieved.
	@exception IOException if there is an error getting an input stream to the resource.
	*/
	public synchronized InputStream getInputStream() throws IOException
	{
		if(isImported())	//if the resource is already stored
		{
			throw new IllegalStateException("Resource already stored.");
		}
		imported=true;	//show that the resource has been accessed
		return getFileItem().getInputStream();	//get an input stream from the file item
	}

	/**Convenience method for storing the imported resource in a file.
	Depending on the implementation, this may allow greater efficiency than reading from the stream.
	There can only be at most a single call to this method or {@link #getInputStream()}.
	@param file The file to which the resource should be written.
	@exception IllegalStateException if this resource has already been stored in a file, or an input stream to the resource has already been retrieved.
	@exception IOException If there is an error writing the resource to the file.
	*/
	public synchronized void store(final File file) throws IOException
	{
		if(isImported())	//if the resource is already stored
		{
			throw new IllegalStateException("Resource already stored.");
		}
		try
		{
			getFileItem().write(file);	//ask the file item to write the resource to the given file
			imported=true;	//show that the resource has been accessed
		}
		catch(final Exception exception)	//if there is an error
		{
			throw exception instanceof IOException ? (IOException)exception : (IOException)new IOException(exception.getMessage()).initCause(exception);	//the file item can send back any kind of exception, so if it isn't an IOException, convert it to one 
		}
	}

	/**@return A string representation of this resource import.*/
	public String toString()
	{
		final StringBuilder stringBuilder=new StringBuilder(super.toString());	//create a string builder for constructing the string
		final String name=getName();	//get the name
		if(name!=null)	//if a name is available
		{
			stringBuilder.append(' ');	//separate the information
			stringBuilder.append(name);	//append the name
		}
		final ContentType contentType=getContentType();	//get the content type
		if(contentType!=null)	//if a content type is given
		{
			stringBuilder.append(' ');	//separate the information
			stringBuilder.append('(').append(contentType).append(')');	//(contentType)
		}
		final long contentLength=getContentLength();	//get the content length
		if(contentLength>=0)	//if a content length is available
		{
			stringBuilder.append(' ');	//separate the information
			stringBuilder.append("length: ").append(contentLength);	//length: contentLength
		}
		return stringBuilder.toString();	//return the string we constructed
	}
}
