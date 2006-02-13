package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.converter.*;
import com.guiseframework.model.*;

/**Control to accept text input from the user representing a particular value type.
This control keeps track of literal text entered by the user, distinct from the value stored in the model.
Default converters are available for the following types:
<ul>
	<li><code>char[]</code></li>
	<li><code>java.lang.Boolean</code></li>
	<li><code>java.lang.Float</code></li>
	<li><code>java.lang.Integer</code></li>
	<li><code>java.lang.String</code></li>
</ul>
@param <V> The type of value the input text is to represent.
@author Garret Wilson
*/
public class TextControl<V> extends AbstractTextControl<V, TextControl<V>>
{

	/**The masked bound property.*/
	public final static String MASKED_PROPERTY=getPropertyName(TextControl.class, "masked");
	/**The maximum length bound property.*/
	public final static String MAXIMUM_LENGTH_PROPERTY=getPropertyName(TextControl.class, "maximumLength");

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

	/**The maximum number of input characters to allow, or -1 if there is no maximum length.*/
	private int maximumLength=-1;

		/**@return The maximum number of input characters to allow, or -1 if there is no maximum length.*/
		public int getMaximumLength() {return maximumLength;}

		/**Sets the maximum number of input characters to allow.
		This is a bound property of type <code>Integer</code>.
		@param newMaximumLength The new maximum number of input characters to allow, or -1 if there is no maximum length.
		@see #MAXIMUM_LENGTH_PROPERTY 
		*/
		public void setMaximumLength(final int newMaximumLength)
		{
			if(maximumLength!=newMaximumLength)	//if the value is really changing
			{
				final int oldMaximumLength=maximumLength;	//get the old value
				maximumLength=newMaximumLength;	//actually change the value
				firePropertyChange(MAXIMUM_LENGTH_PROPERTY, new Integer(oldMaximumLength), new Integer(newMaximumLength));	//indicate that the value changed
			}			
		}

	/**Session constructor with a default data model to represent a given type and a default converter.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	*/
	public TextControl(final GuiseSession session, final Class<V> valueClass)
	{
		this(session, null, valueClass);	//construct the component, indicating that a default ID should be used
	}

	/**Session, and model constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	*/
	public TextControl(final GuiseSession session, final ValueModel<V> model)
	{
		this(session, null, model);	//construct the component, indicating that a default ID should be used				
	}

	/**Session, model, and converter constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@param converter The converter for this component.
	@exception NullPointerException if the given session, model, and/or converter is <code>null</code>.
	*/
	public TextControl(final GuiseSession session, final ValueModel<V> model, final Converter<V, String> converter)
	{
		this(session, null, model, converter);	//construct the component, indicating that a default ID should be used		
	}

	/**Session and ID constructor with a default data model to represent a given type and a default converter.
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

	/**Session, ID, and model constructor with a default converter.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TextControl(final GuiseSession session, final String id, final ValueModel<V> model)
	{
		this(session, id, model, AbstractStringLiteralConverter.getInstance(session, model.getValueClass()));	//construct the class with a default converter
	}

	/**Session, ID, model, and converter constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param converter The converter for this component.
	@exception NullPointerException if the given session, model, and/or converter is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TextControl(final GuiseSession session, final String id, final ValueModel<V> model, final Converter<V, String> converter)
	{
		super(session, id, model, converter);	//construct the parent class
	}

}
