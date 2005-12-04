package com.javaguise.component;

import com.javaguise.GuiseSession;
import com.javaguise.component.layout.Layout;
import com.javaguise.component.layout.RegionLayout;
import com.javaguise.model.DefaultLabelModel;
import com.javaguise.model.LabelModel;

/**Default implementation of a panel that represents a point of modal navigation with default region layout.
Each panel subclass must provide either a Guise session constructor; or a Guise session and string ID constructor.
@param <R> The type of modal result this modal panel produces.
@author Garret Wilson
@see RegionLayout
*/
public class DefaultModalNavigationPanel<R> extends AbstractModalNavigationPanel<R, DefaultModalNavigationPanel<R>>
{

	/**Session constructor with a default region layout.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultModalNavigationPanel(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and model constructor with a default region layout.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultModalNavigationPanel(final GuiseSession session, final LabelModel model)
	{
		this(session, (String)null, model);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default region layout.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultModalNavigationPanel(final GuiseSession session, final String id)
	{
		this(session, id, new RegionLayout(session));	//default to flowing vertically
	}

	/**Session, ID, and model constructor with a default region layout.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultModalNavigationPanel(final GuiseSession session, final String id, final LabelModel model)
	{
		this(session, id, new RegionLayout(session), model);	//default to flowing vertically
	}

	/**Session and layout constructor.
	@param session The Guise session that owns this component.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session and/or layout is <code>null</code>.
	*/
	public DefaultModalNavigationPanel(final GuiseSession session, final Layout layout)
	{
		this(session, null, layout);	//construct the component with the layout, indicating that a default ID should be used
	}

	/**Session, layout, and model constructor.
	@param session The Guise session that owns this component.
	@param layout The layout definition for the container.
	@param model The component data model.
	@exception NullPointerException if the given session, layout, and/or model is <code>null</code>.
	*/
	public DefaultModalNavigationPanel(final GuiseSession session, final Layout layout, final LabelModel model)
	{
		this(session, null, layout, model);	//construct the component with the layout, indicating that a default ID should be used
	}

	/**Session, ID, and layout constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session and/or layout is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultModalNavigationPanel(final GuiseSession session, final String id, final Layout layout)
	{
		this(session, id, layout, new DefaultLabelModel(session));	//construct the class with a default model
	}

	/**Session, ID, layout, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@param model The component data model.
	@exception NullPointerException if the given session, layout, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultModalNavigationPanel(final GuiseSession session, final String id, final Layout layout, final LabelModel model)
	{
		super(session, id, layout, model);	//construct the parent class
	}

	/**Ends this frame's modal interaction and navigates either to the previous modal navigation or to this frame's referring URI, if any.
	@param result The result of this frame's modal interaction, or <code>null</code> if no result is given.
	@see #setResult(R)
	@see GuiseSession#endModalNavigation(ModalPanel)
	*/
	public void endModal(final R result)
	{
		setResult(result);	//update the result
		getSession().endModalNavigation(this);	//end modal navigation for this modal frame
	}

}
