package com.javaguise.converter;

/**The style of a time in its string literal form.*/
public enum TimeStringLiteralStyle
{
	/**A completely numeric representation, such as 3:30pm.*/
	SHORT,
	/**A medium representation*/
	MEDIUM,
	/**A long representation, such as 3:30:32pm.*/
	LONG,
	/**A completely specified representation, such as 3:30:42pm PST*/
	FULL;
}
