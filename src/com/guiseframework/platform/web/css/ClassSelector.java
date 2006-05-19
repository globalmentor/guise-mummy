package com.guiseframework.platform.web.css;

import static com.garretwilson.lang.ObjectUtilities.*;

import static com.garretwilson.text.xml.stylesheets.css.XMLCSSConstants.*;

/**A class simple selector.
@author Garret Wilson
*/
public class ClassSelector implements SimpleSelector
{

	/**The name of the class to be selected.*/
	private final String className;

		/**@return The name of the class to be selected.*/
		public String getClassName() {return className;}

	/**Class name constructor.
	@param className The name of the class to be selected.
	@exception NullPointerException if the given class name is <code>null</code>.
	*/
	public ClassSelector(final String className)
	{
		this.className=checkInstance(className, "Class name cannot be null.");
	}

	/**@return A hash code for this object.*/
	public int hashCode()
	{
		return getClassName().hashCode();
	}

	/**Determines whether this object is equivalent to another object.
	@param object The object to compare with this object.
	@return <code>true</code> if this object is equivalent to the given object.
	*/
	public boolean equals(final Object object)
	{
		return object instanceof ClassSelector && getClassName().equals(((ClassSelector)object).getClassName());
	}

	/**@return A string representation of this object.*/
	public String toString()
	{
		return new StringBuilder().append(CLASS_SELECTOR_DELIMITER).append(getClassName()).toString();
	}
}
