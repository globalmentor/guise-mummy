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

package com.guiseframework.component;

import java.beans.PropertyVetoException;

import static com.globalmentor.java.Objects.*;

import com.guiseframework.component.layout.Layout;
import com.guiseframework.model.*;
import com.guiseframework.validator.*;

/**
 * An abstract implementation of a layout component that is also a value control. The component valid status is updated before a change in the
 * {@link #VALUE_PROPERTY} or the {@link #VALIDATOR_PROPERTY} is fired.
 * @param <V> The type of value to represent.
 * @author Garret Wilson
 */
public abstract class AbstractLayoutValueControl<V> extends AbstractLayoutControl implements ValueControl<V> {

	/** The value model used by this component. */
	private final ValueModel<V> valueModel;

	/** @return The value model used by this component. */
	protected ValueModel<V> getValueModel() {
		return valueModel;
	}

	/**
	 * Layout and value model constructor.
	 * @param layout The layout definition for the layout component.
	 * @param valueModel The component value model.
	 * @throws NullPointerException if the given layout and/or value model is <code>null</code>.
	 */
	public AbstractLayoutValueControl(final Layout<?> layout, final ValueModel<V> valueModel) {
		super(layout); //construct the parent class
		this.valueModel = checkInstance(valueModel, "Value model cannot be null."); //save the table model
		this.valueModel.addPropertyChangeListener(getRepeatPropertyChangeListener()); //listen an repeat all property changes of the value model
		this.valueModel.addVetoableChangeListener(getRepeatVetoableChangeListener()); //listen and repeat all vetoable changes of the value model
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version first updates the valid status if the value is reported as being changed.
	 * </p>
	 */
	@Override
	protected <VV> void firePropertyChange(final String propertyName, final VV oldValue, final VV newValue) {
		if(VALUE_PROPERTY.equals(propertyName) || VALIDATOR_PROPERTY.equals(propertyName)) { //if the value property or the validator property is being reported as changed
			updateValid(); //update the valid status based upon the new property, so that any listeners will know whether the new property is valid
		}
		super.firePropertyChange(propertyName, oldValue, newValue); //fire the property change event normally
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version checks the validity of the value model.
	 * </p>
	 */
	@Override
	protected boolean determineValid() {
		if(!super.determineValid()) { //if we don't pass the default validity checks
			return false; //the component isn't valid
		}
		return getValueModel().isValidValue(); //the component is valid if the value model has a valid value
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version validates the associated value model.
	 * </p>
	 */
	@Override
	public boolean validate() {
		super.validate(); //validate the parent class
		try {
			getValueModel().validateValue(); //validate the value model
		} catch(final ValidationException validationException) { //if there is a validation error
			setNotification(new Notification(validationException)); //add a notification of this error to the component
		}
		return isValid(); //return the current valid state
	}

	@Override
	public V getDefaultValue() {
		return getValueModel().getDefaultValue();
	}

	@Override
	public V getValue() {
		return getValueModel().getValue();
	}

	@Override
	public void setValue(final V newValue) throws PropertyVetoException {
		getValueModel().setValue(newValue);
	}

	@Override
	public void clearValue() {
		getValueModel().clearValue();
	}

	@Override
	public void resetValue() {
		getValueModel().resetValue();
	}

	@Override
	public Validator<V> getValidator() {
		return getValueModel().getValidator();
	}

	@Override
	public void setValidator(final Validator<V> newValidator) {
		getValueModel().setValidator(newValidator);
	}

	@Override
	public boolean isValidValue() {
		return getValueModel().isValidValue();
	}

	@Override
	public void validateValue() throws ValidationException {
		getValueModel().validateValue();
	}

	@Override
	public Class<V> getValueClass() {
		return getValueModel().getValueClass();
	}

}
