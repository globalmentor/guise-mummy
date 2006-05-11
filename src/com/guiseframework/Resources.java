package com.guiseframework;

import java.net.URI;
import java.util.HashMap;
import java.util.ResourceBundle;

import com.garretwilson.rdf.*;
import com.garretwilson.util.HashMapResourceBundle;

import static com.garretwilson.rdf.RDFUtilities.*;
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
	public final static String RESOURCE_KEY_NAMESPACE_PREFIX="resourceKey";
	/**The URI to the resource key ontology namespace.*/
	public final static URI RESOURCE_KEY_NAMESPACE_URI=URI.create("http://guiseframework.com/namespaces/resourceKey#");

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
			if(propertyURI!=null && RESOURCE_KEY_NAMESPACE_URI.equals(getNamespaceURI(propertyURI)))	//if this property is in the resourceKey namespace
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
}
