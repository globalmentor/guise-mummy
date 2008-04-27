/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.platform.web.css;


import static com.globalmentor.java.Objects.*;
import static com.globalmentor.text.xml.stylesheets.css.XMLCSS.*;

/**A pseudo class simple selector.
@author Garret Wilson
*/
public class PseudoClass implements SimpleSelector, Comparable<PseudoClass>
{

	/**The name of the pseudo class to be selected.*/
	private final String pseudoClassName;

		/**@return The name of the pseudo class to be selected.*/
		public String getPseudoClassName() {return pseudoClassName;}

	/**Pseudo class name constructor.
	@param pseudoClassName The name of the pseudo class to be selected.
	@exception NullPointerException if the given pseudo class name is <code>null</code>.
	*/
	public PseudoClass(final String pseudoClassName)
	{
		this.pseudoClassName=checkInstance(pseudoClassName, "Pseudo class name cannot be null.");
	}

	/**@return A hash code for this object.*/
	public int hashCode()
	{
		return getPseudoClassName().hashCode();
	}

	/**Determines whether this object is equivalent to another object.
	@param object The object to compare with this object.
	@return <code>true</code> if this object is equivalent to the given object.
	*/
	public boolean equals(final Object object)
	{
		return object instanceof PseudoClass && getPseudoClassName().equals(((PseudoClass)object).getPseudoClassName());
	}

	/**@return A string representation of this object.*/
	public String toString()
	{
		return new StringBuilder().append(PSEUDO_CLASS_DELIMITER).append(getPseudoClassName()).toString();
	}

  /**Compares this object with the specified object for order.
  This implementation compares pseudo-class names.
	Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	@param object The object to be compared.
	@return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	*/
  public int compareTo(final PseudoClass object)
  {
  	return getPseudoClassName().compareTo(object.getPseudoClassName());	//compare pseudo-class names
  }
}
