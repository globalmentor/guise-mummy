package com.guiseframework.model.rdf;

import com.garretwilson.rdf.*;

/**A tree node model that represents an RDF resource.
<p>Any properties will be dynamically loaded</p>
<p>This class has special support for RDF lists, the contents of which are by default displayed as children of the given resource.</p>
@author Garret Wilson
*/
public class RDFResourceTreeNodeModel extends AbstractRDFResourceTreeNodeModel<RDFResource>
{

	/**Default constructor with no initial value.*/
	public RDFResourceTreeNodeModel()
	{
		this(null);	//construct the class with no initial value
	}

	/**Initial value constructor.
	@param initialValue The initial value, which will not be validated.
	*/
	public RDFResourceTreeNodeModel(final RDFResource initialValue)
	{
		this(null, initialValue);	//construct the class with a null initial value
	}

	/**Property and initial value constructor.
	@param rdfProperty The property of which this object is a resource, or <code>null</code> if this object should not be considered the object of any property.
	@param initialValue The initial value, which will not be validated.
	*/
	public RDFResourceTreeNodeModel(final RDFResource rdfProperty, final RDFResource initialValue)
	{
		super(RDFResource.class, rdfProperty, initialValue);	//construct the parent class
	}

}
