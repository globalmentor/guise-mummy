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
	/**The array of date format styles (or -1 indicating no corresponding format style) indexed by enumerated styles for quick conversion.*/ 
	private final static int[] DATE_FORMAT_STYLES=new int[DateStringLiteralStyle.values().length];

	/**The array of time format styles (or -1 indicating no corresponding format style) indexed by enumerated styles for quick conversion.*/ 
	private final static int[] TIME_FORMAT_STYLES=new int[TimeStringLiteralStyle.values().length];

	/**The date representation style, or <code>null</code> if the date should not be represented.*/
	private final DateStringLiteralStyle dateStyle;

		/**@return The date representation style, or <code>null</code> if the date should not be represented.*/
		public DateStringLiteralStyle getDateStyle() {return dateStyle;}

	/**The time representation style, or <code>null</code> if the time should not be represented.*/
	private final TimeStringLiteralStyle timeStyle;

		/**@return The time representation style, or <code>null</code> if the time should not be represented.*/
		public TimeStringLiteralStyle getTimeStyle() {return timeStyle;}

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
			dateFormat=createDateFormat(getDateStyle(), getTimeStyle(), sessionLocale);	//create a new date format
			locale=sessionLocale;	//update the locale			
		}
		return dateFormat;	//return the date format
	}

	/**Session, date style, and time style constructor.
	@param session The Guise session that owns this converter.
	@param dateStyle The date representation style, or <code>null</code> if the date should not be represented.
	@param timeStyle The time representation style, or <code>null</code> if the time should not be represented.
	@exception NullPointerException if the given session and/or both the date style and time style is <code>null</code>.
	*/
	public AbstractDateStringLiteralConverter(final GuiseSession session, final DateStringLiteralStyle dateStyle, final TimeStringLiteralStyle timeStyle)
	{
		super(session);	//construct the parent class
		if(dateStyle==null && timeStyle==null)	//if neither a date style or a time style is specified
		{
			throw new NullPointerException("Either a date style or a time style must be specified.");
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
/*TODO del when works
	protected DateFormat createDateFormat(final Locale locale)
	{
		final Style dateStyle=getDateStyle();	//get the date style
		final Style timeStyle=getTimeStyle();	//get the time style
		final DateFormat dateFormat;	//we'll store here the date format
		if(dateStyle!=null)	//if a date style is requested
		{
			if(timeStyle!=null)	//if both a date style and a time style is requested
			{
				dateFormat=DateFormat.getDateTimeInstance(DATE_FORMAT_STYLES[dateStyle.ordinal()], DATE_FORMAT_STYLES[timeStyle.ordinal()], locale);	//create a date/time instance
			}
			else	//if only a date style is requested
			{
				dateFormat=DateFormat.getDateInstance(DATE_FORMAT_STYLES[dateStyle.ordinal()], locale);	//create a date instance				
			}
		}
		else	//if a date style is not requested, only a time style is requested
		{
			assert timeStyle!=null : "Neither date style or time style specified.";
			dateFormat=DateFormat.getTimeInstance(DATE_FORMAT_STYLES[timeStyle.ordinal()], locale);	//create a time instance							
		}
		return dateFormat;	//return the date format we created
	}
*/

	/**Creates a new date format object for the indicated styles and locale.
	This implementation does not allow both date and time styles to be specified if one of the styles specifies other than short/medium/long/full format.
	@param dateStyle The date representation style, or <code>null</code> if the date should not be represented.
	@param timeStyle The date representation style, or <code>null</code> if the date should not be represented.
	@param locale The locale for which a date format should be created.
	@return A date format object appropriate for the given locale.
	@exception NullPointerException if the both the date style and time style is <code>null</code>.
	@exception IllegalArgumentException if both date style and time style is given and one of the styles specifies other than short/medium/long/full format.
	*/
	public static DateFormat createDateFormat(final DateStringLiteralStyle dateStyle, final TimeStringLiteralStyle timeStyle, final Locale locale)
	{
		final int dateFormatStyle=dateStyle!=null ? DATE_FORMAT_STYLES[dateStyle.ordinal()] : -1;	//get the date format style
		final int timeFormatStyle=timeStyle!=null ? TIME_FORMAT_STYLES[timeStyle.ordinal()] : -1;	//get the time format style
		final DateFormat dateFormat;	//we'll store here the date format
		if(dateStyle!=null)	//if a date style is requested
		{
			if(timeStyle!=null)	//if both a date style and a time style is requested
			{
				if(dateFormatStyle<0 || timeFormatStyle<0)	//if one of the styles isn't a valid format style
				{
					throw new IllegalArgumentException("If both date style and style style are specified, each must be one of short/medium/long/full.");
				}
				dateFormat=DateFormat.getDateTimeInstance(dateFormatStyle, timeFormatStyle, locale);	//create a date/time instance
			}
			else	//if only a date style is requested
			{
				switch(dateStyle)
				{
					case SHORT:	//for the predefined format styles
					case MEDIUM:
					case LONG:
					case FULL:
						dateFormat=DateFormat.getDateInstance(dateFormatStyle, locale);	//create a predefined date format instance
						break;
					case DAY_OF_WEEK:
						dateFormat=new SimpleDateFormat("EEEE", locale);	//get a day-of-week date format
						break;
					case DAY_OF_WEEK_SHORT:
						dateFormat=new SimpleDateFormat("E", locale);	//get a short day-of-week date format
						break;
					case MONTH_OF_YEAR:
						dateFormat=new SimpleDateFormat("MMMM", locale);	//get a month-of-year  date format
						break;
					case MONTH_OF_YEAR_SHORT:
						dateFormat=new SimpleDateFormat("M", locale);	//get a short month-of-year date format
						break;
					default:
						throw new AssertionError("Unrecognized date style: "+dateStyle);
				}
			}
		}
		else	//if a date style is not requested, only a time style is requested
		{
			if(timeStyle!=null)	//if only a time style is requested
			{
				dateFormat=DateFormat.getTimeInstance(timeFormatStyle, locale);	//create a time instance							
			}
			else	//if neither a date style nor a time style is requested
			{
				throw new NullPointerException("Either a date style or a time style must be specified.");
			}
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

	/**Fills the date/time format style arrays for fast conversion from enumerated styles.*/
	static
	{
			//fill the date format style array
		final DateStringLiteralStyle[] dateStyles=DateStringLiteralStyle.values();	//get the available styles
		for(int dateStyleIndex=dateStyles.length-1; dateStyleIndex>=0; --dateStyleIndex)	//for each style
		{
			int dateFormatStyle;	//we'll determine the appropriate format style integer
			final DateStringLiteralStyle dateStyle=dateStyles[dateStyleIndex];	//get this style
			switch(dateStyle)	//see which style this is
			{
				case SHORT:
					dateFormatStyle=DateFormat.SHORT;
					break;
				case MEDIUM:
					dateFormatStyle=DateFormat.MEDIUM;
					break;
				case LONG:
					dateFormatStyle=DateFormat.LONG;
					break;
				case FULL:
					dateFormatStyle=DateFormat.FULL;
					break;
				default:
					dateFormatStyle=-1;	//indicate a style with no corresponding format style
					break;
			}
			DATE_FORMAT_STYLES[dateStyleIndex]=dateFormatStyle;	//save this date format style
		}		
			//fill the time format style array
		final TimeStringLiteralStyle[] timeStyles=TimeStringLiteralStyle.values();	//get the available styles
		for(int timeStyleIndex=timeStyles.length-1; timeStyleIndex>=0; --timeStyleIndex)	//for each style
		{
			int timeFormatStyle;	//we'll determine the appropriate format style integer
			final TimeStringLiteralStyle timeStyle=timeStyles[timeStyleIndex];	//get this style
			switch(timeStyle)	//see which style this is
			{
				case SHORT:
					timeFormatStyle=DateFormat.SHORT;
					break;
				case MEDIUM:
					timeFormatStyle=DateFormat.MEDIUM;
					break;
				case LONG:
					timeFormatStyle=DateFormat.LONG;
					break;
				case FULL:
					timeFormatStyle=DateFormat.FULL;
					break;
				default:
					timeFormatStyle=-1;	//indicate a style with no corresponding format style
					break;
			}
			TIME_FORMAT_STYLES[timeStyleIndex]=timeFormatStyle;	//save this time format style
		}		
	}
}
