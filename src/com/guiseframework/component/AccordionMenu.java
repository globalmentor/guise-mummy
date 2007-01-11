package com.guiseframework.component;

import com.guiseframework.component.layout.*;
import com.guiseframework.event.ActionEvent;
import com.guiseframework.event.ActionListener;
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

	/**Performs the action with the given force and option.
	An {@link ActionEvent} is fired to all registered {@link ActionListener}s.
	This version toggles the menu's open status.
	@param force The zero-based force, such as 0 for no force or 1 for an action initiated by from a mouse single click.
	@param option The zero-based option, such as 0 for an event initiated by a mouse left button click or 1 for an event initiaged by a mouse right button click.
	*/
	public void performAction(final int force, final int option)
	{
		setOpen(!isOpen());	//toggle the menu's open status
		super.performAction(force, option);	//do the default action performance
	}

}
