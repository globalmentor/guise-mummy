package com.guiseframework.model;

import java.beans.PropertyVetoException;

import com.garretwilson.lang.Objects;
import com.guiseframework.validator.*;

/**A default implementation of a model representing a value.
@param <V> The type of value contained in the model.
@author Garret Wilson
*/
public class DefaultValueModel<V> extends AbstractValueModel<V>
{
	
	/**The default value.*/
	private final V defaultValue;

		/**@return The default value.*/
		public V getDefaultValue() {return defaultValue;}

	/**The input value, or <code>null</code> if there is no value.*/
	private V value;

		/**@return The input value, or <code>null</code> if there is no input value.*/
		public V getValue() {return value;}

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
		public void setValue(final V newValue) throws PropertyVetoException
		{
			final V oldValue=value;	//get the old value
			final Validator<V> validator=getValidator();	//get the currently installed validator, if there is one
			if(validator!=null)	//if a validator is installed, always validate the value, even if it isn't changing, so that an initial value that may not be valid will throw an error when it's tried to be set to the same, but invalid, value
			{
				try
				{
					validator.validate(newValue);	//validate the new value
				}
				catch(final ValidationException validationException)	//if the new value doesn't pass validation
				{
					throw createPropertyVetoException(this, validationException, VALUE_PROPERTY, oldValue, newValue);	//throw a property veto exception representing the validation error
				}
			}
			if(!Objects.equals(value, newValue))	//if the value is really changing (compare their values, rather than identity)
			{
				fireVetoableChange(VALUE_PROPERTY, oldValue, newValue);	//notify vetoable change listeners of the impending change
				value=newValue;	//actually change the value, of the change wasn't vetoed
				firePropertyChange(VALUE_PROPERTY, oldValue, newValue);	//indicate that the value changed
			}			
		}

		/**Clears the value by setting the value to <code>null</code>, which may be invalid according to any installed validators.
		No validation occurs.
		@see ValueModel#VALUE_PROPERTY
		*/
		public void clearValue()
		{
			if(!Objects.equals(value, null))	//if the value is really changing (compare their values, rather than identity)
			{
				final V oldValue=value;	//get the old value
				value=null;	//actually change the value
				firePropertyChange(VALUE_PROPERTY, oldValue, null);	//indicate that the value changed
			}						
		}

		/**Resets the value to a default value, which may be invalid according to any installed validators.
		No validation occurs.
		@see ValueModel#VALUE_PROPERTY
		*/
		public void resetValue()
		{
			final V defaultValue=getDefaultValue();	//get the default value
			if(!Objects.equals(value, defaultValue))	//if the value is really changing (compare their values, rather than identity)
			{
				final V oldValue=value;	//get the old value
				value=defaultValue;	//actually change the value
				firePropertyChange(VALUE_PROPERTY, oldValue, null);	//indicate that the value changed
			}						
		}

	/**Constructs a value model indicating the type of value it can hold.
	The default value is set to <code>null</code>.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public DefaultValueModel(final Class<V> valueClass)
	{
		this(valueClass, null);	//construct the class with a null default value
	}

	/**Constructs a value model indicating the type of value it can hold, along with an initial value.
	@param valueClass The class indicating the type of value held in the model.
	@param defaultValue The default value, which will not be validated.
	@exception NullPointerException if the given value class is <code>null</code>.
	*/
	public DefaultValueModel(final Class<V> valueClass, final V defaultValue)
	{
		super(valueClass);	//construct the parent class
		this.defaultValue=defaultValue;	//save the default value
		this.value=defaultValue;	//set the value to the default value
	}

}
