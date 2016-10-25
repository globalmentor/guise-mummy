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

package com.guiseframework.component;

import java.util.*;
import java.util.concurrent.*;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Objects.*;
import com.globalmentor.beans.*;
import com.globalmentor.event.TargetedEvent;

import com.guiseframework.component.transfer.*;
import com.guiseframework.converter.*;
import com.guiseframework.event.*;
import com.guiseframework.model.*;

/**
 * A tree control. Property change events and action events on one tree node will be repeated to this object's listeners, with the tree node initiating the
 * event accessible via {@link TargetedEvent#getTarget()}.
 * @author Garret Wilson
 */
public class TreeControl extends AbstractCompositeStateControl<TreeNodeModel<?>, TreeControl.TreeNodeComponentState> implements TreeModel {

	/** The bound property of whether the root node is displayed. */
	public static final String ROOT_NODE_DISPLAYED_PROPERTY = getPropertyName(TreeControl.class, "rootNodeDisplayed");
	/** The bound property of whether the tree node components have dragging enabled. */
	public static final String TREE_NODE_DRAG_ENABLED_PROPERTY = getPropertyName(TreeControl.class, "treeNodeDragEnabled");

	/** The tree model used by this component. */
	private final TreeModel treeModel;

	/** @return The tree model used by this component. */
	protected TreeModel getTreeModel() {
		return treeModel;
	}

	/** Whether the tree node components have dragging enabled. */
	private boolean treeNodeDragEnabled = false;

	/** @return Whether the tree node component have dragging enabled. */
	public boolean isTreeNodeDragEnabled() {
		return treeNodeDragEnabled;
	}

	/**
	 * Sets whether the tree node components have dragging enabled. This is a bound property of type <code>Boolean</code>.
	 * @param newTreeNodeDragEnabled <code>true</code> if each tree node component should allow dragging, else <code>false</code>.
	 * @see #TREE_NODE_DRAG_ENABLED_PROPERTY
	 */
	public void setTreeNodeDragEnabled(final boolean newTreeNodeDragEnabled) {
		if(treeNodeDragEnabled != newTreeNodeDragEnabled) { //if the value is really changing
			final boolean oldTreeNodeDragEnabled = treeNodeDragEnabled; //get the current value
			treeNodeDragEnabled = newTreeNodeDragEnabled; //update the value
			for(final TreeNodeComponentState componentState : getComponentStates()) { //for existing component states
				componentState.getComponent().setDragEnabled(newTreeNodeDragEnabled); //update the drag enabled state of this tree node's component
			}
			firePropertyChange(TREE_NODE_DRAG_ENABLED_PROPERTY, Boolean.valueOf(oldTreeNodeDragEnabled), Boolean.valueOf(newTreeNodeDragEnabled));
		}
	}

	/** Whether the root node is displayed. */
	private boolean rootNodeDisplayed = true;

	/**
	 * @return Whether the root node is displayed.
	 * @see #isDisplayed()
	 */
	public boolean isRootNodeDisplayed() {
		return rootNodeDisplayed;
	}

	/**
	 * Sets whether the root node is displayed. This is a bound property of type <code>Boolean</code>. If the root is requested not to be displayed, the root is
	 * automatically expanded.
	 * @param newRootNodeDisplayed <code>true</code> if the root node should be displayed, else <code>false</code>.
	 * @see #ROOT_NODE_DISPLAYED_PROPERTY
	 * @see TreeNodeModel#setExpanded(boolean)
	 */
	public void setRootNodeDisplayed(final boolean newRootNodeDisplayed) {
		if(rootNodeDisplayed != newRootNodeDisplayed) { //if the value is really changing
			final boolean oldRootNodeDisplayed = rootNodeDisplayed; //get the current value
			rootNodeDisplayed = newRootNodeDisplayed; //update the value
			firePropertyChange(ROOT_NODE_DISPLAYED_PROPERTY, Boolean.valueOf(oldRootNodeDisplayed), Boolean.valueOf(newRootNodeDisplayed));
			if(!newRootNodeDisplayed) { //if the root is no longer displayed
				getRootNode().setExpanded(true); //expand the root node so that the children can be displayed
			}
		}
	}

	/** An action listener to repeat copies of events received, using this component as the source. */
	private ActionListener repeatActionListener = new ActionListener() {

		@Override
		public void actionPerformed(final ActionEvent actionEvent) { //if an action was performed
			final ActionEvent repeatActionEvent = new ActionEvent(TreeControl.this, actionEvent); //copy the action event with this class as its source, keeping the same target
			fireActionPerformed(repeatActionEvent); //fire the repeated action
		}

	};

	/** The map of tree node representation strategies for classes. */
	private final Map<Class<?>, TreeNodeRepresentationStrategy<?>> classTreeNodeRepresentationStrategyMap = new ConcurrentHashMap<Class<?>, TreeNodeRepresentationStrategy<?>>();

	/**
	 * Installs the given tree node representation strategy to produce representation components for the given value class.
	 * @param <V> The type of value to represent.
	 * @param valueClass The class of value with which the strategy should be associated.
	 * @param treeNodeRepresentationStrategy The strategy for generating components to represent values of the given type.
	 * @return The representation strategy previously associated with the given value type.
	 */
	@SuppressWarnings("unchecked")
	//we check the generic types before putting them in the map, so it's fine to cast the retrieved values
	public <V> TreeNodeRepresentationStrategy<? super V> setTreeNodeRepresentationStrategy(final Class<V> valueClass,
			TreeNodeRepresentationStrategy<? super V> treeNodeRepresentationStrategy) {
		return (TreeNodeRepresentationStrategy<? super V>)classTreeNodeRepresentationStrategyMap.put(valueClass, treeNodeRepresentationStrategy); //associate the strategy with the value class in the map
	}

	/**
	 * Returns the given tree node representation strategy assigned to produce representation components for the given value class.
	 * @param <V> The type of value to represent.
	 * @param valueClass The class of value with which the strategy should be associated.
	 * @return The strategy for generating components to represent values of the given type, or <code>null</code> if there is no associated representation
	 *         strategy.
	 * @see DefaultValueRepresentationStrategy
	 */
	@SuppressWarnings("unchecked")
	//we check the generic types before putting them in the map, so it's fine to cast the retrieved values
	public <V> TreeNodeRepresentationStrategy<? super V> getTreeNodeRepresentationStrategy(final Class<V> valueClass) {
		TreeNodeRepresentationStrategy<? super V> treeNodeRepresentationStrategy = (TreeNodeRepresentationStrategy<? super V>)classTreeNodeRepresentationStrategyMap
				.get(valueClass); //for fastest results. try to get the strategy linked directly to the value class in the map
		if(treeNodeRepresentationStrategy == null) { //if there is no associated representation strategy, work our way up the hierarchy
			for(final Class<?> ancestorClass : getProperAncestorClasses(valueClass)) { //look at each ancestor class of the value class, ignoring the current class
				treeNodeRepresentationStrategy = (TreeNodeRepresentationStrategy<? super V>)classTreeNodeRepresentationStrategyMap.get(ancestorClass); //try to get the representation strategy for this ancestor class
				if(treeNodeRepresentationStrategy != null) { //if we found a representation strategy
					break; //stop searching
				}
			}
		}
		return treeNodeRepresentationStrategy; //return the tree node representation strategy
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version is provided to allow public access.
	 * </p>
	 */
	@Override
	public Component getComponent(final TreeNodeModel<?> treeNode) {
		return super.getComponent(treeNode); //delegate to the parent version
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation delegates to {@link #createTypedComponentState(TreeNodeModel)}.
	 * </p>
	 */
	@Override
	protected TreeNodeComponentState createComponentState(final TreeNodeModel<?> treeNode) {
		return createTypedComponentState(treeNode); //delegate to the typed version
	}

	/**
	 * Creates a component state to represent the given object.
	 * @param <T> The type of value contained in the tree node.
	 * @param treeNode The object with which the component state is to be associated.
	 * @return The component state to represent the given object.
	 * @throws IllegalArgumentException if the given object is not an appropriate object for a component state to be created.
	 */
	private <T> TreeNodeComponentState createTypedComponentState(final TreeNodeModel<T> treeNode) {
		final boolean editable = false; //TODO fix
		//TODO the API currently allows the returned tree node representation strategy to be null; add a check
		final Component treeNodeComponent = getTreeNodeRepresentationStrategy(treeNode.getValueClass()).createComponent(this, getTreeModel(), treeNode, editable,
				false, false); //create a new component for the tree node

		treeNodeComponent.addExportStrategy(new ExportStrategy() { //TODO fix for generics with a separate method 

			@Override
			public Transferable<Component> exportTransfer(final Component component) {
				return new TreeNodeTransferable<T>(TreeControl.this, treeNode); //return a default transferable for the tree and the tree node
			}
		});

		treeNodeComponent.setDragEnabled(isTreeNodeDragEnabled()); //set the drag mode appropriately
		return new TreeNodeComponentState(treeNodeComponent, editable); //create a new component state for the tree node's component and metadata
	}

	/** Default constructor with a default tree model. */
	public TreeControl() {
		this(new DefaultTreeModel()); //construct the class with a default model
	}

	/**
	 * Tree model constructor.
	 * @param treeModel The component tree model.
	 * @throws NullPointerException if the given tree model is <code>null</code>.
	 */
	public TreeControl(final TreeModel treeModel) {
		this.treeModel = checkInstance(treeModel, "Tree model cannot be null."); //save the tree model
		this.treeModel.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen and repeat all property changes of the tree model
		this.treeModel.addVetoableChangeListener(getRepeatVetoableChangeListener()); //listen and repeat all vetoable changes of the tree model
		this.treeModel.addActionListener(repeatActionListener); //listen and repeat all actions of the tree model
		//TODO listen for and repeat tree model-specific events
		setTreeNodeRepresentationStrategy(Object.class, new DefaultValueRepresentationStrategy<Object>(Object.class)); //create a default representation strategy and set it as the default by associating it with the Object class
		setTreeNodeRepresentationStrategy(InfoModel.class, new InfoModelTreeNodeRepresentationStrategy()); //create and associate a label model representation strategy
		//TODO fix		setTreeNodeRepresentationStrategy(MessageModel.class, new MessageModelRepresentationStrategy(session));	//create and associate a message model representation strategy
		setTreeNodeRepresentationStrategy(TextModel.class, new TextModelTreeNodeRepresentationStrategy()); //create and associate a text model representation strategy
		addPropertyChangeListener(TreeNodeModel.SELECTED_PROPERTY, new TreeNodeSelectChangeListener()); //TODO comment
		addActionListener(new TreeNodeActionListener()); //listen for action events so that we can select nodes and/or pop up context menus
	}

	//TreeModel delegations

	@Override
	public TreeNodeModel<?> getRootNode() {
		return getTreeModel().getRootNode();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * If the this control requests that the root not be displayed, this implementation automatically expanded the root node after it is added. This is a bound
	 * property.
	 * </p>
	 */
	@Override
	public void setRootNode(final TreeNodeModel<?> newRootNode) {
		getTreeModel().setRootNode(newRootNode); //delegate to the tree model
		if(!isRootNodeDisplayed()) { //if the root is not displayed
			getRootNode().setExpanded(true); //expand the root node so that the children can be displayed
		}
	}

	@Override
	public void setAllExpanded(final boolean newAllExpanded) {
		getTreeModel().setAllExpanded(newAllExpanded);
	}

	/**
	 * An encapsulation of a component for a tree node along with other metadata, such as whether the component was editable when created.
	 * @author Garret Wilson
	 */
	protected static class TreeNodeComponentState extends AbstractCompositeStateComponent.ComponentState {

		/** Whether the component is for a tree node that was editable when the component was created. */
		private final boolean editable;

		/** @return Whether the component is for a tree node that was editable when the component was created. */
		public boolean isEditable() {
			return editable;
		}

		/** Whether the component is for a tree node that was expanded when the component was created. */
		//TODO del		private final boolean expanded;

		/** @return Whether the component is for a tree node that was expanded when the component was created. */
		//		TODO del			public boolean isExpanded() {return expanded;}

		/**
		 * Constructor
		 * @param component The component for a tree node.
		 * @param editable Whether the component is for a tree node that was editable when the component was created.
		 * @throws NullPointerException if the given component is <code>null</code>.
		 */
		public TreeNodeComponentState(final Component component, final boolean editable/*TODO del, final boolean expanded*/) {
			super(component); //construct the parent class
			this.editable = editable;
			//		TODO del			this.expanded=expanded;
		}
	}

	/**
	 * A strategy for generating components to represent tree node models.
	 * @param <V> The type of value the strategy is to represent.
	 * @author Garret Wilson
	 */
	public interface TreeNodeRepresentationStrategy<V> {

		/**
		 * Creates a component to represent the given tree node.
		 * @param <N> The type of value contained in the node.
		 * @param treeControl The component containing the model.
		 * @param model The model containing the value.
		 * @param treeNode The node containing the value.
		 * @param editable Whether values in this column are editable.
		 * @param selected <code>true</code> if the value is selected.
		 * @param focused <code>true</code> if the value has the focus.
		 * @return A new component to represent the given value.
		 */
		public <N extends V> Component createComponent(final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode,
				final boolean editable, final boolean selected, final boolean focused);
	}

	/**
	 * An abstract tree node representation strategy.
	 * @param <V> The type of value the strategy is to represent.
	 * @author Garret Wilson
	 */
	public static abstract class AbstractTreeNodeRepresentationStrategy<V> implements TreeNodeRepresentationStrategy<V> {
	}

	/**
	 * A default tree node representation strategy. A label component will be generated containing the default string representation of a value.
	 * @param <V> The type of value the strategy is to represent.
	 * @see Label
	 * @author Garret Wilson
	 */
	public static class DefaultValueRepresentationStrategy<V> extends AbstractTreeNodeRepresentationStrategy<V> {

		/** The converter to use for displaying the value as a string. */
		private final Converter<V, String> converter;

		/** @return The converter to use for displaying the value as a string. */
		public Converter<V, String> getConverter() {
			return converter;
		}

		/**
		 * Value class constructor with a default converter. This implementation uses a {@link DefaultStringLiteralConverter}.
		 * @param valueClass The class indicating the type of value to convert.
		 * @throws NullPointerException if the given value class is <code>null</code>.
		 */
		public DefaultValueRepresentationStrategy(final Class<V> valueClass) {
			this(AbstractStringLiteralConverter.getInstance(valueClass)); //construct the class with the appropriate string literal converter
		}

		/**
		 * Converter constructor.
		 * @param converter The converter to use for displaying the value as a string.
		 * @throws NullPointerException if the given converter is <code>null</code>.
		 */
		public DefaultValueRepresentationStrategy(final Converter<V, String> converter) {
			this.converter = checkInstance(converter, "Converter cannot be null."); //save the converter
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * This implementation returns a label with string value of the given value using the object's <code>toString()</code> method.
		 * </p>
		 */
		@Override
		@SuppressWarnings("unchecked")
		public <N extends V> Component createComponent(final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode,
				final boolean editable, final boolean selected, final boolean focused) {
			if(editable) { //if the component should be editable
				final Class<N> valueClass = treeNode.getValueClass(); //get the value class of the column
				if(Boolean.class.isAssignableFrom(valueClass)) { //if the value class is subclass of Boolean
					return new CheckControl((ValueModel<Boolean>)(Object)treeNode); //create a new check control for the Boolean value model TODO find out why JDK 1.5.0_03 requires the intermediate Object cast
				} else { //for all other values
					return new TextControl<N>(treeNode); //generate a text input control for the value model
				}
			} else { //if the component should not be editable, return a label component
				return new SelectableLabel(new ValueConverterInfoModel<V>(treeNode.getValue(), getConverter())); //create a label that will convert the value to a string
			}
		}
	}

	/**
	 * A tree node representation strategy for a label model, generating a label component.
	 * @see Label
	 * @author Garret Wilson
	 */
	public static class InfoModelTreeNodeRepresentationStrategy extends AbstractTreeNodeRepresentationStrategy<InfoModel> {

		@Override
		public <N extends InfoModel> Label createComponent(final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode,
				final boolean editable, final boolean selected, final boolean focused) {
			return new Label(treeNode.getValue()); //return a label from the label model
		}
	}

	/**
	 * A tree node representation strategy for a {@link TextModel}, generating a {@link TextBox} component.
	 * @see Message
	 * @author Garret Wilson
	 */
	public static class TextModelTreeNodeRepresentationStrategy extends AbstractTreeNodeRepresentationStrategy<TextModel> {

		@Override
		public <N extends TextModel> TextBox createComponent(final TreeControl treeControl, final TreeModel model, final TreeNodeModel<N> treeNode,
				final boolean editable, final boolean selected, final boolean focused) {
			return new TextBox(treeNode.getValue()); //return a message from the message model
		}
	}

	/**
	 * A tree node representation strategy for a message model, generating a message component.
	 * @see Message
	 * @author Garret Wilson
	 */
	/*TODO fix
		public static class MessageModelRepresentationStrategy extends AbstractValueRepresentationStrategy<MessageModel>
		{
	*/

	/**
	 * Session constructor.
	 * @param session The Guise session that owns this representation strategy.
	 * @throws NullPointerException if the given session is <code>null</code>.
	 */
	/*TODO fix
			public MessageModelRepresentationStrategy(final GuiseSession session)
			{
				super(session);	//construct the parent class
			}
	*/

	/**
	 * Creates a message to represent the given tree node.
	 * @param treeControl The component containing the model.
	 * @param model The model containing the value.
	 * @param treeNode The node containing the value.
	 * @param editable Whether values in this column are editable.
	 * @param selected <code>true</code> if the value is selected.
	 * @param focused <code>true</code> if the value has the focus.
	 * @return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
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
		getTreeModel().performAction(); //delegate to the installed tree model, which will fire an event which we will catch and queue for refiring
	}

	@Override
	public void performAction(final int force, final int option) {
		getTreeModel().performAction(force, option); //delegate to the installed tree model, which will fire an event which we will catch and queue for refiring
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

	/**
	 * The listener to listen for actions on a tree node to handle selections and context menus.
	 * @author Garret Wilson
	 */
	private class TreeNodeActionListener implements ActionListener {

		@Override
		public void actionPerformed(final ActionEvent actionEvent) {
			//TODO del Log.trace("received action from source", actionEvent.getSource(), "for target", actionEvent.getTarget(), "with force", actionEvent.getForce(), "and option", actionEvent.getOption());
			final Object target = actionEvent.getTarget(); //get the event target
			if(target instanceof TreeNodeModel) { //if this action was on a tree node
				final TreeNodeModel<?> treeNode = (TreeNodeModel<?>)target; //get the tree node
				//Log.trace("selecting tree node", treeNode);
				treeNode.setSelected(true); //select the tree node
				/*TODO fix
								final Component component=getComponent(treeNode);	//TODO testing
								component.setBackgroundColor(Theme.COLOR_SELECTED_BACKGROUND);
				*/

				/*TODO finish; this works so far
								final Component component=getComponent(treeNode);	//TODO testing
								component.setBackgroundColor(RGBColor.BLUE);
				*/
				/*TODO fix
								final Object value=treeNode.getValue();	//get the tree node value
								if(value instanceof Interaction) {	//if the tree node value is an interaction
									final Interaction interaction=(Interaction)value;	//get the specified interaction
									final TreeNodeModel<?> parentTreeNode=treeNode.getParent();	//get the parent tree node
				//TODO del Log.trace("ready to edit; is there a parent?", parentTreeNode);
									final Interaction contextInteraction=asInstance(parentTreeNode!=null ? parentTreeNode.getValue() : null, Interaction.class);	//get the parent's value, if any, which will be the context interaction
				//TODO del					final Question contextQuestion=asInstance(parentTreeNode!=null ? parentTreeNode.getValue() : null, Question.class);	//get the parent's value, if any, which will be the context question
				//TODO del Log.trace("is there a context question?", contextQuestion);
									final FollowupEvaluation followupEvaluation=treeNode instanceof AbstractInteractionTreeNodeModel ? ((AbstractInteractionTreeNodeModel<?>)treeNode).getFollowupEvaluation() : null;	//get the followup evaluation, if any
									switch(actionEvent.getOption()) {	//see which option was requested
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

	/**
	 * The listener to listen for tree node selection status and update the representation component status if possible. This class also handles mutual exclusion
	 * selection.
	 * @author Garret Wilson
	 */
	private class TreeNodeSelectChangeListener extends AbstractGenericPropertyChangeListener<Boolean> {

		@Override
		public void propertyChange(GenericPropertyChangeEvent<Boolean> genericPropertyChangeEvent) {
			final Boolean newValue = genericPropertyChangeEvent.getNewValue(); //get the new value
			if(newValue != null) { //if we know the new selected value
				final boolean newSelected = newValue.booleanValue(); //get the new boolean selected status
				final Object target = genericPropertyChangeEvent.getTarget(); //get the event target
				if(target instanceof TreeNodeModel) { //if this action was on a tree node
					final TreeNodeModel<?> treeNode = (TreeNodeModel<?>)target; //get the tree node
					//update the associated component, if any
					final TreeNodeComponentState componentState = getComponentState(treeNode); //see if we have a component associated with this tree node
					if(componentState != null) { //if we have a component state
						final Component component = componentState.getComponent(); //get the representation component
						if(component instanceof Selectable) { //if the component is selectable
							((Selectable)component).setSelected(newSelected); //update the representation component's selected status to match that of the tree node
						}
					}
					if(newSelected) { //if the tree node is being selected
						selectSingleTreeNode(getRootNode(), treeNode); //implement mutual exclusion by making sure this is the only tree node selected
					}
				}
			}
		}

		/**
		 * Unselects the given parent tree node and all its children, recursively, except for the given selected tree node. This method is used to implement mutual
		 * exclusive selections.
		 * @param treeNode The parent of the tree node to unselect.
		 * @param selectedTreeNode The tree node to select exclusively.
		 */
		private void selectSingleTreeNode(final TreeNodeModel<?> treeNode, final TreeNodeModel<?> selectedTreeNode) {
			treeNode.setSelected(treeNode == selectedTreeNode); //select the tree node based upon whether it is the selected tree node
			for(final TreeNodeModel<?> childTreeNode : treeNode) { //for each child tree node
				selectSingleTreeNode(childTreeNode, selectedTreeNode); //select or unselect this child recursively using mutual exclusion
			}
		}
	}

	/**
	 * The transferable object for a tree node. This transferable is able to transfer either the tree node itself or the object stored in the tree node.
	 * @param <V> The type of value contained in the tree node.
	 * @author Garret Wilson
	 */
	protected static class TreeNodeTransferable<V> extends AbstractObjectTransferable<Component> { //TODO fix Component with another generic parameter

		/** The tree node to transfer. */
		private final TreeNodeModel<V> treeNode;

		/**
		 * Source and tree node constructor.
		 * @param source The source of the transferable data.
		 * @param treeNode The tree node representing the transferred data.
		 * @throws NullPointerException if the provided source and/or tree node is <code>null</code>.
		 */
		public TreeNodeTransferable(final TreeControl source, final TreeNodeModel<V> treeNode) {
			super(source, treeNode.getClass(), treeNode.getValueClass()); //construct the parent class, indicating support for transferring the tree node itself or the value contained in the tree node
			this.treeNode = checkInstance(treeNode, "Tree node cannot be null.");
		}

		/**
		 * {@inheritDoc}
		 * <p>
		 * This implementation returns subclasses.
		 * </p>
		 */
		@Override
		public <T> T transfer(final Class<T> objectClass) {
			if(objectClass.isAssignableFrom(treeNode.getValueClass())) { //if the value class can be cast to the object class
				return objectClass.cast(treeNode.getValue()); //return the value of the tree node
			} else if(objectClass.isAssignableFrom(treeNode.getClass())) { //if the tree node class can be cast to the object class
				return objectClass.cast(treeNode); //return the tree node itself				
			}
			throw new IllegalArgumentException("Transfer class not supported: " + objectClass);
		}
	}
}
