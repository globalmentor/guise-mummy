package com.guiseframework.component.layout;

import static com.globalmentor.java.Objects.*;

/**Constraints on individual component layout.
These constraints specify an identifier to which a component should be bound.
@author Garret Wilson
*/
public class ReferenceConstraints extends AbstractConstraints
{
	/**The reference ID to which the component should be bound.*/
	private final String id;	//TODO maybe make this mutable

		/**@return The reference ID to which the component should be bound.*/
		public String getID() {return id;}

	/**Creates constraints with a reference ID to which a component should be bound.
	@param id The ID to which the component should be bound.
	@exception NullPointerException if the given ID is <code>null</code>.
	*/
	public ReferenceConstraints(final String id)
	{
		this.id=checkInstance(id, "ID cannot be null");	//save the ID
	}

}
