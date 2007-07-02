package com.guiseframework.component;

import com.guiseframework.model.DefaultValueModel;
import com.guiseframework.model.ValueModel;

/**Default implementation of a frame meant for communication of a value.
@param <V> The value to be communicated.
@author Garret Wilson
*/
public class DefaultDialogFrame<V> extends AbstractDialogFrame<V>
{

	/**Value class constructor.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public DefaultDialogFrame(final Class<V> valueClass)
	{
		this(new DefaultValueModel<V>(valueClass));	//use a default value model
	}

	/**Value class and component constructor.
	@param valueClass The class indicating the type of value held in the model.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public DefaultDialogFrame(final Class<V> valueClass, final Component component)
	{
		this(new DefaultValueModel<V>(valueClass), component);	//use a default value model
	}

	/**Value model constructor.
	@param valueModel The component value model.
	@exception NullPointerException if the given value model is <code>null</code>.
	*/
	public DefaultDialogFrame(final ValueModel<V> valueModel)
	{
		this(valueModel, new LayoutPanel());	//default to a layout panel
	}

	/**Value model and component constructor.
	@param valueModel The component value model.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@exception NullPointerException if the given value model is <code>null</code>.
	*/
	public DefaultDialogFrame(final ValueModel<V> valueModel, final Component component)
	{
		super(valueModel, component);	//construct the parent class
	}

}
