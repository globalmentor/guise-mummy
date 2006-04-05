package com.guiseframework.component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.GuiseSession;
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

	/**The map of tree node representation strategies for classes.*/
	private final Map<Class<?>, ValueRepresentationStrategy<?>> classTreeNodeRepresentationStrategyMap=new ConcurrentHashMap<Class<?>, ValueRepresentationStrategy<?>>();

	/**Installs the given tree node representation strategy to produce representation components for the given value class.
	@param <V> The type of value to represent.
	@param valueClass The class of value with which the strategy should be associated.
	@param treeNodeRepresentationStrategy The strategy for generating components to represent values of the given type.
	@return The representation strategy previously associated with the given value type.
	*/	
	@SuppressWarnings("unchecked")	//we check the generic types before putting them in the map, so it's fine to cast the retrieved values
	public <V> ValueRepresentationStrategy<? super V> setTreeNodeRepresentationStrategy(final Class<V> valueClass, ValueRepresentationStrategy<V> treeNodeRepresentationStrategy)
	{
		return (ValueRepresentationStrategy<? super V>)classTreeNodeRepresentationStrategyMap.put(valueClass, treeNodeRepresentationStrategy);	//associate the strategy with the value class in the map
	}

	/**Returns the given tree node representation strategy assigned to produce representation components for the given value class.
	If there is no associated representation strategy, the default representation strategy (associated with the {@link Object} class) will be returned, if present.
	@param <V> The type of value to represent.
	@param valueClass The class of value with which the strategy should be associated.
	@return The strategy for generating components to represent values of the given type, or <code>null</code> if there is no associated representation strategy.
	*/	
	@SuppressWarnings("unchecked")	//we check the generic types before putting them in the map, so it's fine to cast the retrieved values
	public <V> ValueRepresentationStrategy<? super V> getTreeNodeRepresentationStrategy(final Class<V> valueClass)
	{
		ValueRepresentationStrategy<? super V> treeNodeRepresentationStrategy=(ValueRepresentationStrategy<? super V>)classTreeNodeRepresentationStrategyMap.get(valueClass);	//get the strategy linked to the value class in the map
			//TODO instead of directly trying Object.class, work up the hierarchy
		if(treeNodeRepresentationStrategy==null && !Object.class.equals(valueClass))	//if there is no associated representation strategy and this isn't the object class
		{
			treeNodeRepresentationStrategy=(ValueRepresentationStrategy<? super V>)classTreeNodeRepresentationStrategyMap.get(Object.class);	//get the default strategy (the one associated with Object.class)			
		}
		return treeNodeRepresentationStrategy;	//return the tree node representation strategy
	}

	/**Ensures the component for a particular tree hierarchy exists.
	This method is meant to be called primarily from the associated controller.
	This method recursively calls itself for all child nodes of the given tree node.
	@param <T> The type of value contained in the tree node.
	@param treeNode The tree node to verify.
	@exception IOException if there is an error updating the tree node view.
	*/
	public <T> void verifyTreeNodeComponent(final TreeNodeModel<T> treeNode) throws IOException
	{
//TODO important fix		final boolean editable=treeNode.isEditable();	//see if the tree node is editable
		final boolean editable=false;	//TODO fix
		TreeNodeComponentState treeNodeComponentState=getComponentState(treeNode);	//get the component information for this tree node
		if(treeNodeComponentState==null || treeNodeComponentState.isEditable()!=editable)	//if there is no component for this tree node, or the component has a different editable status
		{
				//TODO assert that there is a representation strategy, or otherwise check
//TODO del			final Component<?> valueComponent=((TreeControl.ValueRepresentationStrategy<T>)getTreeNodeRepresentationStrategy(treeNode.getValueClass())).createComponent(this, getModel(), treeNode, editable, false, false);	//create a new component for the tree node
//TODO bring back when Eclipse fixes its bug			final Component<?> valueComponent=component.getTreeNodeRepresentationStrategy(treeNode.getValueClass()).createComponent(treeModel, treeNode, editable, false, false);	//create a new component for the tree node
			final Component<?> valueComponent=getTreeNodeRepresentationStrategy(treeNode.getValueClass()).createComponent(this, getTreeModel(), treeNode, editable, false, false);	//create a new component for the tree node
			if(valueComponent!=null)	//if a value component is given TODO see if this check occurs in the table controller TODO make sure this is the way we want to do this---why not just return a label with a null value?
			{
				valueComponent.setParent(this);	//tell this component that this tree component is its parent
				treeNodeComponentState=new TreeNodeComponentState(valueComponent, editable);	//create a new component state for the tree node's component and metadata
				putComponentState(treeNode, treeNodeComponentState);	//store the component state in the map for next time
			}
		}
		for(final TreeNodeModel<?> childTreeNode:treeNode)	//for each child tree node
		{
			verifyTreeNodeComponent(childTreeNode);	//verify this child node tree
		}
	}

	/**Session constructor with a default data model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public TreeControl(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TreeControl(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultTreeModel(session));	//construct the class with a default model
	}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public TreeControl(final GuiseSession session, final TreeModel model)
	{
		this(session, null, model);	//construct the component, indicating that a default ID should be used
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param treeModel The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TreeControl(final GuiseSession session, final String id, final TreeModel treeModel)
	{
		super(session, id);	//construct the parent class
		this.treeModel=checkInstance(treeModel, "Tree model cannot be null.");	//save the tree model
		this.treeModel.addPropertyChangeListener(getRepeatPropertyChangeListener());	//listen and repeat all property changes of the tree model
		this.treeModel.addTreeNodePropertyChangeListener(treeNodePropertyChangeListener);	//listen and repeat all property changes of the tree nodes in the tree model
			//TODO listen for and repeat tree model-specific events
		setTreeNodeRepresentationStrategy(Object.class, new DefaultValueRepresentationStrategy<Object>(session));	//create a default representation strategy and set it as the default by associating it with the Object class
		setTreeNodeRepresentationStrategy(LabelModel.class, new LabelModelRepresentationStrategy(session));	//create and associate a label model representation strategy
//TODO fix		setTreeNodeRepresentationStrategy(MessageModel.class, new MessageModelRepresentationStrategy(session));	//create and associate a message model representation strategy
		setTreeNodeRepresentationStrategy(TextModel.class, new TextModelRepresentationStrategy(session));	//create and associate a text model representation strategy
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

	/**A strategy for generating components to represent tree node model values.
	The component ID should reflect a unique identifier for the tree node.
	@param <V> The type of value the strategy is to represent.
	@author Garret Wilson
	*/
	public interface ValueRepresentationStrategy<V>
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
	The label's ID will be generated by appending the hexadecimal representation of the object's hash code to the word "hash".
	@param <V> The type of value the strategy is to represent.
	@see Object#hashCode() 
	@author Garret Wilson
	*/
	public static abstract class AbstractValueRepresentationStrategy<V> implements ValueRepresentationStrategy<V>
	{

		/**The Guise session that owns this representation strategy.*/
		private final GuiseSession session;

			/**@return The Guise session that owns this representation strategy.*/
			public GuiseSession getSession() {return session;}

		/**Session constructor.
		@param session The Guise session that owns this representation strategy.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public AbstractValueRepresentationStrategy(final GuiseSession session)
		{
			this.session=checkInstance(session, "Session cannot be null");	//save the session
		}

		/**Determines an identifier for the given object.
		This implementation returns the hexadecimal representation of the object's hash code appended to the word "hash".
		@param value The value for which an identifier should be returned.
		@return A string identifying the value, or <code>null</code> if the provided value is <code>null</code>.
		@see Component#getID()
		*/
		public String getID(final V value)
		{
				//TODO put this in a common routine
				//TODO important: change the ID routine to generate a UUID-based ID, because the contained value can be modified
			return value!=null ? "hash"+Integer.toHexString(value.hashCode()) : null;	//if a value is given return the word "hash" followed by a hexadecimal representation of the value's hash code
		}
	}

	/**A default tree node representation strategy.
	A label component will be generated containing the default string representation of a value.
	@param <V> The type of value the strategy is to represent.
	@see Label
	@see Object#toString() 
	@see Object#hashCode() 
	@author Garret Wilson
	*/
	public static class DefaultValueRepresentationStrategy<V> extends AbstractValueRepresentationStrategy<V>
	{

		/**Session constructor.
		@param session The Guise session that owns this representation strategy.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public DefaultValueRepresentationStrategy(final GuiseSession session)
		{
			super(session);	//construct the parent class
		}

		/**Creates a component to represent the given tree node.
		This implementation returns a label with string value of the given value using the object's <code>toString()</code> method.
		The label's ID is set to the hexadecimal representation of the object's hash code appended to the word "hash".
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
			final GuiseSession session=getSession();	//get the session
			final String id=treeControl.createID(getID(treeNode.getValue()));	//get the ID from the value TODO don't get the ID from the value, as this can change if edited
			if(editable)	//if the component should be editable
			{
				final Class<N> valueClass=treeNode.getValueClass();	//get the value class of the column
				if(Boolean.class.isAssignableFrom(valueClass))	//if the value class is subclass of Boolean
				{
					return new CheckControl(session, id, (ValueModel<Boolean>)(Object)treeNode);	//create a new check control for the Boolean value model TODO find out why JDK 1.5.0_03 requires the intermediate Object cast
				}
				else	//for all other values
				{
					return new TextControl<N>(session, id, treeNode);	//generate a text input control for the value model
				}
			}
			else	//if the component should not be editable, return a label component
			{
				final N value=treeNode.getValue();	//get the current value
				if(value!=null)	//if there is value
				{
					final Label label=new Label(getSession(), getID(value));	//create a new label
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
	public static class LabelModelRepresentationStrategy extends AbstractValueRepresentationStrategy<LabelModel>
	{

		/**Session constructor.
		@param session The Guise session that owns this representation strategy.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public LabelModelRepresentationStrategy(final GuiseSession session)
		{
			super(session);	//construct the parent class
		}

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
			final GuiseSession session=getSession();	//get the session
			final String id=treeControl.createID(getID(treeNode.getValue()));	//get the ID from the value TODO don't get the ID from the value, as this can change if edited
			return new Label(session, id, treeNode.getValue());	//return a label from the label model
		}
	}

	/**A tree node representation strategy for a {@link TextModel}, generating a {@link Text} component.
	@see Message
	@author Garret Wilson
	*/
	public static class TextModelRepresentationStrategy extends AbstractValueRepresentationStrategy<TextModel>
	{

		/**Session constructor.
		@param session The Guise session that owns this representation strategy.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public TextModelRepresentationStrategy(final GuiseSession session)
		{
			super(session);	//construct the parent class
		}

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
			final GuiseSession session=getSession();	//get the session
			final String id=treeControl.createID(getID(treeNode.getValue()));	//get the ID from the value TODO don't get the ID from the value, as this can change if edited
			return new Text(session, id, treeNode.getValue());	//return a message from the message model
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
}
