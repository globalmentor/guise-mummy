package com.guiseframework.component.rdf.maqro;

import com.garretwilson.rdf.*;
import com.garretwilson.rdf.maqro.Question;
import com.guiseframework.component.TreeControl;
import com.guiseframework.model.TreeModel;
import com.guiseframework.model.TreeNodeModel;

/**A tree node representation strategy representing a MAQRO question.
@author Garret Wilson
*/
public class QuestionTreeNodeRepresentationStrategy extends AbstractInteractionTreeNodeRepresentationStrategy<Question>
{

	/**Default constructor with a default RDF XMLifier.*/
	public QuestionTreeNodeRepresentationStrategy()
	{
		this(new RDFXMLifier());	//create the class with a default RDF XMLifier
	}

	/**RDF XMLifier constructor.
	@param rdfXMLifier The RDF XMLifier to use for creating labels.
	@exception NullPointerException if the given RDF XMLifier is <code>null</code>.
	*/
	public QuestionTreeNodeRepresentationStrategy(final RDFXMLifier rdfXMLifier)
	{
		super(rdfXMLifier);	//construct the parent
	}


	/**Builds the label to be used for a tree node.
	This version adds the query of the question if any.
	@param <N> The type of value contained in the node.
	@param stringBuilder The string builder to hold the label text.
	@param treeControl The component containing the model.
	@param model The model containing the value.
	@param treeNode The node containing the value.
	@param value The value contained in the node.
	@return The string builder used to construct the label. 
	*/
//TODO fix	protected <N extends V> StringBuilder buildLabelText(final StringBuilder stringBuilder, final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode, final N value)	//TODO fix public
	protected StringBuilder buildLabelText(final StringBuilder stringBuilder, final TreeControl treeControl, final TreeModel model, final TreeNodeModel<? extends Question> treeNode, final Question value)	//TODO later put this method hierarchy in a custom label model
	{
		super.buildLabelText(stringBuilder, treeControl, model, treeNode, value);	//do the default label text building
		if(value!=null)	//if there is a value
		{
			if(stringBuilder.length()>0)	//if there already is content
			{
				stringBuilder.append(':').append(' ');	//add separater content
			}
			stringBuilder.append(value);	//TODO testing; improve
		}
//TODO del		super.buildLabelText(stringBuilder, treeControl, model, treeNode, value);	//do the default label text building
		return stringBuilder;	//return the string builder
	}
}
