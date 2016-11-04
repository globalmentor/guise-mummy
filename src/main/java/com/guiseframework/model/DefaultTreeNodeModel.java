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

package com.guiseframework.model;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import com.globalmentor.event.TargetedEvent;
import com.guiseframework.event.ActionEvent;
import com.guiseframework.event.ActionListener;

/**
 * A default node in a tree model. Property change events and action events on one tree node will be bubbled up the hierarchy, with the tree node initiating the
 * event accessible via {@link TargetedEvent#getTarget()}.
 * @author Garret Wilson
 * @param <V> The type of value contained in the tree node.
 */
public class DefaultTreeNodeModel<V> extends DefaultValueModel<V> implements TreeNodeModel<V> {

	/** An action listener to repeat copies of events received, using this component as the source. */
	private ActionListener repeatActionListener = new ActionListener() {

		@Override
		public void actionPerformed(final ActionEvent actionEvent) { //if an action was performed
			final ActionEvent repeatActionEvent = new ActionEvent(DefaultTreeNodeModel.this, actionEvent); //copy the action event with this class as its source, keeping the same target
			fireActionPerformed(repeatActionEvent); //fire the repeated action
		}

	};

	/** Whether the node is expanded, showing its children, if any. */
	private boolean expanded = false;

	@Override
	public boolean isExpanded() {
		return expanded;
	}

	@Override
	public void setExpanded(final boolean newExpanded) {
		if(expanded != newExpanded) { //if the value is really changing
			final boolean oldExpanded = expanded; //get the old value
			expanded = newExpanded; //actually change the value
			firePropertyChange(EXPANDED_PROPERTY, Boolean.valueOf(oldExpanded), Boolean.valueOf(newExpanded)); //indicate that the value changed
		}
	}

	@Override
	public void setAllExpanded(final boolean newAllExpanded) {
		setExpanded(newAllExpanded); //set this node to be expanded
		for(final TreeNodeModel<?> childTreeNode : this) { //for each child child tree node
			childTreeNode.setAllExpanded(newAllExpanded); //set this child tree node subtree expanded or contracted
		}
	}

	/** Whether the tree node is selected. */
	private boolean selected = false;

	@Override
	public boolean isSelected() {
		return selected;
	}

	@Override
	public void setSelected(final boolean newSelected) {
		if(selected != newSelected) { //if the value is really changing
			final boolean oldSelected = selected; //get the current value
			selected = newSelected; //update the value
			firePropertyChange(SELECTED_PROPERTY, Boolean.valueOf(oldSelected), Boolean.valueOf(newSelected));
		}
	}

	/** The list of child tree nodes. */
	private final List<TreeNodeModel<?>> treeNodeList = new CopyOnWriteArrayList<TreeNodeModel<?>>();

	/** @return The list of child tree nodes. */
	protected List<TreeNodeModel<?>> getTreeNodeList() {
		return treeNodeList;
	}

	@Override
	public Iterator<TreeNodeModel<?>> iterator() {
		return treeNodeList.iterator();
	}

	@Override
	public boolean isLeaf() {
		return !hasChildren();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation delegates to the tree node list.
	 * </p>
	 */
	@Override
	public boolean hasChildren() {
		return !treeNodeList.isEmpty();
	}

	@Override
	public boolean hasChild(final TreeNodeModel<?> treeNode) {
		return treeNodeList.contains(treeNode);
	}

	@Override
	public void add(final TreeNodeModel<?> treeNode) {
		if(treeNode.getParent() != null) { //if this tree node has already been added to tree node
			throw new IllegalArgumentException("Tree node " + treeNode + " is already a child of a tree node, " + treeNode.getParent() + ".");
		}
		treeNodeList.add(treeNode); //add the tree node to the list
		treeNode.setParent(this); //tell the tree node who its parent is
		treeNode.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen and repeat all property changes of the tree node
		treeNode.addVetoableChangeListener(getRepeatVetoableChangeListener()); //listen and repeat all vetoable changes of the tree node
		treeNode.addActionListener(repeatActionListener); //listen and repeat all actions of the tree node		
	}

	@Override
	public void remove(final TreeNodeModel<?> treeNode) {
		if(treeNode.getParent() != this) { //if the tree node is not a child of this tree node
			throw new IllegalArgumentException("Tree node " + treeNode + " is not child of tree node " + this + ".");
		}
		treeNode.removePropertyChangeListener(getRepeatPropertyChangeListener()); //stop listening and repeating all property changes of the tree node
		treeNode.removeVetoableChangeListener(getRepeatVetoableChangeListener()); //stop listening and repeating all vetoable changes of the tree node
		treeNode.removeActionListener(repeatActionListener); //stop listening and repeating all actions of the tree node
		treeNodeList.remove(treeNode); //remove the tree node to the list
		treeNode.setParent(null); //tell the tree node it no longer has a parent
	}

	@Override
	public void clear() {
		for(final TreeNodeModel<?> treeNode : this) { //for each child tree node
			remove(treeNode); //remove this tree node
		}
	}

	/** The parent of this node, or <code>null</code> if this node has no parent. */
	private TreeNodeModel<?> parent;

	@Override
	public TreeNodeModel<?> getParent() {
		return parent;
	}

	@Override
	public void setParent(final TreeNodeModel<?> newParent) {
		final TreeNodeModel<?> oldParent = parent; //get the old parent
		if(oldParent != newParent) { //if the parent is really changing
			if(newParent != null) { //if a parent is provided
				if(oldParent != null) { //if we already have a parent
					throw new IllegalStateException("Tree node " + this + " already has parent: " + oldParent);
				}
				if(!newParent.hasChild(this)) { //if the new parent is not really our parent
					throw new IllegalArgumentException("Provided parent " + newParent + " is not really parent of tree node " + this);
				}
			} else { //if no parent is provided
				if(oldParent.hasChild(this)) { //if we had a parent before, and that parent still thinks this tree node is its child
					throw new IllegalStateException("Old parent " + oldParent + " still thinks this tree node, " + this + ", is a child.");
				}
			}
			parent = newParent; //this is really our parent; make a note of it
		}
	}

	/**
	 * Determines the tree model in which this tree node is located. This implementation delegates to the parent tree node, if available.
	 * @return The tree model in which this tree node is located, or <code>null</code> if this tree node is not in a tree model.
	 */
	/*TODO fix
		public TreeModel getTreeModel()
		{
			final TreeNodeModel<?> parent=getParent();	//get the parent tree node
			return parent!=null ? parent.getTreeModel() : null;	//delegate to the parent, if there is one
		}	
	*/

	/**
	 * Constructs a tree node model indicating the type of value it can hold.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 */
	public DefaultTreeNodeModel(final Class<V> valueClass) {
		this(valueClass, null); //construct the class with a null initial value
	}

	/**
	 * Constructs a tree node model indicating the type of value it can hold, along with an initial value.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @param initialValue The initial value, which will not be validated.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 */
	public DefaultTreeNodeModel(final Class<V> valueClass, final V initialValue) {
		super(valueClass, initialValue); //construct the parent class
		//TODO del or move		setEditable(false);	//default to not being editable
	}

	/**
	 * Returns a list of children. This method along with {@link #setChildren(List)} provides a <code>children</code> property for alternate children access.
	 * @return A list of tree node children in order.
	 * @see #iterator()
	 */
	public List<TreeNodeModel<?>> getChildren() {
		return new ArrayList<TreeNodeModel<?>>(getTreeNodeList()); //create and return a copy of the list
	}

	/**
	 * Sets the children in this container. This method along with {@link #getChildren()} provides a <code>children</code> property for alternate children access.
	 * @param treeNodes The new children of this tree node in order.
	 * @see #clear()
	 * @see #add(TreeNodeModel)
	 */
	public void setChildren(final List<TreeNodeModel<?>> treeNodes) {
		clear(); //remove all children from the tree node
		for(final TreeNodeModel<?> treeNode : treeNodes) { //for each child
			add(treeNode); //add this child
		}
	}

	@Override
	public int getDepth() {
		final TreeNodeModel<?> parentNode = getParent(); //get the parent node
		return parentNode != null ? parentNode.getDepth() + 1 : 0; //if there is a parent node, this node's depth is one more than the parent's; otherwise, this is the root node with depth zero
	}

	//ActionModel support

	@Override
	public void addActionListener(final ActionListener actionListener) {
		getEventListenerManager().add(ActionListener.class, actionListener); //add the listener
	}

	@Override
	public void removeActionListener(final ActionListener actionListener) {
		getEventListenerManager().remove(ActionListener.class, actionListener); //remove the listener
	}

	@Override
	public Iterable<ActionListener> getActionListeners() {
		return getEventListenerManager().getListeners(ActionListener.class); //remove the listener
	}

	@Override
	public void performAction() {
		performAction(1, 0); //fire an event saying that the action has been performed with the default force and option
	}

	@Override
	public void performAction(final int force, final int option) {
		fireActionPerformed(force, option); //fire an event saying that the action has been performed with the given force and option
	}

	/**
	 * Fires an action event to all registered action listeners. This method delegates to {@link #fireActionPerformed(ActionEvent)}.
	 * @param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	 * @param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiaged by a mouse right button
	 *          click.
	 * @see ActionListener
	 * @see ActionEvent
	 */
	protected void fireActionPerformed(final int force, final int option) {
		if(getEventListenerManager().hasListeners(ActionListener.class)) { //if there are action listeners registered
			fireActionPerformed(new ActionEvent(this, force, option)); //create and fire a new action event
		}
	}

	/**
	 * Fires a given action event to all registered action listeners.
	 * @param actionEvent The action event to fire.
	 */
	protected void fireActionPerformed(final ActionEvent actionEvent) {
		for(final ActionListener actionListener : getEventListenerManager().getListeners(ActionListener.class)) { //for each action listener
			actionListener.actionPerformed(actionEvent); //dispatch the action to the listener
		}
	}

}
