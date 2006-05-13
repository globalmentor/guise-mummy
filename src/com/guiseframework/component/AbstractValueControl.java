package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

import com.guiseframework.model.*;
import com.guiseframework.validator.ValidationException;
import com.guiseframework.validator.Validator;

/**Abstract implementation of a control to accept input from the user.
The component valid status is updated before a change in the {@link #VALUE_PROPERTY} or the {@link #VALIDATOR_PROPERTY} is fired. 
@param <V> The type of value to represent.
@author Garret Wilson
*/
public abstract class AbstractValueControl<V, C extends ValueControl<V, C>> extends AbstractControl<C> implements ValueControl<V, C>
{

	/**The value model used by this component.*/
	private final ValueModel<V> valueModel;

		/**@return The value model used by this component.*/
		protected ValueModel<V> getValueModel() {return valueModel;}

	/**Whether the value is editable and the control will allow the the user to change the value.*/
	private boolean editable=true;

		/**@return Whether the value is editable and the control will allow the the user to change the value.*/
		public boolean isEditable() {return editable;}

		/**Sets whether the value is editable and the control will allow the the user to change the value.
		This is a bound property of type <code>Boolean</code>.
		@param newEditable <code>true</code> if the control should allow the user to change the value.
		@see #EDITABLE_PROPERTY
		*/
		public void setEditable(final boolean newEditable)
		{
			if(editable!=newEditable)	//if the value is really changing
			{
				final boolean oldEditable=editable;	//get the old value
				editable=newEditable;	//actually change the value
				firePropertyChange(EDITABLE_PROPERTY, Boolean.valueOf(oldEditable), Boolean.valueOf(newEditable));	//indicate that the value changed
			}			
		}

	/**The property change listener that removes any notification in response to a property changing.*/
	private final PropertyChangeListener clearNotificationPropertyChangeListener=new PropertyChangeListener()	//create a listener to update the text in response to a property changing
			{
				public void propertyChange(final PropertyChangeEvent propertyChangeEvent)	//if the property changes
				{
					setNotification(null);	//clear the notification
				}
			};

	/**Label model, value model, and enableable constructor.
	@param labelModel The component label model.
	@param valueModel The component value model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model, value model, and/or enableable object is <code>null</code>.
	*/
	public AbstractValueControl(final LabelModel labelModel, final ValueModel<V> valueModel, final Enableable enableable)
	{
		super(labelModel, enableable);	//construct the parent class
		this.valueModel=checkInstance(valueModel, "Value model cannot be null.");	//save the value model
		if(valueModel!=labelModel && valueModel!=enableable)	//if the value model is not the same as the enableable object and the label model (we don't want to repeat property change events twice) TODO eventually just listen to specific events for each object
		{
			this.valueModel.addPropertyChangeListener(getRepeatPropertyChangeListener());	//listen and repeat all property changes of the value model
		}
		this.valueModel.addVetoableChangeListener(getRepeatVetoableChangeListener());	//listen and repeat all vetoable changes of the value model
		addPropertyChangeListener(VALUE_PROPERTY, clearNotificationPropertyChangeListener);	//listen for the value changing, and clear the notification in response TODO this needs to be put in other value controls as well
	}

	/**Reports that a bound property has changed.
	This version first updates the valid status if the value is reported as being changed.
	@param propertyName The name of the property being changed.
	@param oldValue The old property value.
	@param newValue The new property value.
	*/
	protected <VV> void firePropertyChange(final String propertyName, final VV oldValue, final VV newValue)
	{
		if(VALUE_PROPERTY.equals(propertyName) || VALIDATOR_PROPERTY.equals(propertyName))	//if the value property or the validator property is being reported as changed
		{
			updateValid();	//update the valid status based upon the new property, so that any listeners will know whether the new property is valid
		}
		super.firePropertyChange(propertyName, oldValue, newValue);	//fire the property change event normally
	}

	/**Checks the state of the component for validity.
	This version checks the validity of the value model.
	@return <code>true</code> if the component and all children passes all validity tests, else <code>false</code>.
	*/ 
	protected boolean determineValid()
	{
		if(!super.determineValid())	//if we don't pass the default validity checks
		{
			return false;	//the component isn't valid
		}
		return getValueModel().isValidValue();	//the component is valid if the value model has a valid value
	}
	
	/**Validates the user input of this component and all child components.
	The component will be updated with error information.
	This version validates the associated value model.
	@return The current state of {@link #isValid()} as a convenience.
	*/
	public boolean validate()
	{
		super.validate();	//validate the parent class
		try
		{
			getValueModel().validateValue();	//validate the value model
		}
		catch(final ValidationException validationException)	//if there is a validation error
		{
//TODO del			componentException.setComponent(this);	//make sure the exception knows to which component it relates
			setNotification(new Notification(validationException));	//add notification of this error to the component
		}
		return isValid();	//return the current valid state
	}

		//ValueModel delegations

	/**@return The default value.*/
	public V getDefaultValue() {return getValueModel().getDefaultValue();}

	/**@return The input value, or <code>null</code> if there is no input value.*/
	public V getValue() {return getValueModel().getValue();}

	/**Sets the new value.
	This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
	If a validator is installed, the value will first be validated before the current value is changed.
	Validation always occurs if a validator is installed, even if the value is not changing.
	If the value change is vetoed by the installed validator, the validation exception will be accessible via {@link PropertyVetoException#getCause()}.
	@param newValue The new value.
	@exception PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	@see #getValidator()
	@see #VALUE_PROPERTY
	*/
	public void setValue(final V newValue) throws PropertyVetoException {getValueModel().setValue(newValue);}

	/**Clears the value by setting the value to <code>null</code>, which may be invalid according to any installed validators.
	No validation occurs.
	@see Value#VALUE_PROPERTY
	*/
	public void clearValue() {getValueModel().clearValue();}

	/**Resets the value to a default value, which may be invalid according to any installed validators.
	No validation occurs.
	@see #VALUE_PROPERTY
	*/
	public void resetValue() {getValueModel().resetValue();}

	/**@return The validator for this model, or <code>null</code> if no validator is installed.*/
	public Validator<V> getValidator() {return getValueModel().getValidator();}

	/**Sets the validator.
	This is a bound property
	@param newValidator The validator for this model, or <code>null</code> if no validator should be used.
	@see #VALIDATOR_PROPERTY
	*/
	public void setValidator(final Validator<V> newValidator) {getValueModel().setValidator(newValidator);}

	/**Determines whether the value of this model is valid.
	@return Whether the value of this model is valid.
	*/
	public boolean isValidValue() {return getValueModel().isValidValue();}

	/**Validates the value of this model, throwing an exception if the model is not valid.
	@exception ValidationException if the value of this model is not valid.	
	*/
	public void validateValue() throws ValidationException {getValueModel().validateValue();}

	/**@return The class representing the type of value this model can hold.*/
	public Class<V> getValueClass() {return getValueModel().getValueClass();}

}
