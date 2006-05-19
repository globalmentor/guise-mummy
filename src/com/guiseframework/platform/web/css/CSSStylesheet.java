package com.guiseframework.platform.web.css;

import java.util.*;

/**A CSS stylesheet.
@author Garret Wilson
*/
public class CSSStylesheet
{

	/**The list of stylesheet rules.*/
	private final List<Rule> rules=new ArrayList<Rule>();

		/**@return The list of stylesheet rules.*/
		public List<Rule> getRules() {return rules;}

	/**@return A string representation of this object.*/
	public String toString()
	{
		final StringBuilder stringBuilder=new StringBuilder();
		for(final Rule rule:rules)	//for each rule
		{
			stringBuilder.append(rule).append('\n');
		}
		return stringBuilder.toString();
	}
}
