package com.guiseframework.component;

import com.garretwilson.rdf.*;
import com.guiseframework.model.*;

/**An tree node representation strategy representing an RDF literal.
@author Garret Wilson
*/
public class RDFLiteralTreeNodeRepresentationStrategy extends AbstractRDFObjectTreeNodeRepresentationStrategy<RDFLiteral>
{

	/**Default constructor with a default RDF XMLifier.*/
	public RDFLiteralTreeNodeRepresentationStrategy()
	{
		this(new RDFXMLifier());	//create the class with a default RDF XMLifier
	}

	/**RDF XMLifier constructor.
	@param rdfXMLifier The RDF XMLifier to use for creating labels.
	@exception NullPointerException if the given RDF XMLifier is <code>null</code>.
	*/
	public RDFLiteralTreeNodeRepresentationStrategy(final RDFXMLifier rdfXMLifier)
	{
		super(rdfXMLifier);	//construct the parent
	}

	/**Builds the label to be used for a tree node.
	This version appends information about the literal.
	@param <N> The type of value contained in the node.
	@param stringBuilder The string builder to hold the label text.
	@param treeControl The component containing the model.
	@param model The model containing the value.
	@param treeNode The node containing the value.
	@param value The value contained in the node.
	@return The string builder used to construct the label. 
	*/
	protected <N extends RDFLiteral> StringBuilder buildLabelText(final StringBuilder stringBuilder, final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode, final N value)
	{
		super.buildLabelText(stringBuilder, treeControl, model, treeNode, value);	//do the default label text building
		final boolean hasProperty=stringBuilder.length()>0;	//see if we have property information
		if(hasProperty) //if we had a property
			stringBuilder.append(':').append(' '); //append ": " to separate the property from the literal
		stringBuilder.append('"').append(value.getLexicalForm()).append('"');  //append the literal value in quotes
		return stringBuilder;	//return the string builder
	}
}
