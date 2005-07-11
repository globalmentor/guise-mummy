package com.garretwilson.guise.model;

import static com.garretwilson.lang.ClassUtilities.*;
import com.garretwilson.guise.validator.*;

/**A model for user input of a value.
@param <V> The type of value contained in the model.
@author Garret Wilson
*/
public interface ValueModel<V> extends ControlModel
{

	/**The editable bound property.*/
	public final static String EDITABLE_PROPERTY=getPropertyName(ValueModel.class, "editable");
	/**The value bound property.*/
	public final static String VALUE_PROPERTY=getPropertyName(ValueModel.class, "value");
	/**The validator bound property.*/
	public final static String VALIDATOR_PROPERTY=getPropertyName(ValueModel.class, "validator");

	/**@return Whether the model's value is editable and the corresponding control will allow the the user to change the value.*/
	public boolean isEditable();

	/**Sets whether the model's value is editable and the corresponding control will allow the the user to change the value.
	This is a bound property of type <code>Boolean</code>.
	@param newEditable <code>true</code> if the corresponding control should allow the user to change the value.
	@see #EDITABLE_PROPERTY
	*/
	public void setEditable(final boolean newEditable);

	/**@return The input value, or <code>null</code> if there is no input value.*/
	public V getValue();

	/**Sets the input value.
	This is a bound property that only fires a change event when the new value is different via the <code>equals()</code> method.
	If a validator is installed, the value will first be validated before the current value is changed.
	Validation always occurs if a validator is installed, even if the value is not changing.
	@param newValue The input value of the model.
	@exception ValidationException if the provided value is not valid.
	@see #getValidator()
	@see #VALUE_PROPERTY
	*/
	public void setValue(final V newValue) throws ValidationException;

	/**@return The validator for this model, or <code>null</code> if no validator is installed.*/
	public Validator<V> getValidator();

	/**Sets the validator.
	This is a bound property
	@param newValidator The validator for this model, or <code>null</code> if no validator should be used.
	@see #VALIDATOR_PROPERTY
	*/
	public void setValidator(final Validator<V> newValidator);

	/**@return The class representing the type of value this model can hold.*/
	public Class<V> getValueClass();

}
