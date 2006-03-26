package com.guiseframework.model;

import java.io.IOException;
import java.util.Iterator;

import com.garretwilson.rdf.*;
import com.guiseframework.GuiseSession;

/**A tree node model that represents an object described in RDF.
<p>The object can be either a literal or a resource; if a resource, any properties will be dynamically loaded</p>
<p>The RDF object is stored as the value of the tree node model.</p>
<p>This class has special support for RDF lists, the contents of which are by default displayed as children of the given resource.</p>
@author Garret Wilson
*/
public class RDFObjectTreeNodeModel extends DynamicTreeNodeModel<RDFObject>
{

	/**The RDF property of which this resource is an object, or <code>null</code> if this object should not be considered the object of any property.*/
	private final RDFResource property;

		/**@return The RDF property of which this resource is an object, or <code>null</code> if this object should not be considered the object of any property.*/
		protected RDFResource getProperty() {return property;}

	/**Determines whether this tree node has children.
	If this is not a predictive tree node, this implementation first ensures that child nodes are loaded.
	A predictive dynamic tree node it must override this method and return whether there would be children were children loaded.
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
			final RDFObject rdfObject=getValue();	//get the value contained in the node
			return rdfObject instanceof RDFResource && ((RDFResource)rdfObject).getPropertyCount()>0;	//there are children if there are resource properties
		}
	}

	/**Session constructor with no initial value.
	@param session The Guise session that owns this model.
	@param rdfProperty The property of which this object is a resource, or <code>null</code> if this object should not be considered the object of any property.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public RDFObjectTreeNodeModel(final GuiseSession session, final RDFResource rdfProperty)
	{
		this(session, rdfProperty, null);	//construct the class with a null initial value
	}

	/**Session and initial value constructor.
	@param session The Guise session that owns this model.
	@param rdfProperty The property of which this object is a resource, or <code>null</code> if this object should not be considered the object of any property.
	@param initialValue The initial value, which will not be validated.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public RDFObjectTreeNodeModel(final GuiseSession session, final RDFResource rdfProperty, final RDFObject initialValue)
	{
		super(session, RDFObject.class, true, initialValue);	//construct the parent class
		property=rdfProperty; //save the property of which this resource is the object
	}


	/**Dynamically loads child nodes for all properties.
	@exception IOException if there is an error loading the child nodes.
	*/
	protected void loadChildNodes() throws IOException
	{
		final RDFObject rdfObject=getValue();	//get the value contained in the node		
		if(rdfObject instanceof RDFResource)  //if we represent an RDF resource
		{
			final RDFResource resource=(RDFResource)rdfObject;	//cast the value to a resource
//G***del when works			if(RDFUtilities.isType(resource, RDFConstants.RDF_NAMESPACE_URI, RDFConstants.LIST_TYPE_NAME))	//if this is a list
			if(resource instanceof RDFListResource)	//if this is a list
			{
				for(final RDFResource childRDFResource:(RDFListResource)resource)	//for each child resource
				{
					loadChildNode(null, childRDFResource);	//load the child resource without indicating a property
				}
			}
			else	//if this is a non-list resource
			{
				final Iterator<RDFPropertyValuePair> propertyIterator=resource.getPropertyIterator();  //get an iterator to all properties
				while(propertyIterator.hasNext()) //while there are more properties
				{
					final RDFPropertyValuePair propertyValuePair=propertyIterator.next(); //get the next property/value pair
					final RDFResource property=propertyValuePair.getProperty();  //get the property resource
					final RDFObject value=propertyValuePair.getPropertyValue();  //get the property value
					loadChildNode(property, value);	//load the property-value pair
				}
			}
		}
	}

	/**Loads a child node to represent a property object and optional property.
	@param rdfProperty The property of which the object is a resource, or <code>null</code> if this object should not be considered the object of any property.
	@param rdfObject The resource to represent in the new node.
	*/ 
	protected void loadChildNode(final RDFResource rdfProperty, final RDFObject rdfObject)
	{
			//create a new tree node to represent the property and value
		final RDFObjectTreeNodeModel rdfPropertyNode=new RDFObjectTreeNodeModel(getSession(), rdfProperty, rdfObject);
		add(rdfPropertyNode); //add the property node to this resource node		
	}

}
