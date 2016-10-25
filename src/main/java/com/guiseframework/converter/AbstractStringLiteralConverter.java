/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.converter;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.globalmentor.java.Objects.*;

import com.globalmentor.itu.TelephoneNumber;
import com.globalmentor.net.EmailAddress;

/**
 * An abstract implementation an object that can convert a value from and to a string.
 * @param <V> The value type this converter supports.
 * @author Garret Wilson
 */
public abstract class AbstractStringLiteralConverter<V> extends AbstractConverter<V, String> {

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation returns the {@link Object#toString()} version of the value, if a value is given.
	 * </p>
	 */
	@Override
	public String convertValue(final V value) throws ConversionException {
		return value != null ? value.toString() : null; //convert the value to a string if there is a value
	}

	/**
	 * Creates a default string literal converter for the value type represented by the given value class. Specific converters are available for the following
	 * types:
	 * <ul>
	 * <li><code>char[]</code></li>
	 * <li><code>java.lang.Boolean</code></li>
	 * <li><code>java.util.Calendar</code></li>
	 * <li><code>java.util.Date</code></li>
	 * <li><code>java.util.Double</code></li>
	 * <li>{@link EmailAddress}</li>
	 * <li><code>java.lang.Float</code></li>
	 * <li><code>java.lang.Integer</code></li>
	 * <li><code>java.lang.Long</code></li>
	 * <li><code>java.util.Locale</code></li>
	 * <li><code>java.lang.Long</code></li>
	 * <li><code>java.lang.String</code></li>
	 * <li>{@link TelephoneNumber}</li>
	 * </ul>
	 * If the given type is not recognized, a default one-way value-to-literal converter will be returned that uses a value's {@link Object#toString()} method for
	 * generating values in the lexical space.
	 * @param <VV> The type of value represented.
	 * @param valueClass The class of the represented value.
	 * @return The default converter for the value type represented by the given value class.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	//we check the value class before generic casting
	public static <VV> Converter<VV, String> getInstance(final Class<VV> valueClass) {
		checkInstance(valueClass, "Value class cannot be null.");
		if(char[].class.isAssignableFrom(valueClass)) { //char[]
			return (Converter<VV, String>)new CharArrayStringLiteralConverter();
		} else if(Boolean.class.isAssignableFrom(valueClass)) { //Boolean
			return (Converter<VV, String>)new BooleanStringLiteralConverter();
		} else if(Calendar.class.isAssignableFrom(valueClass)) { //Calendar
			return (Converter<VV, String>)new CalendarStringLiteralConverter(DateStringLiteralStyle.SHORT, TimeStringLiteralStyle.FULL);
		} else if(Date.class.isAssignableFrom(valueClass)) { //Date
			return (Converter<VV, String>)new DateStringLiteralConverter(DateStringLiteralStyle.SHORT, TimeStringLiteralStyle.FULL);
		} else if(Double.class.isAssignableFrom(valueClass)) { //Double
			return (Converter<VV, String>)new DoubleStringLiteralConverter();
		} else if(EmailAddress.class.isAssignableFrom(valueClass)) { //EmailAddress
			return (Converter<VV, String>)new EmailAddressStringLiteralConverter();
		} else if(Float.class.isAssignableFrom(valueClass)) { //Float
			return (Converter<VV, String>)new FloatStringLiteralConverter();
		} else if(Integer.class.isAssignableFrom(valueClass)) { //Integer
			return (Converter<VV, String>)new IntegerStringLiteralConverter();
		} else if(Locale.class.isAssignableFrom(valueClass)) { //Locale
			return (Converter<VV, String>)new LocaleStringLiteralConverter(LocaleStringLiteralStyle.NAME);
		} else if(Long.class.isAssignableFrom(valueClass)) { //Long
			return (Converter<VV, String>)new LongStringLiteralConverter();
		} else if(String.class.isAssignableFrom(valueClass)) { //String
			return (Converter<VV, String>)new StringStringLiteralConverter();
		} else if(TelephoneNumber.class.isAssignableFrom(valueClass)) { //TelephoneNumber
			return (Converter<VV, String>)new TelephoneNumberStringLiteralConverter();
		} else { //if we don't recognize the value class
			return new DefaultStringLiteralConverter<VV>(valueClass); //return a default string literal converter
		}
	}

}
