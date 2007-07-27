package com.guiseframework.model.rdf;

import static com.garretwilson.lang.ClassUtilities.*;

import java.util.*;
import static java.util.Collections.*;

import com.garretwilson.rdf.*;
import com.garretwilson.util.Debug;
import com.guiseframework.model.*;

import static com.garretwilson.rdf.RDFConstants.*;

/**Abstract functionality for a tree node model that represents an RDF resource.
<p>Any properties will be dynamically loaded</p>
<p>This class has special support for RDF lists, the contents of which are by default displayed as children of the given resource.</p>
@param <V> The type of value contained in the tree node.
@author Garret Wilson
*/
public abstract class AbstractRDFResourceTreeNodeModel<V extends RDFResource> extends DynamicTreeNodeModel<V> implements RDFObjectTreeNodeModel<V>
{

	/**The bound property of whether resource children are included in the node.*/
	public final static String RESOURCE_CHILDREN_INCLUDED_PROPERTY=getPropertyName(AbstractRDFResourceTreeNodeModel.class, "resourceChildrenIncluded");
	/**The bound property of whether resource properties are included in the node.*/
	public final static String RESOURCE_PROPERTIES_INCLUDED_PROPERTY=getPropertyName(AbstractRDFResourceTreeNodeModel.class, "resourcePropertiesIncluded");
	
	/**The RDF property of which this RDF object is an object, or <code>null</code> if this object should not be considered the object of any property.*/
	private final RDFResource property;

		/**@return The RDF property of which this RDF object is an object, or <code>null</code> if this object should not be considered the object of any property.*/
		public RDFResource getProperty() {return property;}

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
	public AbstractRDFResourceTreeNodeModel(final Class<V> valueClass)
	{
		this(valueClass, null);	//construct the class with no initial value
	}

	/**Initial value constructor.
	@param valueClass The class indicating the type of value held in the model.
	@param initialValue The initial value, which will not be validated.
	*/
	public AbstractRDFResourceTreeNodeModel(final Class<V> valueClass, final V initialValue)
	{
		this(valueClass, null, initialValue);	//construct the class with no property
	}

	/**Property and initial value constructor.
	@param valueClass The class indicating the type of value held in the model.
	@param rdfProperty The property of which this object is a resource, or <code>null</code> if this object should not be considered the object of any property.
	@param initialValue The initial value, which will not be validated.
	*/
	public AbstractRDFResourceTreeNodeModel(final Class<V> valueClass, final RDFResource rdfProperty, final V initialValue)
	{
		super(valueClass, initialValue);	//construct the parent class
		property=rdfProperty; //save the property of which this resource is the object
	}

	/**Dynamically determines whether this node is a leaf.
	This version determines if resource properties are included and there are properties, or if resource children are included and there are children.
	This version ignores RDF type properties and properties of an {@link RDFListResource}. 
	@return Whether this node should be considered a leaf with no children.
	*/
	protected boolean determineLeaf()
	{
		final RDFResource rdfResource=getValue();	//get the resource
		if(rdfResource!=null)	//if we have a resource
		{
			if(isResourcePropertiesIncluded() && !(rdfResource instanceof RDFListResource))	//if resource properties are included (ignore properties of an RDF list resource, because they will not be shown)
			{
				for(final RDFPropertyValuePair rdfPropertyValuePair:rdfResource.getProperties())	//look at all properties
				{
					if(!TYPE_PROPERTY_REFERENCE_URI.equals(rdfPropertyValuePair.getName().getReferenceURI()))	//if this is not a type property
					{
						return false;	//this is not a leaf
					}
				}
			}
			if(isResourceChildrenIncluded())	//if child resources are included
			{
				if(rdfResource instanceof RDFListResource)	//if this is a list resource
				{
					if(!((RDFListResource)rdfResource).isEmpty())	//if this list resource has children
					{
						return false;	//this is not a leaf
					}
				}
			}
		}
		return true;	//we couldn't find included properties or children, so this is a leaf 
	}

	/**Dynamically determines children.
	This version ignores RDF type properties and properties of an {@link RDFListResource}. 
	@return The dynamically loaded list of children.
	*/
	protected List<TreeNodeModel<?>> determineChildren()
	{
		final List<TreeNodeModel<?>> children=new ArrayList<TreeNodeModel<?>>();	//create a list to hold the children, even if we don't have any here, for child classes may add to this list
		final RDFResource rdfResource=getValue();	//get the resource
		if(rdfResource!=null)	//if we have a resource
		{
			if(isResourcePropertiesIncluded() && !(rdfResource instanceof RDFListResource))	//if resource properties are included (don't show properties of an RDF list resource
			{
				for(final RDFPropertyValuePair rdfPropertyValuePair:rdfResource.getProperties())	//look at all properties
				{
					final RDFResource property=rdfPropertyValuePair.getProperty();  //get the property resource
					if(!TYPE_PROPERTY_REFERENCE_URI.equals(property.getReferenceURI()))	//if this is not a type property
					{
						final RDFObjectTreeNodeModel<?> treeNode;	//we'll determine the tree node to use for this object
						final RDFObject value=rdfPropertyValuePair.getPropertyValue();  //get the property value
						if(value instanceof RDFLiteral)	//if the value is a literal
						{
							treeNode=createRDFLiteralTreeNode(property, (RDFLiteral)value);	//create a tree node from the literal property-value pair
						}
						else if(value instanceof RDFResource)	//if the value is a resource
						{
							treeNode=createRDFResourceTreeNode(property, (RDFResource)value);	//create a tree node from the resource property-value pair
						}
						else	//if we don't recognize the RDF object type
						{
							throw new AssertionError("Unrecognized RDF object type: "+value.getClass());
						}
						children.add(treeNode);	//add the tree node to our list
					}
				}
			}
			if(isResourceChildrenIncluded())	//if child resources are included
			{
				if(rdfResource instanceof RDFListResource)	//if this is a list resource
				{
					for(final RDFObject value:(RDFListResource)rdfResource)	//for each child resource
					{
						final RDFObjectTreeNodeModel<?> treeNode;	//we'll determine the tree node to use for this object
						if(value instanceof RDFLiteral)	//if the value is a literal
						{
							treeNode=createRDFLiteralTreeNode(null, (RDFLiteral)value);	//create a tree node from the literal without indicating a property
						}
						else if(value instanceof RDFResource)	//if the value is a resource
						{
							treeNode=createRDFResourceTreeNode(null, (RDFResource)value);	//create a tree node from the resource without indicating a property
						}
						else	//if we don't recognize the RDF object type
						{
							throw new AssertionError("Unrecognized RDF object type: "+value.getClass());
						}
						children.add(treeNode);	//add the tree node to our list
					}
				}
			}
		}
		return children;	//return the children we determined
	}
	
	/**Creates a child node to represent a property object literal and optional property.
	This version returns an {@link RDFLiteralTreeNodeModel}.
	@param rdfProperty The property of which the object is a resource, or <code>null</code> if this object should not be considered the object of any property.
	@param rdfLiteral The literal to represent in the new node.
	@return A child node to represent the given property object literal.
	*/ 
	protected RDFLiteralTreeNodeModel createRDFLiteralTreeNode(final RDFResource rdfProperty, final RDFLiteral rdfLiteral)
	{
		return new RDFLiteralTreeNodeModel(rdfProperty, rdfLiteral);	//create a new tree node to represent the property and value
	}

	/**Creates a child node to represent a property object resource and optional property.
	This version returns an {@link RDFResourceTreeNodeModel}.
	@param rdfProperty The property of which the object is a resource, or <code>null</code> if this object should not be considered the object of any property.
	@param rdfResource The resource to represent in the new node.
	@return A child node to represent the given property object resource.
	*/ 
	protected AbstractRDFResourceTreeNodeModel createRDFResourceTreeNode(final RDFResource rdfProperty, final RDFResource rdfResource)
	{			
		return new RDFResourceTreeNodeModel(rdfProperty, rdfResource);	//create a new tree node to represent the property and value
	}

}
