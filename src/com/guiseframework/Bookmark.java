package com.guiseframework;

import static java.util.Collections.*;

import java.util.*;

import static com.garretwilson.lang.Objects.*;
import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIs.*;

import static com.garretwilson.lang.EnumUtilities.*;
import com.garretwilson.net.URIs;
import com.garretwilson.text.ArgumentSyntaxException;
import com.garretwilson.util.Debug;
import com.garretwilson.util.NameValuePair;

/**An identification of a saved state at a navigation point. The bookmark is relative to the navigation path.
@author Garret Wilson
*/
public class Bookmark implements Cloneable
{

	/**The bookmark ID.*/
//TODO del	private final String id;

		/**@return The bookmark ID.*/
//TODO del		public String getID() {return id;}

	/**The map of parameters keyd to parameter names. This map is not thread-safe, but it should never be modified by more than one thread because this is an immutable class.*/
	private HashMap<String, Parameter> parameterMap=new HashMap<String, Parameter>();
	
	/**The array of parameters.*/
//TODO del	private final Parameter[] parameters;

		/**@return A read-only list of parameters.*/
//TODO del		public List<Parameter> getParameters() {return unmodifiableList(asList(parameters));}	//return a read-only list of the parameters

	/**@return A read-only set of parameters.*/
	public Set<Parameter> getParameters() {return unmodifiableSet(new HashSet<Parameter>(parameterMap.values()));}	//return a read-only set of the parameters
	
	/**Returns the value associated with the first parameter of the given name.
	@param parameterName The name of the parameter.
	@return The value of the first parameter with the given name, or <code>null</code> if there is no parameter with the given name.
	@exception NullPointerException if the given name is <code>null</code>.
	*/
	public String getParameterValue(final String parameterName)
	{
		final Parameter parameter=parameterMap.get(checkInstance(parameterName, "Parameter name cannot be null."));	//get the requested parameter
		return parameter!=null ? parameter.getValue() : null;	//return the parameter value, if there was a parameter
/*TODO del when works
		for(int i=0; i<parameters.length; ++i)	//for each parameter
		{
			final Parameter parameter=parameters[i];	//get a reference to this parameter
			if(parameterName.equals(parameter.getName()))	//if this parameter has the correct name
			{
				return parameter.getValue();	//return this parameter value
			}
		}
		return null;	//indicate that no parameter with the given name was found
*/
	}

	/**String constructor.
	The string must be suitable for use as a URI query, in the form "?<var>parameter1Name</var>=<var>parameter1Value</var>&amp;<var>parameter2Name</var>=<var>parameter2Value</var>&hellip;".
	The parameter names and values should be percent-encoded as required for URIs.
	If there are parameters with duplicate names, only the first ones are used and the rest with the same name are ignored.
	@param bookmark A string representation of the bookmark, beginning with '?'.
	@exception NullPointerException if the given bookmark string is <code>null</code>.
	@exception ArgumentSyntaxException if the bookmark does not begin with '?' or otherwise is not in the correct format.
	*/
	public Bookmark(final CharSequence bookmark) throws ArgumentSyntaxException
	{
		final int bookmarkLength=checkInstance(bookmark, "Bookmark string cannot be null.").length();	//get the length of the bookmark
		if(bookmarkLength==0 || bookmark.charAt(0)!=QUERY_SEPARATOR)	//if the bookmark string does not begin with '?'
		{
			throw new ArgumentSyntaxException("Bookmark string "+bookmark+" must being with '?'.");
		}
		final NameValuePair<String, String>[] parameters=URIs.getParameters(bookmark.subSequence(1, bookmarkLength).toString());	//get the parameters from the string following the query character
		final Parameter[] bookmarkParameters=new Parameter[parameters.length];	//create a new array of bookmark parameters
		for(int i=parameters.length-1; i>=0; --i)	//for each parameter
		{
			final NameValuePair<String, String> parameter=parameters[i];	//get a reference to this parameter
			bookmarkParameters[i]=new Parameter(parameter.getName(), parameter.getValue());	//create a corresponding bookmark parameter
		}
		setParameters(bookmarkParameters);	//set the parameters
	}
	
	/**ID and optional parameters constructor.
	If there are parameters with duplicate names, only the first ones are used and the rest with the same name are ignored.
	@param id The bookmark ID.
	@param parameters The optional bookmark parameters.
	@exception NullPointerException if the given ID and/or the parameters array is <code>null</code>.
	*/
	public Bookmark(/*TODO del final String id, */final Parameter... parameters)
	{
//TODO del		this.id=checkNull(id, "ID cannot be null.");
		setParameters(parameters);	//set the parameters
	}

	/**Sets bookmark parameters.
	If there are parameters with duplicate names, only the first ones are used and the rest with the same name are ignored.
	This method should only be called during constructor initialization.
	@param parameters The optional bookmark parameters.
	*/
	protected void setParameters(final Parameter...parameters)
	{
		for(final Parameter parameter:parameters)	//for each parameter
		{
			final String parameterName=parameter.getName();	//get the parameter name
			if(!parameterMap.containsKey(parameterName))	//if this parameter is not already stored in the map
			{
				parameterMap.put(parameterName, parameter);	//store this parameter in the map
			}
		}		
	}

	/**Creates a new bookmark with the given parameter set to the given value.
	If the named parameter does not exist, it will be added.
	If this bookmark already contains a parameter with the given value, this bookmark will be returned.
	@param name The parameter name.
	@param value The parameter value.
	@exception NullPointerException if the given name and/or value is <code>null</code>.
	*/
	public Bookmark setParameter(final String name, final String value)
	{
		if(value.equals(getParameterValue(name)))	//if this bookmark already has the given parameter name and value
		{
			return this;	//return this bookmark; it already has the correct values
		}
		final Parameter newParameter=new Parameter(name, value);	//create the new parameter with the new value
		final Bookmark newBookmark=(Bookmark)clone();	//clone this bookmark
		newBookmark.parameterMap.put(name, newParameter);	//store the new parameter in the new bookmark
		return newBookmark;	//return our modified clone
	}

	/**Creates a new bookmark with the given parameter removed.
	If this bookmark does not contains the given parameter, this bookmark will be returned.
	@param name The parameter name.
	@exception NullPointerException if the given name is <code>null</code>.
	*/
	public Bookmark removeParameter(final String name)
	{
		if(getParameterValue(name)==null)	//if this bookmark does not have given parameter name and value
		{
			return this;	//return this bookmark; it already has the parameter removed
		}
		final Bookmark newBookmark=(Bookmark)clone();	//clone this bookmark
		newBookmark.parameterMap.remove(name);	//remove the parameter from the new bookmark
		return newBookmark;	//return our modified clone
	}

	/**Creates a shallow clone of this object.*/
	@SuppressWarnings("unchecked")	//we clone our internal hash map, so we know the generic return type
	public Object clone()
	{
		try
		{
			final Bookmark clone=(Bookmark)super.clone();	//create a clone of the bookmark
			clone.parameterMap=(HashMap<String, Parameter>)parameterMap.clone();	//clone the map of parameters
			return clone;	//return the clone
		}
		catch(final CloneNotSupportedException cloneNotSupportedException)	//we support cloning in this class, so we should never get this exception
		{
			throw new AssertionError(cloneNotSupportedException);
		}
	}

	/**@return A hash code value for the object.*/
	public int hashCode()
	{
		return parameterMap.hashCode();	//return the hash code of the parameter map
//TODO del		return Arrays.hashCode(parameters);	//calculate a hash code from the parameters
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
//TODO del			final Bookmark bookmark=(Bookmark)object;	//cast the object to a boomark
			return parameterMap.equals(((Bookmark)object).parameterMap);	//compare parameter maps
//TODO del			return Arrays.equals(parameters, bookmark.parameters);	//compare the ID and parameters
//TODO del			return getID().equals(bookmark.getID()) && Arrays.equals(parameters, bookmark.parameters);	//compare the ID and parameters
		}
		else	//if the given object is not a bookmark
		{
			return false;	//indicate that the objects are not equal
		}
	}

	/**Returns a string representation of the bookmark.
	The string is suitable for use as a URI query, in the form "?<var>parameter1Name</var>=<var>parameter1Value</var>&amp;<var>parameter2Name</var>=<var>parameter2Value</var>&hellip;".
	Each name and value will be percent-encoded as appropriate for URIs.
	@return A string representation of the bookmark suitable for use as a URI query.
	*/
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
		if(!parameterMap.isEmpty())	//if there are parameters
		{
			final Set<Parameter> parameterSet=getParameters();	//get the parameters
			final Parameter[] parameters=parameterSet.toArray(new Parameter[parameterSet.size()]);	//create an array of parameters
			bookmarkQueryStringBuilder.append(constructQuery((NameValuePair<String, String>[])parameters));	//append the parameters to the query string
		}
		else	//if there are no parameters
		{
			bookmarkQueryStringBuilder.append(QUERY_SEPARATOR);	//append the query prefix			
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
			super(checkInstance(name, "Parameter name cannot be null."), checkInstance(value, "Parameter value cannot be null."));	//construct the parent class
		}		

		/**Constructor specifying the name and an enum value.
		The value will be converted to a serialized form by changing the enum name to lowercase and replacing every '_' with '-'.
		@param name The parameter name.
		@param value The parameter value.
		@exception NullPointerException if the given name and/or value is <code>null</code>.
		*/
		public <E extends Enum<E>> Parameter(final String name, final E value)
		{
			this(name, getSerializationName(value));	//get the serialized form of the enum and create the bookmark
		}
	}
}
