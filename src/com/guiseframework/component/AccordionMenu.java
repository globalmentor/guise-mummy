package com.guiseframework.component;

import com.guiseframework.component.layout.*;
import com.guiseframework.model.*;

/**A menu that collapses its children's children between its children, like an accordion.
By default rollover open is disabled.
@author Garret Wilson
@see Menu#setRolloverOpenEnabled
*/
public class AccordionMenu extends AbstractMenu<AccordionMenu>
{

	/**Axis constructor.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given axis is <code>null</code>.
	*/
	public AccordionMenu(final Flow axis)
	{
		this(new DefaultActionModel(), axis);	//construct the class with a default model
	}

	/**Action model and axis constructor.
	@param actionModel The component action model.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given action model and/or axis is <code>null</code>.
	*/
	public AccordionMenu(final ActionModel actionModel, final Flow axis)
	{
		super(new MenuLayout(axis), actionModel);	//construct the parent class
		setRolloverOpenEnabled(false);	//default to not showing the menu as open upon rollover
	}

}
