package com.guiseframework.validator;

import java.util.regex.Pattern;

import static com.garretwilson.lang.Objects.*;

/**An abstract validator that can validate against regular expressions.
@param <V> The value type this validator supports.
@author Garret Wilson
*/
public abstract class AbstractRegularExpressionValidator<V> extends AbstractValidator<V>
{

	/**The regular expression pattern against which to validate string values.*/
	private final Pattern pattern;

		/**@return The regular expression pattern against which to validate string values.*/
		public Pattern getPattern() {return pattern;}

	/**Constructs a string regular expression validator from a regular expression string, without requiring a non-<code>null</code> value..
	@param regularExpression The regular expression against which to validate string values.
	@exception NullPointerException if the given regular expression is <code>null</code>.
	*/
	public AbstractRegularExpressionValidator(final String regularExpression)
	{
		this(regularExpression, false);	//construct the class without requiring a value
	}

	/**Constructs a string regular expression validator from a regular expression string.
	@param regularExpression The regular expression against which to validate string values.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given regular expression is <code>null</code>.
	*/
	public AbstractRegularExpressionValidator(final String regularExpression, final boolean valueRequired)
	{
		this(Pattern.compile(checkInstance(regularExpression, "Regular expression cannot be null.")), valueRequired);	//compile the regular expression into a pattern and construct the class
	}

	/**Constructs a string regular expression validator from a regular expression pattern, without requiring a non-<code>null</code> value.
	@param pattern The regular expression pattern against which to validate string values.
	@exception NullPointerException if the given regular expression pattern is <code>null</code>.
	*/
	public AbstractRegularExpressionValidator(final Pattern pattern)
	{
		this(pattern, false);	//construct the class without requiring a value
	}

	/**Constructs a string regular expression validator from a regular expression pattern.
	@param pattern The regular expression pattern against which to validate string values.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given regular expression pattern is <code>null</code>.
	*/
	public AbstractRegularExpressionValidator(final Pattern pattern, final boolean valueRequired)
	{
		super(valueRequired);	//construct the parent class
		this.pattern=checkInstance(pattern, "Regular expression pattern cannot be null.");	//save the pattern
	}

	/**Returns a string representation of the given value so that it may be validated against the regular expression.
	@param value The value being validated.
	@return A string representation of the given value, or <code>null</code> if the value is <code>null</code>.
	*/
	protected abstract String toString(final V value);

	/**Checks whether a given string matches the regular expression.
	@param value The value to validate.
	@exception ValidationException if the provided value is not valid.
	@see #getPattern()
	*/
	public void validate(final V value) throws ValidationException
	{
		super.validate(value);	//do the default validation
		if(value!=null && !pattern.matcher(toString(value)).matches())	//if there is a non-null value being checked, and it doesn't match the pattern
		{
			throwInvalidValueValidationException(value);	//indicate that the value is invalid
		}
	}

}
