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

import java.util.List;

/**
 * A node in a tree model that can dynamically load its children when needed.
 * <p>
 * Child classes must override {@link #determineLeaf()} and {@link #determineChildren()}.
 * </p>
 * <p>
 * Property change events on one tree node will be bubbled up the hierarchy, with the source indicating the tree node on which the property change occurred.
 * </p>
 * @param <V> The type of value contained in the tree node.
 * @author Garret Wilson
 */
public abstract class DynamicTreeNodeModel<V> extends DefaultTreeNodeModel<V> { //TODO update all the caches when the value changes

	/** Whether children have been determined. */
	private boolean isChildrenDetermined = false;

	/** @return Whether the child nodes have been loaded. */
	//TODO del if not needed		protected boolean isChildNodesLoaded() {return childNodesLoaded;}

	/** The cached value of whether this node is a leaf, or <code>null</code> if whether this is a leaf has not yet been determined. */
	private Boolean isLeaf = null;

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation <code>true</code> if child nodes are not loaded and this is not a predictive dynamic tree node.
	 * </p>
	 * TODO comment
	 */
	@Override
	public boolean isLeaf() {
		synchronized(this) { //synchronize access to the isLeaf variable
			if(isLeaf == null) { //if we haven't yet determined if this resource has children
				isLeaf = Boolean.valueOf(determineLeaf()); //determine if this is a leaf or not
			}
		}
		return isLeaf.booleanValue(); //return the leaf determination
	}

	@Override
	public void setExpanded(final boolean newExpanded) {
		synchronized(this) { //synchronize access to the dynamic variables variable
			if(newExpanded != isExpanded())
				; //if the expansion state is changing
			{
				if(newExpanded) { //if the tree node is expanding
					if(!isChildrenDetermined) { //if children have not yet been determined
						final List<TreeNodeModel<?>> children = determineChildren(); //determine the new children
						setChildren(determineChildren()); //set the new children
						isChildrenDetermined = true; //show that we've loaded the children
						isLeaf = Boolean.valueOf(children.isEmpty()); //we also know whether this node is a leaf
					}
				} else { //if the tree node is collapsing
					clear(); //unload child nodes, if any
				}
			}
		}
		super.setExpanded(newExpanded); //expand or collapse normally
	}

	/**
	 * Constructs a tree node model indicating the type of value it can hold.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 */
	public DynamicTreeNodeModel(final Class<V> valueClass) {
		this(valueClass, null); //construct the class with a null initial value
	}

	/**
	 * Constructs a tree node model indicating the type of value it can hold, along with an initial value.
	 * @param valueClass The class indicating the type of value held in the model.
	 * @param initialValue The initial value, which will not be validated.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 */
	public DynamicTreeNodeModel(final Class<V> valueClass, final V initialValue) {
		super(valueClass, initialValue); //construct the parent class
	}

	/**
	 * Loads children if they haven't already been loaded.
	 * @see #determineChildren()
	 */
	/*TODO del if not needed
		protected synchronized void ensureChildrenDetermined()
		{
	//TODO del Log.trace("ensuring child nodes loaded for resource", getValue(), "isChildNodesLoaded()", isChildNodesLoaded());
			if(!isChildNodesLoaded()) {	//if the children are not yet loaded
				childNodesLoaded=true;  //show that we've loaded the child nodes (this is done before the actual loading so that future calls to getChildCount() won't cause reloading)
				clear();	//make sure we've removed all children before trying to load the children
				try
				{
					loadChildNodes(); //load the child nodes
				}
				catch(final IOException ioException) {	//if there was an error loading the child loads
	//TODO fix				loadError=ioException;	//save the load error
	//TODO fix				SwingApplication.displayApplicationError(null, ioException);	//display the error
				}
			}
		}
	*/

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version resets the children determined status.
	 * </p>
	 */
	@Override
	public void clear() {
		synchronized(this) { //make thread-safe our dynamic variable access
			super.clear(); //clear the model
			isChildrenDetermined = false; //show that children have not been determined
		}
	}

	/**
	 * Unloads all child nodes and sets the state to unloaded. Any error condition is reset.
	 */
	/*TODO del
		public void unloadChildNodes()
		{
			clear();	//remove all the children
			childNodesLoaded=false;	//show that we have no nodes loaded
	//TODO fix		setLoadError(null);	//show that there is no load error.
		}
	*/

	/**
	 * Dynamically determines whether this node is a leaf.
	 * @return Whether this node should be considered a leaf with no children.
	 */
	protected abstract boolean determineLeaf();

	/**
	 * Dynamically determines children.
	 * @return The dynamically loaded list of children.
	 */
	protected abstract List<TreeNodeModel<?>> determineChildren();
}
