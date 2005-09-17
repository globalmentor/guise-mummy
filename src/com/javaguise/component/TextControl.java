package com.javaguise.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;

/**Control to accept text input from the user representing a particular value type.
@param <V> The type of value the input text is to represent.
@author Garret Wilson
*/
public class TextControl<V> extends AbstractValueControl<V, TextControl<V>>
{

	/**The masked bound property.*/
	public final static String MASKED_PROPERTY=getPropertyName(TextControl.class, "masked");

	/**Whether the user input text is masked to prevent viewing of the literal entered value.*/
	private boolean masked=false;

		/**@return Whether the user input text is masked to prevent viewing of the litereal entered value.*/
		public boolean isMasked() {return masked;}

		/**Sets whether the user input text is masked to prevent viewing of the litereal entered value.
		This is a bound property of type <code>Boolean</code>.
		@param newMasked <code>true</code> if the user input text should be masked.
		@see #MASKED_PROPERTY
		*/
		public void setMasked(final boolean newMasked)
		{
			if(masked!=newMasked)	//if the value is really changing
			{
				final boolean oldEnabled=masked;	//get the old value
				masked=newMasked;	//actually change the value
				firePropertyChange(MASKED_PROPERTY, Boolean.valueOf(oldEnabled), Boolean.valueOf(newMasked));	//indicate that the value changed
			}			
		}

	/**Session constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	*/
	public TextControl(final GuiseSession session, final Class<V> valueClass)
	{
		this(session, null, valueClass);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model to represent a given type.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TextControl(final GuiseSession session, final String id, final Class<V> valueClass)
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
	public TextControl(final GuiseSession session, final String id, final ValueModel<V> model)
	{
		super(session, id, model);	//construct the parent class
	}

}
