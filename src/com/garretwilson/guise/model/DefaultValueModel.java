package com.garretwilson.guise.model;

import com.garretwilson.guise.session.GuiseSession;
import com.garretwilson.guise.validator.*;
import com.garretwilson.lang.ObjectUtilities;

import static com.garretwilson.lang.ObjectUtilities.*;

/**A default implementation of a model for user input of a value.
@param <V> The type of value contained in the model.
@author Garret Wilson
*/
public class DefaultValueModel<V> extends DefaultControlModel implements ValueModel<V>
{

	/**The input value, or <code>null</code> if there is no value.*/
	private V value=null;

		/**@return The input value, or <code>null</code> if there is no input value.*/
		public V getValue() {return value;}

		/**Sets the input value.
		This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
		If a validator is installed, the value will first be validated before the current value is changed.
		Validation always occurs if a validator is installed, even if the value is not changing.
		@param newValue The input value of the model.
		@exception ValidationException if the provided value is not valid.
		@see #getValidator()
		@see ValueModel#VALUE_PROPERTY
		*/
		public void setValue(final V newValue) throws ValidationException
		{
			if(validator!=null)	//if a validator is installed, always validate the value, even if it isn't changing, so that an initial value that may not be valid will throw an error when it's tried to be set to the same, but invalid, value
			{
				validator.validate(newValue);	//validate the new value, throwing an exception if anything is wrong
			}
			if(!ObjectUtilities.equals(value, newValue))	//if the value is really changing (compare their values, rather than identity)
			{
				final Validator<V> validator=getValidator();	//get the currently installed validator, if there is one
				final V oldValue=value;	//get the old value
				value=newValue;	//actually change the value
				firePropertyChange(VALUE_PROPERTY, oldValue, newValue);	//indicate that the value changed
			}			
		}

	/**The validator for this model, or <code>null</code> if no validator is installed.*/
	private Validator<V> validator;

		/**@return The validator for this model, or <code>null</code> if no validator is installed.*/
		public Validator<V> getValidator() {return validator;}

		/**Sets the validator.
		This is a bound property
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
	@param session The Guise session that owns this model.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	*/
	public DefaultValueModel(final GuiseSession<?> session, final Class<V> valueClass)
	{
		super(session);	//construct the parent class
		this.valueClass=checkNull(valueClass, "Value class cannot be null.");	//store the value class
	}

	/**Determines whether the contenxt of this model are valid.
	This version delegates to the validator, if one is installed.
	@return Whether the contents of this model are valid.
	@see #getValidator()
	@see #getValue()
	*/
	public boolean isValid()
	{
		if(super.isValid())	//if the super class thinks we're valid
		{
			final Validator<V> validator=getValidator();	//get the current validator
			return validator!=null ? validator.isValid(getValue()) : true;	//if we have a validator, make sure it thinks the value is valid
		}
		else	//if the super class doesn't think we're valid
		{
			return false;	//indicate that we're not valid
		}
	}

}
