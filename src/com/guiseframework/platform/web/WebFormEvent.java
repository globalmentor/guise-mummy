package com.guiseframework.platform.web;

import java.util.*;

import static com.garretwilson.text.FormatUtilities.*;
import com.garretwilson.util.*;

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
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public WebFormEvent(final WebPlatform source)
	{
		this(source, true);	//construct the class as exhaustive
	}

	/**Source and exhaustive constructor.
	@param source The object on which the event initially occurred.
	@param exhaustive Whether this event represents all components on the form.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public WebFormEvent(final WebPlatform source, final boolean exhaustive)
	{
		super(source);	//construct the parent class
		this.exhaustive=exhaustive;
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
