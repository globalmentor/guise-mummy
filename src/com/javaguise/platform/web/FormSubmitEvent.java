package com.javaguise.platform.web;

import java.util.*;

import static com.garretwilson.text.FormatUtilities.*;
import com.garretwilson.util.*;

import com.javaguise.controller.*;

/**A control event indicating that a full or partial form submission occurred.
@author Garret Wilson
*/
public class FormSubmitEvent implements ControlEvent
{

	/**The map of parameter lists.*/
	private final ListMap<String, Object> parameterListMap=new ArrayListHashMap<String, Object>();

		/**@return The map of parameter lists.*/
		public ListMap<String, Object> getParameterListMap() {return parameterListMap;}

	/**@return A string representation of this event.*/
	public String toString()
	{
		final StringBuilder stringBuilder=new StringBuilder();	//create a string builder for constructing a string
		final ListMap<String, Object> parameterListMap=getParameterListMap();	//get the request parameter map
		for(final Map.Entry<String, List<Object>> parameterListMapEntry:parameterListMap.entrySet())	//for each entry in the map of parameter lists
		{
			stringBuilder.append("Key: ").append(parameterListMapEntry.getKey()).append(" Value: {");	//Key: key Value: {
			formatList(stringBuilder, ',', parameterListMapEntry.getValue());	//values
			stringBuilder.append('}');	//}
		}
		return stringBuilder.toString();	//return the string we constructed
	}
}
