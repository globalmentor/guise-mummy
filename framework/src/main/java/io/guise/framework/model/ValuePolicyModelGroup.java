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

package io.guise.framework.model;

import java.beans.PropertyChangeEvent;

import com.globalmentor.beans.*;

/**
 * An abstract implementation of a group of value models implementing a value policy across several models, such as mutual exclusion. When value models are
 * added to the list, the group adds itself as a property value change listeners to the model, listening for changes in the value.
 * @param <V> The type of value contained in the value model.
 * @author Garret Wilson.
 * @see io.guise.framework.model.ValueModel
 */
public abstract class ValuePolicyModelGroup<V> extends AbstractModelGroup<ValueModel<V>> implements GenericPropertyChangeListener<V> {

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version adds this class instance as a listener to the model's value changing.
	 * </p>
	 */
	@Override
	protected void addImpl(final ValueModel<V> valueModel) {
		super.addImpl(valueModel); //add the model normally
		valueModel.addPropertyChangeListener(ValueModel.VALUE_PROPERTY, this); //listen for value changes
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version removes this class instance as a listener to the model's value changing.
	 * </p>
	 */
	@Override
	protected void removeImpl(final ValueModel<V> valueModel) {
		valueModel.removePropertyChangeListener(ValueModel.VALUE_PROPERTY, this); //stop listening for value changes
		super.removeImpl(valueModel); //remove the model normally
	}

	@SuppressWarnings("unchecked")
	@Override
	public final void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
		final GenericPropertyChangeEvent<V> guisePropertyChangeEvent = AbstractGenericPropertyChangeListener.getGenericPropertyChangeEvent(propertyChangeEvent); //create a genericized event object
		propertyChange(guisePropertyChangeEvent); //call the generic version of the method with the genericized event object
	}

	/**
	 * Model constructor.
	 * @param models Zero or more models with which to initially place in the group.
	 * @throws NullPointerException if one of the models is <code>null</code>.
	 */
	public ValuePolicyModelGroup(final ValueModel<V>... models) {
		super(models); //construct the parent class
	}

}
