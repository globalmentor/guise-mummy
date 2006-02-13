package com.guiseframework.converter;

import java.util.MissingResourceException;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.GuiseSession;
import com.guiseframework.event.GuiseBoundPropertyObject;

import static com.guiseframework.GuiseResourceConstants.*;

/**An abstract implementation an object that can convert a value from and to its lexical form.
@param <V> The value type this converter supports.
@param <L> The literal type of the lexical form of the value.
@author Garret Wilson
*/
public abstract class AbstractConverter<V, L> extends GuiseBoundPropertyObject implements Converter<V, L>
{

	/**Session constructor.
	@param session The Guise session that owns this converter.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractConverter(final GuiseSession session)
	{
		super(session);
	}

	/**The invalid value message text, or <code>null</code> if there is no message text.*/
	private String invalidValueMessage=null;

		/**Determines the text of the invalid value message.
		If a message is specified, it will be used; otherwise, a value will be loaded from the resources if possible.
		@return The invalid value message text, or <code>null</code> if there is no invalid value message text.
		@exception MissingResourceException if there was an error loading the value from the resources.
		@see #getInvalidValueMessageResourceKey()
		*/
		public String getInvalidValueMessage() throws MissingResourceException
		{
			return getSession().determineString(invalidValueMessage, getInvalidValueMessageResourceKey());	//get the value or the resource, if available
		}

		/**Sets the text of the invalid value message.
		This is a bound property.
		@param newInvalidValueMessage The new text of the invalid value message.
		@see Converter#INVALID_VALUE_MESSAGE_PROPERTY
		*/
		public void setInvalidValueMessage(final String newInvalidValueMessage)
		{
			if(!ObjectUtilities.equals(invalidValueMessage, newInvalidValueMessage))	//if the value is really changing
			{
				final String oldInvalidValueMessage=invalidValueMessage;	//get the old value
				invalidValueMessage=newInvalidValueMessage;	//actually change the value
				firePropertyChange(INVALID_VALUE_MESSAGE_PROPERTY, oldInvalidValueMessage, newInvalidValueMessage);	//indicate that the value changed
			}			
		}

	/**The invalid value message text resource key, or <code>null</code> if there is no invalid value message text resource specified.*/
	private String invalidValueMessageResourceKey=CONVERTER_INVALID_VALUE_MESSAGE_RESOURCE;

		/**@return The invalid value message text resource key, or <code>null</code> if there is no invalid value message text resource specified.*/
		public String getInvalidValueMessageResourceKey() {return invalidValueMessageResourceKey;}

		/**Sets the key identifying the text of the invalid value message in the resources.
		This property defaults to {@link CONVERTER_INVALID_VALUE_MESSAGE_RESOURCE}.
		This is a bound property.
		@param newInvalidValueMessageResourceKey The new invalid value message text resource key.
		@see Converter#INVALID_VALUE_MESSAGE_RESOURCE_KEY_PROPERTY
		*/
		public void setInvalidValueMessageResourceKey(final String newInvalidValueMessageResourceKey)
		{
			if(!ObjectUtilities.equals(invalidValueMessageResourceKey, newInvalidValueMessageResourceKey))	//if the value is really changing
			{
				final String oldInvalidValueMessageResourceKey=invalidValueMessageResourceKey;	//get the old value
				invalidValueMessageResourceKey=newInvalidValueMessageResourceKey;	//actually change the value
				firePropertyChange(INVALID_VALUE_MESSAGE_RESOURCE_KEY_PROPERTY, oldInvalidValueMessageResourceKey, newInvalidValueMessageResourceKey);	//indicate that the value changed
			}
		}

	/**Determines whether a given literal value in the lexical space can be converted to a value in the value space.
	This implementation attempts to convert the literal value and returns <code>false</code> if conversion is unsuccessful.
	@param literal The literal value to validate.
	@return <code>true</code> if the literal is valid, else <code>false</code>.
	*/
	public boolean isValidLiteral(final L literal)
	{
		try
		{
			convertLiteral(literal);	//try to convert the literal
			return true;	//indicate that the literal can be converted
		}
		catch(final ConversionException conversionException)
		{
			return false;	//indicate that the literal cannot be converted 
		}
	}

	/**Determines if the given literal in the lexical space is a valid representation of the given value in the value space.
	This implementation assumes that non-<code>null</code> values can correctly be compared using {@link Object#equals(Object)}.
	@param value The value to compare.
	@param literal The literal value in the lexical space to compare with the value after conversion.
	@return <code>true</code> if the given literal in the lexical space is a valid representation of the given value in the value space.
	*/
	public boolean isEquivalent(final V value, final L literal)
	{
		try
		{
			return ObjectUtilities.equals(value, convertLiteral(literal));	//see if the literal value converted to the value space matches the given value
		}
		catch(final ConversionException conversionException)	//if the literal value couldn't be converted
		{
			return false;	//the values aren't equivalent
		}		
	}

}
