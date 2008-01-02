package com.guiseframework.platform.web.css;

import static com.globalmentor.java.Objects.*;

/**A type simple selector.
This implementation represents the universal selector by an instance of a type selector with the type "*".
@author Garret Wilson
*/
public class TypeSelector implements SimpleSelector, Comparable<TypeSelector>
{

	/**The name of the type to be selected.*/
	private final String typeName;

		/**@return The type to be selected.*/
		public String getTypeName() {return typeName;}

	/**Type name constructor.
	@param typeName The name of the type to be selected.
	@exception NullPointerException if the given type name is <code>null</code>.
	*/
	public TypeSelector(final String typeName)
	{
		this.typeName=checkInstance(typeName, "Type name cannot be null.");
	}

	/**@return A hash code for this object.*/
	public int hashCode()
	{
		return getTypeName().hashCode();
	}

	/**Determines whether this object is equivalent to another object.
	@param object The object to compare with this object.
	@return <code>true</code> if this object is equivalent to the given object.
	*/
	public boolean equals(final Object object)
	{
		return object instanceof TypeSelector && getTypeName().equals(((TypeSelector)object).getTypeName());
	}

	/**@return A string representation of this object.*/
	public String toString()
	{
		return getTypeName();
	}

  /**Compares this object with the specified object for order.
  This implementation compares type names.
	Returns a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	@param object The object to be compared.
	@return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
	*/
  public int compareTo(final TypeSelector object)
  {
  	return getTypeName().compareTo(object.getTypeName());	//compare type names
  }
}
