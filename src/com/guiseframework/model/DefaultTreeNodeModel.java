package com.guiseframework.model;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**A default node in a tree model.
Property change events on one tree node will be bubbled up the hierarchy, with the source indicating the tree node on which the property change occurred.
@author Garret Wilson
@param <V> The type of value contained in the tree node.
*/
public class DefaultTreeNodeModel<V> extends DefaultValueModel<V> implements TreeNodeModel<V>
{

	/**Whether the node is expanded, showing its children, if any.*/
	private boolean expanded=false;

		/**@return Whether the node is expanded, showing its children, if any.*/
		public boolean isExpanded() {return expanded;}

		/**Sets whether the node is expanded, showing its children, if any.
		This is a bound property of type <code>Boolean</code>.
		@param newExpanded <code>true</code> if the node is expanded
		@see #EXPANDED_PROPERTY
		*/
		public void setExpanded(final boolean newExpanded)
		{
			if(expanded!=newExpanded)	//if the value is really changing
			{
				final boolean oldExpanded=expanded;	//get the old value
				expanded=newExpanded;	//actually change the value
				firePropertyChange(EXPANDED_PROPERTY, Boolean.valueOf(oldExpanded), Boolean.valueOf(newExpanded));	//indicate that the value changed
			}			
		}

	/**The list of child tree nodes.*/ 
	private final List<TreeNodeModel<?>> treeNodeList=new CopyOnWriteArrayList<TreeNodeModel<?>>();

		/**@return The list of child tree nodes.*/ 
		protected List<TreeNodeModel<?>> getTreeNodeList() {return treeNodeList;}

	/**@return An iterator to contained tree nodes.*/
	public Iterator<TreeNodeModel<?>> iterator() {return treeNodeList.iterator();}

	/**Determines whether this node could be considered a leaf node.
	This method may return <code>false</code> even if it currently has no children, if it intends to load them later and there is no way to know ahead of time if there will be children.
	This implementation returns the opposite value of {@link #hasChildren()}.
	@return <code>true</code> if this is a leaf node, else <code>false</code> if this node should not be considered a leaf.
	*/
	public boolean isLeaf() {return !hasChildren();}

	/**@return Whether this tree node has children. This implementation delegates to the tree node list.*/
	public boolean hasChildren() {return !treeNodeList.isEmpty();}

	/**Determines whether this tree node contains the given child tree node.
	@param treeNode The tree node to check.
	@return <code>true</code> if this tree node contains the given tree node.
	*/
	public boolean hasChild(final TreeNodeModel<?> treeNode) {return treeNodeList.contains(treeNode);}

	/**Adds a child tree node to this tree node.
	@param treeNode The tree node to add.
	@exception IllegalArgumentException if the tree node already has a parent.
	*/
	public void add(final TreeNodeModel<?> treeNode)
	{
		if(treeNode.getParent()!=null)	//if this tree node has already been added to tree node
		{
			throw new IllegalArgumentException("Tree node "+treeNode+" is already a child of a tree node, "+treeNode.getParent()+".");
		}
		treeNodeList.add(treeNode);	//add the tree node to the list
		treeNode.setParent(this);	//tell the tree node who its parent is
		treeNode.addPropertyChangeListener(getForwardPropertyChangeListener());	//listen for property changes and bubble them up the hierarchy
	}

	/**Removes a child tree node from this tree node.
	@param treeNode The child tree node to remove.
	@exception IllegalArgumentException if the tree node is not a child of this tree node.
	*/
	public void remove(final TreeNodeModel<?> treeNode)
	{
		if(treeNode.getParent()!=this)	//if the tree node is not a child of this tree node
		{
			throw new IllegalArgumentException("Tree node "+treeNode+" is not child of tree node "+this+".");
		}
		treeNode.removePropertyChangeListener(getForwardPropertyChangeListener());	//stop listen for property changes to bubble up the hierarchy
		treeNodeList.remove(treeNode);	//remove the tree node to the list
		treeNode.setParent(null);	//tell the tree node it no longer has a parent
	}

	/**Removes all of the child tree nodes from this tree node.*/
	public void clear()
	{
		for(final TreeNodeModel<?> treeNode:this)	//for each child tree node
		{
			remove(treeNode);	//remove this component
		}
	}

	/**The parent of this node, or <code>null</code> if this node has no parent.*/
	private TreeNodeModel<?> parent;

		/**@return The parent of this node, or <code>null</code> if this node has no parent.*/
		public TreeNodeModel<?> getParent() {return parent;}

		/**Sets the parent of this tree node.
		This method is managed by other tree nodes, and normally should not be called by applications.
		A tree node cannot be given a parent if it already has a parent.
		A tree node's parent cannot be removed this component is still a child of that parent.
		A tree node's parent cannot be set unless that parent already recognizes this tree node as one of its children.
		If a tree node is given the same parent it already has, no action occurs.
		@param newParent The new parent for this tree node, or <code>null</code> if this tree node is being removed from a parent.
		@exception IllegalStateException if a parent is provided and this tree node already has a parent.
		@exception IllegalStateException if no parent is provided and this tree node's old parent still recognizes this tree node as its child.
		@exception IllegalArgumentException if a parent is provided and the given parent does not already recognize this tree node as its child.
		@see #add(TreeNodeModel)
		@see #remove(TreeNodeModel)
		*/
		public void setParent(final TreeNodeModel<?> newParent)
		{
			final TreeNodeModel<?> oldParent=parent;	//get the old parent
			if(oldParent!=newParent)	//if the parent is really changing
			{
				if(newParent!=null)	//if a parent is provided
				{
					if(oldParent!=null)	//if we already have a parent
					{
						throw new IllegalStateException("Tree node "+this+" already has parent: "+oldParent);
					}
					if(!newParent.hasChild(this))	//if the new parent is not really our parent
					{
						throw new IllegalArgumentException("Provided parent "+newParent+" is not really parent of tree node "+this);
					}
				}
				else	//if no parent is provided
				{
					if(oldParent.hasChild(this))	//if we had a parent before, and that parent still thinks this tree node is its child
					{
						throw new IllegalStateException("Old parent "+oldParent+" still thinks this tree node, "+this+", is a child."); 
					}
				}
				parent=newParent;	//this is really our parent; make a note of it
			}
		}

	/**Determines the tree model in which this tree node is located.
	This implementation delegates to the parent tree node, if available.
	@return The tree model in which this tree node is located, or <code>null</code> if this tree node is not in a tree model.
	*/
/*TODO fix
	public TreeModel getTreeModel()
	{
		final TreeNodeModel<?> parent=getParent();	//get the parent tree node
		return parent!=null ? parent.getTreeModel() : null;	//delegate to the parent, if there is one
	}	
*/

	/**Constructs a tree node model indicating the type of value it can hold.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public DefaultTreeNodeModel(final Class<V> valueClass)
	{
		this(valueClass, null);	//construct the class with a null initial value
	}

	/**Constructs a tree node model indicating the type of value it can hold, along with an initial value.
	@param valueClass The class indicating the type of value held in the model.
	@param initialValue The initial value, which will not be validated.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public DefaultTreeNodeModel(final Class<V> valueClass, final V initialValue)
	{
		super(valueClass, initialValue);	//construct the parent class
//TODO del or move		setEditable(false);	//default to not being editable
	}

	/**Returns a list of children.
	This method along with {@link #setChildren()} provides a <code>children</code> property for alternate children access.
	@return A list of tree node children in order.
	@see #iterator()
	*/
	public List<TreeNodeModel<?>> getChildren()
	{
		return new ArrayList<TreeNodeModel<?>>(getTreeNodeList());	//create and return a copy of the list
	}

	/**Sets the children in this container.
	This method along with {@link #getChildren()} provides a <code>children</code> property for alternate children access.
	@param treeNodes The new children of this tree node in order.
	@see #clear()
	@see #add(TreeNodeModel)
	*/
	public void setChildren(final List<TreeNodeModel<?>> treeNodes)
	{
		clear();	//remove all children from the tree node
		for(final TreeNodeModel<?> treeNode:treeNodes)	//for each child
		{
			add(treeNode);	//add this child
		}
	}
	
	/**Returns the zero-based depth of the node within in its tree.
	This result represents the number of levels above this node needed to reach the root node.
	@return The zero-based depth of this node from the root.
	*/
	public int getDepth()
	{
		final TreeNodeModel<?> parentNode=getParent();	//get the parent node
		return parentNode!=null ? parentNode.getDepth()+1 : 0;	//if there is a parent node, this node's depth is one more than the parent's; otherwise, this is the root node with depth zero
	}
}
