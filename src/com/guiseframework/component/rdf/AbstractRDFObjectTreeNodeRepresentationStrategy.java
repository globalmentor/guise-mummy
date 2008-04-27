/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.component.rdf;

import static com.globalmentor.java.Objects.*;

import com.globalmentor.rdf.*;
import com.globalmentor.util.Debug;
import com.guiseframework.component.Label;
import com.guiseframework.component.SelectableLabel;
import com.guiseframework.component.TreeControl;
import com.guiseframework.model.*;
import com.guiseframework.model.rdf.RDFObjectTreeNodeModel;

/**An abstract tree node representation strategy representing an RDF object.
@param <V> The type of value the strategy is to represent.
@author Garret Wilson
*/
public abstract class AbstractRDFObjectTreeNodeRepresentationStrategy<V extends RDFObject> extends TreeControl.AbstractTreeNodeRepresentationStrategy<V>
{

	/**The RDF XMLifier to use for creating labels.*/
	private final RDFXMLGenerator xmlGenerator;

	  /**@return The RDF XMLifier to use for creating labels.*/
		public RDFXMLGenerator getXMLGenerator() {return xmlGenerator;}

	/**RDF XMLifier constructor.
	@param rdfXMLifier The RDF XMLifier to use for creating labels.
	@exception NullPointerException if the given RDF XMLifier is <code>null</code>.
	*/
	public AbstractRDFObjectTreeNodeRepresentationStrategy(final RDFXMLGenerator rdfXMLifier)
	{
		xmlGenerator=checkInstance(rdfXMLifier, "RDF XMLifier cannot be null."); //save the XMLifier we'll use for generating labels
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
	@return A new component to represent the given value.
	*/
	@SuppressWarnings("unchecked")
	public <N extends V> Label createComponent(final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode, final boolean editable, final boolean selected, final boolean focused)
	{
			//TODO improve this entire strategy
		final N value=treeNode.getValue();	//get the current value
		final Label label=new SelectableLabel(createLabelModel(treeControl, model, treeNode));	//create a new label using the created label model TODO always create a label model, not just if there is a value
		final String labelText=buildLabelText(new StringBuilder(), treeControl, model, treeNode, value).toString();	//construct the label text
		label.setLabel(labelText);	//set the label's text
		return label;	//return the label
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
		return new DefaultLabelModel();	//return a default label model
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
//TODO fix	protected <N extends V> StringBuilder buildLabelText(final StringBuilder stringBuilder, final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode, final N value)	//TODO later put this method hierarchy in a custom label model
	protected StringBuilder buildLabelText(final StringBuilder stringBuilder, final TreeControl treeControl, final TreeModel model, final TreeNodeModel<? extends V> treeNode, final V value)	//TODO later put this method hierarchy in a custom label model
	{
		if(treeNode instanceof RDFObjectTreeNodeModel)	//if the tree node is an RDF object tree node
		{
			final RDFResource rdfProperty=((RDFObjectTreeNodeModel<?>)treeNode).getProperty();	//get the property, if any, associated with the RDF object
			if(rdfProperty!=null)  //if object is the object of a property
			{
				assert rdfProperty.getURI()!=null : "RDF property has no reference URI.";
				stringBuilder.insert(0, getXMLGenerator().getLabel(rdfProperty.getURI())); //prepend "property"
			}
		}
		return stringBuilder;	//return the string builder
	}
}
