package com.guiseframework.component;

import java.util.*;
import java.util.concurrent.*;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.event.*;
import com.guiseframework.model.*;

/**A tree control.
@author Garret Wilson
*/
public class TreeControl extends AbstractCompositeStateControl<TreeNodeModel<?>, TreeControl.TreeNodeComponentState, TreeControl> implements TreeModel
{

	/**The tree model used by this component.*/
	private final TreeModel treeModel;

		/**@return The tree model used by this component.*/
		protected TreeModel getTreeModel() {return treeModel;}

	/**A listener to listen for changes in properties of tree nodes in the model and re-fire them as coming from this component.*/
	private final TreeNodePropertyChangeListener<Object> treeNodePropertyChangeListener=new TreeNodePropertyChangeListener<Object>()
		{
			public void propertyChange(final TreeNodePropertyChangeEvent<Object> treeNodePropertyChangeEvent)	//if the model fires a tree node property change event
			{
					//create a copy of the property change event indicating this control as the source
				final TreeNodePropertyChangeEvent<Object> repeatTreeNodePropertyChangeEvent=new TreeNodePropertyChangeEvent<Object>(this, treeNodePropertyChangeEvent.getTreeNode(), treeNodePropertyChangeEvent.getPropertyName(), treeNodePropertyChangeEvent.getOldValue(), treeNodePropertyChangeEvent.getNewValue()); 
				fireTreeNodePropertyChange(repeatTreeNodePropertyChangeEvent);	//repeat the event to this control's listener
			}		
		};

	/**An action listener to repeat copies of events received, using this component as the source.*/ 
	private ActionListener repeatActionListener=new ActionListener()
			{
				public void actionPerformed(final ActionEvent actionEvent)	//if an action was performed
				{
					final ActionEvent repeatActionEvent=new ActionEvent(TreeControl.this, actionEvent);	//copy the action event with this class as its source
					fireActionPerformed(repeatActionEvent);	//fire the repeated action
				}
			};

	/**The map of tree node representation strategies for classes.*/
	private final Map<Class<?>, TreeNodeRepresentationStrategy<?>> classTreeNodeRepresentationStrategyMap=new ConcurrentHashMap<Class<?>, TreeNodeRepresentationStrategy<?>>();

	/**Installs the given tree node representation strategy to produce representation components for the given value class.
	@param <V> The type of value to represent.
	@param valueClass The class of value with which the strategy should be associated.
	@param treeNodeRepresentationStrategy The strategy for generating components to represent values of the given type.
	@return The representation strategy previously associated with the given value type.
	*/	
	@SuppressWarnings("unchecked")	//we check the generic types before putting them in the map, so it's fine to cast the retrieved values
	public <V> TreeNodeRepresentationStrategy<? super V> setTreeNodeRepresentationStrategy(final Class<V> valueClass, TreeNodeRepresentationStrategy<? super V> treeNodeRepresentationStrategy)
	{
		return (TreeNodeRepresentationStrategy<? super V>)classTreeNodeRepresentationStrategyMap.put(valueClass, treeNodeRepresentationStrategy);	//associate the strategy with the value class in the map
	}

	/**Returns the given tree node representation strategy assigned to produce representation components for the given value class.
	If there is no associated representation strategy, the default representation strategy (associated with the {@link Object} class) will be returned, if present.
	@param <V> The type of value to represent.
	@param valueClass The class of value with which the strategy should be associated.
	@return The strategy for generating components to represent values of the given type, or <code>null</code> if there is no associated representation strategy.
	*/	
	@SuppressWarnings("unchecked")	//we check the generic types before putting them in the map, so it's fine to cast the retrieved values
	public <V> TreeNodeRepresentationStrategy<? super V> getTreeNodeRepresentationStrategy(final Class<V> valueClass)
	{
		TreeNodeRepresentationStrategy<? super V> treeNodeRepresentationStrategy=(TreeNodeRepresentationStrategy<? super V>)classTreeNodeRepresentationStrategyMap.get(valueClass);	//get the strategy linked to the value class in the map
			//TODO instead of directly trying Object.class, work up the hierarchy
		if(treeNodeRepresentationStrategy==null && !Object.class.equals(valueClass))	//if there is no associated representation strategy and this isn't the object class
		{
			treeNodeRepresentationStrategy=(TreeNodeRepresentationStrategy<? super V>)classTreeNodeRepresentationStrategyMap.get(Object.class);	//get the default strategy (the one associated with Object.class)			
		}
		return treeNodeRepresentationStrategy;	//return the tree node representation strategy
	}

	/**Ensures the component for a particular tree hierarchy exists.
	This method is meant to be called primarily from the associated view.
	This method recursively calls itself for all child nodes of the given tree node.
	@param <T> The type of value contained in the tree node.
	@param treeNode The tree node to verify.
	@return The child component representing the given tree node.
	*/
	public <T> Component<?> verifyTreeNodeComponent(final TreeNodeModel<T> treeNode)
	{
//TODO important fix		final boolean editable=treeNode.isEditable();	//see if the tree node is editable
		final boolean editable=false;	//TODO fix
		TreeNodeComponentState treeNodeComponentState=getComponentState(treeNode);	//get the component information for this tree node
		if(treeNodeComponentState==null || treeNodeComponentState.isEditable()!=editable)	//if there is no component for this tree node, or the component has a different editable status
		{
				//TODO assert that there is a representation strategy, or otherwise check
			final Component<?> valueComponent=getTreeNodeRepresentationStrategy(treeNode.getValueClass()).createComponent(this, getTreeModel(), treeNode, editable, false, false);	//create a new component for the tree node
			if(valueComponent!=null)	//if a value component is given TODO see if this check occurs in the table controller TODO make sure this is the way we want to do this---why not just return a label with a null value?
			{
//TODO del				valueComponent.setParent(this);	//tell this component that this tree component is its parent
				treeNodeComponentState=new TreeNodeComponentState(valueComponent, editable);	//create a new component state for the tree node's component and metadata
				putComponentState(treeNode, treeNodeComponentState);	//store the component state in the map for next time
			}
		}
		for(final TreeNodeModel<?> childTreeNode:treeNode)	//for each child tree node
		{
			verifyTreeNodeComponent(childTreeNode);	//verify this child node tree
		}
		return treeNodeComponentState.getComponent();	//return the representation component
	}

	/**Default constructor with a default tree model.*/
	public TreeControl()
	{
		this(new DefaultTreeModel());	//construct the class with a default model
	}

	/**Tree model constructor.
	@param treeModel The component tree model.
	@exception NullPointerException if the given tree model is <code>null</code>.
	*/
	public TreeControl(final TreeModel treeModel)
	{
		this.treeModel=checkInstance(treeModel, "Tree model cannot be null.");	//save the tree model
		this.treeModel.addPropertyChangeListener(getRepeatPropertyChangeListener());	//listen and repeat all property changes of the tree model
		this.treeModel.addTreeNodePropertyChangeListener(treeNodePropertyChangeListener);	//listen and repeat all property changes of the tree nodes in the tree model
		this.treeModel.addActionListener(repeatActionListener);	//listen and repeat all actions of the tree model
			//TODO listen for and repeat tree model-specific events
		setTreeNodeRepresentationStrategy(Object.class, new DefaultValueRepresentationStrategy<Object>());	//create a default representation strategy and set it as the default by associating it with the Object class
		setTreeNodeRepresentationStrategy(LabelModel.class, new LabelModelTreeNodeRepresentationStrategy());	//create and associate a label model representation strategy
//TODO fix		setTreeNodeRepresentationStrategy(MessageModel.class, new MessageModelRepresentationStrategy(session));	//create and associate a message model representation strategy
		setTreeNodeRepresentationStrategy(TextModel.class, new TextModelTreeNodeRepresentationStrategy());	//create and associate a text model representation strategy
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
	@param treeNodePropertyChangeEvent The tree node property change event representing the property change of the tree node.
	@see TreeNodePropertyChangeListener
	@see TreeNodePropertyChangeEvent
	*/
	protected void fireTreeNodePropertyChange(final TreeNodePropertyChangeEvent<Object> treeNodePropertyChangeEvent)
	{
		if(getEventListenerManager().hasListeners(TreeNodePropertyChangeListener.class))	//if there are tree node property change listeners registered
		{
			getSession().queueEvent(new PostponedTreeNodePropertyChangeEvent<Object>(getEventListenerManager(), treeNodePropertyChangeEvent));	//tell the Guise session to queue the event
		}
	}

		//TreeModel delegations
	
	/**@return The root node of the tree model.*/
	public TreeNodeModel<?> getRootNode() {return getTreeModel().getRootNode();}

	/**An encapsulation of a component for a tree node along with other metadata, such as whether the component was editable when created.
	@author Garret Wilson
	*/ 
	protected static class TreeNodeComponentState extends AbstractCompositeStateComponent.ComponentState
	{
		/**Whether the component is for a tree node that was editable when the component was created.*/
		private final boolean editable;

			/**@return Whether the component is for a tree node that was editable when the component was created.*/
			public boolean isEditable() {return editable;}

		/**Whether the component is for a tree node that was expanded when the component was created.*/
//TODO del		private final boolean expanded;

			/**@return Whether the component is for a tree node that was expanded when the component was created.*/
//		TODO del			public boolean isExpanded() {return expanded;}
			
		/**Constructor
		@param component The component for a tree node.
		@param editable Whether the component is for a tree node that was editable when the component was created.
		@param expanded Whether the component is for a tree node that was expanded when the component was created.
		@exception NullPointerException if the given component is <code>null</code>.
		*/
		public TreeNodeComponentState(final Component<?> component, final boolean editable/*TODO del, final boolean expanded*/)
		{
			super(component);	//construct the parent class
			this.editable=editable;
//		TODO del			this.expanded=expanded;
		}
	}

	/**A strategy for generating components to represent tree node models.
	The component ID should reflect a unique identifier for the tree node.
	@param <V> The type of value the strategy is to represent.
	@author Garret Wilson
	*/
	public interface TreeNodeRepresentationStrategy<V>
	{
		/**Creates a component to represent the given tree node.
		@param <N> The type of value contained in the node.
		@param treeControl The component containing the model.
		@param model The model containing the value.
		@param treeNode The node containing the value. 
		@param editable Whether values in this column are editable.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		public <N extends V> Component<?> createComponent(final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode, final boolean editable, final boolean selected, final boolean focused);
	}

	/**An abstract tree node representation strategy.
	@param <V> The type of value the strategy is to represent.
	@author Garret Wilson
	*/
	public static abstract class AbstractTreeNodeRepresentationStrategy<V> implements TreeNodeRepresentationStrategy<V>
	{
	}

	/**A default tree node representation strategy.
	A label component will be generated containing the default string representation of a value.
	@param <V> The type of value the strategy is to represent.
	@see Label
	@author Garret Wilson
	*/
	public static class DefaultValueRepresentationStrategy<V> extends AbstractTreeNodeRepresentationStrategy<V>
	{
		/**Creates a component to represent the given tree node.
		This implementation returns a label with string value of the given value using the object's <code>toString()</code> method.
		@param <N> The type of value contained in the node.
		@param treeControl The component containing the model.
		@param model The model containing the value.
		@param treeNode The node containing the value. 
		@param editable Whether values in this column are editable.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		@SuppressWarnings("unchecked")
		public <N extends V> Component<?> createComponent(final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode, final boolean editable, final boolean selected, final boolean focused)
		{
			if(editable)	//if the component should be editable
			{
				final Class<N> valueClass=treeNode.getValueClass();	//get the value class of the column
				if(Boolean.class.isAssignableFrom(valueClass))	//if the value class is subclass of Boolean
				{
					return new CheckControl((ValueModel<Boolean>)(Object)treeNode);	//create a new check control for the Boolean value model TODO find out why JDK 1.5.0_03 requires the intermediate Object cast
				}
				else	//for all other values
				{
					return new TextControl<N>(treeNode);	//generate a text input control for the value model
				}
			}
			else	//if the component should not be editable, return a label component
			{
				final N value=treeNode.getValue();	//get the current value
				if(value!=null)	//if there is value
				{
					final Label label=new Label();	//create a new label
					label.setLabel(value.toString());	//generate a label containing the value's string value
					return label;	//return the label
				}
				else	//if there is no value
				{
					return null;	//don't return a component
				}
			}
		}
	}

	/**A tree node representation strategy for a label model, generating a label component.
	@see Label
	@author Garret Wilson
	*/
	public static class LabelModelTreeNodeRepresentationStrategy extends AbstractTreeNodeRepresentationStrategy<LabelModel>
	{

		/**Creates a label to represent the given tree node.
		@param <N> The type of value contained in the node.
		@param treeControl The component containing the model.
		@param model The model containing the value.
		@param treeNode The node containing the value. 
		@param editable Whether values in this column are editable.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		public <N extends LabelModel> Label createComponent(final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode, final boolean editable, final boolean selected, final boolean focused)
		{
			return new Label(treeNode.getValue());	//return a label from the label model
		}
	}

	/**A tree node representation strategy for a {@link TextModel}, generating a {@link Text} component.
	@see Message
	@author Garret Wilson
	*/
	public static class TextModelTreeNodeRepresentationStrategy extends AbstractTreeNodeRepresentationStrategy<TextModel>
	{

		/**Creates a text component to represent the given tree node.
		@param <N> The type of value contained in the node.
		@param treeControl The component containing the model.
		@param model The model containing the value.
		@param treeNode The node containing the value. 
		@param editable Whether values in this column are editable.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		public <N extends TextModel> Text createComponent(final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode, final boolean editable, final boolean selected, final boolean focused)
		{
			return new Text(treeNode.getValue());	//return a message from the message model
		}
	}

	/**A tree node representation strategy for a message model, generating a message component.
	@see Message
	@author Garret Wilson
	*/
/*TODO fix
	public static class MessageModelRepresentationStrategy extends AbstractValueRepresentationStrategy<MessageModel>
	{
*/

		/**Session constructor.
		@param session The Guise session that owns this representation strategy.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
/*TODO fix
		public MessageModelRepresentationStrategy(final GuiseSession session)
		{
			super(session);	//construct the parent class
		}
*/

		/**Creates a message to represent the given tree node.
		@param <N> The type of value contained in the node.
		@param treeControl The component containing the model.
		@param model The model containing the value.
		@param treeNode The node containing the value. 
		@param editable Whether values in this column are editable.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
//TODO del		public Message createComponent(final TreeControl treeControl, final TreeModel model, final TreeNodeModel<MessageModel> treeNode, final boolean editable, final boolean selected, final boolean focused)
//TODO bring back after Eclipse fixes its bug		public <N extends LabelModel> Label createComponent(final TreeModel model, final TreeNodeModel<N> treeNode, final boolean editable, final boolean selected, final boolean focused)
/*TODO fix
		public <N extends MessageModel> Message createComponent(final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode, final boolean editable, final boolean selected, final boolean focused)
		{
			final GuiseSession session=getSession();	//get the session
			final String id=treeControl.createID(getID(treeNode.getValue()));	//get the ID from the value TODO don't get the ID from the value, as this can change if edited
			return new Message(session, id, treeNode.getValue());	//return a message from the message model
		}
	}
*/

		//ActionModel support

	/**Adds an action listener.
	@param actionListener The action listener to add.
	*/
	public void addActionListener(final ActionListener actionListener)
	{
		getEventListenerManager().add(ActionListener.class, actionListener);	//add the listener
	}

	/**Removes an action listener.
	@param actionListener The action listener to remove.
	*/
	public void removeActionListener(final ActionListener actionListener)
	{
		getEventListenerManager().remove(ActionListener.class, actionListener);	//remove the listener
	}

	/**@return all registered action listeners.*/
	public Iterable<ActionListener> getActionListeners()
	{
		return getEventListenerManager().getListeners(ActionListener.class);	//remove the listener
	}

	/**Performs the action with default force and default option.
	An {@link ActionEvent} is fired to all registered {@link ActionListener}s.
	This method delegates to {@link #performAction(int, int)}.
	*/
	public void performAction()
	{
		getTreeModel().performAction();	//delegate to the installed tree model, which will fire an event which we will catch and queue for refiring
	}

	/**Performs the action with the given force and option.
	An {@link ActionEvent} is fired to all registered {@link ActionListener}s.
	@param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	@param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiaged by a mouse right button click.
	*/
	public void performAction(final int force, final int option)
	{
		getTreeModel().performAction(force, option);	//delegate to the installed tree model, which will fire an event which we will catch and queue for refiring
	}

	/**Fires an action event to all registered action listeners.
	This method delegates to {@link #fireActionPerformed(ActionEvent)}.
	@param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	@param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiaged by a mouse right button click.
	@see ActionListener
	@see ActionEvent
	*/
	protected void fireActionPerformed(final int force, final int option)
	{
		final EventListenerManager eventListenerManager=getEventListenerManager();	//get event listener support
		if(eventListenerManager.hasListeners(ActionListener.class))	//if there are action listeners registered
		{
			fireActionPerformed(new ActionEvent(this, force, option));	//create and fire a new action event
		}
	}

	/**Fires a given action event to all registered action listeners.
	@param actionEvent The action event to fire.
	*/
	protected void fireActionPerformed(final ActionEvent actionEvent)
	{
		for(final ActionListener actionListener:getEventListenerManager().getListeners(ActionListener.class))	//for each action listener
		{
			actionListener.actionPerformed(actionEvent);	//dispatch the action to the listener
		}
	}

}
