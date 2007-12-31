package com.guiseframework.converter;

import static com.garretwilson.lang.ClassUtilities.*;
import static com.garretwilson.lang.Objects.*;
import static java.text.MessageFormat.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**A converter that converts any object to a string literal using its {@link Object#toString()} method.
This converter converts from a string literal to an object using a string constructor, or if one is not present, the first constructor with a single parameter that is type-compatible with {@link String}, such as {@link CharSequence}.
If there is no string-compatible constructor, a {@link ConversionException} is thrown.
@param <V> The value type this converter supports.
@author Garret Wilson
*/
public class DefaultStringLiteralConverter<V> extends AbstractStringLiteralConverter<V>
{

	/**The class representing the type of value to convert.*/
	private final Class<V> valueClass;

		/**@return The class representing the type of value to convert.*/
		public Class<V> getValueClass() {return valueClass;}

	/**Constructs a default string literal converter indicating the type of value to convert.
	@param valueClass The class indicating the type of value to convert.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public DefaultStringLiteralConverter(final Class<V> valueClass)
	{
		this.valueClass=checkInstance(valueClass, "Value class cannot be null.");	//store the value class
	}

	/**Converts a literal representation of a value from the lexical space into a value in the value space.
	This implementation converts from a string literal to an object using a string constructor, or if one is not present, the first constructor with a single parameter that is type-compatible with {@link String}, such as {@link CharSequence}.
	If there is no string-compatible constructor, a {@link ConversionException} is thrown.
	@param literal The literal value in the lexical space to convert.
	@return The converted value in the value space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the literal value cannot be converted.
	*/ 
	public V convertLiteral(final String literal) throws ConversionException
	{
		if(literal==null)	//if the literal is null
		{
			return null;	//the value is null
		}
		else	//if the literal is not null
		{
			final Class<V> valueClass=getValueClass();	//get the value class
			final Constructor<V> stringCompatibleConstructor=getCompatibleConstructor(valueClass, String.class);	//get the string-compatible constructor
			if(stringCompatibleConstructor!=null)	//if there is a string-compatible constructor
			{
				try
				{
					return stringCompatibleConstructor.newInstance(literal);	//try to invoke the constructor and return a new object	
				}
				catch(final InvocationTargetException invocationTargetException)	//if the constructor threw an error
				{
					final Throwable cause=invocationTargetException.getCause();	//get the cause of the exception
					if(cause instanceof IllegalArgumentException)	//if there was something incorrect about the string literal argument
					{
						throw new ConversionException(format(getSession().dereferenceString(getInvalidValueMessage()), literal), literal);	//indicate that the value was invalid
					}
					else	//if there is some other constructor error
					{
						throw new ConversionException(cause);	//send it back; we don't know what it is, and we can't be sure it means the value is invalid
					}
				}
				catch(final IllegalArgumentException e)
				{
					throw new ConversionException(e);
				}
				catch(final InstantiationException e)
				{
					throw new ConversionException(e);
				}
				catch(final IllegalAccessException e)
				{
					throw new ConversionException(e);
				}
			}
			else	//if there are no string-compatible constructors
			{
				throw new ConversionException("Class "+getValueClass()+" does not support literal to value conversions.", literal);
			}
		}
	}
}
