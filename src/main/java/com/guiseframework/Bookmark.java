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

package com.guiseframework;

import static java.util.Collections.*;

import java.util.*;

import static com.globalmentor.java.Objects.*;
import static com.globalmentor.net.URIs.*;

import com.globalmentor.model.NameValuePair;
import com.globalmentor.net.URIQueryParameter;
import com.globalmentor.net.URIs;
import com.globalmentor.text.ArgumentSyntaxException;

/**
 * An identification of a saved state at a navigation point. The bookmark is relative to the navigation path.
 * @author Garret Wilson
 */
public class Bookmark implements Cloneable {

	/**
	 * The map of parameters keyd to parameter names. This map is not thread-safe, but it should never be modified by more than one thread because this is an
	 * immutable class.
	 */
	private HashMap<String, Parameter> parameterMap = new HashMap<String, Parameter>();

	/** @return A read-only set of parameters. */
	public Set<Parameter> getParameters() {
		return unmodifiableSet(new HashSet<Parameter>(parameterMap.values()));
	} //return a read-only set of the parameters

	/**
	 * Returns the value associated with the first parameter of the given name.
	 * @param parameterName The name of the parameter.
	 * @return The value of the first parameter with the given name, or <code>null</code> if there is no parameter with the given name.
	 * @throws NullPointerException if the given name is <code>null</code>.
	 */
	public String getParameterValue(final String parameterName) {
		final Parameter parameter = parameterMap.get(checkInstance(parameterName, "Parameter name cannot be null.")); //get the requested parameter
		return parameter != null ? parameter.getValue() : null; //return the parameter value, if there was a parameter
	}

	/**
	 * String constructor. The string must be suitable for use as a URI query, in the form
	 * "?<var>parameter1Name</var>=<var>parameter1Value</var>&amp;<var>parameter2Name</var>=<var>parameter2Value</var>&hellip;". The parameter names and values
	 * should be percent-encoded as required for URIs. If there are parameters with duplicate names, only the first ones are used and the rest with the same name
	 * are ignored.
	 * @param bookmark A string representation of the bookmark, beginning with '?'.
	 * @throws NullPointerException if the given bookmark string is <code>null</code>.
	 * @throws ArgumentSyntaxException if the bookmark does not begin with '?' or otherwise is not in the correct format.
	 */
	public Bookmark(final CharSequence bookmark) throws ArgumentSyntaxException {
		final int bookmarkLength = checkInstance(bookmark, "Bookmark string cannot be null.").length(); //get the length of the bookmark
		if(bookmarkLength == 0 || bookmark.charAt(0) != QUERY_SEPARATOR) { //if the bookmark string does not begin with '?'
			throw new ArgumentSyntaxException("Bookmark string " + bookmark + " must being with '?'.");
		}
		final NameValuePair<String, String>[] parameters = URIs.getParameters(bookmark.subSequence(1, bookmarkLength).toString()); //get the parameters from the string following the query character
		final Parameter[] bookmarkParameters = new Parameter[parameters.length]; //create a new array of bookmark parameters
		for(int i = parameters.length - 1; i >= 0; --i) { //for each parameter
			final NameValuePair<String, String> parameter = parameters[i]; //get a reference to this parameter
			bookmarkParameters[i] = new Parameter(parameter.getName(), parameter.getValue()); //create a corresponding bookmark parameter
		}
		setParameters(bookmarkParameters); //set the parameters
	}

	/**
	 * Parameter list constructor. If there are parameters with duplicate names, only the first ones are used and the rest with the same name are ignored.
	 * @param parameters The bookmark parameters.
	 * @throws NullPointerException if the given parameters list is <code>null</code>.
	 */
	public Bookmark(final List<Parameter> parameters) {
		setParameters(parameters); //set the parameters
	}

	/**
	 * Parameter array constructor. If there are parameters with duplicate names, only the first ones are used and the rest with the same name are ignored.
	 * @param parameters The optional bookmark parameters.
	 * @throws NullPointerException if the given parameters array is <code>null</code>.
	 */
	public Bookmark(final Parameter... parameters) {
		setParameters(parameters); //set the parameters
	}

	/**
	 * Sets bookmark parameters. If there are parameters with duplicate names, only the first ones are used and the rest with the same name are ignored. This
	 * method should only be called during constructor initialization.
	 * @param parameters The optional bookmark parameters.
	 */
	protected void setParameters(final Parameter... parameters) {
		for(final Parameter parameter : parameters) { //for each parameter
			final String parameterName = parameter.getName(); //get the parameter name
			if(!parameterMap.containsKey(parameterName)) { //if this parameter is not already stored in the map
				parameterMap.put(parameterName, parameter); //store this parameter in the map
			}
		}
	}

	/**
	 * Sets bookmark parameters. If there are parameters with duplicate names, only the first ones are used and the rest with the same name are ignored. This
	 * method should only be called during constructor initialization.
	 * @param parameters The optional bookmark parameters.
	 */
	protected void setParameters(final List<Parameter> parameters) {
		for(final Parameter parameter : parameters) { //for each parameter
			final String parameterName = parameter.getName(); //get the parameter name
			if(!parameterMap.containsKey(parameterName)) { //if this parameter is not already stored in the map
				parameterMap.put(parameterName, parameter); //store this parameter in the map
			}
		}
	}

	/**
	 * Creates a new bookmark with the given parameter set to the given value. If the named parameter does not exist, it will be added. If this bookmark already
	 * contains a parameter with the given value, this bookmark will be returned.
	 * @param name The parameter name.
	 * @param value The parameter value.
	 * @throws NullPointerException if the given name and/or value is <code>null</code>.
	 * @return The new bookmark with the given parameter set to the given value.
	 */
	public Bookmark setParameter(final String name, final String value) {
		if(value.equals(getParameterValue(name))) { //if this bookmark already has the given parameter name and value
			return this; //return this bookmark; it already has the correct values
		}
		final Parameter newParameter = new Parameter(name, value); //create the new parameter with the new value
		final Bookmark newBookmark = (Bookmark)clone(); //clone this bookmark
		newBookmark.parameterMap.put(name, newParameter); //store the new parameter in the new bookmark
		return newBookmark; //return our modified clone
	}

	/**
	 * Creates a new bookmark with the given parameter removed. If this bookmark does not contains the given parameter, this bookmark will be returned.
	 * @param name The parameter name.
	 * @throws NullPointerException if the given name is <code>null</code>.
	 * @return The new bookmark with the given parameter removed.
	 */
	public Bookmark removeParameter(final String name) {
		if(getParameterValue(name) == null) { //if this bookmark does not have given parameter name and value
			return this; //return this bookmark; it already has the parameter removed
		}
		final Bookmark newBookmark = (Bookmark)clone(); //clone this bookmark
		newBookmark.parameterMap.remove(name); //remove the parameter from the new bookmark
		return newBookmark; //return our modified clone
	}

	/** @return A shallow clone of this object. */
	@SuppressWarnings("unchecked")
	//we clone our internal hash map, so we know the generic return type
	public Object clone() {
		try {
			final Bookmark clone = (Bookmark)super.clone(); //create a clone of the bookmark
			clone.parameterMap = (HashMap<String, Parameter>)parameterMap.clone(); //clone the map of parameters
			return clone; //return the clone
		} catch(final CloneNotSupportedException cloneNotSupportedException) { //we support cloning in this class, so we should never get this exception
			throw new AssertionError(cloneNotSupportedException);
		}
	}

	/** @return A hash code value for the object. */
	public int hashCode() {
		return parameterMap.hashCode(); //return the hash code of the parameter map
	}

	/**
	 * Indicates whether some other object is "equal to" this one. This implementation returns whether the given object is a bookmark with the same ID and
	 * parameters.
	 * @param object The reference object with which to compare.
	 * @return <code>true</code> if this object is equivalent to the given object.
	 */
	public boolean equals(final Object object) {
		if(object instanceof Bookmark) { //if the given object is a bookmark
			return parameterMap.equals(((Bookmark)object).parameterMap); //compare parameter maps
		} else { //if the given object is not a bookmark
			return false; //indicate that the objects are not equal
		}
	}

	/**
	 * Returns a string representation of the bookmark. The string is suitable for use as a URI query, in the form
	 * "?<var>parameter1Name</var>=<var>parameter1Value</var>&amp;<var>parameter2Name</var>=<var>parameter2Value</var>&hellip;". Each name and value will be
	 * percent-encoded as appropriate for URIs.
	 * @return A string representation of the bookmark suitable for use as a URI query.
	 */
	public String toString() {
		final StringBuilder bookmarkQueryStringBuilder = new StringBuilder(); //create a new string builder
		if(!parameterMap.isEmpty()) { //if there are parameters
			final Set<Parameter> parameterSet = getParameters(); //get the parameters
			final Parameter[] parameters = parameterSet.toArray(new Parameter[parameterSet.size()]); //create an array of parameters
			bookmarkQueryStringBuilder.append(constructQuery(parameters)); //append the parameters to the query string
		} else { //if there are no parameters
			bookmarkQueryStringBuilder.append(QUERY_SEPARATOR); //append the query prefix			
		}
		return bookmarkQueryStringBuilder.toString(); //return the string we constructed
	}

	/**
	 * A bookmark parameter name/value pair. Neither the name nor the value of a bookmark parameter can be <code>null</code>.
	 * @author Garret Wilson
	 */
	public static class Parameter extends URIQueryParameter {

		/**
		 * Constructor specifying the name and value.
		 * @param name The parameter name.
		 * @param value The parameter value.
		 * @throws NullPointerException if the given name and/or value is <code>null</code>.
		 */
		public Parameter(final String name, final String value) {
			super(checkInstance(name, "Parameter name cannot be null."), checkInstance(value, "Parameter value cannot be null.")); //construct the parent class
		}
	}
}
