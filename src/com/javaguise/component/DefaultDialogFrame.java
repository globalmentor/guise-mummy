package com.javaguise.component;

import com.javaguise.GuiseSession;
import com.javaguise.model.DefaultValueModel;
import com.javaguise.model.ValueModel;

/**Default implementation of a frame meant for communication of a value.
@param <V> The value to be communicated.
@author Garret Wilson
*/
public class DefaultDialogFrame<V> extends AbstractDialogFrame<V, DefaultDialogFrame<V>>
{

	/**Session constructor.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	*/
	public DefaultDialogFrame(final GuiseSession session, final Class<V> valueClass)
	{
		this(session, (String)null, valueClass);	//construct the component, indicating that a default ID should be used
	}

	/**Session and component constructor.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	*/
	public DefaultDialogFrame(final GuiseSession session, final Class<V> valueClass, final Component<?> component)
	{
		this(session, (String)null, valueClass, component);	//construct the component, indicating that a default ID should be used
	}

	/**Session and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultDialogFrame(final GuiseSession session, final ValueModel<V> model)
	{
		this(session, (String)null, model);	//construct the component, indicating that a default ID should be used
	}

	/**Session, model, and component constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultDialogFrame(final GuiseSession session, final ValueModel<V> model, final Component<?> component)
	{
		this(session, (String)null, model, component);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultDialogFrame(final GuiseSession session, final String id, final Class<V> valueClass)
	{
		this(session, id, new DefaultValueModel<V>(session, valueClass));	//use a default value model
	}

	/**Session, ID, and component constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultDialogFrame(final GuiseSession session, final String id, final Class<V> valueClass, final Component<?> component)
	{
		this(session, id, new DefaultValueModel<V>(session, valueClass), component);	//use a default value model
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultDialogFrame(final GuiseSession session, final String id, final ValueModel<V> model)
	{
		this(session, id, model, new LayoutPanel(session));	//default to a layout panel
	}

	/**Session, ID, model, and component constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param component The single child component, or <code>null</code> if this frame should have no child component.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public DefaultDialogFrame(final GuiseSession session, final String id, final ValueModel<V> model, final Component<?> component)
	{
		super(session, id, model, component);	//construct the parent class
	}

}
