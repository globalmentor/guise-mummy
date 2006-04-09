package com.guiseframework.validator;

/**A validator that requires a value to be entered.
@author Garret Wilson
*/
public class ValueRequiredValidator<V> extends AbstractValidator<V>
{

	/**Default constructor.*/
	public ValueRequiredValidator()
	{
		super(true);	//construct the parent class, indicating that values are required
	}
}
