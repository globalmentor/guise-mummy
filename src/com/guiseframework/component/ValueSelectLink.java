package com.guiseframework.component;

import com.guiseframework.model.*;

/**Selectable link that stores a separate value in a value model.
@param <V> The type of value the control represents.
@author Garret Wilson
*/
public class ValueSelectLink<V> extends AbstractSelectActionValueControl<V, ValueSelectLink<V>> implements SelectLinkControl<ValueSelectLink<V>>
{

	/**Value class constructor.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public ValueSelectLink(final Class<V> valueClass)
	{
		this(new DefaultLabelModel(), new DefaultActionModel(), new DefaultValueModel<V>(valueClass), new DefaultEnableable());	//construct the class with default models
	}

	/**Label model, action model, value model, and enableable object constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param valueModel The component value model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model, action model, and/or enableable object is <code>null</code>.
	*/
	public ValueSelectLink(final LabelModel labelModel, final ActionModel actionModel, final ValueModel<V> valueModel, final Enableable enableable)
	{
		super(labelModel, actionModel, valueModel, enableable);	//construct the parent class		
	}

}
