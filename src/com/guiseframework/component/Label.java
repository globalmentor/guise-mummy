package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.model.*;

/**A label component.
This component installs a default export strategy supporting export of the following content types:
<ul>
	<li>The label content type.</li>
</ul>
@author Garret Wilson
*/
public class Label extends AbstractLabel<Label>
{

	/**Session constructor with a default label model.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public Label(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default label model.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Label(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultLabelModel(session));	//construct the class with a default label model
	}

	/**Session and label model constructor.
	@param session The Guise session that owns this component.
	@param labelModel The component label model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public Label(final GuiseSession session, final LabelModel labelModel)
	{
		this(session, null, labelModel);	//construct the class, indicating that a default ID should be used
	}

	/**Session, ID, and label model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param labelModel The component label model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public Label(final GuiseSession session, final String id, final LabelModel labelModel)
	{
		super(session, id, labelModel);	//construct the parent class
	}

}
