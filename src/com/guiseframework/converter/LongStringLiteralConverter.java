package com.guiseframework.converter;

import static java.text.MessageFormat.*;

import com.guiseframework.GuiseSession;

/**A converter that converts a <code>Long</code> from and to a string literal.
@author Garret Wilson
@see Long
*/
public class LongStringLiteralConverter extends AbstractStringLiteralConverter<Long>
{

	/**Session constructor with no value required.
	@param session The Guise session that owns this converter.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public LongStringLiteralConverter(final GuiseSession session)
	{
		super(session);	//construct the parent class
	}

	/**Converts a literal representation of a value from the lexical space into a value in the value space.
	@param literal The literal value in the lexical space to convert.
	@return The converted value in the value space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the literal value cannot be converted.
	*/ 
	public Long convertLiteral(final String literal) throws ConversionException
	{
		try
		{
			return literal!=null && literal.length()>0 ? new Long(Long.parseLong(literal)) : null;	//if there is a literal, convert it to a Long			
		}
		catch(final NumberFormatException numberFormatException)	//if the string does not contain a valid Long
		{
			throw new ConversionException(format(getInvalidValueMessage(), literal), literal);
		}
	}
}
