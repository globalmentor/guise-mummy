package com.guiseframework.converter;

import java.text.*;
import java.util.*;

import com.guiseframework.GuiseSession;

/**An object that can convert a date/time from and to a string.
This implementation caches a date format and only creates a new one if the locale has changed.
This implementation synchronizes all conversions on the {@link DateFormat} object.
@author Garret Wilson
*/
public class DateStringLiteralConverter extends AbstractDateStringLiteralConverter<Date>
{

	/**Session and date style constructor with no time style.
	@param session The Guise session that owns this converter.
	@param dateStyle The date representation style, or <code>null</code> if the date should not be represented.
	@exception NullPointerException if the given session and/or date style is <code>null</code>.
	*/
	public DateStringLiteralConverter(final GuiseSession session, final DateStringLiteralStyle dateStyle)
	{
		this(session, dateStyle, null);	//construct the class with no time style
	}

	/**Session, date style, and time style constructor.
	@param session The Guise session that owns this converter.
	@param dateStyle The date representation style, or <code>null</code> if the date should not be represented.
	@param timeStyle The time representation style, or <code>null</code> if the time should not be represented.
	@exception NullPointerException if the given session and/or both the date style and time style is <code>null</code>.
	*/
	public DateStringLiteralConverter(final GuiseSession session, final DateStringLiteralStyle dateStyle, final TimeStringLiteralStyle timeStyle)
	{
		super(session, dateStyle, timeStyle);	//construct the parent class
	}

	/**Converts a value from the value space to a literal value in the lexical space.
	This implementation converts the value using the date format object.
	This implementation synchronizes on the {@link DateFormat} instance. 
	@param value The value in the value space to convert.
	@return The converted value in the lexical space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the value cannot be converted.
	@see #getDateFormat()
	*/ 
	public String convertValue(final Date value) throws ConversionException
	{
		return convertDateValue(value);	//convert the date to a string
	}

	/**Converts a literal representation of a value from the lexical space into a value in the value space.
	This implementation converts the empty string to a <code>null</code> value.
	@param literal The literal value in the lexical space to convert.
	@return The converted value in the value space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the literal value cannot be converted.
	*/
	public Date convertLiteral(final String literal) throws ConversionException
	{
		return convertDateLiteral(literal);	//convert the string to a date
	}
}
