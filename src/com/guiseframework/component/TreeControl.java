package com.guiseframework.component;

import java.util.*;
import java.util.concurrent.*;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.beans.TargetedEvent;
import com.garretwilson.util.Debug;
import com.guiseframework.event.*;
import com.guiseframework.model.*;

/**A tree control.
Property change events and action events on one tree node will be repeated to this object's listeners, with the tree node initiating the event accessible via {@link TargetedEvent#getTarget()}.
@author Garret Wilson
*/
public class TreeControl extends AbstractCompositeStateControl<TreeNodeModel<?>, TreeControl.TreeNodeComponentState, TreeControl> implements TreeModel
{

	/**The tree model used by this component.*/
	private final TreeModel treeModel;

		/**@return The tree model used by this component.*/
		protected TreeModel getTreeModel() {return treeModel;}

	/**An action listener to repeat copies of events received, using this component as the source.*/
	private ActionListener repeatActionListener=new ActionListener()
			{
				public void actionPerformed(final ActionEvent actionEvent)	//if an action was performed
				{
					final ActionEvent repeatActionEvent=new ActionEvent(TreeControl.this, actionEvent);	//copy the action event with this class as its source, keeping the same target
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

	/**Retrieves the component for the given object.
	If no component yet exists for the given object, one will be created.
	This version is provided to allow public access.
	@param treeNode The object for which a representation component should be returned.
	@return The child component representing the given object.
	@exception IllegalArgumentException if the given object is not an appropriate object for a component to be created.
	*/
	public Component<?> getComponent(final TreeNodeModel<?> treeNode)
	{
		return super.getComponent(treeNode);	//delegate to the parent version
	}

	/**Creates a component state to represent the given object.
	This implementation delegates to {@link #createTypedComponentState(TreeNodeModel)}.
	@param object The object with which the component state is to be associated.
	@return The component state to represent the given object.
	@exception IllegalArgumentException if the given object is not an appropriate object for a component state to be created.
	*/
	protected TreeNodeComponentState createComponentState(final TreeNodeModel<?> treeNode)
	{
		return createTypedComponentState(treeNode);	//delegate to the typed version
	}
	
	/**Creates a component state to represent the given object.
	@param <T> The type of value contained in the tree node.
	@param object The object with which the component state is to be associated.
	@return The component state to represent the given object.
	@exception IllegalArgumentException if the given object is not an appropriate object for a component state to be created.
	*/
	private <T> TreeNodeComponentState createTypedComponentState(final TreeNodeModel<T> treeNode)
	{
		final boolean editable=false;	//TODO fix
		final Component<?> treeNodeComponent=getTreeNodeRepresentationStrategy(treeNode.getValueClass()).createComponent(this, getTreeModel(), treeNode, editable, false, false);	//create a new component for the tree node
		return new TreeNodeComponentState(treeNodeComponent, editable);	//create a new component state for the tree node's component and metadata
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
		this.treeModel.addVetoableChangeListener(getRepeatVetoableChangeListener());	//listen and repeat all vetoable changes of the tree model
		this.treeModel.addActionListener(repeatActionListener);	//listen and repeat all actions of the tree model
			//TODO listen for and repeat tree model-specific events
		setTreeNodeRepresentationStrategy(Object.class, new DefaultValueRepresentationStrategy<Object>());	//create a default representation strategy and set it as the default by associating it with the Object class
		setTreeNodeRepresentationStrategy(LabelModel.class, new LabelModelTreeNodeRepresentationStrategy());	//create and associate a label model representation strategy
//TODO fix		setTreeNodeRepresentationStrategy(MessageModel.class, new MessageModelRepresentationStrategy(session));	//create and associate a message model representation strategy
		setTreeNodeRepresentationStrategy(TextModel.class, new TextModelTreeNodeRepresentationStrategy());	//create and associate a text model representation strategy
		addActionListener(new TreeNodeActionListener());	//listen for action events so that we can select nodes and/or pop up context menus
	}

		//TreeModel delegations
	
	/**@return The root node of the tree model.*/
	public TreeNodeModel<?> getRootNode() {return getTreeModel().getRootNode();}

	/**Sets whether all tree nodes are expanded.
	@param newAllExpanded <code>true</code> if all the nodes should be expanded, or <code>false</code> if they should be collapsed.
	*/
	public void setAllExpanded(final boolean newAllExpanded) {getTreeModel().setAllExpanded(newAllExpanded);}

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
		@return A new component to represent the given value.
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
		@return A new component to represent the given value.
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
					return new Label();	//return a default label TODO improve this entire strategy
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
		@return A new component to represent the given value.
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
		@return A new component to represent the given value.
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

	/**The listener to listen for actions on a tree node to handle selections and context menus.
	@author Garret Wilson
	*/
	private class TreeNodeActionListener implements ActionListener
	{
		/**Called when an action is initiated.
		@param actionEvent The event indicating the source of the action.
		*/
		public void actionPerformed(final ActionEvent actionEvent)
		{
//TODO del Debug.trace("received action from source", actionEvent.getSource(), "for target", actionEvent.getTarget(), "with force", actionEvent.getForce(), "and option", actionEvent.getOption());
			final Object target=actionEvent.getTarget();	//get the event target
			if(target instanceof TreeNodeModel)	//if this action was on a tree node
			{
				final TreeNodeModel<?> treeNode=(TreeNodeModel<?>)target;	//get the tree node
				
/*TODO finish; this works so far
				final Component<?> component=getComponent(treeNode);	//TODO testing
				component.setBackgroundColor(RGBColor.BLUE);
*/
/*TODO fix
				final Object value=treeNode.getValue();	//get the tree node value
				if(value instanceof Interaction)	//if the tree node value is an interaction
				{
					final Interaction interaction=(Interaction)value;	//get the specified interaction
					final TreeNodeModel<?> parentTreeNode=treeNode.getParent();	//get the parent tree node
//TODO del Debug.trace("ready to edit; is there a parent?", parentTreeNode);
					final Interaction contextInteraction=asInstance(parentTreeNode!=null ? parentTreeNode.getValue() : null, Interaction.class);	//get the parent's value, if any, which will be the context interaction
//TODO del					final Question contextQuestion=asInstance(parentTreeNode!=null ? parentTreeNode.getValue() : null, Question.class);	//get the parent's value, if any, which will be the context question
//TODO del Debug.trace("is there a context question?", contextQuestion);
					final FollowupEvaluation followupEvaluation=treeNode instanceof AbstractInteractionTreeNodeModel ? ((AbstractInteractionTreeNodeModel<?>)treeNode).getFollowupEvaluation() : null;	//get the followup evaluation, if any
					switch(actionEvent.getOption())	//see which option was requested
					{
						case 0:	//edit interaction
							editInteraction(contextInteraction, followupEvaluation, interaction, false);	//edit the interaction, but don't add it anywhere
							break;
						case 1:	//add interaction
							addInteraction(interaction, null);	//add a new interaction to this interaction
							break;
		//					addInteraction(null, null);	//add an interaction with no context question
		
					}
				}
*/
			}
		}
	}

}
