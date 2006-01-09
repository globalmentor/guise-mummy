package com.javaguise.component;

import com.javaguise.GuiseSession;
import com.javaguise.component.layout.MenuLayout;
import com.javaguise.component.layout.ReferenceLayout;
import com.javaguise.model.DefaultTextModel;
import com.javaguise.model.TextModel;

/**A text component.
This component may have child components, each bound to a specific ID in the text.
When the text is rendered, XML elements with IDs referencing child components will be replaced with representations of those child components.
Child element ID reference replacement can only occur if the text has an XML-based content type (such as XHTML).
@author Garret Wilson
*/
public class Text extends AbstractContainer<Text>
{

	/**@return The data model used by this component.*/
	public TextModel getModel() {return (TextModel)super.getModel();}

	/**@return The layout definition for the text.*/
	public ReferenceLayout getLayout() {return (ReferenceLayout)super.getLayout();}	//a text component can only have a reference layout

	/**Session constructor with a default model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Text(final GuiseSession session)
	{
		this(session, null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Text(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultTextModel(session));	//construct the class with a default model
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Text(final GuiseSession session, final String id, final TextModel model)
	{
		super(session, id, new ReferenceLayout(session), model);	//construct the parent class
	}
}
