package com.guiseframework.model;

import com.garretwilson.rdf.*;

/**A tree node model that represents an RDF literal.
@author Garret Wilson
*/
public class RDFLiteralTreeNodeModel extends DefaultTreeNodeModel<RDFLiteral> implements RDFObjectTreeNodeModel<RDFLiteral>
{

	/**The RDF property of which this RDF object is an object, or <code>null</code> if this object should not be considered the object of any property.*/
	private final RDFResource property;

		/**@return The RDF property of which this RDF object is an object, or <code>null</code> if this object should not be considered the object of any property.*/
		public RDFResource getProperty() {return property;}

	/**Default constructor with no initial value.*/
	public RDFLiteralTreeNodeModel()
	{
		this(null);	//construct the class with no initial value
	}

	/**Initial value constructor.
	@param initialValue The initial value, which will not be validated.
	*/
	public RDFLiteralTreeNodeModel(final RDFLiteral initialValue)
	{
		this(null, initialValue);	//construct the class with a null initial value
	}

	/**Property and initial value constructor.
	@param rdfProperty The property of which this object is a resource, or <code>null</code> if this object should not be considered the object of any property.
	@param initialValue The initial value, which will not be validated.
	*/
	public RDFLiteralTreeNodeModel(final RDFResource rdfProperty, final RDFLiteral initialValue)
	{
		super(RDFLiteral.class, initialValue);	//construct the parent class
		property=rdfProperty; //save the property of which this resource is the object
	}
}
