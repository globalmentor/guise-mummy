package com.guiseframework.model;

import java.io.IOException;
import java.util.Iterator;

import com.garretwilson.util.Debug;
import com.guiseframework.GuiseSession;

/**A node in a tree model that can dynamically load its children when needed.
<p>Child classes must override {@link #loadChildNodes()}.</p>
<p><dfn>Predictive</dfn> dynamic tree nodes know before loading child nodes whether and how many child nodes there will be once children are loaded.</p>
<p>A predictive dynamic tree node it must override {@link #hasChildren()} and return whether there would be children were children loaded.</p>
<p>Property change events on one tree node will be bubbled up the hierarchy, with the source indicating the tree node on which the property change occurred.</p>
@param <V> The type of value contained in the tree node.
@author Garret Wilson
*/
public abstract class DynamicTreeNodeModel<V> extends DefaultTreeNodeModel<V>	//TODO update all the caches when the resource value changes
{

	/**Whether this tree node is predictive, knowing before it loads its child nodes whether and how many child nodes there will be.*/
	private final boolean predictive;

		/**@return Whether this tree node is predictive, knowing before it loads its child nodes whether and how many child nodes there will be.*/
		protected final boolean isPredictive() {return predictive;}

	/**Whether the child nodes have been loaded.*/
	private boolean childNodesLoaded=false;

		/**@return Whether the child nodes have been loaded.*/
		public boolean isChildNodesLoaded() {return childNodesLoaded;}

	/**Sets whether the node is expanded, showing its children, if any.
	This version ensures that child nodes are loaded or unloaded before expansion or collapsing occurs.
	This is a bound property of type <code>Boolean</code>.
	@param newExpanded <code>true</code> if the node is expanded
	@see #EXPANDED_PROPERTY
	*/
	public void setExpanded(final boolean newExpanded)
	{
		if(newExpanded!=isExpanded());	//if the expansion state is changing
		{
			if(newExpanded)	//if the tree node is expanding
			{
				ensureChildNodesLoaded();	//make sure children have been loaded				
			}
			else	//if the tree node is collapsing
			{
				unloadChildNodes();	//unload child nodes, if any
			}
		}
		super.setExpanded(newExpanded);	//expand or collapse normally
	}

	/**Retrieves an iterator to contained tree nodes.
	@return An iterator to contained tree nodes.
	@see #ensureChildNodesLoaded()
	*/
	public Iterator<TreeNodeModel<?>> iterator()
	{
//TODO del; we shouldn't load children unless actually requested to, such as when opening		ensureChildNodesLoaded();	//make sure children have been loaded
		return super.iterator();	//return the iterator to child nodes
	}

	/**Determines whether this node could be considered a leaf node.
	This implementation <code>true</code> if child nodes are not loaded and this is not a predictive dynamic tree node.
	@return <code>true</code> if this is a leaf node, else <code>false</code> if this node should not be considered a leaf.
	*/
	public boolean isLeaf()
	{
		if(!isChildNodesLoaded() && !isPredictive())	//if the child nodes are not yet loaded and this is not a predictive dynamic tree node
		{
			return true;	//there's no way to know whether there are children, so assume this is not a leaf
		}
		return super.isLeaf();	//if this is a predictive tree node, or the children are loaded, return the default leaf determination
	}

	/**Determines whether this tree node has children.
	If this is not a predictive tree node, this implementation first ensures that child nodes are loaded.
	A predictive dynamic tree node must override this method and return whether there would be children were children loaded.
	@return Whether this tree node has children.
	@exception IllegalStateException if child nodes are not loaded and this is a predictive dynamic tree node that has not overridden this method to return whether there would be child nodes were children loaded.
	*/
	public boolean hasChildren()
	{
		if(isPredictive())	//if this is a predictive dynamic tree node
		{
			if(!isChildNodesLoaded())	//if child nodes are not loaded, the subclass should have overridden this method and return the correct value
			{
				throw new IllegalStateException("Predictive dynamic tree nodes must override hasChildren() and, if children are not loaded, return whether there would be child nodes were the children loaded.");
			}
		}
		if(!isPredictive())	//if this is not a predictive dynamic tree node TODO fix; this will probably result in recursive reloading, until all nodes are loaded, negating the purpose of dynamic tree nodes
		{
			ensureChildNodesLoaded();	//make sure child nodes are loaded
		}
		return super.hasChildren();	//return whether there are children
	}

	/**Constructs a tree node model indicating the type of value it can hold.
	@param session The Guise session that owns this model.
	@param valueClass The class indicating the type of value held in the model.
	@param predictive Whether this tree node is predictive, knowing before it loads its child nodes whether and how many child nodes there will be.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	*/
	public DynamicTreeNodeModel(final GuiseSession session, final Class<V> valueClass, final boolean predictive)
	{
		this(session, valueClass, predictive, null);	//construct the class with a null initial value
	}

	/**Constructs a tree node model indicating the type of value it can hold, along with an initial value.
	@param session The Guise session that owns this model.
	@param valueClass The class indicating the type of value held in the model.
	@param predictive Whether this tree node is predictive, knowing before it loads its child nodes whether and how many child nodes there will be.
	@param initialValue The initial value, which will not be validated.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	*/
	public DynamicTreeNodeModel(final GuiseSession session, final Class<V> valueClass, final boolean predictive, final V initialValue)
	{
		super(session, valueClass, initialValue);	//construct the parent class
		this.predictive=predictive;
	}

	/**Loads children if they haven't already been loaded.
	If there is an error loading the child nodes, the load error variable will be set.
	@see #loadChildNodes()
	*/
	public void ensureChildNodesLoaded()
	{
//TODO del Debug.trace("ensuring child nodes loaded for resource", getValue(), "isChildNodesLoaded()", isChildNodesLoaded());
		if(!isChildNodesLoaded()) //if the children are not yet loaded
		{
			childNodesLoaded=true;  //show that we've loaded the child nodes (this is done before the actual loading so that future calls to getChildCount() won't cause reloading)
			clear();	//make sure we've removed all children before trying to load the children
			try
			{
				loadChildNodes(); //load the child nodes
			}
			catch(final IOException ioException)	//if there was an error loading the child loads
			{
//TODO fix				loadError=ioException;	//save the load error
//TODO fix				SwingApplication.displayApplicationError(null, ioException);	//display the error
			}
		}
	}  

	/**Dynamically loads child nodes when needed. Must be overridden to appropriately load children.
	@exception IOException if there is an error loading the child nodes.
	*/
	protected abstract void loadChildNodes() throws IOException;
	
	/**Unloads all child nodes and sets the state to unloaded.
	Any error condition is reset. 
	*/
	public void unloadChildNodes()
	{
		clear();	//remove all the children
		childNodesLoaded=false;	//show that we have no nodes loaded
//TODO fix		setLoadError(null);	//show that there is no load error.
	}

}
