package com.javaguise.model;

import com.javaguise.GuiseSession;
import com.javaguise.validator.*;
import com.garretwilson.lang.ObjectUtilities;

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
			final Validator<V> validator=getValidator();	//get the currently installed validator, if there is one
			if(validator!=null)	//if a validator is installed, always validate the value, even if it isn't changing, so that an initial value that may not be valid will throw an error when it's tried to be set to the same, but invalid, value
			{
				validator.validate(newValue);	//validate the new value, throwing an exception if anything is wrong
			}
			if(!ObjectUtilities.equals(value, newValue))	//if the value is really changing (compare their values, rather than identity)
			{
				final V oldValue=value;	//get the old value
				value=newValue;	//actually change the value
				firePropertyChange(VALUE_PROPERTY, oldValue, newValue);	//indicate that the value changed
			}			
		}

		/**Clears the value by setting the value to <code>null</code>, which may be invalid according to any installed validators.
		No validation occurs.
		@see ValueModel#VALUE_PROPERTY
		*/
		public void clearValue()
		{
			if(!ObjectUtilities.equals(value, null))	//if the value is really changing (compare their values, rather than identity)
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
			if(!ObjectUtilities.equals(value, getDefaultValue()))	//if the value is really changing (compare their values, rather than identity)
			{
				final V oldValue=value;	//get the old value
				value=defaultValue;	//actually change the value
				firePropertyChange(VALUE_PROPERTY, oldValue, null);	//indicate that the value changed
			}						
		}

	/**Constructs a value model indicating the type of value it can hold.
	The default value is set to <code>null</code>.
	@param session The Guise session that owns this model.
	@param valueClass The class indicating the type of value held in the model.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	*/
	public DefaultValueModel(final GuiseSession session, final Class<V> valueClass)
	{
		this(session, valueClass, null);	//construct the class with a null default value
	}

	/**Constructs a value model indicating the type of value it can hold, along with an initial value.
	@param session The Guise session that owns this model.
	@param valueClass The class indicating the type of value held in the model.
	@param defaultValue The default value, which will not be validated.
	@exception NullPointerException if the given session and/or class object is <code>null</code>.
	*/
	public DefaultValueModel(final GuiseSession session, final Class<V> valueClass, final V defaultValue)
	{
		super(session, valueClass);	//construct the parent class
		this.defaultValue=defaultValue;	//save the default value
		this.value=defaultValue;	//set the value to the default value
	}

}
