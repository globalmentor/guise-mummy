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

package com.guiseframework.component.transfer;

import com.globalmentor.io.*;
import com.globalmentor.net.ContentType;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.net.ContentTypeConstants.*;

/**A abstract transferable that carries one or more Java objects.
A subclass must implement {@link #transfer(Class)}.
@param <S> The source of the transfer.
@author Garret Wilson
*/
public abstract class AbstractObjectTransferable<S> implements Transferable<S>
{

//TODO del	private final Map<String, Class<?>> contentTypeObjectMap=new HashMap<String, Class<?>>();

	/**The class representing the type of object to be transferred.*/
//TODO del	private final Class<T> objectClass;

		/**@return The class representing the type of object to be transferred.*/
//TODO del		public Class<T> getObjectClass() {return objectClass;}

	/**The source of the transferable data.*/
	private final S source;

		/**@return The source of the transferable data.*/
		public S getSource() {return source;}

	/**The classes supported by this transferable.*/
	private final Class<?>[] objectClasses;

	/**The content types supported by this transferable.*/
	private final ContentType[] contentTypes;
	
	/**Source and object esclass constructor.
	@param source The source of the transferable data.
	@param objectClasses The classes indicating the typees of object to be transferred.
	@throws NullPointerException if the provided source and/or object classes is <code>null</code>.
	*/
	public AbstractObjectTransferable(final S source, final Class<?>... objectClasses)
	{
		this.source=checkInstance(source, "Source cannot be null.");
		this.objectClasses=checkInstance(objectClasses, "Object classes cannot be null.");
		final int objectClassCount=objectClasses.length;	//find out how many object classes there are
		contentTypes=new ContentType[objectClassCount];	//create an array of content types
		for(int i=objectClassCount-1; i>=0; --i)	//for each object class
		{
			contentTypes[i]=getObjectContentType(objectClasses[i]);	//create a content type for this object
		}
	}

	/**Determines the class this content type matches.
	This method matches a content type against the {@value ContentType#APPLICATION_PRIMARY_TYPE} primary type and {@value ContentType#X_JAVA_OBJECT} subtype, with a "class" parameter indicating the given object class.
	@param contentType The type of data requested, which may include wildcards.
	@return The matching class, or <code>null</code> if no supported class matches the requested content type.
	*/
	protected Class<?> getClass(final ContentType contentType)
	{
		if(contentType.match(ContentType.APPLICATION_PRIMARY_TYPE, ContentType.X_JAVA_OBJECT))	//if this is an application/x-java-object type
		{
			final String className=contentType.getParameter("class");	//get the class parameter TODO use a constant
			if(className!=null)	//if a class name was given
			{
				for(final Class<?> objectClass:objectClasses)	//for each supported class
				{
					if(objectClass.getName().equals(className))	//if this class is of the requested type
					{
						return objectClass;	//return the match we found
					}
				}
			}
		}
		return null;	//indicate that there is no matching class		
	}

	/**Determines whether this transferable can transfer data with the given content type.
	This implementation matches a content type against the {@value ContentType#APPLICATION_PRIMARY_TYPE} primary type and {@value ContentType#X_JAVA_OBJECT} subtype, with a "class" parameter indicating the given object class.
	@param contentType The type of data requested, which may include wildcards.
	@return <code>true</code> if this object can transfer data with the requested content type.
	*/
	public boolean canTransfer(final ContentType contentType)
	{
		return getClass(contentType)!=null;	//see if there is a class that matches
	}

	/**Transfers data using the given content type.
	This implementation delegates to {@link #transfer(Class)}.
	@param contentType The type of data expected.
	@return The transferred data, which may be <code>null</code>.
	@throws IllegalArgumentException if the given content type is not supported.
	*/
	public Object transfer(final ContentType contentType)
	{
		final Class<?> objectClass=getClass(contentType);	//return the class indicated by this content type
		if(objectClass!=null)	//if there is a class for this content type
		{
			return transfer(objectClass);	//transfer based upon the class
		}
		else	//if there is no class for this content type
		{
			throw new IllegalArgumentException("Content type not supported: "+contentType);
		}
	}


	/**Determines the content types available for this transfer.
	This implementation returns content types in the form <code>application/x-java-object;class=<var>package.Class</var></code>.
	@return The content types available for this transfer.
	*/
	public ContentType[] getContentTypes()
	{
		return contentTypes;	//return the available content types
	}
}
