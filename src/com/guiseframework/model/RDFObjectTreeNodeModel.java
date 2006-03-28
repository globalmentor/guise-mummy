package com.guiseframework.model;

import com.garretwilson.rdf.*;

/**A tree node model that represents an object described in RDF.
@param <V> The type of value contained in the tree node.
@author Garret Wilson
*/
public interface RDFObjectTreeNodeModel<V extends RDFObject> extends TreeNodeModel<V>
{

	/**@return The RDF property of which this RDF object is an object, or <code>null</code> if this object should not be considered the object of any property.*/
	public RDFResource getProperty();
}
