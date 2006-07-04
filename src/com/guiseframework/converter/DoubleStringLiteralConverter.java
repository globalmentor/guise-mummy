package com.guiseframework.converter;

import java.util.Currency;

/**A converter that converts a <code>Double</code> from and to a string literal.
@author Garret Wilson
@see Double
*/
public class DoubleStringLiteralConverter extends AbstractNumberStringLiteralConverter<Double>
{

	/**Default constructor with a default number style.*/
	public DoubleStringLiteralConverter()
	{
		this(Style.NUMBER);	//construct the class with a default number style
	}

	/**Style constructor.
	If the currency style is requested, the currency used will dynamically change whenever the locale changes.
	@param style The representation style.
	@exception NullPointerException if the given style is <code>null</code>.
	*/
	public DoubleStringLiteralConverter(final Style style)
	{
		this(style, null);	//construct the class using a dynamic default currency for the locale
	}

	/**Style, and currency constructor.
	@param style The representation style.
	@param currency The constant currency type to use, or <code>null</code> if currency representation is not requested or the currency should be dynamically determined by the locale.
	@exception NullPointerException if the given style is <code>null</code>.
	@exception IllegalArgumentException if a currency is provided for a style other than {@link Style#CURRENCY}.
	*/
	public DoubleStringLiteralConverter(final Style style, final Currency currency)
	{
		super(style, currency);	//construct the parent class
	}

	/**Converts a literal representation of a value from the lexical space into a value in the value space.
	@param literal The literal value in the lexical space to convert.
	@return The converted value in the value space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the literal value cannot be converted.
	*/
	public Double convertLiteral(final String literal) throws ConversionException
	{
		final Number number=parseNumber(literal);	//parse a number from the literal value
		if(number!=null)	//if there is a number
		{
			return number instanceof Double ? (Double)number : new Double(number.doubleValue());	//convert the number to a double object if necessary
		}
		else	//if there is no number
		{
			return null;	//there is no number to return
		}
	}

}
