package com.javaguise.converter;

import java.text.*;
import java.util.*;

import com.javaguise.session.GuiseSession;

/**An object that can convert a calendar from and to a string.
@author Garret Wilson
*/
public class CalendarStringLiteralConverter extends AbstractDateStringLiteralConverter<Calendar>
{
	/**Session, date style, time style, and currency constructor.
	@param session The Guise session that owns this converter.
	@param dateStyle The date representation style, or <code>null</code> if the date should not be represented.
	@param timeStyle The time representation style, or <code>null</code> if the time should not be represented.
	@exception NullPointerException if the given session and/or both the date style and time style is <code>null</code>.
	*/
	public CalendarStringLiteralConverter(final GuiseSession session, final DateStringLiteralStyle dateStyle, final TimeStringLiteralStyle timeStyle)
	{
		super(session, dateStyle, timeStyle);	//construct the parent class
	}

	/**Converts a value from the value space to a literal value in the lexical space.
	This implementation converts the value using the date format object.
	This implementation synchronizes on the {@link DateFormat} instance. 
	@param value The value in the value space to convert.
	@return The converted value in the lexical space, or <code>null</code> if the given literal is <code>null</code>..
	@exception ConversionException if the value cannot be converted.
	@see #getDateFormat()
	*/ 
	public String convertValue(final Calendar value) throws ConversionException
	{
		return value!=null ? convertDateValue(value.getTime()) : null;	//convert the value from the calendar's time
	}

	/**Converts a literal representation of a value from the lexical space into a value in the value space.
	This implementation converts the empty string to a <code>null</code> value.
	@param literal The literal value in the lexical space to convert.
	@return The converted value in the value space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the literal value cannot be converted.
	*/
	public Calendar convertLiteral(final String literal) throws ConversionException
	{
		final Date date=convertDateLiteral(literal);	//convert the literal to a date
		if(date!=null)	//if there is a date
		{
			final Calendar calendar=Calendar.getInstance(getSession().getLocale());	//create a new calendar
			calendar.setTime(date);	//give the calendar the same time as the date
			return calendar;	//return the calendar
		}
		else	//if there is no date
		{
			return null;	//there can be no calendar
		}
	}
}
