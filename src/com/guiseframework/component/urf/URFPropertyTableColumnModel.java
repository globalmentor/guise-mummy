package com.guiseframework.component.urf;

import java.net.URI;

import static com.globalmentor.java.Objects.*;

import com.globalmentor.urf.*;
import com.guiseframework.model.DefaultTableColumnModel;

/**A model for a table column representing an URF property.
@param <V> The type of values contained in the table column representing the property value, which must be an URF resource.
@author Garret Wilson
*/
public class URFPropertyTableColumnModel<V extends URFResource> extends DefaultTableColumnModel<V>
{

	/**The URI of the URF property this column represents.*/
	private final URI propertyURI;

		/**@return The URI of the URF property this column represents.*/
		public URI getPropertyURI() {return propertyURI;}

	/**Value class constructor.
	@param valueClass The class indicating the type of values held in the model.
	@param propertyURI The URI of the URF property this column represents.
	@exception NullPointerException if the given value class and/or property URI is <code>null</code>.
	*/
	public URFPropertyTableColumnModel(final Class<V> valueClass, final URI propertyURI)
	{
		this(valueClass, propertyURI, URFTURFGenerator.createReferenceString(propertyURI, new TURFNamespaceLabelManager(), null, null, true));	//construct the class with a label appropriate for this property URI, determining a new namespace prefix if needed TODO use a shared namespace label manager
	}
	
	/**Value class and label constructor.
	@param valueClass The class indicating the type of values held in the model.
	@param propertyURI The URI of the URF property this column represents.
	@param labelText The text of the label.
	@exception NullPointerException if the given value class and/or property URI is <code>null</code>.
	*/
	public URFPropertyTableColumnModel(final Class<V> valueClass, final URI propertyURI, final String labelText)
	{
		super(valueClass, labelText);	//construct the parent class
		this.propertyURI=checkInstance(propertyURI, "Property URI cannot be null.");
	}
	
}
