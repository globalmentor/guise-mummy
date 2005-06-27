package com.garretwilson.guise.component;

import com.garretwilson.guise.model.*;
import com.garretwilson.guise.session.GuiseSession;
import static com.garretwilson.lang.ObjectUtilities.*;

/**Control to accept input from the user.
@author Garret Wilson
*/
public class ValueControl<V> extends AbstractControl<ValueModel<V>>
{

	/**Session constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	*/
	public ValueControl(final GuiseSession<?> session, final Class<V> valueClass)
	{
		this(session, null, checkNull(valueClass, "Value class cannot be null."));	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ValueControl(final GuiseSession<?> session, final String id, final Class<V> valueClass)
	{
		this(session, id, new DefaultValueModel<V>(session, valueClass));	//construct the class with a default model
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public ValueControl(final GuiseSession<?> session, final String id, final ValueModel<V> model)
	{
		super(session, id, model);	//construct the parent class
	}

}
