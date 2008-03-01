package com.guiseframework.platform.web.css;

import java.util.*;
import static java.util.Collections.*;

import com.globalmentor.util.NameValuePair;

import static com.globalmentor.java.Objects.*;

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

	/**The list of IE6 fix classes.
	This map is populated by {@link #fixIE6Stylesheet(CSSStylesheet)}.
	*/
	private final List<IE6FixClass> ie6FixClasses=new ArrayList<IE6FixClass>();

		/**The list of IE6 fix classes.
		This map is populated by {@link #fixIE6Stylesheet(CSSStylesheet)}.
		*/
		public List<IE6FixClass> getIE6FixClasses() {return ie6FixClasses;}

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
						final List<SimpleSelector> unfixedSimpleSelectorSequence=new ArrayList<SimpleSelector>(simpleSelectorSequence);	//create a copy of the unfixed simple selector sequence
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
						final String ie6FixClass=ie6FixClassStringBuilder.toString();	//get the fix class to use
						ie6FixClasses.add(new IE6FixClass(unfixedSimpleSelectorSequence, ie6FixClass));	//add this IE6 fix class
						simpleSelectorSequence.add(new ClassSelector(ie6FixClass));	//add a class selector for the fixed version of the multiple class selector
						simpleSelectorSequence.addAll(otherSelectors);	//add all the remaining simple selectors
					}
				}
			}
		}
		stylesheet.getRules().add(0, new Rule(new Selector(GUISE_IE6_FIX_TYPE_SELECTOR)));	//prepend a new rule with a Guise IE6 fix selector as a flag
	}


	/**A specification for an IE6 fix class and the selectors that cause it to be added to an element's class list.
	@author Garret Wilson
	*/
	public static class IE6FixClass
	{
		
		/**The unfixed sequence of simple selectors that match an element (e.g. "div.class1.class2").*/
		private final List<SimpleSelector> simpleSelectorSequence; 

			/**@return The unfixed sequence of simple selectors that match an element (e.g. "div.class1.class2").*/
			public List<SimpleSelector> getSimpleSelectorSequence() {return simpleSelectorSequence;} 

		/**The class to add for fixing IE6 for multiple class selectors.*/
		private final String fixClass;

			/**@return The class to add for fixing IE6 for multiple class selectors.*/
			public String getFixClass() {return fixClass;}
	
		/**Selectors and class constructor.
		@param simpleSelectors The unfixed simple selectors that match an element (e.g. "div.class1.class2").
		@param fixClass The class to add for fixing IE6 for multiple class selectors.
		@exception NullPointerException if the selectors and/or the fix class is <code>null</code>.
		*/
		public IE6FixClass(final List<SimpleSelector> simpleSelectors, final String fixClass)
		{
			this.simpleSelectorSequence=new ArrayList<SimpleSelector>(checkInstance(simpleSelectors, "Simple selectors cannot be null."));
			this.fixClass=checkInstance(fixClass, "Fix class cannot be null.");
		}
	}

}
