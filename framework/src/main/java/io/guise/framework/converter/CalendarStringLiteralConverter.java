/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.guise.framework.converter;

import java.text.*;
import java.util.*;

/**
 * An object that can convert a calendar from and to a string.
 * @author Garret Wilson
 */
public class CalendarStringLiteralConverter extends AbstractDateStringLiteralConverter<Calendar> {

	/**
	 * Date style constructor with no time style.
	 * @param dateStyle The date representation style, or <code>null</code> if the date should not be represented.
	 * @throws NullPointerException if the given date style is <code>null</code>.
	 */
	public CalendarStringLiteralConverter(final DateStringLiteralStyle dateStyle) {
		this(dateStyle, null); //construct the class with no time style
	}

	/**
	 * Date style and time style constructor.
	 * @param dateStyle The date representation style, or <code>null</code> if the date should not be represented.
	 * @param timeStyle The time representation style, or <code>null</code> if the time should not be represented.
	 * @throws NullPointerException if both the given date style and time style are <code>null</code>.
	 */
	public CalendarStringLiteralConverter(final DateStringLiteralStyle dateStyle, final TimeStringLiteralStyle timeStyle) {
		super(dateStyle, timeStyle); //construct the parent class
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation converts the value using the date format object. This implementation synchronizes on the {@link DateFormat} instance.
	 * </p>
	 * @see #getDateFormat()
	 */
	@Override
	public String convertValue(final Calendar value) throws ConversionException {
		return value != null ? convertDateValue(value.getTime()) : null; //convert the value from the calendar's time
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation converts the empty string to a <code>null</code> value.
	 * </p>
	 */
	@Override
	public Calendar convertLiteral(final String literal) throws ConversionException {
		final Date date = convertDateLiteral(literal); //convert the literal to a date
		if(date != null) { //if there is a date
			final Calendar calendar = Calendar.getInstance(getSession().getLocale()); //create a new calendar
			calendar.setTime(date); //give the calendar the same time as the date
			return calendar; //return the calendar
		} else { //if there is no date
			return null; //there can be no calendar
		}
	}
}
