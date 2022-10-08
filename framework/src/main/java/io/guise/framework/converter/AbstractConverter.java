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

import static io.guise.framework.Resources.*;
import static java.util.Objects.*;

import com.globalmentor.java.Objects;

import io.guise.framework.event.GuiseBoundPropertyObject;

/**
 * An abstract implementation an object that can convert a value from and to its lexical form.
 * @param <V> The value type this converter supports.
 * @param <L> The literal type of the lexical form of the value.
 * @author Garret Wilson
 */
public abstract class AbstractConverter<V, L> extends GuiseBoundPropertyObject implements Converter<V, L> {

	/** The invalid value message text, which may include a resource reference. */
	private String invalidValueMessage = CONVERTER_INVALID_VALUE_MESSAGE_RESOURCE_REFERENCE;

	@Override
	public String getInvalidValueMessage() {
		return invalidValueMessage;
	}

	@Override
	public void setInvalidValueMessage(final String newInvalidValueMessage) {
		if(!invalidValueMessage.equals(requireNonNull(newInvalidValueMessage, "Invalid value message cannot be null."))) { //if the value is really changing
			final String oldInvalidValueMessage = invalidValueMessage; //get the old value
			invalidValueMessage = newInvalidValueMessage; //actually change the value
			firePropertyChange(INVALID_VALUE_MESSAGE_PROPERTY, oldInvalidValueMessage, newInvalidValueMessage); //indicate that the value changed
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation attempts to convert the literal value and returns <code>false</code> if conversion is unsuccessful.
	 * </p>
	 */
	@Override
	public boolean isValidLiteral(final L literal) {
		try {
			convertLiteral(literal); //try to convert the literal
			return true; //indicate that the literal can be converted
		} catch(final ConversionException conversionException) {
			return false; //indicate that the literal cannot be converted 
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation assumes that non- <code>null</code> values can correctly be compared using {@link Object#equals(Object)}.
	 * </p>
	 */
	@Override
	public boolean isEquivalent(final V value, final L literal) {
		try {
			return Objects.equals(value, convertLiteral(literal)); //see if the literal value converted to the value space matches the given value
		} catch(final ConversionException conversionException) { //if the literal value couldn't be converted
			return false; //the values aren't equivalent
		}
	}

}
