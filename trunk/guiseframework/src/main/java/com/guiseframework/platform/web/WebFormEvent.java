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

import java.io.IOException;
import java.util.*;

import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.text.TextFormatter.*;

import com.globalmentor.collections.ArrayListHashMap;
import com.globalmentor.collections.CollectionMap;

/**Indicates the submission of a full or partial form on the web platform
@author Garret Wilson
*/
public class WebFormEvent extends AbstractWebPlatformEvent
{

	/**@return The platform on which the event initially occurred.*/
	public WebPlatform getPlatform() {return (WebPlatform)getSource();}

	/**Whether this event represents all components on the form.*/
	private final boolean exhaustive;

		/**@return Whether this event represents all components on the form.*/
		public final boolean isExhaustive() {return exhaustive;}

	/**The map of parameter lists.*/
	private final CollectionMap<String, Object, List<Object>> parameterListMap=new ArrayListHashMap<String, Object>();

		/**@return The map of parameter lists.*/
		public CollectionMap<String, Object, List<Object>> getParameterListMap() {return parameterListMap;}
		
	/**Source exhaustive constructor.
	@param source The object on which the event initially occurred.
	@throws NullPointerException if the given source is <code>null</code>.
	*/
	public WebFormEvent(final WebPlatform source)
	{
		this(source, true);	//construct the class as exhaustive
	}

	/**Source and exhaustive constructor.
	@param source The object on which the event initially occurred.
	@param exhaustive Whether this event represents all components on the form.
	@throws NullPointerException if the given source is <code>null</code>.
	*/
	public WebFormEvent(final WebPlatform source, final boolean exhaustive)
	{
		super(source);	//construct the parent class
		this.exhaustive=exhaustive;
	}

	/**@return A string representation of this event.*/
	public String toString()
	{
		try
		{
			final StringBuilder stringBuilder=new StringBuilder();	//create a string builder for constructing a string
			if(isExhaustive())	//if the event is exhaustive
			{
				stringBuilder.append("(exhaustive) ");
			}
			final CollectionMap<String, Object, List<Object>> parameterListMap=getParameterListMap();	//get the request parameter map
			for(final Map.Entry<String, List<Object>> parameterListMapEntry:parameterListMap.entrySet())	//for each entry in the map of parameter lists
			{
				stringBuilder.append("Key: ").append(parameterListMapEntry.getKey()).append(" Value: {");	//Key: key Value: {
				formatList(stringBuilder, ',', parameterListMapEntry.getValue());	//values
				stringBuilder.append('}');	//}
			}
			return stringBuilder.toString();	//return the string we constructed
		}
		catch(final IOException ioException)
		{
			throw unexpected(ioException);
		}
	}
}
