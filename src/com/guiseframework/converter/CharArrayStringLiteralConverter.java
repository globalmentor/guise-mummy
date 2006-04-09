package com.guiseframework.converter;

/**A converter that converts a <code>char[]</code> from and to a string literal.
@author Garret Wilson
*/
public class CharArrayStringLiteralConverter extends AbstractStringLiteralConverter<char[]>
{

	/**Converts a literal representation of a value from the lexical space into a value in the value space.
	@param literal The literal value in the lexical space to convert.
	@return The converted value in the value space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the literal value cannot be converted.
	*/ 
	public char[] convertLiteral(final String literal) throws ConversionException
	{
		return literal!=null ? literal.toCharArray() : null;	//if there is a literal, convert it to a character array			
	}

	/**Converts a value from the value space to a literal value in the lexical space.
	This version creates a new string based upon the character array value. 
	@param value The value in the value space to convert.
	@return The converted value in the lexical space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the value cannot be converted.
	*/ 
	public String convertValue(final char[] value) throws ConversionException
	{
		return value!=null ? new String(value) : null;	//construct a string from the characters, if there is a character array
	}

	/**Determines if the given literal in the lexical space is a valid representation of the given value in the value space.
	This implementation converts the value to a string and compares taht string with the literal value.
	@param value The value to compare.
	@param literal The literal value in the lexical space to compare with the value after conversion.
	@return <code>true</code> if the given literal in the lexical space is a valid representation of the given value in the value space.
	*/
	public boolean isEquivalent(final char[] value, final String literal)
	{
		if(value!=null)	//if there is a value
		{
			return new String(value).equals(literal);	//convert the value to a string and compare it with the literal
		}
		else	//if there is no value
		{
			return literal==null;	//the values are equivalent if there also is no literal
		}
	}
}
