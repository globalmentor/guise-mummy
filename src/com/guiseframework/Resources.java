package com.guiseframework;

import java.net.URI;

import com.garretwilson.rdf.*;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.text.TextUtilities.*;

/**Guise resources description in RDF.
This implementation considers property value resources with reference URIs to establish resource properties of type URI, using the resource's reference URI as the value.
@author Garret Wilson
*/
public class Resources extends ClassTypedRDFResource
{

	/**The recommended prefix to the resource key ontology namespace.*/
	public final static String RESOURCE_NAMESPACE_PREFIX="resource";
	/**The URI to the resource key ontology namespace.*/
	public final static URI RESOURCE_NAMESPACE_URI=URI.create("http://guiseframework.com/namespaces/resource#");

	/**The prefix character used to introduce string value references.*/
	public final static char STRING_VALUE_REFERENCE_PREFIX_CHAR='=';

		//standard labels
	public final static String APPLICATION_NAME=createStringResourceReference("application.name");
	public final static String APPLICATION_NAME_SHORT=createStringResourceReference("application.name.short");
	public final static String APPLICATION_LABEL=createStringResourceReference("application.label");
	public final static String APPLICATION_DESCRIPTION=createStringResourceReference("application.description");
	public final static String APPLICATION_COPYRIGHT=createStringResourceReference("application.copyright");
	public final static String APPLICATION_VERSION=createStringResourceReference("application.version");

	/**Default constructor.*/
	public Resources()
	{
		this(null);	//construct the class with no reference URI
	}

	/**Reference URI constructor.
	@param referenceURI The reference URI for the new resource.
	*/
	public Resources(final URI referenceURI)
	{
		super(referenceURI, RESOURCE_NAMESPACE_URI);  //construct the parent class
	}

	/**Creates a string containing a reference to the given string resource key.
	The string resource reference is a control string according to ECMA-48, "Control Functions for Coded Character Sets", Section 5.6, "Control strings".
	A control string begins with the Start of String control character (U+0098) and ends with a String Terminator control character (U+009C).
	ECMA-48 publication is also approved as ISO/IEC 6429.
	@param resourceKey The resource key to a string in the resources which could be retrieved using {@link GuiseSession#getStringResource(String)}.
	@return A string containing a reference to the given resource key, an ECMA-48 control string with the given resource key as its content, which can be resolved using {@link GuiseSession#resolveString(String)}.
	@exception NullPointerException if the given resource key is <code>null</code>.
	@see <a href="http://www.ecma-international.org/publications/standards/Ecma-048.htm">ECMA-48: Control Functions for Coded Character Sets</a>
	*/
	public final static String createStringResourceReference(final String resourceKey)
	{
		return createControlString(resourceKey);	//return a control string for the given resource key
	}

	/**Creates a string containing a value that can be used as an argument in formatting.
	@param value The value to be used as an argument.
	@return A string containing a reference to the given value, an ECMA-48 control string beginning with {@value #STRING_VALUE_REFERENCE_PREFIX_CHAR} with the given Value as its content.
	@exception NullPointerException if the given value is <code>null</code>.
	@see <a href="http://www.ecma-international.org/publications/standards/Ecma-048.htm">ECMA-48: Control Functions for Coded Character Sets</a>
	*/
	public final static String createStringValueReference(final String value)
	{
		return createControlString(new StringBuilder().append(STRING_VALUE_REFERENCE_PREFIX_CHAR).append(value).toString());	//return a control string for the given value prefixed by the value reference character
	}

	/**Creates a URI containing a reference to the given string resource key.
	The URI resource reference is URI with the scheme <code>resource</code> and the scheme-specific part indicating the resource key.
	@param resourceKey The resource key to a string in the resources which could be retrieved using {@link GuiseSession#getURIResource(String)}.
	@return A URI containing a reference to the given resource key, which can be resolved using {@link GuiseSession#resolveURI(String)}.
	@exception NullPointerException if the given resource key is <code>null</code>.
	*/
	public final static URI createURIResourceReference(final String resourceKey)
	{
		return createURI(RESOURCE_SCHEME, checkInstance(resourceKey, "Resource key cannot be null."));
	}

	/**Returns a form of the enum name appropriate for a resource key.
	The name is converted to lowercaes and all underscore characters ('_') are replaced by periods ('.').
	For example, <code>FILE_NOT_FOUND</code> would produce <code>file.not.found</code>.
	@param e The enum instance to convert to a resource key.
	@return A string representing the enum instance in a style appropriate for use as a resource key.
	@see Enum#name()
	*/
	public static String getResourceKeyName(final Enum<?> e)
	{
		return e.name().toLowerCase().replace('_', '.');	//convert the name to lowercase and replace underscores with periods
	}

}
