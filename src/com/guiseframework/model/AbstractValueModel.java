package com.guiseframework.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import com.garretwilson.beans.GenericPropertyChangeEvent;
import com.guiseframework.validator.*;

import static com.garretwilson.lang.ObjectUtilities.*;

/**An abstract implementation of a model representing a value.
A derived class need only implement the value access methods.
@param <V> The type of value contained in the model.
@author Garret Wilson
*/
public abstract class AbstractValueModel<V> extends AbstractModel implements ValueModel<V>
{

	/**The validator for this model, or <code>null</code> if no validator is installed.*/
	private Validator<V> validator;

		/**@return The validator for this model, or <code>null</code> if no validator is installed.*/
		public Validator<V> getValidator() {return validator;}

		/**Sets the validator.
		This is a bound property.
		@param newValidator The validator for this model, or <code>null</code> if no validator should be used.
		@see ValueModel#VALIDATOR_PROPERTY
		*/
		public void setValidator(final Validator<V> newValidator)
		{
			if(validator!=newValidator)	//if the value is really changing
			{
				final Validator<V> oldValidator=validator;	//get the old value
				validator=newValidator;	//actually change the value
				firePropertyChange(VALIDATOR_PROPERTY, oldValidator, newValidator);	//indicate that the value changed
			}
		}

	/**The class representing the type of value this model can hold.*/
	private final Class<V> valueClass;

		/**@return The class representing the type of value this model can hold.*/
		public Class<V> getValueClass() {return valueClass;}

	/**Constructs a value model indicating the type of value it can hold.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public AbstractValueModel(final Class<V> valueClass)
	{
		this.valueClass=checkInstance(valueClass, "Value class cannot be null.");	//store the value class
	}

	/**Determines whether the value of this model is valid.
	This version delegates to the validator, if one is installed.
	@return Whether the value of this model is valid.
	@see #getValidator()
	@see #getValue()
	*/
	public boolean isValidValue()
	{
		final Validator<V> validator=getValidator();	//get the current validator
		return validator!=null ? validator.isValid(getValue()) : true;	//if we have a validator, make sure it thinks the value is valid
	}

	/**Validates the value of this model, throwing an exception if the model is not valid.
	This version validates the current value if there is a validator installed.
	@exception ValidationException if the value of this model is not valid.	
	*/
	public void validateValue() throws ValidationException
	{
		final Validator<V> validator=getValidator();	//get the currently installed validator, if there is one
		if(validator!=null)	//if a validator is installed
		{
			validator.validate(getValue());	//validate the current value, throwing an exception if anything is wrong
		}
	}

	/**Creates a property veto exception that represents a validation exception.
	The validation exception will be accessible via {@link PropertyVetoException#getCause()}.
	This is useful for converting a validation exception into a property veto exception in {@link ValueModel#setValue(Object)} if an installed validator deems a value invalid.
	@param <VV> The type of property the change of which was vetoed because of invalidity.
	@param source The source of the property change event.
	@param validationException The validation exception that is the cause of the property veto.
	@param propertyName The name of the property the change of which was vetoed.
	@param oldValue The old value of the property.
	@param newValue The new value of the property.
	@return A property veto exception representing the validation error.
	*/
	public static <VV> PropertyVetoException createPropertyVetoException(final Object source, final ValidationException validationException, final String propertyName, final VV oldValue, final VV newValue)
	{	
		final PropertyChangeEvent propertyChangeEvent=new GenericPropertyChangeEvent<VV>(source, propertyName, oldValue, newValue);	//create a new property change event
		final PropertyVetoException propertyVetoException=new PropertyVetoException(validationException.getMessage(), propertyChangeEvent);	//create a new property veto exception with the message from the validation exception
		propertyVetoException.initCause(validationException);	//indicate the cause of the property veto exception
		return propertyVetoException;	//return the new property veto exception
	}

}
