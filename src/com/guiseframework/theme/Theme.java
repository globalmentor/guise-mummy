package com.guiseframework.theme;

import java.net.URI;
import java.util.Locale;

import com.garretwilson.rdf.*;

import static com.garretwilson.rdf.RDFUtilities.*;
import static com.garretwilson.rdf.xpackage.XMLOntologyConstants.*;
import static com.guiseframework.Guise.*;

/**Guise theme specification.
@author Garret Wilson
*/
public class Theme extends ClassTypedRDFResource
{

	/**@return The namespace URI of the ontology defining the default type of this resource.*/
	public URI getDefaultTypeNamespaceURI() {return GUISE_NAMESPACE_URI;}

	/**The resources property name; the local name of <code>http://guiseframework.com/namespaces/guise#</code>.*/
	public final static String RESOURCES_PROPERTY_NAME="resources";

	/**The theme parent, or <code>null</code> if there is no resolving parent.*/
	private Theme parent=null;

		/**@return The theme parent, or <code>null</code> if there is no resolving parent.*/
		public Theme getParent() {return parent;}

		/**Sets the theme parent.
		@param newParent The new theme parent, or <code>null</code> if there should be no resolving parent.
		*/
		public void setParent(final Theme newParent) {parent=newParent;}	//TODO maybe remove and create custom ThemeIO

	/**Default constructor.*/
	public Theme()
	{
		super();	//construct the parent class
	}

	/**Reference URI constructor.
	@param referenceURI The reference URI for the new resource.
	*/
	public Theme(final URI referenceURI)
	{
		super(referenceURI);  //construct the parent class
	}

	/**Retrieves the URI of resources for the given locale.
	@return The URI to resources for the given locale, or <code>null</code> if no appropriate resources property could be found.
	*/
	public URI getResourcesURI(final Locale locale)
	{
		final RDFResource rdfResource=asResource(getPropertyValue(GUISE_NAMESPACE_URI, RESOURCES_PROPERTY_NAME));	//get the resources resource, if any
		return rdfResource!=null ? rdfResource.getReferenceURI() : null;	//return the resources URI TODO search based upon locale
	}

	/**Retrieves an iterable to the XML style resources, represented by <code>x:style</code> properties.
	@return An iterable to the styles, if any.
	*/
	public Iterable<RDFResource> getStyles()
	{
		return getPropertyValues(XML_ONTOLOGY_NAMESPACE_URI, STYLE_PROPERTY_NAME, RDFResource.class); //return an iterable to style properties
	}

}
