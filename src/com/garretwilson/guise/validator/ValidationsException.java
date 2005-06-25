package com.garretwilson.guise.validator;

import java.util.*;
import static java.util.Collections.*;

import com.garretwilson.guise.component.Component;

/**Validation exception indicating that multiple validation errors occured.
This exception can be caught and updated with new validation exceptions.
@author Garret Wilson
*/
public class ValidationsException extends ValidationException
{

	/**The mutable list of validation exceptions.*/
	private final Collection<ValidationException> validationExceptionList=new ArrayList<ValidationException>();

		/**The mutable list of validation exceptions.*/
		public Collection<ValidationException> getValidationExceptions() {return validationExceptionList;}

	/**Validation exceptions constructor.
	The provided validation exceptions will be added to the list of exceptions.
	*/
	public ValidationsException(final ValidationException... validationExceptions)
	{
		this((Component<?>)null, validationExceptions);	//construct the class with no component 
	}

	/**Component and validation exceptions constructor.
	The provided validation exceptions will be added to the list of exceptions.
	@param component The component for which validation failed, or <code>null</code> if the component is not known.
	*/
	public ValidationsException(final Component<?> component, final ValidationException... validationExceptions)
	{
		super(component);	//create the parent class
		addAll(validationExceptionList, validationExceptions);	//add all the provided exceptions to the list
	}
}
