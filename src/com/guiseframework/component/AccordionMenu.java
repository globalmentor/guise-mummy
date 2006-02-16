package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.*;
import com.guiseframework.model.*;

/**A menu that collapses its children's children between its children, like an accordion.
By default rollover open is disabled.
@author Garret Wilson
@see Menu#setRolloverOpenEnabled
*/
public class AccordionMenu extends AbstractMenu<AccordionMenu>
{

	/**Session and axis constructor.
	@param session The Guise session that owns this component.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given session and/or axis is <code>null</code>.
	*/
	public AccordionMenu(final GuiseSession session, final Flow axis)
	{
		this(session, (String)null, axis);	//construct the component with the axis, indicating that a default ID should be used
	}

	/**Session, axis, and model constructor.
	@param session The Guise session that owns this component.
	@param actionModel The component action model.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given session, axis, and/or model is <code>null</code>.
	*/
	public AccordionMenu(final GuiseSession session, final ActionModel actionModel, final Flow axis)
	{
		this(session, null, actionModel, axis);	//construct the component with the axis, indicating that a default ID should be used
	}

	/**Session, ID, and axis constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given session and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AccordionMenu(final GuiseSession session, final String id, final Flow axis)
	{
		this(session, id, new DefaultActionModel(session), axis);	//construct the class with a default model
	}

	/**Session, ID, axis, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param actionModel The component action model.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given session, axis, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AccordionMenu(final GuiseSession session, final String id, final ActionModel actionModel, final Flow axis)
	{
		super(session, id, new MenuLayout(session, axis), actionModel);	//construct the parent class
		setRolloverOpenEnabled(false);	//default to not showing the menu as open upon rollover
	}

}
