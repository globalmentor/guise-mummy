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

package com.guiseframework.model;

import com.guiseframework.converter.ConversionException;
import com.guiseframework.converter.Converter;

import static com.globalmentor.java.Objects.*;

/**
 * An info model that converts a value to a string for the label. If no label is explicitly set, the label will represent the given value converted to a string
 * using the given converter.
 * @param <V> The type of value represented by the label.
 * @author Garret Wilson
 */
public class ValueConverterInfoModel<V> extends DefaultInfoModel {

	/** The represented value. */
	private final V value;

	/** @return The represented value. */
	public final V getValue() {
		return value;
	}

	/** The converter to use for displaying the value as a string. */
	private final Converter<V, String> converter;

	/** @return The converter to use for displaying the value as a string. */
	public Converter<V, String> getConverter() {
		return converter;
	}

	/**
	 * Value and converter constructor.
	 * @param value The value to represent as a label.
	 * @param converter The converter to use for displaying the value as a string.
	 * @throws NullPointerException if the given converter is <code>null</code>.
	 */
	public ValueConverterInfoModel(final V value, final Converter<V, String> converter) {
		this.value = value; //save the value
		this.converter = checkInstance(converter, "Converter cannot be null."); //save the converter		
	}

	@Override
	public String getLabel() {
		String label = super.getLabel(); //get the specified label
		if(label == null) { //if no label is specified
			try {
				label = getConverter().convertValue(getValue()); //convert the value to a string
			} catch(final ConversionException conversionException) {
				throw new AssertionError(conversionException); //TODO fix better
			}
		}
		return label; //return the label
	}

}
