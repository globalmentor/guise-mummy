package com.javaguise.component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;

/**A tree control.
@author Garret Wilson
*/
public class TreeControl extends AbstractModelComponent<TreeModel, TreeControl>
{

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
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TreeControl(final GuiseSession session, final String id, final TreeModel model)
	{
		super(session, id, model);	//construct the parent class
		setTreeNodeRepresentationStrategy(Object.class, new DefaultValueRepresentationStrategy<Object>(session));	//create a default representation strategy and set it as the default by associating it with the Object class
		setTreeNodeRepresentationStrategy(LabelModel.class, new LabelModelRepresentationStrategy(session));	//create and associate a label model representation strategy
		setTreeNodeRepresentationStrategy(MessageModel.class, new MessageModelRepresentationStrategy(session));	//create and associate a message model representation strategy
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
		@param model The model containing the value.
		@param treeNode The node containing the value. 
		@param editable Whether values in this column are editable.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		public Component<?> createComponent(final TreeModel model, final TreeNodeModel<V> treeNode, final boolean editable, final boolean selected, final boolean focused);
//TODO bring back after Eclipse fixes its bug		public <N extends V> Component<?> createComponent(final TreeModel model, final TreeNodeModel<N> treeNode, final boolean editable, final boolean selected, final boolean focused);
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
			this.session=checkNull(session, "Session cannot be null");	//save the session
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
	public static class DefaultValueRepresentationStrategy<N> extends AbstractValueRepresentationStrategy<N>
//TODO bring back after Eclipse fixes its bug	public static class DefaultValueRepresentationStrategy<V> extends AbstractValueRepresentationStrategy<V>
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
		@param model The model containing the value.
		@param treeNode The node containing the value. 
		@param editable Whether values in this column are editable.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		@SuppressWarnings("unchecked")
		public Component<?> createComponent(final TreeModel model, final TreeNodeModel<N> treeNode, final boolean editable, final boolean selected, final boolean focused)
//TODO bring back after Eclipse fixes its bug		public <N extends V> Component<?> createComponent(final TreeModel model, final TreeNodeModel<N> treeNode, final boolean editable, final boolean selected, final boolean focused)
		{
			final GuiseSession session=getSession();	//get the session
			final String id=getID(treeNode.getValue());	//get the ID from the value TODO don't get the ID from the value, as this can change if edited
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
				return value!=null	//if there is a value
					? new Label(getSession(), getID(value), new DefaultLabelModel(getSession(), value.toString()))	//generate a label containing the value's string value
					: null;	//otherwise return null
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
		@param model The model containing the value.
		@param treeNode The node containing the value. 
		@param editable Whether values in this column are editable.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		public Label createComponent(final TreeModel model, final TreeNodeModel<LabelModel> treeNode, final boolean editable, final boolean selected, final boolean focused)
//TODO bring back after Eclipse fixes its bug		public <N extends LabelModel> Label createComponent(final TreeModel model, final TreeNodeModel<N> treeNode, final boolean editable, final boolean selected, final boolean focused)
		{
			final GuiseSession session=getSession();	//get the session
			final String id=getID(treeNode.getValue());	//get the ID from the value TODO don't get the ID from the value, as this can change if edited
			return new Label(session, id, treeNode.getValue());	//return a label from the label model
		}
	}

	/**A tree node representation strategy for a message model, generating a message component.
	@see Message
	@author Garret Wilson
	*/
	public static class MessageModelRepresentationStrategy extends AbstractValueRepresentationStrategy<MessageModel>
	{

		/**Session constructor.
		@param session The Guise session that owns this representation strategy.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public MessageModelRepresentationStrategy(final GuiseSession session)
		{
			super(session);	//construct the parent class
		}

		/**Creates a message to represent the given tree node.
		@param <N> The type of value contained in the node.
		@param model The model containing the value.
		@param treeNode The node containing the value. 
		@param editable Whether values in this column are editable.
		@param selected <code>true</code> if the value is selected.
		@param focused <code>true</code> if the value has the focus.
		@return A new component to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		public Message createComponent(final TreeModel model, final TreeNodeModel<MessageModel> treeNode, final boolean editable, final boolean selected, final boolean focused)
//TODO bring back after Eclipse fixes its bug		public <N extends LabelModel> Label createComponent(final TreeModel model, final TreeNodeModel<N> treeNode, final boolean editable, final boolean selected, final boolean focused)
		{
			final GuiseSession session=getSession();	//get the session
			final String id=getID(treeNode.getValue());	//get the ID from the value TODO don't get the ID from the value, as this can change if edited
			return new Message(session, id, treeNode.getValue());	//return a message from the message model
		}
	}

}
