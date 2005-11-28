package com.javaguise.converter;

import com.javaguise.session.GuiseSession;

/**A converter that converts any object to a string literal using its {@link Object#toString()} method.
This class cannot convert from a literal to the supported object, and considers all non-<code>null</code> literal representations invalid.
@param <V> The value type this converter supports.
@author Garret Wilson
*/
public class DefaultStringLiteralConverter<V> extends AbstractStringLiteralConverter<V>
{

	/**Session constructor.
	@param session The Guise session that owns this converter.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DefaultStringLiteralConverter(final GuiseSession session)
	{
		super(session);	//construct the parent class
	}

	/**Converts a literal representation of a value from the lexical space into a value in the value space.
	This version throws a conversion exception if the literal is not <code>null</code>.
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
			throw new ConversionException("This class does not support literal to value conversions.", literal);
		}
	}
}
