package com.guiseframework.converter;

import com.globalmentor.java.Objects;
import com.guiseframework.event.GuiseBoundPropertyObject;

import static com.globalmentor.java.Objects.*;
import static com.guiseframework.GuiseResourceConstants.*;

/**An abstract implementation an object that can convert a value from and to its lexical form.
@param <V> The value type this converter supports.
@param <L> The literal type of the lexical form of the value.
@author Garret Wilson
*/
public abstract class AbstractConverter<V, L> extends GuiseBoundPropertyObject implements Converter<V, L>
{

	/**The invalid value message text, which may include a resource reference.*/
	private String invalidValueMessage=CONVERTER_INVALID_VALUE_MESSAGE_RESOURCE_REFERENCE;

		/**@return The invalid value message text, which may include a resource reference.*/
		public String getInvalidValueMessage() {return invalidValueMessage;}

		/**Sets the text of the invalid value message.
		This is a bound property.
		@param newInvalidValueMessage The new text of the invalid value message, which may include a resource reference.
		@exception NullPointerException if the given message is <code>null</code>.
		@see #INVALID_VALUE_MESSAGE_PROPERTY
		*/
		public void setInvalidValueMessage(final String newInvalidValueMessage)
		{
			if(!invalidValueMessage.equals(checkInstance(newInvalidValueMessage, "Invalid value message cannot be null.")))	//if the value is really changing
			{
				final String oldInvalidValueMessage=invalidValueMessage;	//get the old value
				invalidValueMessage=newInvalidValueMessage;	//actually change the value
				firePropertyChange(INVALID_VALUE_MESSAGE_PROPERTY, oldInvalidValueMessage, newInvalidValueMessage);	//indicate that the value changed
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
			return Objects.equals(value, convertLiteral(literal));	//see if the literal value converted to the value space matches the given value
		}
		catch(final ConversionException conversionException)	//if the literal value couldn't be converted
		{
			return false;	//the values aren't equivalent
		}		
	}

}
