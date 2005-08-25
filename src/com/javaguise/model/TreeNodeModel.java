package com.javaguise.model;

/**A node in a tree model.
@author Garret Wilson
@param <V> The type of value contained in the tree node.
*/
public interface TreeNodeModel<V> extends ValueModel<V>, Iterable<TreeNodeModel<?>>
{

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

}
