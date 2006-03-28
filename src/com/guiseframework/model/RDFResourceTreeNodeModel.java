package com.guiseframework.model;

import java.io.IOException;
import java.util.Iterator;

import com.garretwilson.rdf.*;
import com.guiseframework.GuiseSession;

/**A tree node model that represents an RDF resource.
<p>Any properties will be dynamically loaded</p>
<p>This class has special support for RDF lists, the contents of which are by default displayed as children of the given resource.</p>
@author Garret Wilson
*/
public class RDFResourceTreeNodeModel extends DynamicTreeNodeModel<RDFResource> implements RDFObjectTreeNodeModel<RDFResource>
{

	/**The RDF property of which this RDF object is an object, or <code>null</code> if this object should not be considered the object of any property.*/
	private final RDFResource property;

		/**@return The RDF property of which this RDF object is an object, or <code>null</code> if this object should not be considered the object of any property.*/
		public RDFResource getProperty() {return property;}

	/**Determines whether this tree node has children.
	@return Whether this tree node has children.
	*/
	public boolean hasChildren()	//TODO compensate for type properties
	{
		if(isChildNodesLoaded())	//if child nodes are loaded
		{
			return super.hasChildren();	//return the default determination of child nodes
		}
		else	//if child nodes are not loaded
		{
			return getValue().getPropertyCount()>0;	//there are children if there are resource properties
		}
	}

	/**Session constructor with no initial value.
	@param session The Guise session that owns this model.
	@param rdfProperty The property of which this object is a resource, or <code>null</code> if this object should not be considered the object of any property.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public RDFResourceTreeNodeModel(final GuiseSession session, final RDFResource rdfProperty)
	{
		this(session, rdfProperty, null);	//construct the class with a null initial value
	}

	/**Session and initial value constructor.
	@param session The Guise session that owns this model.
	@param rdfProperty The property of which this object is a resource, or <code>null</code> if this object should not be considered the object of any property.
	@param initialValue The initial value, which will not be validated.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public RDFResourceTreeNodeModel(final GuiseSession session, final RDFResource rdfProperty, final RDFResource initialValue)
	{
		super(session, RDFResource.class, true, initialValue);	//construct the parent class
		property=rdfProperty; //save the property of which this resource is the object
	}

	/**Dynamically loads child nodes for all properties.
	@exception IOException if there is an error loading the child nodes.
	*/
	protected void loadChildNodes() throws IOException
	{
		final RDFResource rdfResource=getValue();	//get the value contained in the node		
//G***del when works			if(RDFUtilities.isType(resource, RDFConstants.RDF_NAMESPACE_URI, RDFConstants.LIST_TYPE_NAME))	//if this is a list
		if(rdfResource instanceof RDFListResource)	//if this is a list
		{
			for(final RDFResource childRDFResource:(RDFListResource)rdfResource)	//for each child resource
			{
				loadChildResource(null, childRDFResource);	//load the child resource without indicating a property
			}
		}
		else	//if this is a non-list resource
		{
			final Iterator<RDFPropertyValuePair> propertyIterator=rdfResource.getPropertyIterator();  //get an iterator to all properties
			while(propertyIterator.hasNext()) //while there are more properties
			{
				final RDFPropertyValuePair propertyValuePair=propertyIterator.next(); //get the next property/value pair
				final RDFResource property=propertyValuePair.getProperty();  //get the property resource
				final RDFObject value=propertyValuePair.getPropertyValue();  //get the property value
				if(value instanceof RDFLiteral)	//if the value is a literal
				{
					loadChildLiteral(property, (RDFLiteral)value);	//load the literal property-value pair					
				}
				else if(value instanceof RDFResource)	//if the value is a resource
				{
					loadChildResource(property, (RDFResource)value);	//load the resource property-value pair					
				}
				else	//if we don't recognize the RDF object type
				{
					throw new AssertionError("Unrecognized RDF object type: "+value.getClass());
				}
			}
		}
	}

	/**Loads a child node to represent a property object literal and optional property.
	@param rdfProperty The property of which the object is a resource, or <code>null</code> if this object should not be considered the object of any property.
	@param rdfLiteral The literal to represent in the new node.
	*/ 
	protected void loadChildLiteral(final RDFResource rdfProperty, final RDFLiteral rdfLiteral)
	{
			//create a new tree node to represent the property and value
		final RDFLiteralTreeNodeModel rdfPropertyNode=new RDFLiteralTreeNodeModel(getSession(), rdfProperty, rdfLiteral);
		add(rdfPropertyNode); //add the property node to this resource node		
	}

	/**Loads a child node to represent a property object resource and optional property.
	@param rdfProperty The property of which the object is a resource, or <code>null</code> if this object should not be considered the object of any property.
	@param rdfResource The resource to represent in the new node.
	*/ 
	protected void loadChildResource(final RDFResource rdfProperty, final RDFResource rdfResource)
	{
			//create a new tree node to represent the property and value
		final RDFResourceTreeNodeModel rdfPropertyNode=new RDFResourceTreeNodeModel(getSession(), rdfProperty, rdfResource);
		add(rdfPropertyNode); //add the property node to this resource node		
	}

}
