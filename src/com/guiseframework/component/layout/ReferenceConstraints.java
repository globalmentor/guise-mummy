package com.guiseframework.component.layout;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.GuiseSession;

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
	@param session The Guise session that owns these constraints.
	@param id The ID to which the component should be bound.
	@exception NullPointerException if the given session and/or ID is <code>null</code>.
	*/
	public ReferenceConstraints(final GuiseSession session, final String id)
	{
		super(session);	//construct the parent class
		this.id=checkInstance(id, "ID cannot be null");	//save the ID
	}

}
