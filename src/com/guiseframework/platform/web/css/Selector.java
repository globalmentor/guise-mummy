package com.guiseframework.platform.web.css;

import java.util.*;

import com.globalmentor.util.NameValuePair;

/**A selector of a CSS rule.
A selector represents a list of simple selector sequences, each paired with a combinator.
The first combinator of which will be ignored.
@author Garret Wilson
@see <a href="http://www.w3.org/TR/css3-selectors/">CSS 3 Selectors</a>
*/
public class Selector extends ArrayList<NameValuePair<Combinator, List<SimpleSelector>>>
{

//TODO add support for pseudo-elements

	/**Default constructor.*/
	public Selector()
	{
		this(null);	//construct the class with no type selector
	}

	/**Type selector constructor.
	@param typeSelector The type selector to add, or <code>null</code> if this selector should have no type selector.*/
	public Selector(final TypeSelector typeSelector)
	{
		if(typeSelector!=null)	//if a type selector is given
		{
			final List<SimpleSelector> simpleSelectorSequence=new ArrayList<SimpleSelector>();	//create a new list of simple selectors
			simpleSelectorSequence.add(typeSelector);	//add the type selector to the sequence
			add(new NameValuePair<Combinator, List<SimpleSelector>>(null, simpleSelectorSequence));	//add the selector sequence with no combinator
		}
	}

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
