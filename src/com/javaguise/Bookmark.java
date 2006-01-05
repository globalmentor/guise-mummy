package com.javaguise;

import static java.util.Arrays.*;
import static java.util.Collections.*;

import java.util.Arrays;
import java.util.List;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIUtilities.*;

import com.garretwilson.util.NameValuePair;

/**An identification of a saved state at a navigation point. The bookmark is relative to the navigation path.
@author Garret Wilson
*/
public class Bookmark
{

	/**The bookmark ID.*/
//TODO del	private final String id;

		/**@return The bookmark ID.*/
//TODO del		public String getID() {return id;}

	/**The array of parameters.*/
	private final Parameter[] parameters;

		/**@return A read-only list of parameters.*/
		public List<Parameter> getParameters() {return unmodifiableList(asList(parameters));}	//return a read-only list of the parameters

	/**Returns the value associated with the first parameter of the given name.
	@param parameterName The name of the parameter.
	@return The value of the first parameter with the given name, or <code>null</code> if there is no parameter with the given name.
	*/
	public String getParameterValue(final String parameterName)
	{
		for(int i=0; i<parameters.length; ++i)	//for each parameter
		{
			final Parameter parameter=parameters[i];	//get a reference to this parameter
			if(parameter.getName().equals(parameterName))	//if this parameter has the correct name (this parameter's name should never be null, but the requested parameter name might be null
			{
				return parameter.getValue();	//return this parameter value
			}
		}
		return null;	//indicate that no parameter with the given name was found
	}
		
	/**ID and optional parameters constructor.
	@param id The bookmark ID.
	@param parameters The optional bookmark parameters.
	@exception NullPointerException if the given ID and/or the parameters array is <code>null</code>.
	*/
	public Bookmark(/*TODO del final String id, */final Parameter... parameters)
	{
//TODO del		this.id=checkNull(id, "ID cannot be null.");
		this.parameters=checkNull(parameters, "Parameters cannot be null.");
	}

	/**@return A hash code value for the object.*/
	public int hashCode()
	{
		return Arrays.hashCode(parameters);	//calculate a hash code from the parameters
//TODO fix		return ObjectUtilities.hashCode(id, Arrays.hashCode(parameters));	//calculate a hash code from the ID and the parameters
	}

	/**Indicates whether some other object is "equal to" this one.
	This implementation returns whether the given object is a bookmark with the same ID and parameters.
	@param object The reference object with which to compare.
	@return <code>true</code> if this object is equivalent to the given object.
	*/
	public boolean equals(final Object object)
	{
		if(object instanceof Bookmark)	//if the given object is a bookmark
		{
			final Bookmark bookmark=(Bookmark)object;	//cast the object to a boomark
			return Arrays.equals(parameters, bookmark.parameters);	//compare the ID and parameters
//TODO del			return getID().equals(bookmark.getID()) && Arrays.equals(parameters, bookmark.parameters);	//compare the ID and parameters
		}
		else	//if the given object is not a bookmark
		{
			return false;	//indicate that the objects are not equal
		}
	}

	/**@return A string representation of the bookmark suitable for use in a URI query, in the form "#<var>id</var>?<var>parameter1Name</var>=<var>parameter2Name</var>&amp;&hellip;".*/
	public String toString()
	{
		final StringBuilder bookmarkQueryStringBuilder=new StringBuilder();	//create a new string builder
/*TODO fix
		final String bookmarkID=bookmark.getID();	//get the bookmark ID
		if(bookmarkID!=null)	//if there is a bookmark ID
		{
			bookmarkQueryStringBuilder.append(FRAGMENT_SEPARATOR).append(encode(bookmarkID));	//append #bookmarkID (encoded)
		}
*/
		if(parameters.length>0)	//if there are parameters
		{
			bookmarkQueryStringBuilder.append(constructQuery((NameValuePair<String, String>[])parameters));	//append the parameters to the query string
		}
		return bookmarkQueryStringBuilder.toString();	//return the string we constructed
	}

	/**A bookmark parameter name/value pair.
	Neither the name nor the value of a bookmark parameter can be <code>null</code>.
	@author Garret Wilson
	*/
	public static class Parameter extends NameValuePair<String, String>
	{
		/**Constructor specifying the name and value.
		@param name The parameter name.
		@param value The parameter value.
		@exception NullPointerException if the given name and/or value is <code>null</code>.
		*/
		public Parameter(final String name, final String value)
		{
			super(checkNull(name, "Parameter name cannot be null."), checkNull(value, "Parameter value cannot be null."));	//construct the parent class
		}		
	}
}
