package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.rdf.*;
import com.guiseframework.GuiseSession;
import com.guiseframework.model.*;

/**An abstract tree node representation strategy representing an RDF object.
@param <V> The type of value the strategy is to represent.
@author Garret Wilson
*/
public abstract class AbstractRDFObjectTreeNodeRepresentationStrategy<V extends RDFObject> extends TreeControl.AbstractTreeNodeRepresentationStrategy<V>
{

	/**The RDF XMLifier to use for creating labels.*/
	private final RDFXMLifier xmlifier;

	  /**@return The RDF XMLifier to use for creating labels.*/
		public RDFXMLifier getXMLifier() {return xmlifier;}

	/**Session and RDF XMLifier constructor.
	@param session The Guise session that owns this representation strategy.
	@param rdfXMLifier The RDF XMLifier to use for creating labels.
	@exception NullPointerException if the given session and/or RDF XMLifier is <code>null</code>.
	*/
	public AbstractRDFObjectTreeNodeRepresentationStrategy(final GuiseSession session, final RDFXMLifier rdfXMLifier)
	{
		super(session);	//construct the parent
		xmlifier=checkInstance(rdfXMLifier, "RDF XMLifier cannot be null."); //save the XMLifier we'll use for generating labels
	}

	/**Creates a component to represent the given tree node.
	This implementation returns a label with an appropriate string value to represent the RDF object.
	@param <N> The type of value contained in the node.
	@param treeControl The component containing the model.
	@param model The model containing the value.
	@param treeNode The node containing the value. 
	@param editable Whether values in this column are editable.
	@param selected <code>true</code> if the value is selected.
	@param focused <code>true</code> if the value has the focus.
	@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
	*/
	@SuppressWarnings("unchecked")
	public <N extends V> Label createComponent(final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode, final boolean editable, final boolean selected, final boolean focused)
	{
		final N value=treeNode.getValue();	//get the current value
		if(value!=null)	//if there is value
		{
			final GuiseSession session=getSession();	//get the session
			final String id=treeControl.createID(getID(treeNode.getValue()));	//get the ID from the value TODO don't get the ID from the value, as this can change if edited
			final Label label=new Label(getSession(), getID(value), createLabelModel(treeControl, model, treeNode));	//create a new label using the created label model TODO always create a label model, not just if there is a value
			final String labelText=buildLabelText(new StringBuilder(), treeControl, model, treeNode, value).toString();	//construct the label text
			label.setLabel(labelText);	//set the label's text
			return label;	//return the label
		}
		else	//if there is no value
		{
			return null;	//don't return a component
		}
	}

	/**Creates a label model for the representation label.
	@param <N> The type of value contained in the node.
	@param treeControl The component containing the model.
	@param model The model containing the value.
	@param treeNode The node containing the value. 
	@return The label model to use for the label.
	*/
	protected <N extends V> LabelModel createLabelModel(final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode)
	{
		return new DefaultLabelModel(getSession());	//return a default label model
	}

	/**Builds the label to be used for a tree node.
	If the tree node is an {@link RDFObjectTreeNodeModel}, this version prepends a representation of its property.
	@param <N> The type of value contained in the node.
	@param stringBuilder The string builder to hold the label text.
	@param treeControl The component containing the model.
	@param model The model containing the value.
	@param treeNode The node containing the value.
	@param value The value contained in the node.
	@return The string builder used to construct the label. 
	*/
	protected <N extends V> StringBuilder buildLabelText(final StringBuilder stringBuilder, final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode, final N value)	//TODO later put this method hierarchy in a custom label model
	{
		if(treeNode instanceof RDFObjectTreeNodeModel)	//if the tree node is an RDF object tree node
		{
			final RDFResource rdfProperty=((RDFObjectTreeNodeModel<?>)treeNode).getProperty();	//get the property, if any, associated with the RDF object
			if(rdfProperty!=null)  //if object is the object of a property
			{
				stringBuilder.insert(0, getXMLifier().getLabel(rdfProperty)); //prepend "property"
			}
		}
		return stringBuilder;	//return the string builder
	}
}
