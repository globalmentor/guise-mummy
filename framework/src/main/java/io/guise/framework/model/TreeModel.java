/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.model;

import static com.globalmentor.java.Classes.*;

/**
 * A model for a tree of nodes.
 * @author Garret Wilson
 */
public interface TreeModel extends ActionModel {

	/** The bound property of the root node. */
	public static final String ROOT_NODE_PROPERTY = getPropertyName(TreeModel.class, "rootNode");

	/** @return The root node of the tree model. */
	public TreeNodeModel<?> getRootNode();

	/**
	 * Sets the root node of the tree model. This is a bound property.
	 * @param newRootNode The new root node of the tree model.
	 * @throws NullPointerException if the given root node is <code>null</code>.
	 * @see #ROOT_NODE_PROPERTY
	 */
	public void setRootNode(final TreeNodeModel<?> newRootNode);

	/**
	 * Adds an action listener.
	 * @param actionListener The action listener to add.
	 */
	//TODO bring back maybe	public void addActionListener(final ActionListener<TreeModel> actionListener);

	/**
	 * Removes an action listener.
	 * @param actionListener The action listener to remove.
	 */
	//TODO bring back maybe	public void removeActionListener(final ActionListener<TreeModel> actionListener);

	/**
	 * Sets whether all tree nodes are expanded. This method delegates to the root node {@link TreeNodeModel#setAllExpanded(boolean)}.
	 * @param newAllExpanded <code>true</code> if all the nodes should be expanded, or <code>false</code> if they should be collapsed.
	 */
	public void setAllExpanded(final boolean newAllExpanded);

	/**
	 * Adds a tree node property change listener.
	 * @param treeNodePropertyChangeListener The tree node property change listener to add.
	 */
	//TODO del	public void addTreeNodePropertyChangeListener(final TreeNodePropertyChangeListener<?> treeNodePropertyChangeListener);

	/**
	 * Removes a tree node property change listener.
	 * @param treeNodePropertyChangeListener The tree node property change listener to remove.
	 */
	//TODO del	public void removeTreeNodePropertyChangeListener(final TreeNodePropertyChangeListener<?> treeNodePropertyChangeListener);

}
