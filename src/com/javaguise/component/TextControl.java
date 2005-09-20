package com.javaguise.component;

import static com.garretwilson.lang.ClassUtilities.*;

import com.garretwilson.beans.AbstractPropertyValueChangeListener;
import com.garretwilson.beans.PropertyValueChangeEvent;
import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.util.Debug;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.javaguise.converter.*;
import com.javaguise.model.*;
import com.javaguise.session.GuiseSession;
import com.javaguise.validator.ValidationException;

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
public class TextControl<V> extends AbstractValueControl<V, TextControl<V>>
{

	/**The converter bound property.*/
	public final static String CONVERTER_PROPERTY=getPropertyName(TextControl.class, "converter");
	/**The text literal bound property.*/
	public final static String TEXT_PROPERTY=getPropertyName(TextControl.class, "text");
	/**The masked bound property.*/
	public final static String MASKED_PROPERTY=getPropertyName(TextControl.class, "masked");


	/**The map of string literal registered converters for each type.*/
//TODO del	private final Map<Class<?>, Converter<?, String>> textConverterMap=new ConcurrentHashMap<Class<?>, Converter<?,String>>();

	/**The converter for this component.*/
	private Converter<V, String> converter;

		/**@return The converter for this component.*/
		public Converter<V, String> getConverter() {return converter;}

		/**Sets the converter.
		This is a bound property
		@param newConverter The converter for this component.
		@exception NullPointerException if the given converter is <code>null</code>.
		@see #CONVERTER_PROPERTY
		*/
		public void setConverter(final Converter<V, String> newConverter)
		{
			if(converter!=newConverter)	//if the value is really changing
			{
				final Converter<V, String> oldConverter=converter;	//get the old value
				converter=checkNull(newConverter, "Converter cannot be null.");	//actually change the value
				firePropertyChange(CONVERTER_PROPERTY, oldConverter, newConverter);	//indicate that the value changed
			}
		}
	
	/**The text literal value displayed in the control, or <code>null</code> if there is no literal value.*/
	private String text;

		/**@return The text literal value displayed in the control, or <code>null</code> if there is no literal value.*/
		public String getText() {return text;}

		/**Sets the text literal value displayed in the control.
		This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
		@param newText The text literal value displayed in the control.
		@see #TEXT_PROPERTY
		*/
		public void setText(final String newText)
		{
			if(!ObjectUtilities.equals(text, newText))	//if the value is really changing (compare their values, rather than identity)
			{
				final String oldText=text;	//get the old value
				text=newText;	//actually change the value
				firePropertyChange(TEXT_PROPERTY, oldText, newText);	//indicate that the value changed
			}			
		}

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

	/**Session constructor with a default data model to represent a given type and a default converter.
	@param session The Guise session that owns this component.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	@exception IllegalArgumentException if no default converter is available for the given value class.
	*/
	public TextControl(final GuiseSession session, final Class<V> valueClass)
	{
		this(session, null, valueClass);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor with a default data model to represent a given type and a default converter.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or value class is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalArgumentException if no default converter is available for the given value class.
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
	@exception IllegalArgumentException if no default converter is available for the given model's value class.
	*/
	public TextControl(final GuiseSession session, final String id, final ValueModel<V> model)
	{
		this(session, id, model, createDefaultConverter(session, model.getValueClass()));	//construct the parent class
	}

	/**Session, ID, model, and converter constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@param converter The converter for this component.
	@exception NullPointerException if the given session model, and/or converter is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public TextControl(final GuiseSession session, final String id, final ValueModel<V> model, final Converter<V, String> converter)
	{
		super(session, id, model);	//construct the parent class
		this.converter=checkNull(converter, "Converter cannot be null");	//save the converter
		updateText(model.getValue());	//initialize the text with the literal form of the initial model value
		model.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, new AbstractPropertyValueChangeListener<V>()	//listen for the model changing value, and update the text in response
				{
					public void propertyValueChange(final PropertyValueChangeEvent<V> propertyValueChangeEvent)	//if the model value changes
					{
						updateText(propertyValueChangeEvent.getNewValue());	//update the text with the new value
					}
				});
	}

	/**Updates the component text with literal form of the given value.
	@param value The value to convert and store in the literal text property.
	@see Converter#convertValue(Object)
	@see #setText(String)
	*/
	protected void updateText(final V value)
	{
		final Converter<V, String> converter=getConverter();	//get the current converter
		try
		{
			final String newText=converter.convertValue(value);	//convert the value to text
			setText(newText);	//convert the value to text
		}
		catch(final ConversionException conversionException)	//TODO fix better; decide what to do if there is an error here
		{
			throw new AssertionError(conversionException);
		}									
	}

	/**Determines whether the models of this component and all of its child components are valid.
	This version in addition to default functionality checks to make sure the literal text representation matches the value.
	@return Whether the models of this component and all of its child components are valid.
	*/
	public boolean isValid()
	{
/*TODO decide whether this is needed, now that we've refactored information into the component
		if(!getController().isValid())	//if the controller isn't valid
		{
			return false;	//although the model may be valid, its view representation is not
		}
*/
		return super.isValid() && getConverter().isEquivalent(getModel().getValue(), getText());	//if this component is valid, make sure the model value and the literal text value match
	}

	/**Creates a default converter for the value type represented by the given value class.
	Default converters are available for the following types:
	<ul>
		<li><code>char[]</code></li>
		<li><code>java.lang.Boolean</code></li>
		<li><code>java.lang.Float</code></li>
		<li><code>java.lang.Integer</code></li>
		<li><code>java.lang.String</code></li>
	</ul>
	@param <VV> The type of value represented.
	@param session The Guise session that will own the converter.
	@param valueClass The class of the represented value.
	@return The default converter for the value type represented by the given value class.
	@exception NullPointerException if the given value class is <code>null</code>.
	@exception IllegalArgumentException if no default converter is available for the given value class.
	*/
	@SuppressWarnings("unchecked")	//we check the value class before generic casting
	protected static <VV> Converter<VV, String> createDefaultConverter(final GuiseSession session, final Class<VV> valueClass)
	{
		checkNull(valueClass, "Value class cannot be null.");
		if(char[].class.equals(valueClass))	//char[]
		{
			return (Converter<VV, String>)new CharArrayStringLiteralConverter(session);
		}
		else if(Boolean.class.equals(valueClass))	//Boolean
		{
			return (Converter<VV, String>)new BooleanStringLiteralConverter(session);
		}
		else if(Float.class.equals(valueClass))	//Float
		{
			return (Converter<VV, String>)new FloatStringLiteralConverter(session);
		}
		else if(Integer.class.equals(valueClass))	//Integer
		{
			return (Converter<VV, String>)new IntegerStringLiteralConverter(session);
		}
		else if(String.class.equals(valueClass))	//String
		{
			return (Converter<VV, String>)new StringStringLiteralConverter(session);
		}
		else	//if we don't recognize the value class
		{
			throw new IllegalArgumentException("Unrecognized value class: "+valueClass);
		}
	}
}
