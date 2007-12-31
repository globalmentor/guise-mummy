package com.guiseframework.validator;

import java.util.regex.Pattern;

import static com.garretwilson.lang.Objects.*;

/**A string validator that can validate against regular expressions.
@author Garret Wilson
*/
public class RegularExpressionStringValidator extends AbstractRegularExpressionValidator<String>
{

	/**Constructs a string regular expression validator from a regular expression string, without requiring a non-<code>null</code> value..
	@param regularExpression The regular expression against which to validate string values.
	@exception NullPointerException if the given regular expression is <code>null</code>.
	*/
	public RegularExpressionStringValidator(final String regularExpression)
	{
		this(regularExpression, false);	//construct the class without requiring a value
	}

	/**Constructs a string regular expression validator from a regular expression string.
	@param regularExpression The regular expression against which to validate string values.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given regular expression is <code>null</code>.
	*/
	public RegularExpressionStringValidator(final String regularExpression, final boolean valueRequired)
	{
		this(Pattern.compile(checkInstance(regularExpression, "Regular expression cannot be null.")), valueRequired);	//compile the regular expression into a pattern and construct the class
	}

	/**Constructs a string regular expression validator from a regular expression pattern, without requiring a non-<code>null</code> value.
	@param pattern The regular expression pattern against which to validate string values.
	@exception NullPointerException if the given regular expression pattern is <code>null</code>.
	*/
	public RegularExpressionStringValidator(final Pattern pattern)
	{
		this(pattern, false);	//construct the class without requiring a value
	}

	/**Constructs a string regular expression validator from a regular expression pattern.
	@param pattern The regular expression pattern against which to validate string values.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given regular expression pattern is <code>null</code>.
	*/
	public RegularExpressionStringValidator(final Pattern pattern, final boolean valueRequired)
	{
		super(pattern, valueRequired);	//construct the parent class
	}
	/**Returns a string representation of the given value so that it may be validated against the regular expression.
	This version returns the value itself.
	@param value The value being validated.
	@return A string representation of the given value, or <code>null</code> if the value is <code>null</code>.
	*/
	protected String toString(final String value)
	{
		return value;	//return the value, which is already a string
	}
}
