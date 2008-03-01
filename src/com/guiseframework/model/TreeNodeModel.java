package com.guiseframework.model;

import static com.globalmentor.java.Classes.*;

import com.globalmentor.beans.TargetedEvent;

/**A node in a tree model.
Property change events and action events on one tree node will be bubbled up the hierarchy, with the tree node initiating the event accessible via {@link TargetedEvent#getTarget()}.
@author Garret Wilson
@param <V> The type of value contained in the tree node.
*/
public interface TreeNodeModel<V> extends ActionModel, ValueModel<V>, Selectable, Iterable<TreeNodeModel<?>>
{

	/**The expanded bound property.*/
	public final static String EXPANDED_PROPERTY=getPropertyName(TreeNodeModel.class, "expanded");

	/**@return Whether the node is expanded, showing its children, if any.*/
	public boolean isExpanded();

	/**Sets whether the node is expanded, showing its children, if any.
	This is a bound property of type <code>Boolean</code>.
	@param newExpanded <code>true</code> if the node is expanded.
	@see #EXPANDED_PROPERTY
	*/
	public void setExpanded(final boolean newExpanded);

	/**Sets whether all tree nodes, including this node, are expanded in this subtree.
	@param newAllExpanded <code>true</code> if all the nodes in this tree should be expanded, or <code>false</code> if they should be collapsed.
	@see #setExpanded(boolean)
	*/
	public void setAllExpanded(final boolean newAllExpanded);
	
	/**Determines whether this node could be considered a leaf node.
	This method may return <code>false</code> even if it currently has no children, if it intends to load them later and there is no way to know ahead of time if there will be children.
	@return <code>true</code> if this is a leaf node, else <code>false</code> if this node should not be considered a leaf.
	*/
	public boolean isLeaf();

	/**@return Whether this tree node has children. This implementation delegates to the tree node list.*/
	public boolean hasChildren();

	/**Determines whether this tree node contains the given child tree node.
	@param treeNode The tree node to check.
	@return <code>true</code> if this tree node contains the given tree node.
	*/
	public boolean hasChild(final TreeNodeModel<?> treeNode);

	/**Adds a child tree node to this tree node.
	@param treeNode The tree node to add.
	@exception IllegalArgumentException if the tree node already has a parent.
	*/
	public void add(final TreeNodeModel<?> treeNode);

	/**Removes a child tree node from this tree node.
	@param treeNode The child tree node to remove.
	@exception IllegalArgumentException if the tree node is not a child of this tree node.
	*/
	public void remove(final TreeNodeModel<?> treeNode);

	/**Removes all of the child tree nodes from this tree node.*/
	public void clear();

	/**@return The parent of this node, or <code>null</code> if this node has no parent.*/
	public TreeNodeModel<?> getParent();

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
	public void setParent(final TreeNodeModel<?> newParent);

	/**Determines the tree model in which this tree node is located.
	@return The tree model in which this tree node is located, or <code>null</code> if this tree node is not in a tree model.
	*/
//TODO fix	public TreeModel getTreeModel();

	/**Returns the zero-based depth of the node within in its tree.
	This result represents the number of levels above this node needed to reach the root node.
	@return The zero-based depth of this node from the root.
	*/
	public int getDepth();

}
