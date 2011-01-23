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

import java.text.*;
import java.util.*;

/**An object that can convert a date/time from and to a string.
This implementation caches a date format and only creates a new one if the locale has changed.
This implementation synchronizes all conversions on the {@link DateFormat} object.
@author Garret Wilson
*/
public class DateStringLiteralConverter extends AbstractDateStringLiteralConverter<Date>
{

	/**Date style constructor with no time style.
	@param dateStyle The date representation style, or <code>null</code> if the date should not be represented.
	@exception NullPointerException if the given date style is <code>null</code>.
	*/
	public DateStringLiteralConverter(final DateStringLiteralStyle dateStyle)
	{
		this(dateStyle, null);	//construct the class with no time style
	}

	/**Date style and time style constructor.
	@param dateStyle The date representation style, or <code>null</code> if the date should not be represented.
	@param timeStyle The time representation style, or <code>null</code> if the time should not be represented.
	@exception NullPointerException if both the given date style and time style are <code>null</code>.
	*/
	public DateStringLiteralConverter(final DateStringLiteralStyle dateStyle, final TimeStringLiteralStyle timeStyle)
	{
		super(dateStyle, timeStyle);	//construct the parent class
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
