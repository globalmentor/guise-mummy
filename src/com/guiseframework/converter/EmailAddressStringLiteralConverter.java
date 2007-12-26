package com.guiseframework.converter;

import static java.text.MessageFormat.format;

import com.garretwilson.net.EmailAddress;
import com.garretwilson.text.ArgumentSyntaxException;

/**A converter that converts an {@link EmailAddress} from and to a string literal.
@author Garret Wilson
*/
public class EmailAddressStringLiteralConverter extends AbstractStringLiteralConverter<EmailAddress>
{

	/**Converts a literal representation of a value from the lexical space into a value in the value space.
	@param literal The literal value in the lexical space to convert.
	@return The converted value in the value space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the literal value cannot be converted.
	*/ 
	public EmailAddress convertLiteral(final String literal) throws ConversionException
	{
		if(literal==null)	//if the literal is null
		{
			return null;	//the value is null
		}
		else	//if the literal is not null
		{
			try
			{
				return new EmailAddress(literal);	//construct the email address
			}
			catch(final ArgumentSyntaxException argumentSyntaxException)	//if the email addrss is not in the correct syntax
			{
				throw new ConversionException(format(getSession().dereferenceString(getInvalidValueMessage()), literal), literal);	//indicate that the value was invalid				
			}
		}
	}
}
