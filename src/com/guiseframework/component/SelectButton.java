package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.model.*;

/**Button that stores a boolean value in its model representing the selected state.
A validator requiring a non-<code>null</code> value is automatically installed.
@author Garret Wilson
*/
public class SelectButton extends AbstractSelectActionControl<SelectButton> implements ButtonControl<SelectButton>
{

	/**Session constructor with a default data model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public SelectButton(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public SelectButton(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultValueModel<Boolean>(session, Boolean.class, Boolean.FALSE));	//construct the class with a default model
	}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public SelectButton(final GuiseSession session, final ValueModel<Boolean> model)
	{
		this(session, null, model);	//construct the component, indicating that a default ID should be used
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public SelectButton(final GuiseSession session, final String id, final ValueModel<Boolean> model)
	{
		super(session, id, model);	//construct the parent class
	}

}
