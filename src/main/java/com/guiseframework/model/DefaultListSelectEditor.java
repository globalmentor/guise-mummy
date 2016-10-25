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

import static com.globalmentor.java.Objects.*;

import com.guiseframework.component.*;

/**
 * The default implementation for editing values in a list model. Prototypes are provided for common edit functionality. This class registers itself with the
 * given list, which will cause memory leaks if an instance of this class is discarded without also discarding the list. The editing component class and the
 * list select model's value class must each have a public default constructor.
 * @param <V> The type of values contained in the model.
 * @author Garret Wilson
 */
public class DefaultListSelectEditor<V> extends AbstractListSelectEditor<V> {

	/** The class for the component allowing a list value to be edited. */
	private final Class<? extends ValuedComponent<V>> valuedComponentClass;

	/** @return The class for the component allowing a list value to be edited. */
	public Class<? extends ValuedComponent<V>> getValuedComponentClass() {
		return valuedComponentClass;
	}

	/**
	 * List select model and valued component constructor.
	 * @param listSelectModel The list select model this prototype manipulates.
	 * @param valuedComponentClass The class for the component allowing a list value to be edited.
	 * @throws NullPointerException if the given list select model and/or value component class is <code>null</code>.
	 */
	public DefaultListSelectEditor(final ListSelectModel<V> listSelectModel, final Class<? extends ValuedComponent<V>> valuedComponentClass) {
		super(listSelectModel); //construct the parent class
		this.valuedComponentClass = checkInstance(valuedComponentClass, "Valued component class cannot be null.");
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation instantiates a default instance of the list select model's value class.
	 * </p>
	 * @see ListSelectModel#getValueClass()
	 */
	@Override
	protected V createValue() {
		try {
			return getListSelectModel().getValueClass().newInstance(); //return a new instance of the list select model's value class
		} catch(final InstantiationException instantiationException) {
			throw new AssertionError(instantiationException);
		} catch(final IllegalAccessException illegalAccessException) {
			throw new AssertionError(illegalAccessException);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version creates a new valued component from the valued component class.
	 * </p>
	 * @see #getValuedComponentClass()
	 */
	@Override
	protected ValuedComponent<V> createValuedComponent() {
		try {
			return getValuedComponentClass().newInstance(); //return a new instance of the valued component class
		} catch(final InstantiationException instantiationException) {
			throw new AssertionError(instantiationException);
		} catch(final IllegalAccessException illegalAccessException) {
			throw new AssertionError(illegalAccessException);
		}
	}

}
