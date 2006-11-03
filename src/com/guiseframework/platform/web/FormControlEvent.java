package com.guiseframework.platform.web;

import java.util.*;

import static com.garretwilson.text.FormatUtilities.*;
import com.garretwilson.util.*;
import com.guiseframework.controller.*;


/**A control event indicating that a full or partial form submission occurred.
@author Garret Wilson
*/
public class FormControlEvent implements ControlEvent
{

	/**Whether this event represents all components on the form.*/
	private final boolean exhaustive;

		/**@return Whether this event represents all components on the form.*/
		public final boolean isExhaustive() {return exhaustive;}

	/**Whether the values in this event are provisional.*/
	private final boolean provisional;

		/**@return Whether the values in this event are provisional.*/
		public final boolean isProvisional() {return provisional;}

	/**The map of parameter lists.*/
	private final CollectionMap<String, Object, List<Object>> parameterListMap=new ArrayListHashMap<String, Object>();

		/**@return The map of parameter lists.*/
		public CollectionMap<String, Object, List<Object>> getParameterListMap() {return parameterListMap;}

	/**Constructor that indicates non-provisional exhaustive values.
	@param exhaustive Whether this event represents all components on the form.
	*/
	public FormControlEvent(final boolean exhaustive)
	{
		this(exhaustive, false);	//construct the class, indicate
	}

	/**Constructor that indicates whether the event is exhaustive.
	@param exhaustive Whether this event represents all components on the form.
	@param provisional Whether the values in this event are provisional.
	*/
	public FormControlEvent(final boolean exhaustive, final boolean provisional)
	{
		this.exhaustive=exhaustive;
		this.provisional=provisional;
	}

	/**@return A string representation of this event.*/
	public String toString()
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
}
