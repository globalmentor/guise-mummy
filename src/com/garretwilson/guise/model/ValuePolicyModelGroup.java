package com.garretwilson.guise.model;

import java.beans.PropertyChangeEvent;

import com.garretwilson.beans.*;

/**An abstract implementation of a group of value models implementing a value policy across several models, such as mutual exclusion.
When value models are added to the list, the group adds itself as a property value change listeners to the model, listening for changes in the value.
@param <V> The type of value contained in the value model.
@author Garret Wilson.
@see com.garretwilson.guise.model.ValueModel
*/
public abstract class ValuePolicyModelGroup<V> extends AbstractModelGroup<ValueModel<V>> implements PropertyValueChangeListener<V>
{

	/**Actual implementation of adding a model to the group.
	This version adds this class instance as a listener to the model's value changing.
	@param valueModel The model to add to the group.	
	*/
	protected void addImpl(final ValueModel<V> valueModel)
	{
		super.addImpl(valueModel);	//add the model normally
		valueModel.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, this);	//listen for value changes
	}

	/**Actual implementation of removing a model from the group.
	This version removes this class instance as a listener to the model's value changing.
	@param valueModel The model to remove from the group.
	*/
	protected void removeImpl(final ValueModel<V> valueModel)
	{
		valueModel.removePropertyChangeListener(ValueModel.VALUE_PROPERTY, this);	//stop listening for value changes
		super.removeImpl(valueModel);	//remove the model normally
	}

	/**Called when a bound property is changed.
	This not-generics version calls the generic version, creating a new event if necessary.
	No checks are made at compile time to ensure the given event actually supports the given generic type.
	@param propertyChangeEvent An event object describing the event source, the property that has changed, and its old and new values.
	@see PropertyValueChangeListener#propertyValueChange(PropertyValueChangeEvent)
	*/
	@SuppressWarnings("unchecked")
	public final void propertyChange(final PropertyChangeEvent propertyChangeEvent)
	{
		propertyValueChange((PropertyValueChangeEvent<V>)AbstractPropertyValueChangeListener.getPropertyValueChangeEvent(propertyChangeEvent));	//call the generic version of the method with the genericized event object
	}

	/**Model constructor.
	@param models Zero or more models with which to initially place in the group.
	@exception NullPointerException if one of the models is <code>null</code>.
	*/
	public ValuePolicyModelGroup(final ValueModel<V>... models)
	{
		super(models);	//construct the parent class
	}

}
