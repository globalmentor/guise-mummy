package com.javaguise.component;

import java.util.MissingResourceException;

import com.javaguise.GuiseSession;
import com.javaguise.component.layout.*;
import com.javaguise.model.DefaultLabelModel;
import com.javaguise.model.LabelModel;

/**A panel for grouping multiple components with a default page flow layout.
@author Garret Wilson
*/
public class GroupPanel extends AbstractBox<GroupPanel> implements Panel<GroupPanel>
{

	/**@return The data model used by this component.*/
	public LabelModel getModel() {return (LabelModel)super.getModel();}

	/**Determines the text of the label.
	If a label is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
	@return The label text, or <code>null</code> if there is no label text.
	@exception MissingResourceException if there was an error loading the value from the resources.
	@see #getLabelResourceKey()
	*/
	public String getLabelText() throws MissingResourceException	//TODO testing
	{
		return getModel().getLabel();	//TODO fix
	}

	/**Sets the text of the label.
	This is a bound property.
	@param newLabel The new text of the label.
	@see LabelModel#LABEL_PROPERTY
	*/
	public void setLabelText(final String newLabel)	//TODO testing
	{
		getModel().setLabel(newLabel);	//TODO fix
	}

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
	public GroupPanel(final GuiseSession session, final LabelModel model)
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
	public GroupPanel(final GuiseSession session, final String id, final LabelModel model)
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
	public GroupPanel(final GuiseSession session, final Layout layout, final LabelModel model)
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
	public GroupPanel(final GuiseSession session, final String id, final Layout layout, final LabelModel model)
	{
		super(session, id, layout, model);	//construct the parent class
	}

}
