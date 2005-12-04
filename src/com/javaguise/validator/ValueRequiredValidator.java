package com.javaguise.validator;

import com.javaguise.GuiseSession;

/**A validator that requires a value to be entered.
@author Garret Wilson
*/
public class ValueRequiredValidator<V> extends AbstractValidator<V>
{

	/**Session constructor.
	@param session The Guise session that owns this validator.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public ValueRequiredValidator(final GuiseSession session)
	{
		super(session, true);	//construct the parent class, indicating that values are required
	}
}
