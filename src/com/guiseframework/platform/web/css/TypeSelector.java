package com.guiseframework.platform.web.css;

import static com.garretwilson.lang.ObjectUtilities.*;

/**A type simple selector.
This implementation represents the universal selector by an instance of a type selector with the type "*".
@author Garret Wilson
*/
public class TypeSelector implements SimpleSelector
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
}
