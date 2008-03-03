package com.guiseframework.converter;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.globalmentor.java.Objects.*;

import com.globalmentor.itu.TelephoneNumber;
import com.globalmentor.net.EmailAddress;

/**An abstract implementation an object that can convert a value from and to a string.
@param <V> The value type this converter supports.
@author Garret Wilson
*/
public abstract class AbstractStringLiteralConverter<V> extends AbstractConverter<V, String>
{

	/**Converts a value from the value space to a literal value in the lexical space.
	This implementation returns the {@link Object#toString()} version of the value, if a value is given.
	@param value The value in the value space to convert.
	@return The converted value in the lexical space, or <code>null</code> if the given literal is <code>null</code>.
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
		<li>{@link EmailAddress}</li>
		<li><code>java.lang.Float</code></li>
		<li><code>java.lang.Integer</code></li>
		<li><code>java.lang.Long</code></li>
		<li><code>java.util.Locale</code></li>
		<li><code>java.lang.Long</code></li>
		<li><code>java.lang.String</code></li>
		<li>{@link TelephoneNumber}</li>
	</ul>
	If the given type is not recognized, a default one-way value-to-literal converter will be returned that uses a value's {@link Object#toString()} method for generating values in the lexical space.
	@param <VV> The type of value represented.
	@param valueClass The class of the represented value.
	@return The default converter for the value type represented by the given value class.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	@SuppressWarnings("unchecked")	//we check the value class before generic casting
	public static <VV> Converter<VV, String> getInstance(final Class<VV> valueClass)
	{
		checkInstance(valueClass, "Value class cannot be null.");
		if(char[].class.equals(valueClass))	//char[]
		{
			return (Converter<VV, String>)new CharArrayStringLiteralConverter();
		}
		else if(Boolean.class.equals(valueClass))	//Boolean
		{
			return (Converter<VV, String>)new BooleanStringLiteralConverter();
		}
		else if(Calendar.class.equals(valueClass))	//Calendar
		{
			return (Converter<VV, String>)new CalendarStringLiteralConverter(DateStringLiteralStyle.FULL, TimeStringLiteralStyle.FULL);
		}
		else if(Date.class.equals(valueClass))	//Date
		{
			return (Converter<VV, String>)new DateStringLiteralConverter(DateStringLiteralStyle.FULL, TimeStringLiteralStyle.FULL);
		}
		else if(EmailAddress.class.equals(valueClass))	//EmailAddress
		{
			return (Converter<VV, String>)new EmailAddressStringLiteralConverter();
		}
		else if(Float.class.equals(valueClass))	//Float
		{
			return (Converter<VV, String>)new FloatStringLiteralConverter();
		}
		else if(Integer.class.equals(valueClass))	//Integer
		{
			return (Converter<VV, String>)new IntegerStringLiteralConverter();
		}
		else if(Locale.class.equals(valueClass))	//Locale
		{
			return (Converter<VV, String>)new LocaleStringLiteralConverter(LocaleStringLiteralStyle.NAME);
		}
		else if(Long.class.equals(valueClass))	//Long
		{
			return (Converter<VV, String>)new LongStringLiteralConverter();
		}
		else if(String.class.equals(valueClass))	//String
		{
			return (Converter<VV, String>)new StringStringLiteralConverter();
		}
		else if(TelephoneNumber.class.equals(valueClass))	//TelephoneNumber
		{
			return (Converter<VV, String>)new TelephoneNumberStringLiteralConverter();
		}
		else	//if we don't recognize the value class
		{
			return new DefaultStringLiteralConverter<VV>(valueClass);	//return a default string literal converter
		}
	}

}
