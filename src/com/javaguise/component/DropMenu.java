package com.javaguise.component;

import com.javaguise.component.layout.*;
import com.javaguise.model.DefaultLabelModel;
import com.javaguise.model.LabelModel;
import com.javaguise.session.GuiseSession;

/**A menu control that drops its children down from the top or over to the side.
This component uses a {@link MenuLayout}.
@author Garret Wilson
@see MenuLayout
*/
public class DropMenu extends AbstractContainerControl<DropMenu>
{

	/**@return The data model used by this component.*/
	public LabelModel getModel() {return (LabelModel)super.getModel();}

	/**Session and axis constructor.
	@param session The Guise session that owns this component.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given session and/or axis is <code>null</code>.
	*/
	public DropMenu(final GuiseSession session, final Orientation.Flow axis)
	{
		this(session, (String)null, axis);	//construct the component with the axis, indicating that a default ID should be used
	}

	/**Session, axis, and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given session, axis, and/or model is <code>null</code>.
	*/
	public DropMenu(final GuiseSession session, final LabelModel model, final Orientation.Flow axis)
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
	public DropMenu(final GuiseSession session, final String id, final Orientation.Flow axis)
	{
		this(session, id, new DefaultLabelModel(session), axis);	//construct the class with a default model
	}

	/**Session, ID, axis, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param axis The axis along which the menu is oriented.
	@exception NullPointerException if the given session, axis, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DropMenu(final GuiseSession session, final String id, final LabelModel model, final Orientation.Flow axis)
	{
		super(session, id, new MenuLayout(session, axis), model);	//construct the parent class
	}

	/**@return The layout definition for the menu.*/
	public MenuLayout getLayout() {return (MenuLayout)super.getLayout();}	//a menu can only have a menu layout

}
