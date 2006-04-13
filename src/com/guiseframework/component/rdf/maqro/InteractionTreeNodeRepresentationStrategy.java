package com.guiseframework.component.rdf.maqro;

import com.garretwilson.rdf.*;
import com.garretwilson.rdf.maqro.Interaction;

/**A tree node representation strategy representing a MAQRO interaction.
@author Garret Wilson
*/
public class InteractionTreeNodeRepresentationStrategy extends AbstractInteractionTreeNodeRepresentationStrategy<Interaction>
{

	/**Default constructor with a default RDF XMLifier.*/
	public InteractionTreeNodeRepresentationStrategy()
	{
		this(new RDFXMLifier());	//create the class with a default RDF XMLifier
	}

	/**RDF XMLifier constructor.
	@param rdfXMLifier The RDF XMLifier to use for creating labels.
	@exception NullPointerException if the given RDF XMLifier is <code>null</code>.
	*/
	public InteractionTreeNodeRepresentationStrategy(final RDFXMLifier rdfXMLifier)
	{
		super(rdfXMLifier);	//construct the parent
	}
}
