package com.guiseframework.component.rdf;

import com.garretwilson.rdf.*;

/**A default tree node representation strategy representing an RDF resource.
@author Garret Wilson
*/
public class RDFResourceTreeNodeRepresentationStrategy extends AbstractRDFResourceTreeNodeRepresentationStrategy<RDFResource>
{

	/**Default constructor with a default RDF XMLifier.*/
	public RDFResourceTreeNodeRepresentationStrategy()
	{
		this(new RDFXMLGenerator());	//create the class with a default RDF XMLifier
	}

	/**RDF XMLifier constructor.
	@param rdfXMLifier The RDF XMLifier to use for creating labels.
	@exception NullPointerException if the given RDF XMLifier is <code>null</code>.
	*/
	public RDFResourceTreeNodeRepresentationStrategy(final RDFXMLGenerator rdfXMLifier)
	{
		super(rdfXMLifier);	//construct the parent
	}

}
