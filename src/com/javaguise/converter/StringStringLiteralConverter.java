package com.javaguise.converter;

import com.javaguise.GuiseSession;

/**A converter that converts a <code>String</code> from and to a string literal.
@author Garret Wilson
*/
public class StringStringLiteralConverter extends AbstractStringLiteralConverter<String>
{

	/**Session constructor with no value required.
	@param session The Guise session that owns this converter.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public StringStringLiteralConverter(final GuiseSession session)
	{
		super(session);	//construct the parent class
	}

	/**Converts a literal representation of a value from the lexical space into a value in the value space.
	This version returns the literal itself.
	@param literal The literal value in the lexical space to convert.
	@return The converted value in the value space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the literal value cannot be converted.
	*/ 
	public String convertLiteral(final String literal) throws ConversionException
	{
		return literal;	//a string's value and string literal lexical spaces are identical
	}

	/**Converts a value from the value space to a literal value in the lexical space.
	This version returns the value itself.
	@param value The value in the value space to convert.
	@return The converted value in the lexical space, or <code>null</code> if the given literal is <code>null</code>..
	@exception ConversionException if the value cannot be converted.
	*/ 
	public String convertValue(final String value) throws ConversionException
	{
		return value;	//a string's value and string literal lexical spaces are identical
	}
}
