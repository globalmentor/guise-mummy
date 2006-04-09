package com.guiseframework.component;

import com.guiseframework.model.*;

/**Selectable link that stores a separate value in a value model.
@param <V> The type of value the control represents.
@author Garret Wilson
*/
public class ValueSelectLink<V> extends AbstractSelectActionValueControl<V, ValueSelectLink<V>> implements SelectLinkControl<ValueSelectLink<V>>
{

	/**Constructor with a default data model to represent a given type.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public ValueSelectLink(final Class<V> valueClass)
	{
		this(new DefaultValueModel<V>(valueClass));	//construct the class with a default model
	}
	
	/**Value model constructor.
	@param valueModel The component value model.
	@exception NullPointerException if the given value model is <code>null</code>.
	*/
	public ValueSelectLink(final ValueModel<V> valueModel)
	{
		super(valueModel);	//construct the parent class
	}

}
