package com.guiseframework.component;

import com.guiseframework.component.layout.*;
import com.guiseframework.event.ActionEvent;
import com.guiseframework.event.ActionListener;
import com.guiseframework.model.*;
import com.guiseframework.prototype.MenuPrototype;

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
		this(new DefaultLabelModel(), new DefaultActionModel(), new DefaultEnableable(), axis);	//construct the class with default models
	}

	/**Label model, action model, enableable, and menu layout constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param enableable The enableable object in which to store enabled status.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given label model, action model, enableable, and/or layout is <code>null</code>.
	*/
	public AccordionMenu(final LabelModel labelModel, final ActionModel actionModel, final Enableable enableable, final Flow axis)
	{
		super(labelModel, actionModel, enableable, new MenuLayout(axis));	//construct the parent class
		setRolloverOpenEnabled(false);	//default to not showing the menu as open upon rollover
		addActionListener(new ActionListener()	//create an action listener to toggle the menu's open status
		{
			public void actionPerformed(final ActionEvent actionEvent)	//if the action is performed
			{
				setOpen(!isOpen());	//toggle the menu's open status
			}
		});
	}

	/**Prototype and axis constructor.
	@param actionPrototype The prototype on which this component should be based.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given prototype is <code>null</code>.
	*/
	public AccordionMenu(final MenuPrototype menuPrototype, final Flow axis)
	{
		this(menuPrototype, menuPrototype, menuPrototype, axis);	//use the menu prototype as every needed model
	}
	
}
