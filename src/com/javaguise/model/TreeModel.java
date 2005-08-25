package com.javaguise.model;

import com.javaguise.event.*;
import static com.garretwilson.lang.ClassUtilities.*;

/**A model for a tree of nodes.
@author Garret Wilson
*/
public interface TreeModel extends ControlModel
{

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

}
