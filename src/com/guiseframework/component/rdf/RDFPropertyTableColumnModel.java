package com.guiseframework.component.rdf;

import java.net.URI;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.rdf.*;
import com.guiseframework.model.DefaultTableColumnModel;

/**A model for a table column representing an RDF property.
@param <V> The type of values contained in the table column representing the property value, which must be an RDF object.
@author Garret Wilson
*/
public class RDFPropertyTableColumnModel<V extends RDFObject> extends DefaultTableColumnModel<V>
{

	/**The URI of the RDF property this column represents.*/
	private final URI propertyURI;

		/**@return The URI of the RDF property this column represents.*/
		public URI getPropertyURI() {return propertyURI;}

	/**Value class constructor.
	@param valueClass The class indicating the type of values held in the model.
	@param propertyURI The URI of the RDF property this column represents.
	@exception NullPointerException if the given value class and/or property URI is <code>null</code>.
	*/
	public RDFPropertyTableColumnModel(final Class<V> valueClass, final URI propertyURI)
	{
		this(valueClass, propertyURI, new RDFXMLGenerator().getLabel(propertyURI));	//construct the class with a label appropriate for this property URI TODO use a shared RDF XMLifier
	}
	
	/**Value class and label constructor.
	@param valueClass The class indicating the type of values held in the model.
	@param propertyURI The URI of the RDF property this column represents.
	@param labelText The text of the label.
	@exception NullPointerException if the given value class and/or property URI is <code>null</code>.
	*/
	public RDFPropertyTableColumnModel(final Class<V> valueClass, final URI propertyURI, final String labelText)
	{
		super(valueClass, labelText);	//construct the parent class
		this.propertyURI=checkInstance(propertyURI, "Property URI cannot be null.");
	}
	
}
