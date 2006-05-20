package com.guiseframework.platform.web.css;

import java.util.*;
import static java.util.Collections.*;

import com.garretwilson.util.NameValuePair;
import static com.garretwilson.lang.ObjectUtilities.*;

/**A CSS stylesheet processor that handles Guise-specific manipulations.
@author Garret Wilson
*/
public class GuiseCSSProcessor extends CSSProcessor
{

	/**The type selector for indicating that a stylesheet has been fixed for IE6.*/
	public final static TypeSelector GUISE_IE6_FIX_TYPE_SELECTOR=new TypeSelector("guiseIE6Fix");

	/**The class prefix for classes that have been fixed for IE6.*/
	public final static String GUISE_IE6_FIX_CLASS_PREFIX="guiseIE6";

	/**The class delimiter for classes that have been fixed for IE6.*/
	public final static String GUISE_IE6_FIX_CLASS_DELIMITER="_-";

	/**Modifies a CSS stylesheet to allow workarounds with IE6 shortcomings.
	An empty rule with the type selector {@link #GUISE_IE6_FIX_TYPE_SELECTOR} will be added as the first style for identification purposes.
	For selectors that contain multiple class selectors, the class selectors will be combined to form a selector in the form element.guiseIE6_-class1_-class2_-class3:pseudoClass, with the class selectors in alphabetical order.
	@param stylesheet The stylesheet to fix.
	*/
	public void fixIE6Stylesheet(final CSSStylesheet stylesheet)
	{
		for(final Rule rule:stylesheet.getRules())	//for each rule
		{
			for(final Selector selector:rule.getSelectors())	//for each selector
			{
				for(final NameValuePair<Combinator, List<SimpleSelector>> combinatorSimpleSelectorSequence:selector)
				{
					final List<SimpleSelector> simpleSelectorSequence=combinatorSimpleSelectorSequence.getValue();	//get this simple selector sequence
					final TypeSelector typeSelector=simpleSelectorSequence.size()>0 ? asInstance(simpleSelectorSequence.get(0), TypeSelector.class) : null;	//get the first simple selector if it is a type selector
					final List<ClassSelector> classSelectors=new ArrayList<ClassSelector>();	//create a list to store class selectors
					final List<SimpleSelector> otherSelectors=new ArrayList<SimpleSelector>();	//create a list to store other selectors
					for(final SimpleSelector simpleSelector:simpleSelectorSequence)	//for each simple selector
					{
						if(simpleSelector!=typeSelector)	//if this isn't the type selector
						{
							if(simpleSelector instanceof ClassSelector)	//if this is a class selector
							{
								classSelectors.add((ClassSelector)simpleSelector);	//add this to our list of class selectors
							}
							else	//for all other selectors
							{
								otherSelectors.add(simpleSelector);	//add this to our list of other selectors
							}
						}
					}
					if(classSelectors.size()>1)	//if there are more than one class selector, fix this selector for IE6
					{
						simpleSelectorSequence.clear();	//clear the sequence of simple selectors
						if(typeSelector!=null)	//if there is a type selector
						{
							simpleSelectorSequence.add(typeSelector);	//add it first to the list
						}
						final StringBuilder ie6FixClassStringBuilder=new StringBuilder(GUISE_IE6_FIX_CLASS_PREFIX);	//start out with a string "guiseIE6"
						sort(classSelectors);	//sort the class selectors
						for(final ClassSelector classSelector:classSelectors)	//for each class selector
						{
							ie6FixClassStringBuilder.append(GUISE_IE6_FIX_CLASS_DELIMITER).append(classSelector.getClassName());	//_-className							
						}
						simpleSelectorSequence.add(new ClassSelector(ie6FixClassStringBuilder.toString()));	//add a class selector for the fixed version of the multiple class selector
						simpleSelectorSequence.addAll(otherSelectors);	//add all the remaining simple selectors
					}
				}
			}
		}
		stylesheet.getRules().add(0, new Rule(new Selector(GUISE_IE6_FIX_TYPE_SELECTOR)));	//prepend a new rule with a Guise IE6 fix selector as a flag
	}
}
