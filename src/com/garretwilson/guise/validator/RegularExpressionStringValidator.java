package com.garretwilson.guise.validator;

import static com.garretwilson.lang.ObjectUtilities.*;

import java.util.regex.Pattern;

/**A string validator that can validate against regular expressions.
@author Garret Wilson
*/
public class RegularExpressionStringValidator extends AbstractValidator<String>
{

	/**The regular expression pattern against which to validate string values.*/
	private final Pattern pattern;

		/**@return The regular expression pattern against which to validate string values.*/
		public Pattern getPattern() {return pattern;}

	/**Constructs a string regular expression validator from a regular expression string.
	@param regularExpression The regular expression against which to validate string values.
	@exception NullPointerException if the given regular expression is <code>null</code>.
	*/
	public RegularExpressionStringValidator(final String regularExpression)
	{
		this(Pattern.compile(checkNull(regularExpression, "Regular expression cannot be null.")));	//compile the regular expression into a pattern and construct the class
	}

	/**Constructs a string regular expression validator from a regular expression pattern.
	@param pattern The regular expression pattern against which to validate string values.
	@exception NullPointerException if the given regular expression is <code>null</code>.
	*/
	public RegularExpressionStringValidator(final Pattern pattern)
	{
		this.pattern=checkNull(pattern, "Regular expression pattern cannot be null.");	//save the pattern
	}

	/**Determines whether a given string matches the regular expression or is <code>null</code>.
	@param value The value to validate.
	@return <code>true</code> if the string is <code>null</code> or matches the regular expression, else <code>false</code>.
	@see #getPattern()
	*/
	public boolean isValid(final String value)
	{
		return value==null || pattern.matcher(value).matches();	//make sure the string matches the regular expression if the string is not null
	}
}
