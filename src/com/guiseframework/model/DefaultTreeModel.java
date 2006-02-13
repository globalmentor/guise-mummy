package com.guiseframework.model;

import com.guiseframework.GuiseSession;
import com.guiseframework.event.*;

/**A default implementation of a tree model.
If no root node is specified, the root node will be a dummy root node that will not be displayed.
If a dummy root node is used, it will automatically be set to an expanded state.
@author Garret Wilson
@see DummyTreeNodeModel
*/
public class DefaultTreeModel extends AbstractControlModel implements TreeModel
{

	/**The root node of the tree model.*/
	private final TreeNodeModel<?> rootNode;

		/**@return The root node of the tree model.*/
		public TreeNodeModel<?> getRootNode() {return rootNode;}

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

	/**Session constructor with a dummy root tree node.
	@param session The Guise session that owns this model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultTreeModel(final GuiseSession session)
	{
		this(session, new DummyTreeNodeModel(session));	//create a dummy root node
	}

	/**Session constructor.
	@param session The Guise session that owns this model.
	@param rootNode The root node of the tree model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultTreeModel(final GuiseSession session, final TreeNodeModel<?> rootNode)
	{
		super(session);	//construct the parent class
		this.rootNode=rootNode;	//save the root node
		if(rootNode instanceof DummyTreeNodeModel)	//if a dummy tree node model is being used
		{
			rootNode.setExpanded(true);	//expand the dummy root, as the root node itself will not normally be available to the user
		}
	}

}
