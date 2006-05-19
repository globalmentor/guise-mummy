package com.guiseframework.platform.web.css;

import java.util.*;

import com.garretwilson.util.NameValuePair;

/**A selector of a CSS rule.
A selector represents a list of simple selector sequences, each paired with a combinator.
The first combinator of which will be ignored.
@author Garret Wilson
@see <a href="http://www.w3.org/TR/css3-selectors/">CSS 3 Selectors</a>
*/
public class Selector extends ArrayList<NameValuePair<Combinator,List<SimpleSelector>>>
{

	/**The list of simple selector sequences, each paired with a combinator, the first combinator of which will be ignored.*/ 
//TODO del	private final List<NameValuePair<Combinator, List<SimpleSelector>>> simpleSelectorSequenceChain=new ArrayList<NameValuePair<Combinator,List<SimpleSelector>>>();

		/**@return The list of simple selector sequences, each paired with a combinator, the first combinator of which will be <code>null</code>.*/ 
//TODO del		public List<NameValuePair<Combinator, List<SimpleSelector>>> getSimpleSelectorSequenceChain() {return simpleSelectorSequenceChain;}

//TODO add support for pseudo-elements

	/**@return A string representation of the selector.*/
	public String toString()
	{
		final StringBuilder stringBuilder=new StringBuilder();
		for(final NameValuePair<Combinator, List<SimpleSelector>> combinatorSimpleSelectorSequence:this)	//for each combinator/simple selector sequence pair
		{
			if(stringBuilder.length()>0)	//if this isn't the first combinator
			{
				stringBuilder.append(combinatorSimpleSelectorSequence.getName());	//append the combinator
			}
			for(final SimpleSelector simpleSelector:combinatorSimpleSelectorSequence.getValue())	//for each simple selector in the sequence
			{
				stringBuilder.append(simpleSelector);	//append the simple selector
			}
		}
		return stringBuilder.toString();
	}

}
