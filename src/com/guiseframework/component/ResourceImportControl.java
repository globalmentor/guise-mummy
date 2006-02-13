package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.model.*;

/**Control accepting a resource to be imported, such as a web file upload. 
@author Garret Wilson
*/
public class ResourceImportControl extends AbstractValueControl<ResourceImport, ResourceImportControl>
{

	/**Session constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	*/
	public ResourceImportControl(final GuiseSession session)
	{
		this(session, null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ResourceImportControl(final GuiseSession session, final String id)
	{
		this(session, id, new DefaultValueModel<ResourceImport>(session, ResourceImport.class));	//construct the class with a default model
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ResourceImportControl(final GuiseSession session, final String id, final ValueModel<ResourceImport> model)
	{
		super(session, id, model);	//construct the parent class
	}

}
