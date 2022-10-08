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

import static com.globalmentor.java.Classes.*;

import com.globalmentor.beans.PropertyBindable;

import io.guise.framework.GuiseSession;
import io.guise.framework.Resources;

/**
 * Indicates an object that can convert a value from and to its lexical form.
 * @param <V> The value type this converter supports.
 * @param <L> The literal type of the lexical form of the value.
 * @author Garret Wilson
 * @see Resources#CONVERTER_INVALID_VALUE_MESSAGE_RESOURCE_REFERENCE
 */
public interface Converter<V, L> extends PropertyBindable {

	/** The invalid value message bound property. */
	public static final String INVALID_VALUE_MESSAGE_PROPERTY = getPropertyName(Converter.class, "invalidValueMessage");

	/** @return The invalid value message text, which may include a resource reference. */
	public String getInvalidValueMessage();

	/**
	 * Sets the text of the invalid value message. This is a bound property.
	 * @param newInvalidValueMessage The new text of the invalid value message, which may include a resource reference.
	 * @throws NullPointerException if the given message is <code>null</code>.
	 * @see #INVALID_VALUE_MESSAGE_PROPERTY
	 */
	public void setInvalidValueMessage(final String newInvalidValueMessage);

	/** @return The Guise session that owns this converter. */
	public GuiseSession getSession();

	/**
	 * Converts a literal representation of a value from the lexical space into a value in the value space.
	 * @param literal The literal value in the lexical space to convert.
	 * @return The converted value in the value space, or <code>null</code> if the given literal is <code>null</code>.
	 * @throws ConversionException if the literal value cannot be converted.
	 */
	public V convertLiteral(final L literal) throws ConversionException;

	/**
	 * Converts a value from the value space to a literal value in the lexical space.
	 * @param value The value in the value space to convert.
	 * @return The converted value in the lexical space, or <code>null</code> if the given literal is <code>null</code>.
	 * @throws ConversionException if the value cannot be converted.
	 */
	public L convertValue(final V value) throws ConversionException;

	/**
	 * Determines whether a given literal value in the lexical space can be converted to a value in the value space.
	 * @param literal The literal value to validate.
	 * @return <code>true</code> if the literal is valid, else <code>false</code>.
	 */
	public boolean isValidLiteral(final L literal);

	/**
	 * Determines if the given literal in the lexical space is a valid representation of the given value in the value space.
	 * @param value The value to compare.
	 * @param literal The literal value in the lexical space to compare with the value after conversion.
	 * @return <code>true</code> if the given literal in the lexical space is a valid representation of the given value in the value space.
	 */
	public boolean isEquivalent(final V value, final L literal);
}
