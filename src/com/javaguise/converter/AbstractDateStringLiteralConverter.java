package com.javaguise.converter;

import static com.javaguise.GuiseResourceConstants.*;
import static java.text.MessageFormat.*;

import java.text.*;
import java.util.*;

import com.javaguise.session.GuiseSession;

/**An object that can convert a date object from and to a string.
This implementation caches a date format and only creates a new one if the locale has changed.
This implementation synchronizes all conversions on the {@link DateFormat} object.
@param <V> The value type this converter supports.
@author Garret Wilson
*/
public abstract class AbstractDateStringLiteralConverter<V> extends AbstractConverter<V, String>
{
	/**The style of the date/time in its literal form.*/
	public enum Style
	{
		/**A completely numeric representation, such as 12.13.52 or 3:30pm.*/
		SHORT,
		/**A medium representation, such as Jan 12, 1952.*/
		MEDIUM,
		/**A long representation, such as January 12, 1952 or 3:30:32pm.*/
		LONG,
		/**A completely specified representation, such as Tuesday, April 12, 1952 AD or 3:30:42pm PST.*/
		FULL;
	}

	/**The array of date format styles indexed by enumerated styles for quick conversion.*/ 
	private final static int[] DATE_TIME_STYLES=new int[Style.values().length];

	/**The date representation style, or <code>null</code> if the date should not be represented.*/
	private final Style dateStyle;

		/**@return The date representation style, or <code>null</code> if the date should not be represented.*/
		public Style getDateStyle() {return dateStyle;}

	/**The time representation style, or <code>null</code> if the time should not be represented.*/
	private final Style timeStyle;

		/**@return The time representation style, or <code>null</code> if the time should not be represented.*/
		public Style getTimeStyle() {return timeStyle;}

	/**The lazily-created cached object for converting dates to and from strings.*/
	private DateFormat dateFormat=null;

	/**The lazily-assigned locale for which a date format was generated.*/
	private Locale locale=null;

	/**@return A date format object appropriate for the session's current locale.*/
	protected synchronized DateFormat getDateFormat()
	{
		final Locale sessionLocale=getSession().getLocale();	//get the current session locale
		if(dateFormat==null || !sessionLocale.equals(locale))	//if we haven't yet generated a date format or the locale has changed
		{
			dateFormat=createDateFormat(sessionLocale);	//create a new date format
			locale=sessionLocale;	//update the locale			
		}
		return dateFormat;	//return the date format
	}

	/**Session, date style, time style, and currency constructor.
	@param session The Guise session that owns this converter.
	@param dateStyle The date representation style, or <code>null</code> if the date should not be represented.
	@param timeStyle The time representation style, or <code>null</code> if the time should not be represented.
	@exception NullPointerException if the given session and/or both the date style and time style is <code>null</code>.
	*/
	public AbstractDateStringLiteralConverter(final GuiseSession session, final Style dateStyle, final Style timeStyle)
	{
		super(session);	//construct the parent class
		if(dateStyle==null && timeStyle==null)	//if neither a date style or a time style is specified
		{
			throw new IllegalArgumentException("Either a date style or a time style must be specified.");
		}
		this.dateStyle=dateStyle;
		this.timeStyle=timeStyle;
	}

	/**Creates a new date format object for the indicated locale.
	@param locale The locale for which a date format should be created.
	@return A date format object appropriate for the given locale.
	@see #getDateStyle()
	@see #getTimeStyle()
	*/
	protected DateFormat createDateFormat(final Locale locale)
	{
		final Style dateStyle=getDateStyle();	//get the date style
		final Style timeStyle=getTimeStyle();	//get the time style
		final DateFormat dateFormat;	//we'll store here the date format
		if(dateStyle!=null)	//if a date style is requested
		{
			if(timeStyle!=null)	//if both a date style and a time style is requested
			{
				dateFormat=DateFormat.getDateTimeInstance(DATE_TIME_STYLES[dateStyle.ordinal()], DATE_TIME_STYLES[timeStyle.ordinal()], locale);	//create a date/time instance
			}
			else	//if only a date style is requested
			{
				dateFormat=DateFormat.getDateInstance(DATE_TIME_STYLES[dateStyle.ordinal()], locale);	//create a date instance				
			}
		}
		else	//if a date style is not requested, only a time style is requested
		{
			assert timeStyle!=null : "Neither date style or time style specified.";
			dateFormat=DateFormat.getTimeInstance(DATE_TIME_STYLES[timeStyle.ordinal()], locale);	//create a time instance							
		}
		return dateFormat;	//return the date format we created
	}

	/**Converts a value from a date value space to a literal value in the lexical space.
	This implementation converts the value using the date format object.
	This implementation synchronizes on the {@link DateFormat} instance. 
	@param value The value in the value space to convert.
	@return The converted value in the lexical space, or <code>null</code> if the given literal is <code>null</code>..
	@exception ConversionException if the value cannot be converted.
	@see #getDateFormat()
	*/ 
	protected String convertDateValue(final Date value) throws ConversionException
	{
		if(value!=null)	//if there is a value
		{
			final DateFormat dateFormat=getDateFormat();	//get the date format to use
			synchronized(dateFormat)	//don't allow other threads to access the date format object while we're using it
			{
				return dateFormat.format(value);	//format the date
			}
		}
		else	//if there is no value
		{
			return null;	//there's nothing to convert
		}
	}

	/**Converts a literal representation of a value from the lexical space into a date value in the date value space.
	This implementation converts the empty string to a <code>null</code> value.
	@param literal The literal value in the lexical space to convert.
	@return The converted value in the value space, or <code>null</code> if the given literal is <code>null</code>.
	@exception ConversionException if the literal value cannot be converted.
	*/
	protected Date convertDateLiteral(final String literal) throws ConversionException
	{
		if(literal!=null && literal.length()>0)	//if there is a literal value
		{
			final DateFormat dateFormat=getDateFormat();	//get the date format to use

			final ParsePosition parsePosition=new ParsePosition(0);	//create a new parse position
			final Date date;	//we'll store the date here
			synchronized(dateFormat)	//don't allow other threads to access the date format object while we're using it
			{
				date=dateFormat.parse(literal, parsePosition);	//parse the value, retrieving the parse positoin
			}
			if(parsePosition.getIndex()<literal.length())	//if the whole string wasn't parsed, we'll consider that an error (either there was an error, in which case the index is zero, or part of the string was ignored)
			{
				throw new ConversionException(format(getSession().getStringResource(VALIDATOR_INVALID_VALUE_MESSAGE_RESOURCE), literal), literal);	//TODO use a converter message resource
			}
			return date;	//return the date we parsed
		}			
		else	//if there is no literal value
		{
			return null;	//there is nothing to convert
		}
	}
}
