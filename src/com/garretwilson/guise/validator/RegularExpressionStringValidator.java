package com.garretwilson.guise.validator;

import java.util.regex.Pattern;
import com.garretwilson.guise.session.GuiseSession;
import static com.garretwilson.lang.ObjectUtilities.*;

/**A string validator that can validate against regular expressions.
@author Garret Wilson
*/
public class RegularExpressionStringValidator extends AbstractValidator<String>
{

	/**The regular expression pattern against which to validate string values.*/
	private final Pattern pattern;

		/**@return The regular expression pattern against which to validate string values.*/
		public Pattern getPattern() {return pattern;}

	/**Constructs a string regular expression validator from a regular expression string, without requiring a non-<code>null</code> value..
	@param session The Guise session that owns this validator.
	@param regularExpression The regular expression against which to validate string values.
	@exception NullPointerException if the given session and/or regular expression is <code>null</code>.
	*/
	public RegularExpressionStringValidator(final GuiseSession<?> session, final String regularExpression)
	{
		this(session, regularExpression, false);	//construct the class without requiring a value
	}

	/**Constructs a string regular expression validator from a regular expression string.
	@param session The Guise session that owns this validator.
	@param regularExpression The regular expression against which to validate string values.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given session and/or regular expression is <code>null</code>.
	*/
	public RegularExpressionStringValidator(final GuiseSession<?> session, final String regularExpression, final boolean valueRequired)
	{
		this(session, Pattern.compile(checkNull(regularExpression, "Regular expression cannot be null.")), valueRequired);	//compile the regular expression into a pattern and construct the class
	}

	/**Constructs a string regular expression validator from a regular expression pattern, without requiring a non-<code>null</code> value.
	@param session The Guise session that owns this validator.
	@param pattern The regular expression pattern against which to validate string values.
	@exception NullPointerException if the given session and/or regular expression is <code>null</code>.
	*/
	public RegularExpressionStringValidator(final GuiseSession<?> session, final Pattern pattern)
	{
		this(session, pattern, false);	//construct the class without requiring a value
	}

	/**Constructs a string regular expression validator from a regular expression pattern.
	@param session The Guise session that owns this validator.
	@param pattern The regular expression pattern against which to validate string values.
	@param valueRequired Whether the value must be non-<code>null</code> in order to be considered valid.
	@exception NullPointerException if the given session and/or regular expression is <code>null</code>.
	*/
	public RegularExpressionStringValidator(final GuiseSession<?> session, final Pattern pattern, final boolean valueRequired)
	{
		super(session, valueRequired);	//construct the parent class
		this.pattern=checkNull(pattern, "Regular expression pattern cannot be null.");	//save the pattern
	}

	/**Determines whether a given string matches the regular expression.
	This version delgates to the super class version to determine whether <code>null</code> values are allowed.
	@param value The value to validate.
	@return <code>true</code> if the string matches the regular expression and fulfills the required requirement, if any, else <code>false</code>.
	@see #getPattern()
	*/
	public boolean isValid(final String value)
	{
		return super.isValid(value) && (value==null || pattern.matcher(value).matches());	//make sure the string matches the regular expression (just because we allow null here doesn't mean the super class is allowing it---that's configurable)
	}
}
