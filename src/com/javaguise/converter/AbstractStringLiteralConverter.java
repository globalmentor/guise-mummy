package com.javaguise.converter;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.javaguise.GuiseSession;

/**An abstract implementation an object that can convert a value from and to a string.
@param <V> The value type this converter supports.
@author Garret Wilson
*/
public abstract class AbstractStringLiteralConverter<V> extends AbstractConverter<V, String>
{

	/**Session constructor.
	@param session The Guise session that owns this converter.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractStringLiteralConverter(final GuiseSession session)
	{
		super(session);	//construct the parent class
	}

	/**Converts a value from the value space to a literal value in the lexical space.
	This implementation returns the {@link Object#toString()} version of the value, if a value is given.
	@param value The value in the value space to convert.
	@return The converted value in the lexical space, or <code>null</code> if the given literal is <code>null</code>..
	@exception ConversionException if the value cannot be converted.
	*/ 
	public String convertValue(final V value) throws ConversionException
	{
		return value!=null ? value.toString() : null;	//convert the value to a string if there is a value
	}

	/**Creates a default string literal converter for the value type represented by the given value class.
	Specific converters are available for the following types:
	<ul>
		<li><code>char[]</code></li>
		<li><code>java.lang.Boolean</code></li>
		<li><code>java.util.Calendar</code></li>
		<li><code>java.util.Date</code></li>
		<li><code>java.lang.Float</code></li>
		<li><code>java.lang.Integer</code></li>
		<li><code>java.util.Locale</code></li>
		<li><code>java.lang.String</code></li>
	</ul>
	If the given type is not recognized, a default one-way value-to-literal converter will be returned that uses a value's {@link Object#toString()} method for generating values in the lexical space.
	@param <VV> The type of value represented.
	@param session The Guise session that will own the converter.
	@param valueClass The class of the represented value.
	@return The default converter for the value type represented by the given value class.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	@SuppressWarnings("unchecked")	//we check the value class before generic casting
	public static <VV> Converter<VV, String> getInstance(final GuiseSession session, final Class<VV> valueClass)
	{
		checkNull(valueClass, "Value class cannot be null.");
		if(char[].class.equals(valueClass))	//char[]
		{
			return (Converter<VV, String>)new CharArrayStringLiteralConverter(session);
		}
		else if(Boolean.class.equals(valueClass))	//Boolean
		{
			return (Converter<VV, String>)new BooleanStringLiteralConverter(session);
		}
		else if(Calendar.class.equals(valueClass))	//Calendar
		{
			return (Converter<VV, String>)new CalendarStringLiteralConverter(session, DateStringLiteralStyle.FULL, TimeStringLiteralStyle.FULL);
		}
		else if(Date.class.equals(valueClass))	//Date
		{
			return (Converter<VV, String>)new DateStringLiteralConverter(session, DateStringLiteralStyle.FULL, TimeStringLiteralStyle.FULL);
		}
		else if(Float.class.equals(valueClass))	//Float
		{
			return (Converter<VV, String>)new FloatStringLiteralConverter(session);
		}
		else if(Integer.class.equals(valueClass))	//Integer
		{
			return (Converter<VV, String>)new IntegerStringLiteralConverter(session);
		}
		else if(Locale.class.equals(valueClass))	//Locale
		{
			return (Converter<VV, String>)new LocaleStringLiteralConverter(session, LocaleStringLiteralStyle.NAME);
		}
		else if(String.class.equals(valueClass))	//String
		{
			return (Converter<VV, String>)new StringStringLiteralConverter(session);
		}
		else	//if we don't recognize the value class
		{
			return new DefaultStringLiteralConverter<VV>(session);	//return a default string literal converter
		}
	}

}
