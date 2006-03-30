package com.guiseframework.model;

import com.garretwilson.rdf.*;
import com.guiseframework.GuiseSession;

/**A tree node model that represents an RDF literal.
@author Garret Wilson
*/
public class RDFLiteralTreeNodeModel extends DefaultTreeNodeModel<RDFLiteral> implements RDFObjectTreeNodeModel<RDFLiteral>
{

	/**The RDF property of which this RDF object is an object, or <code>null</code> if this object should not be considered the object of any property.*/
	private final RDFResource property;

		/**@return The RDF property of which this RDF object is an object, or <code>null</code> if this object should not be considered the object of any property.*/
		public RDFResource getProperty() {return property;}

	/**Session constructor with no initial value.
	@param session The Guise session that owns this model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public RDFLiteralTreeNodeModel(final GuiseSession session)
	{
		this(session, null);	//construct the class with no initial value
	}

	/**Session and initial value constructor.
	@param session The Guise session that owns this model.
	@param initialValue The initial value, which will not be validated.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public RDFLiteralTreeNodeModel(final GuiseSession session, final RDFLiteral initialValue)
	{
		this(session, null, initialValue);	//construct the class with a null initial value
	}

	/**Session, property, and initial value constructor.
	@param session The Guise session that owns this model.
	@param rdfProperty The property of which this object is a resource, or <code>null</code> if this object should not be considered the object of any property.
	@param initialValue The initial value, which will not be validated.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public RDFLiteralTreeNodeModel(final GuiseSession session, final RDFResource rdfProperty, final RDFLiteral initialValue)
	{
		super(session, RDFLiteral.class, initialValue);	//construct the parent class
		property=rdfProperty; //save the property of which this resource is the object
	}
}
