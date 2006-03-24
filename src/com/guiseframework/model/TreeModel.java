package com.guiseframework.model;

import static com.garretwilson.lang.ClassUtilities.*;

import com.guiseframework.event.TreeNodePropertyChangeListener;

/**A model for a tree of nodes.
@author Garret Wilson
*/
public interface TreeModel extends Model
{

	/**The bound property of the root node.*/
	public final static String ROOT_NODE_PROPERTY=getPropertyName(TreeModel.class, "rootNode");

	/**@return The root node of the tree model.*/
	public TreeNodeModel<?> getRootNode();

	/**Adds an action listener.
	@param actionListener The action listener to add.
	*/
//TODO bring back maybe	public void addActionListener(final ActionListener<TreeModel> actionListener);

	/**Removes an action listener.
	@param actionListener The action listener to remove.
	*/
//TODO bring back maybe	public void removeActionListener(final ActionListener<TreeModel> actionListener);

	/**Adds a tree node property change listener.
	@param treeNodePropertyChangeListener The tree node property change listener to add.
	*/
	public void addTreeNodePropertyChangeListener(final TreeNodePropertyChangeListener<?> treeNodePropertyChangeListener);

	/**Removes a tree node property change listener.
	@param treeNodePropertyChangeListener The tree node property change listener to remove.
	*/
	public void removeTreeNodePropertyChangeListener(final TreeNodePropertyChangeListener<?> treeNodePropertyChangeListener);

}
