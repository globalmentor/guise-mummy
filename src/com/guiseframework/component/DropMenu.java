package com.guiseframework.component;

import com.guiseframework.component.layout.*;
import com.guiseframework.model.*;
import com.guiseframework.prototype.MenuPrototype;

/**A menu that drops its children down from the top or over to the side.
By default rollover open is enabled.
@author Garret Wilson
@see Menu#setRolloverOpenEnabled
*/
public class DropMenu extends AbstractMenu<DropMenu>
{

	/**Axis constructor.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given axis is <code>null</code>.
	*/
	public DropMenu(final Flow axis)
	{
		this(new DefaultLabelModel(), new DefaultActionModel(), new DefaultEnableable(), axis);	//construct the class with default models
	}

	/**Label model, action model, enableable, and menu layout constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param enableable The enableable object in which to store enabled status.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given label model, action model, enableable, and/or layout is <code>null</code>.
	*/
	public DropMenu(final LabelModel labelModel, final ActionModel actionModel, final Enableable enableable, final Flow axis)
	{
		super(labelModel, actionModel, enableable, new MenuLayout(axis));	//construct the parent class
		setRolloverOpenEnabled(true);	//default to showing the menu as open upon rollover
	}

	/**Prototype and axis constructor.
	@param actionPrototype The prototype on which this component should be based.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given prototype is <code>null</code>.
	*/
	public DropMenu(final MenuPrototype menuPrototype, final Flow axis)
	{
		this(menuPrototype, menuPrototype, menuPrototype, axis);	//use the menu prototype as every needed model
	}

}
