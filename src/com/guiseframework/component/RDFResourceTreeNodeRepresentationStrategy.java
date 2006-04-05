package com.guiseframework.component;

import com.garretwilson.rdf.*;

import static com.garretwilson.rdf.RDFUtilities.*;
import static com.garretwilson.rdf.rdfs.RDFSUtilities.*;
import com.guiseframework.GuiseSession;
import com.guiseframework.model.*;

/**A tree node representation strategy representing an RDF resource.
@author Garret Wilson
*/
public class RDFResourceTreeNodeRepresentationStrategy extends AbstractRDFObjectTreeNodeRepresentationStrategy<RDFResource>
{

	/**Whether the reference URI should be included.*/
	private boolean resourceReferenceURIIncluded=true;

		/**@return Whether the reference URI should be included.*/
		protected boolean isResourceReferenceURIIncluded() {return resourceReferenceURIIncluded;}

		/**Sets whether the reference URI should be included.
		@param referenceURIIncluded Whether the reference URI should be included.
		*/
		protected void setResourceReferenceURIIncluded(final boolean referenceURIIncluded) {this.resourceReferenceURIIncluded=referenceURIIncluded;}
	
	/**Session constructor with a default RDF XMLifier.
	@param session The Guise session that owns this representation strategy.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public RDFResourceTreeNodeRepresentationStrategy(final GuiseSession session)
	{
		this(session, new RDFXMLifier());	//create the class with a default RDF XMLifier
	}

	/**Session and RDF XMLifier constructor.
	@param session The Guise session that owns this representation strategy.
	@param rdfXMLifier The RDF XMLifier to use for creating labels.
	@exception NullPointerException if the given session and/or RDF XMLifier is <code>null</code>.
	*/
	public RDFResourceTreeNodeRepresentationStrategy(final GuiseSession session, final RDFXMLifier rdfXMLifier)
	{
		super(session, rdfXMLifier);	//construct the parent
	}

	/**Builds the label to be used for a tree node.
	This version appends information about the resource.
	@param <N> The type of value contained in the node.
	@param stringBuilder The string builder to hold the label text.
	@param treeControl The component containing the model.
	@param model The model containing the value.
	@param treeNode The node containing the value.
	@param value The value contained in the node.
	@return The string builder used to construct the label. 
	*/
	protected <N extends RDFResource> StringBuilder buildLabelText(final StringBuilder stringBuilder, final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode, final N value)
	{
		super.buildLabelText(stringBuilder, treeControl, model, treeNode, value);	//do the default label text building
		final RDFResource type=getType(value);  //get the type of the resource
		final RDFLiteral label=getLabel(value);	//get the label of the resource
		final boolean hasProperty=stringBuilder.length()>0;	//see if we have property information
		boolean hasPredicateToken=false;	//we'll note whether we ever have something to represent the predicate of the statement
		if(type!=null) //if we have a type
		{
			if(hasProperty && !hasPredicateToken) //if we had a property but no predicate representation
				stringBuilder.append(':'); //append a colon to separate the property from the rest
			if(hasPredicateToken) //if we had something to represent the predicate
				stringBuilder.append(' '); //append a space to separate the rest
			stringBuilder.append('(').append(getXMLifier().getLabel(type)).append(')'); //append "(type)"
			hasPredicateToken=true;	//show that we have something to represent the predicate
		}
		if(label!=null)	//if there is a label
		{
			if(hasProperty && !hasPredicateToken) //if we had a property but no predicate representation
				stringBuilder.append(':'); //append a colon to separate the property from the rest
			if(hasPredicateToken) //if we had something to represent the predicate
				stringBuilder.append(' '); //append a space to separate the rest
			stringBuilder.append(label);		//append the text of the label
			hasPredicateToken=true;	//show that we have something to represent the predicate
		} 
		if(isResourceReferenceURIIncluded() && value.getReferenceURI()!=null) //if we should indicate the reference URI this is not a blank node resource
		{
			if(hasProperty && !hasPredicateToken) //if we had a property but no predicate representation
				stringBuilder.append(':'); //append a colon to separate the property from the rest
			if(hasPredicateToken) //if we had something to represent the predicate
				stringBuilder.append(' '); //append a space to separate the rest
			stringBuilder.append('[').append(getXMLifier().getLabel(value)).append(']');  //append "[referenceURI]" label
			hasPredicateToken=true;	//show that we have something to represent the predicate
		}
		final RDFLiteral literalValue=getValue(value);	//get the literal rdf:value property value, if there is one
		if(literalValue!=null)	//if this resource has a literal value
		{
			if(hasProperty && !hasPredicateToken) //if we had a property but no predicate representation
				stringBuilder.append(':'); //append a colon to separate the property from the rest
			if(hasPredicateToken) //if we had something to represent the predicate
				stringBuilder.append(' '); //append a space to separate the rest
			stringBuilder.append('{').append(literalValue.getLexicalForm()).append('}');  //append "{lexicalForm}" label
			hasPredicateToken=true;	//show that we have something to represent the predicate				
		}
		return stringBuilder;	//return the string builder
	}
}
