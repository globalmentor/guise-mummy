package com.guiseframework;

import java.net.URI;
import java.util.HashMap;
import java.util.ResourceBundle;

import com.garretwilson.rdf.*;
import com.garretwilson.util.HashMapResourceBundle;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.rdf.RDFUtilities.*;
import static com.garretwilson.text.TextUtilities.*;
import static com.guiseframework.Guise.*;

/**Guise resources description in RDF.
This implementation considers property value resources with reference URIs to establish resource properties of type URI, using the resource's reference URI as the value.
@author Garret Wilson
*/
public class Resources extends ClassTypedRDFResource
{

	/**@return The namespace URI of the ontology defining the default type of this resource.*/
	public URI getDefaultTypeNamespaceURI() {return GUISE_NAMESPACE_URI;}

	/**The recommended prefix to the resource key ontology namespace.*/
	public final static String RESOURCE_NAMESPACE_PREFIX="resource";
	/**The URI to the resource key ontology namespace.*/
	public final static URI RESOURCE_NAMESPACE_URI=URI.create("http://guiseframework.com/namespaces/resource#");

	/**Default constructor.*/
	public Resources()
	{
		super();	//construct the parent class
	}

	/**Reference URI constructor.
	@param referenceURI The reference URI for the new resource.
	*/
	public Resources(final URI referenceURI)
	{
		super(referenceURI);  //construct the parent class
	}

	/**Creates a resource bundle with contents reflecting these resources.
	@param parent The parent resource bundle, or <code>null</code> if there should be no parent for resolving resources.
	*/
	public ResourceBundle toResourceBundle(final ResourceBundle parent)
	{
		final HashMap<String, Object> resourceHashMap=new HashMap<String, Object>(getPropertyCount());	//create a new hash map with enough initial room for all properties
		for(final RDFPropertyValuePair propertyValuePair:getProperties())	//for each resource property/value pair
		{
			final RDFResource property=propertyValuePair.getProperty();	//get the property
			final URI propertyURI=property.getReferenceURI();	//get the property URI
			if(propertyURI!=null && RESOURCE_NAMESPACE_URI.equals(getNamespaceURI(propertyURI)))	//if this property is in the resourceKey namespace
			{
				final String resourceKey=getLocalName(propertyURI);	//use the local name as the resource key
				Object value=null;	//we'll store the resource value here
				final RDFObject propertyValue=propertyValuePair.getValue();	//get the value
				if(propertyValue instanceof RDFResource)	//if the property value is a resource
				{
					value=((RDFResource)propertyValue).getReferenceURI();	//use the reference URI, if any, as the value
				}
				else if(propertyValue instanceof RDFLiteral)	//if the property value is a literal
				{
					value=((RDFLiteral)propertyValue).getLexicalForm();	//use the lexical form of the literal as the value TODO maybe use the object instead
				}
				if(value!=null)	//if we found a value
				{
					resourceHashMap.put(resourceKey, value);	//store the resource key/value pair in the map
				}				
			}
		}
		return new HashMapResourceBundle(resourceHashMap, parent);	//create a new hash map resource bundle with the given parent and return it		
	}

	/**Creates a string containing a reference to the given string resource key.
	The string sresource reference is a control string according to ECMA-48, "Control Functions for Coded Character Sets", Section 5.6, "Control strings".
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

}
