package com.guiseframework.component;

import com.guiseframework.component.layout.*;
import com.guiseframework.model.*;

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
		this(new DefaultActionModel(), axis);	//construct the class with a default model
	}

	/**Action model and axis constructor.
	@param actionModel The component action model.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given action model and/or axis is <code>null</code>.
	*/
	public DropMenu(final ActionModel actionModel, final Flow axis)
	{
		super(new MenuLayout(axis), actionModel);	//construct the parent class
		setRolloverOpenEnabled(true);	//default to showing the menu as open upon rollover
	}

}
