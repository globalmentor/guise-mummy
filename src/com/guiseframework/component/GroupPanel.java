package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;
import static com.garretwilson.text.TextUtilities.*;

import java.net.URI;

import javax.mail.internet.ContentType;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.*;
import com.guiseframework.model.DefaultModel;
import com.guiseframework.model.Model;

/**A panel for grouping multiple components with a default page flow layout.
@author Garret Wilson
*/
public class GroupPanel extends AbstractBox<GroupPanel> implements Panel<GroupPanel>
{

	/**Session constructor with a default vertical flow layout.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public GroupPanel(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and model constructor with a default vertical flow layout.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public GroupPanel(final GuiseSession session, final Model model)
	{
		this(session, (String)null, model);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default vertical flow layout.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public GroupPanel(final GuiseSession session, final String id)
	{
		this(session, id, new FlowLayout(session, Flow.PAGE));	//default to flowing vertically
	}

	/**Session, ID, and model constructor with a default vertical flow layout.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public GroupPanel(final GuiseSession session, final String id, final Model model)
	{
		this(session, id, new FlowLayout(session, Flow.PAGE), model);	//default to flowing vertically
	}

	/**Session and layout constructor.
	@param session The Guise session that owns this component.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session and/or layout is <code>null</code>.
	*/
	public GroupPanel(final GuiseSession session, final Layout layout)
	{
		this(session, null, layout);	//construct the component with the layout, indicating that a default ID should be used
	}

	/**Session, layout, and model constructor.
	@param session The Guise session that owns this component.
	@param layout The layout definition for the container.
	@param model The component data model.
	@exception NullPointerException if the given session, layout, and/or model is <code>null</code>.
	*/
	public GroupPanel(final GuiseSession session, final Layout layout, final Model model)
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
	public GroupPanel(final GuiseSession session, final String id, final Layout layout)
	{
		this(session, id, layout, new DefaultModel(session));	//construct the class with a default model
	}

	/**Session, ID, layout, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@param model The component data model.
	@exception NullPointerException if the given session, layout, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public GroupPanel(final GuiseSession session, final String id, final Layout layout, final Model model)
	{
		super(session, id, layout, model);	//construct the parent class
	}

}
