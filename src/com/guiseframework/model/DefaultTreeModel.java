package com.guiseframework.model;

import static com.garretwilson.lang.ObjectUtilities.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.event.*;

/**A default implementation of a tree model.
If no root node is specified, the root node will be a dummy root node that will not be displayed.
If a dummy root node is used, it will automatically be set to an expanded state.
@author Garret Wilson
@see DummyTreeNodeModel
*/
public class DefaultTreeModel extends AbstractModel implements TreeModel
{

	/**A listener to listen for changes in properties of tree nodes that have bubbled up the hierarchy and refire them as {@link TreeNodePropertyChangeEvent}s.*/
	private final PropertyChangeListener treeNodePropertyChangeListener=new PropertyChangeListener()
		{
			public void propertyChange(final PropertyChangeEvent propertyChangeEvent)	//if a tree node property changes
			{
				fireTreeNodePropertyChange(propertyChangeEvent);	//fire an event indicating that a tree node property changed
			}		
		};

	/**The rot node of the tree model.*/
	private TreeNodeModel<?> rootNode;

		/**@return The root node of the tree model.*/
		public TreeNodeModel<?> getRootNode() {return rootNode;}

		/**Sets the root node of the tree model.
		This is a bound property.
		@param newRootNode The new root node of the tree model.
		@exception NullPointerException if the given root node is <code>null</code>.
		@see #ROOT_NODE_PROPERTY
		*/
		public void setRootNode(final TreeNodeModel<?> newRootNode)
		{
			if(!ObjectUtilities.equals(rootNode, checkInstance(newRootNode, "Root node cannot be null.")))	//if the value is really changing
			{
				final TreeNodeModel<?> oldRootNode=rootNode;	//get the old value
				oldRootNode.removePropertyChangeListener(treeNodePropertyChangeListener);	//stop listening for bubbled property change events from tree nodes in the old hierarchy
				rootNode=newRootNode;	//actually change the value
				newRootNode.addPropertyChangeListener(treeNodePropertyChangeListener);	//start listening for bubbled property change events from tree nodes in the new hierarchy
				firePropertyChange(ROOT_NODE_PROPERTY, oldRootNode, newRootNode);	//indicate that the value changed
			}			
		}
		
	/**Whether the tree model is editable and the nodes will allow the the user to change their values, if they are designated as editable as well.*/
//TODO del if not needed	private boolean editable=true;

		/**@return Whether the tree model is editable and the nodes will allow the the user to change their values, if they are designated as editable as well.*/
//TODO del if not needed		public boolean isEditable() {return editable;}

		/**Sets whether the tree model is editable and the nodes will allow the the user to change their values, if they are designated as editable as well.
		This is a bound property of type <code>Boolean</code>.
		@param newEditable <code>true</code> if the nodes should allow the user to change their values if they are also designated as editable.
		@see TableModel#EDITABLE_PROPERTY
		*/
/*TODO del if not needed
		public void setEditable(final boolean newEditable)
		{
			if(editable!=newEditable)	//if the value is really changing
			{
				final boolean oldEditable=editable;	//get the old value
				editable=newEditable;	//actually change the value
				firePropertyChange(EDITABLE_PROPERTY, Boolean.valueOf(oldEditable), Boolean.valueOf(newEditable));	//indicate that the value changed
			}			
		}
*/

	/**Default constructor with a dummy root tree node.*/
	public DefaultTreeModel()
	{
		this(new DummyTreeNodeModel());	//create a dummy root node
	}

	/**Root node constructor.
	@param session The Guise session that owns this model.
	@param rootNode The root node of the tree model.
	@exception NullPointerException if the given root node is <code>null</code>.
	*/
	public DefaultTreeModel(final TreeNodeModel<?> rootNode)
	{
		this.rootNode=checkInstance(rootNode, "Root node cannot be null.");	//save the root node
		this.rootNode.addPropertyChangeListener(treeNodePropertyChangeListener);	//start listening for bubbled property change events from tree nodes
		if(rootNode instanceof DummyTreeNodeModel)	//if a dummy tree node model is being used
		{
			rootNode.setExpanded(true);	//expand the dummy root, as the root node itself will not normally be available to the user
		}
	}

	/**Adds a tree node property change listener.
	@param treeNodePropertyChangeListener The tree node property change listener to add.
	*/
	public void addTreeNodePropertyChangeListener(final TreeNodePropertyChangeListener<?> treeNodePropertyChangeListener)
	{
		getEventListenerManager().add(TreeNodePropertyChangeListener.class, treeNodePropertyChangeListener);	//add the listener
	}

	/**Removes a tree node property change listener.
	@param treeNodePropertyChangeListener The tree node property change listener to remove.
	*/
	public void removeTreeNodePropertyChangeListener(final TreeNodePropertyChangeListener<?> treeNodePropertyChangeListener)
	{
		getEventListenerManager().remove(TreeNodePropertyChangeListener.class, treeNodePropertyChangeListener);	//remove the listener
	}

	/**Fires a tree node property change event to all registered tree node property change listeners.
	@param propertyChangeEvent The property change event representing the property change of the tree node.
	@see TreeNodePropertyChangeListener
	@see TreeNodePropertyChangeEvent
	*/
	protected void fireTreeNodePropertyChange(final PropertyChangeEvent propertyChangeEvent)
	{
		if(getEventListenerManager().hasListeners(TreeNodePropertyChangeListener.class))	//if there are tree node property change listeners registered
		{
			final TreeNodePropertyChangeEvent<Object> treeNodePropertyChangeEvent=new TreeNodePropertyChangeEvent<Object>(this, propertyChangeEvent);	//create a new tree node property change event
			getSession().queueEvent(new PostponedTreeNodePropertyChangeEvent<Object>(getEventListenerManager(), treeNodePropertyChangeEvent));	//tell the Guise session to queue the event
		}
	}

}
