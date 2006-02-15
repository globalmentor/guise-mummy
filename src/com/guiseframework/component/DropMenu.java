package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.*;
import com.guiseframework.model.*;

/**A menu that drops its children down from the top or over to the side.
By default rollover open is enabled.
@author Garret Wilson
@see Menu#setRolloverOpenEnabled
*/
public class DropMenu extends AbstractMenu<DropMenu>
{

	/**Session and axis constructor.
	@param session The Guise session that owns this component.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given session and/or axis is <code>null</code>.
	*/
	public DropMenu(final GuiseSession session, final Flow axis)
	{
		this(session, (String)null, axis);	//construct the component with the axis, indicating that a default ID should be used
	}

	/**Session, axis, and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given session, axis, and/or model is <code>null</code>.
	*/
	public DropMenu(final GuiseSession session, final Model model, final Flow axis)
	{
		this(session, null, model, axis);	//construct the component with the axis, indicating that a default ID should be used
	}

	/**Session, ID, and axis constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given session and/or axis is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DropMenu(final GuiseSession session, final String id, final Flow axis)
	{
		this(session, id, new DefaultModel(session), axis);	//construct the class with a default model
	}

	/**Session, ID, axis, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given session, axis, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DropMenu(final GuiseSession session, final String id, final Model model, final Flow axis)
	{
		super(session, id, new MenuLayout(session, axis), model);	//construct the parent class
		setRolloverOpenEnabled(true);	//default to showing the menu as open upon rollover
	}

}
