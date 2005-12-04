package com.javaguise.converter;

import static com.javaguise.GuiseResourceConstants.*;
import static java.text.MessageFormat.*;

import com.javaguise.GuiseSession;

/**A converter that converts a <code>Boolean</code> from and to a string literal.
@author Garret Wilson
@see Boolean
*/
public class BooleanStringLiteralConverter extends AbstractStringLiteralConverter<Boolean>
{

	/**Session constructor with no value required.
	@param session The Guise session that owns this converter.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public BooleanStringLiteralConverter(final GuiseSession session)
	{
		super(session);	//construct the parent class
	}

	/**Converts a literal representation of a value from the lexical space into a value in the value space.
	@param literal The literal value in the lexical space to convert.
	@return The converted value in the value space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the literal value cannot be converted.
	*/ 
	public Boolean convertLiteral(final String literal) throws ConversionException
	{
		try
		{
			return literal!=null && literal.length()>0 ? Boolean.valueOf(literal) : null;	//if there is a literal, convert it to a Boolean			
		}
		catch(final NumberFormatException numberFormatException)	//if the string does not contain a valid Boolean
		{
			throw new ConversionException(format(getSession().getStringResource(VALIDATOR_INVALID_VALUE_MESSAGE_RESOURCE), literal), literal);	//TODO use a converter message resource
		}
	}
}
