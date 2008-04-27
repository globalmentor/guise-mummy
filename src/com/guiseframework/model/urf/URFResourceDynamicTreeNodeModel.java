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

package com.guiseframework.model.urf;

import java.net.URI;
import java.util.*;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.urf.URF.*;

import com.globalmentor.urf.*;
import com.guiseframework.model.*;

/**A dynamic tree node model that represents an URF resource.
<p>Any properties will be dynamically loaded</p>
<p>This class has special support for URF lists and sets, the contents of which are by default displayed as children of the given resource.</p>
@param <V> The type of value contained in the tree node.
@author Garret Wilson
*/
public class URFResourceDynamicTreeNodeModel<V extends URFResource> extends AbstractURFDynamicTreeNodeModel<V>
{

	/**The bound property of whether resource children are included in the node.*/
	public final static String RESOURCE_CHILDREN_INCLUDED_PROPERTY=getPropertyName(URFResourceDynamicTreeNodeModel.class, "resourceChildrenIncluded");
	/**The bound property of whether resource properties are included in the node.*/
	public final static String RESOURCE_PROPERTIES_INCLUDED_PROPERTY=getPropertyName(URFResourceDynamicTreeNodeModel.class, "resourcePropertiesIncluded");

	/**The URI of the URF property of which this URF resource is an object, or <code>null</code> if this resource should not be considered the object of any property.*/
	private final URI propertyURI;

		/**@return The URI of the URF property of which this URF resource is an object, or <code>null</code> if this resource should not be considered the object of any property.*/
		public URI getPropertyURI() {return propertyURI;}

	/**Whether resource children are included in the node.*/
	private boolean resourceChildrenIncluded=true;	//TODO unload cached dynamic variables when changed

		/**@return Whether resource children are included in the node.*/
		public boolean isResourceChildrenIncluded() {return resourceChildrenIncluded;}

		/**Sets whether resource children are included in the node.
		This is a bound property of type <code>Boolean</code>.
		@param newChildrenIncluded <code>true</code> if resource children should be included as children of this node.
		@see #RESOURCE_CHILDREN_INCLUDED_PROPERTY
		*/
		public void setResourceChildrenIncluded(final boolean newChildrenIncluded)
		{
			if(resourceChildrenIncluded!=newChildrenIncluded)	//if the value is really changing
			{
				final boolean oldChildrenIncluded=resourceChildrenIncluded;	//get the current value
				resourceChildrenIncluded=newChildrenIncluded;	//update the value
				firePropertyChange(RESOURCE_CHILDREN_INCLUDED_PROPERTY, Boolean.valueOf(oldChildrenIncluded), Boolean.valueOf(newChildrenIncluded));
			}
		}

	/**Whether resource properties are included in the node.*/
	private boolean resourcePropertiesIncluded=true;	//TODO unload cached dynamic variables when changed

		/**@return Whether resource properties are included in the node.*/
		public boolean isResourcePropertiesIncluded() {return resourcePropertiesIncluded;}

		/**Sets whether resource properties are included in the node.
		This is a bound property of type <code>Boolean</code>.
		@param newPropertiesIncluded <code>true</code> if resource properties should be included as children of this node.
		@see #RESOURCE_PROPERTIES_INCLUDED_PROPERTY
		*/
		public void setResourcePropertiesIncluded(final boolean newPropertiesIncluded)
		{
			if(resourcePropertiesIncluded!=newPropertiesIncluded)	//if the value is really changing
			{
				final boolean oldPropertiesIncluded=resourcePropertiesIncluded;	//get the current value
				resourcePropertiesIncluded=newPropertiesIncluded;	//update the value
				firePropertyChange(RESOURCE_PROPERTIES_INCLUDED_PROPERTY, Boolean.valueOf(oldPropertiesIncluded), Boolean.valueOf(newPropertiesIncluded));
			}
		}

	/**Value class constructor with no initial value.
	@param valueClass The class indicating the type of value held in the model.
	*/
	public URFResourceDynamicTreeNodeModel(final Class<V> valueClass)
	{
		this(valueClass, null);	//construct the class with no initial value
	}

	/**Initial value constructor.
	@param valueClass The class indicating the type of value held in the model.
	@param initialValue The initial value, which will not be validated.
	*/
	public URFResourceDynamicTreeNodeModel(final Class<V> valueClass, final V initialValue)
	{
		this(valueClass, null, initialValue);	//construct the class with no property
	}

	/**Property and initial value constructor.
	@param valueClass The class indicating the type of value held in the model.
	@param propertyURI The URI of the URF property of which this URF resource is an object, or <code>null</code> if this resource should not be considered the object of any property.
	@param initialValue The initial value, which will not be validated.
	*/
	public URFResourceDynamicTreeNodeModel(final Class<V> valueClass, final URI propertyURI, final V initialValue)
	{
		super(valueClass, initialValue);	//construct the parent class
		this.propertyURI=propertyURI; //save the URI of the property, if any, of which this resource is the object
	}

	/**Dynamically determines whether this node is a leaf.
	This version determines if resource properties are included and there are properties, or if resource children are included and there are children.
	This version ignores URF type properties and properties of a {@link URFCollectionResource}.
	@return Whether this node should be considered a leaf with no children.
	*/
	protected boolean determineLeaf()
	{
		final URFResource resource=getValue();	//get the resource
		if(resource!=null)	//if we have a resource
		{
			if(isResourcePropertiesIncluded() && !(resource instanceof URFCollectionResource))	//if resource properties are included (ignore properties of an URF collection resource, because they will not be shown)
			{
				resource.readLock().lock();	//get a read lock
				try
				{
					for(final URI propertyURI:resource.getPropertyURIs())	//look at all property URIs
					{
						if(!TYPE_PROPERTY_URI.equals(propertyURI))	//if this is not the type property URI
						{
							return false;	//this is not a leaf
						}
					}
				}
				finally
				{
					resource.readLock().unlock();	//always release the read lock
				}
			}
			if(isResourceChildrenIncluded())	//if child resources are included
			{
				if(resource instanceof URFCollectionResource)	//if this is collection resource
				{
					if(!((URFCollectionResource<?>)resource).isEmpty())	//if this collection resource has children
					{
						return false;	//this is not a leaf
					}
				}
			}
		}
		return true;	//we couldn't find included properties or children, so this is a leaf
	}

	/**Dynamically determines children.
	This version ignores URF type properties and properties of an {@link URFCollectionResource}.
	@return The dynamically loaded list of children.
	*/
	protected List<TreeNodeModel<?>> determineChildren()
	{
		final List<TreeNodeModel<?>> children=new ArrayList<TreeNodeModel<?>>();	//create a list to hold the children, even if we don't have any here, for child classes may add to this list
		final URFResource resource=getValue();	//get the resource
		if(resource!=null)	//if we have a resource
		{
			resource.readLock().lock();	//get a read lock
			try
			{
				if(isResourcePropertiesIncluded() && !(resource instanceof URFCollectionResource))	//if resource properties are included (don't show properties of an URF collection resource)
				{
					for(final URFProperty property:resource.getProperties())	//look at all properties
					{
						final URI propertyURI=property.getPropertyURI();	//get the URI of this property
						if(!TYPE_PROPERTY_URI.equals(propertyURI))	//if this is not a type property
						{
							final URFResourceDynamicTreeNodeModel<?> treeNode=createURFResourceTreeNode(propertyURI, property.getValue());	//create a tree node for this property and value
							children.add(treeNode);	//add the tree node to our list
						}
					}
				}
				if(isResourceChildrenIncluded())	//if child resources are included
				{
					if(resource instanceof URFCollectionResource)	//if this is a collection resource
					{
						for(final URFResource element:(URFCollectionResource<?>)resource)	//for each child resource
						{
							final URFResourceDynamicTreeNodeModel<?> treeNode=createURFResourceTreeNode(null, element);	//create a tree node for this element, without indicating a property
							children.add(treeNode);	//add the tree node to our list
						}
					}
				}
			}
			finally
			{
				resource.readLock().unlock();	//always release the read lock
			}
		}
		return children;	//return the children we determined
	}

}
