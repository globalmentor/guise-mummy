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

package com.guiseframework.component.urf;

import java.net.URI;

import org.urframework.*;

import static com.globalmentor.java.Objects.*;
import static org.urframework.URF.*;

import com.guiseframework.component.*;
import com.guiseframework.model.*;
import com.guiseframework.model.urf.URFResourceDynamicTreeNodeModel;

/**An abstract tree node representation strategy representing an URF resource.
@param <V> The type of value the strategy is to represent.
@author Garret Wilson
*/
public abstract class AbstractURFResourceTreeNodeRepresentationStrategy<V extends URFResource> extends TreeControl.AbstractTreeNodeRepresentationStrategy<V>
{

	/**The manager responsible for generating namespace labels.*/
	private final TURFNamespaceLabelManager namespaceLabelManager;

	  /**@return The manager responsible for generating namespace labels.*/
		public TURFNamespaceLabelManager getNamespaceLabelManager() {return namespaceLabelManager;}

	/**Whether the resource reference URI should be included.*/
	private boolean resourceReferenceIncluded=true;

		/**@return Whether the resource reference should be included.*/
		protected boolean isResourceReferenceIncluded() {return resourceReferenceIncluded;}

		/**Sets whether the resource reference should be included.
		@param referenceIncluded Whether the resource reference should be included.
		*/
		protected void setResourceReferenceURIIncluded(final boolean referenceIncluded) {this.resourceReferenceIncluded=referenceIncluded;}

	/**Whether the resource type should be included.*/
	private boolean resourceTypeIncluded=true;

		/**@return Whether the resource type should be included.*/
		protected boolean isResourceTypeIncluded() {return resourceTypeIncluded;}

		/**Sets whether the resource type should be included.
		@param resourceTypeIncluded Whether the resource type should be included.
		*/
		protected void setResourceTypeIncluded(final boolean resourceTypeIncluded) {this.resourceTypeIncluded=resourceTypeIncluded;}

	/**Namespace label manager constructor.
	@param namespaceLabelManager The manager responsible for generating namespace labels..
	@exception NullPointerException if the given label manager is <code>null</code>.
	*/
	public AbstractURFResourceTreeNodeRepresentationStrategy(final TURFNamespaceLabelManager namespaceLabelManager)
	{
		this.namespaceLabelManager=checkInstance(namespaceLabelManager, "Namespace label manager cannot be null."); //save the manager we'll use for generating labels
	}

	/**Creates a component to represent the given tree node.
	This implementation returns a label with an appropriate string value to represent the URF resource.
	@param <N> The type of value contained in the node.
	@param treeControl The component containing the model.
	@param model The model containing the value.
	@param treeNode The node containing the value. 
	@param editable Whether values in this column are editable.
	@param selected <code>true</code> if the value is selected.
	@param focused <code>true</code> if the value has the focus.
	@return A new component to represent the given value.
	*/
	public <N extends V> Label createComponent(final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode, final boolean editable, final boolean selected, final boolean focused)
	{
			//TODO improve this entire strategy
		final N value=treeNode.getValue();	//get the current value
		final Label label=new SelectableLabel(createInfoModel(treeControl, model, treeNode));	//create a new label using the created label model TODO always create a label model, not just if there is a value
		final String labelText=buildLabelText(new StringBuilder(), treeControl, model, treeNode, value).toString();	//construct the label text
		label.setLabel(labelText);	//set the label's text
		return label;	//return the label
	}

	/**Creates an info model for the representation label.
	@param <N> The type of value contained in the node.
	@param treeControl The component containing the model.
	@param model The model containing the value.
	@param treeNode The node containing the value. 
	@return The label model to use for the label.
	*/
	protected <N extends V> InfoModel createInfoModel(final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode)
	{
		return new DefaultInfoModel();	//return a default label model
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
	protected <N extends V> StringBuilder buildLabelText(final StringBuilder stringBuilder, final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode, final N value)
//TODO fix	protected StringBuilder buildLabelText(final StringBuilder stringBuilder, final TreeControl treeControl, final TreeModel model, final TreeNodeModel<? extends V> treeNode, final V value)	//TODO later put this method hierarchy in a custom label model
	{
			//TODO shouldn't we make sure the resource is not null?
		final URI propertyURI;
		if(treeNode instanceof URFResourceDynamicTreeNodeModel)	//if the tree node is an URF tree node
		{
			propertyURI=((URFResourceDynamicTreeNodeModel<?>)treeNode).getPropertyURI();	//get the property URI, if any, associated with the URF resource
			if(propertyURI!=null)  //if resource is the object of a property
			{
				final URI propertyNamespaceURI=getNamespaceURI(propertyURI);	//get the namespace of the property URI
				if(propertyNamespaceURI!=null)	//if the property is in a namesapce
				{
						//TODO update algorithm to probably check up the tree node hierarchy for an URF tree node, and check to see if the namespace URI actually exist in the data model
					getNamespaceLabelManager().determineNamespaceLabel(propertyNamespaceURI);	//ask the namespace label manager for a label for this namespace, so that one will be there
				}				
				stringBuilder.append(URFTURFGenerator.createReferenceString(propertyURI, getNamespaceLabelManager(), null)); //append a reference to the property URI
			}
		}
		else	//if this is not an URF tree node
		{
			propertyURI=null;	//there is no property URI
		}
		final URI resourceURI=value.getURI();	//get the URI of the value
		URI typeURI=value.getTypeURI();  //get the type URI of the resource
		if(resourceURI!=null)	//if there is a resource URI
		{
			final URI namespaceURI;	//we'll see if there is some related namespace so that we can ensure a namespace label TODO improve algorithm to ensure somewhere that namespace URIs appear before other resources in the tree
			final URI inlineTypeURI;	//we'll determine an inline type, if there is one
			if(isInlineURI(resourceURI))	//if the resource URI is an inline URI
			{
				inlineTypeURI=getInlineTypeURI(resourceURI);	//get the inline type URI of the resource URI
				namespaceURI=getNamespaceURI(inlineTypeURI);	//get the namespace of the inline type URI
			}
			else	//if the resource URI is not an inline URI
			{
				inlineTypeURI=null;	//there is no inline type
				namespaceURI=getNamespaceURI(resourceURI);	//get the resource URI namespace URI, if any
			}
			if(namespaceURI!=null)	//if there is some related namespace
			{
					//TODO update algorithm to probably check up the tree node hierarchy for an URF tree node, and check to see if the namespace URI actually exist in the data model
				getNamespaceLabelManager().determineNamespaceLabel(namespaceURI);	//ask the namespace label manager for a label for this namespace, so that one will be there
			}
			if(typeURI!=null)	//if we have a type URI
			{
				if(typeURI.equals(inlineTypeURI))	//if this resource has an inline URI of the same type as we just found
				{
					typeURI=null;	//don't use the type URI; the type inherent in the inline URI is sufficient
				}
			}
		}
		if(typeURI!=null)	//if we still have a type URI
		{
			final URI typeNamespaceURI=getNamespaceURI(typeURI);	//get the namespace of the type URI
			if(typeNamespaceURI!=null)	//if the type is in a namesapce
			{
					//TODO update algorithm to probably check up the tree node hierarchy for an URF tree node, and check to see if the namespace URI actually exist in the data model
				getNamespaceLabelManager().determineNamespaceLabel(typeNamespaceURI);	//ask the namespace label manager for a label for this namespace, so that one will be there
			}				
		}
		boolean hasPredicateToken=false;	//we'll note whether we ever have something to represent the predicate of the statement
		if(isResourceTypeIncluded() && typeURI!=null) //if we should indicate the resource type and we have a type
		{
			if(propertyURI!=null) //if we have a property
			{
				stringBuilder.append('='); //separate the property from the rest
			}
					//TODO important: check for type URI null here and elsewhere
			stringBuilder.append('(').append(URFTURFGenerator.createReferenceString(typeURI, getNamespaceLabelManager())).append(')'); //append "(type)"
			hasPredicateToken=true;	//show that we have something to represent the predicate
		}
/*TODO fix if desired
		final String label=value.getLabel()
		if(label!=null)	//if there is a label
		{
			if(hasProperty && !hasPredicateToken) //if we had a property but no predicate representation
				stringBuilder.append(':'); //append a colon to separate the property from the rest
			if(hasPredicateToken) //if we had something to represent the predicate
				stringBuilder.append(' '); //append a space to separate the rest
			stringBuilder.append(label);		//append the text of the label
			hasPredicateToken=true;	//show that we have something to represent the predicate
		} 
*/
		if(isResourceReferenceIncluded() && resourceURI!=null) //if we should indicate the URI this resource has a URI
		{
			if(propertyURI!=null && !hasPredicateToken) //if we had a property but no predicate representation
			{
				stringBuilder.append('='); //separate the property from the rest
			}
			if(hasPredicateToken) //if we had something to represent the predicate
			{
				stringBuilder.append(' '); //append a space to separate the rest
			}
			stringBuilder.append(URFTURFGenerator.createReferenceString(resourceURI, getNamespaceLabelManager()));  //append reference
			hasPredicateToken=true;	//show that we have something to represent the predicate
		}
		if(stringBuilder.length()==0)	//if we haven't constructed any string
		{
			stringBuilder.append('?');	//identify the anonymous resource with a question mark
		}
		return stringBuilder;	//return the string builder
	}
	
}
