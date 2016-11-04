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

import static java.util.Objects.*;

import static com.globalmentor.java.Classes.*;
import static java.text.MessageFormat.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A converter that converts any object to a string literal using its {@link Object#toString()} method. This converter converts from a string literal to an
 * object using a string constructor, or if one is not present, the first constructor with a single parameter that is type-compatible with {@link String}, such
 * as {@link CharSequence}. If there is no string-compatible constructor, a {@link ConversionException} is thrown.
 * @param <V> The value type this converter supports.
 * @author Garret Wilson
 */
public class DefaultStringLiteralConverter<V> extends AbstractStringLiteralConverter<V> {

	/** The class representing the type of value to convert. */
	private final Class<V> valueClass;

	/** @return The class representing the type of value to convert. */
	public Class<V> getValueClass() {
		return valueClass;
	}

	/**
	 * Constructs a default string literal converter indicating the type of value to convert.
	 * @param valueClass The class indicating the type of value to convert.
	 * @throws NullPointerException if the given value class is <code>null</code>.
	 */
	public DefaultStringLiteralConverter(final Class<V> valueClass) {
		this.valueClass = requireNonNull(valueClass, "Value class cannot be null."); //store the value class
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation converts from a string literal to an object using a string constructor, or if one is not present, the first constructor with a single
	 * parameter that is type-compatible with {@link String}, such as {@link CharSequence}. If there is no string-compatible constructor, a
	 * {@link ConversionException} is thrown.
	 * </p>
	 */
	@Override
	public V convertLiteral(final String literal) throws ConversionException {
		if(literal == null) { //if the literal is null
			return null; //the value is null
		} else { //if the literal is not null
			final Class<V> valueClass = getValueClass(); //get the value class
			final Constructor<V> stringCompatibleConstructor = getCompatiblePublicConstructor(valueClass, String.class); //get the string-compatible constructor
			if(stringCompatibleConstructor != null) { //if there is a string-compatible constructor
				try {
					return stringCompatibleConstructor.newInstance(literal); //try to invoke the constructor and return a new object	
				} catch(final InvocationTargetException invocationTargetException) { //if the constructor threw an error
					final Throwable cause = invocationTargetException.getCause(); //get the cause of the exception
					if(cause instanceof IllegalArgumentException) { //if there was something incorrect about the string literal argument
						throw new ConversionException(format(getSession().dereferenceString(getInvalidValueMessage()), literal), literal); //indicate that the value was invalid
					} else { //if there is some other constructor error
						throw new ConversionException(cause); //send it back; we don't know what it is, and we can't be sure it means the value is invalid
					}
				} catch(final IllegalArgumentException e) {
					throw new ConversionException(e);
				} catch(final InstantiationException e) {
					throw new ConversionException(e);
				} catch(final IllegalAccessException e) {
					throw new ConversionException(e);
				}
			} else { //if there are no string-compatible constructors
				throw new ConversionException("Class " + getValueClass() + " does not support literal to value conversions.", literal);
			}
		}
	}
}
