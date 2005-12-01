package com.javaguise.converter;

/**The style of a date in its string literal form.*/
public enum DateStringLiteralStyle
{
	/**A completely numeric representation, such as 12.13.52.*/
	SHORT,
	/**A medium representation, such as Jan 12, 1952.*/
	MEDIUM,
	/**A long representation, such as January 12, 1952.*/
	LONG,
	/**A completely specified representation, such as Tuesday, April 12, 1952 AD.*/
	FULL,
	/**The day of the week, such as Tuesday.*/
	DAY_OF_WEEK,	
	/**The abbreviated day of the week, such as Tue.*/
	DAY_OF_WEEK_SHORT,
	/**The month of the year, such as January.*/
	MONTH_OF_YEAR,	
	/**The abbreviated month of the year, such as Jan.*/
	MONTH_OF_YEAR_SHORT;
}
