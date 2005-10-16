package com.javaguise.converter;

import java.util.Currency;

import com.javaguise.session.GuiseSession;

/**A converter that converts a <code>Float</code> from and to a string literal.
@author Garret Wilson
@see Float
*/
public class FloatStringLiteralConverter extends AbstractNumberStringLiteralConverter<Float>
{

	/**Session constructor with a default number style.
	If the currency style is requested, the currency used will dynamically change whenever the locale changes.
	@param session The Guise session that owns this converter.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public FloatStringLiteralConverter(final GuiseSession session)
	{
		this(session, Style.NUMBER);	//construct the class with a default number style
	}

	/**Session and style constructor.
	If the currency style is requested, the currency used will dynamically change whenever the locale changes.
	@param session The Guise session that owns this converter.
	@param style The representation style.
	@exception NullPointerException if the given session and/or style is <code>null</code>.
	*/
	public FloatStringLiteralConverter(final GuiseSession session, final Style style)
	{
		this(session, style, null);	//construct the class using a dynamic default currency for the locale
	}

	/**Session, style, and currency constructor.
	@param session The Guise session that owns this converter.
	@param style The representation style.
	@param currency The constant currency type to use, or <code>null</code> if currency representation is not requested or the currency should be dynamically determined by the locale.
	@exception NullPointerException if the given session and/or style is <code>null</code>.
	@exception IllegalArgumentException if a currency is provided for a style other than {@link Style#CURRENCY}.
	*/
	public FloatStringLiteralConverter(final GuiseSession session, final Style style, final Currency currency)
	{
		super(session, style, currency);	//construct the parent class
	}

	/**Converts a literal representation of a value from the lexical space into a value in the value space.
	@param literal The literal value in the lexical space to convert.
	@return The converted value in the value space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the literal value cannot be converted.
	*/
	public Float convertLiteral(final String literal) throws ConversionException
	{
		final Number number=parseNumber(literal);	//parse a number from the literal value
		if(number!=null)	//if there is a number
		{
			return number instanceof Float ? (Float)number : new Float(number.floatValue());	//convert the number to a float object if necessary
		}
		else	//if there is no number
		{
			return null;	//there is no number to return
		}
		/*TODO del when works
		try
		{
			return literal!=null && literal.length()>0 ? new Float(Float.parseFloat(literal)) : null;	//if there is a literal, convert it to a Float			
		}
		catch(final NumberFormatException numberFormatException)	//if the string does not contain a valid Float
		{
			throw new ConversionException(format(getSession().getStringResource(VALIDATOR_INVALID_VALUE_MESSAGE_RESOURCE), literal), literal);	//TODO use a converter message resource
		}
		*/
	}

}
