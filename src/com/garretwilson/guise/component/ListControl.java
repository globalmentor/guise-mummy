package com.garretwilson.guise.component;

import com.garretwilson.guise.model.*;
import com.garretwilson.guise.session.GuiseSession;

/**Control to allow selection of one or more values from a list.
@param <V> The type of values to select.
@author Garret Wilson
*/
public class ListControl<V> extends AbstractSelectControl<V, ModelComponent<? extends LabelModel, ?>, ListControl<V>>
{

	/**Session constructor with a default data model to represent a given type with multiple selection.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	*/
	public ListControl(final GuiseSession<?> session, final Class<V> valueClass)
	{
		this(session, null, valueClass);	//construct the component, indicating that a default ID should be used
	}

	/**Session constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@exception NullPointerException if the given session, value class, and/or selection strategy is <code>null</code>.
	*/
	public ListControl(final GuiseSession<?> session, final Class<V> valueClass, final SelectionStrategy<V> selectionStrategy)
	{
		this(session, null, valueClass, selectionStrategy);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model to represent a given type with multiple selection.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession<?> session, final String id, final Class<V> valueClass)
	{
		this(session, id, new DefaultSelectModel<V>(session, valueClass));	//construct the class with a default model
	}

	/**Session and ID constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@param selectionStrategy The strategy for selecting values in the model.
	@exception NullPointerException if the given session, value class, and/or selection strategy is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession<?> session, final String id, final Class<V> valueClass, final SelectionStrategy<V> selectionStrategy)
	{
		this(session, id, new DefaultSelectModel<V>(session, valueClass, selectionStrategy));	//construct the class with a default model
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession<?> session, final String id, final SelectModel<V> model)
	{
		super(session, id, model);	//construct the parent class
		setValueRepresentationStrategy(new DefaultValueRepresentationStrategy());	//install a default value representation strategy
	}

	/**Session, ID, model, and value representation strategy constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param valueRepresentationStrategy The strategy to create label models to represent this model's values.
	@exception NullPointerException if the given session, model, and/or value representation strategy is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ListControl(final GuiseSession<?> session, final String id, final SelectModel<V> model, final ValueRepresentationStrategy<V> valueRepresentationStrategy)
	{
		super(session, id, model, valueRepresentationStrategy);	//construct the parent class
	}

	/**A strategy for generating components to represents model values in a list select control.
	The component ID should reflect a unique identifier of the item
	@param <RR> The type of value the strategy is to represent.
	@author Garret Wilson
	*/
	public interface ValueRepresentationStrategy<RR> extends SelectControl.ValueRepresentationStrategy<RR, ModelComponent<? extends LabelModel, ?>>
	{
	}

	/**A default value representation strategy.
	A label component will be generated containing the default string representation of a value.
	The label's ID will be generated by appending the hexadecimal representation of the object's hash code to the word "hash".
	@see Object#toString() 
	@see Object#hashCode() 
	@author Garret Wilson
	*/
	protected class DefaultValueRepresentationStrategy implements ValueRepresentationStrategy<V>
	{

		/**Creates a label for the given value.
		This implementation returns a label with string value of the given value using the object's <code>toString()</code> method.
		The clabel's ID is set to the hexadecimal representation of the object's hash code appended to the word "hash".
		@param value The value for which a label should be created.
		@return A label to represent the given value, or <code>null</code> if the provided value is <code>null</code>.
		*/
		public Label createComponent(final V value)
		{
			return value!=null	//if there is a value
					? new Label(getSession(), getID(value), new DefaultLabelModel(getSession(), value.toString()))	//generate a label containing the value's string value
					: null;	//otherwise return null
		}

		/**Determines an identier for the given object.
		This implementation returns the hexadecimal representation of the object's hash code appended to the word "hash".
		@param value The value for which an identifier should be returned.
		@return A string identifying the value, or <code>null</code> if the provided value is <code>null</code>.
		@see Component#getID()
		*/
		public String getID(final V value)
		{
			return value!=null ? "hash"+Integer.toHexString(value.hashCode()) : null;	//if a value is given return the word "hash" followed by a hexadecimal representation of the value's hash code
		}
	}
}
