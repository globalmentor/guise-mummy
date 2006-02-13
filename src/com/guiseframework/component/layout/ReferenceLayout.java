package com.guiseframework.component.layout;

import static com.garretwilson.lang.ObjectUtilities.*;

import java.util.Map;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.Component;

/**A layout for components bound to component references such as IDs.
@author Garret Wilson
*/
public class ReferenceLayout extends AbstractLayout<ReferenceLayout.Constraints>
{

	/**Session constructor.
	@param session The Guise session that owns this layout.
	*/
	public ReferenceLayout(final GuiseSession session)
	{
		super(session);	//construct the parent class
	}

	/**Creates default constraints for the container.
	@return New default constraints for the container.
	@exception IllegalStateException if this layout does not support default constraints.
	*/
	public Constraints createDefaultConstraints()
	{
		throw new IllegalStateException("Component cannot have default constraints; it must be bound to some ID.");
	}

	/**Retrieves a component bound to a given ID.
	@param id The ID with which a component may be bound.
	@return A component with constraints specifying the given ID, or <code>null</code> if there is no component bound to the given ID.
	*/
	public Component<?> getComponentByID(final String id)	//TODO make this lookup more efficient by mapping constraints by IDs
	{
		for(final Map.Entry<Component<?>, Constraints> constraintsEntry:componentConstraintsMap.entrySet())	//for each component-constraints pair
		{
			if(constraintsEntry.getValue().getID().equals(id))	//if this component is keyed to this ID
			{
				return constraintsEntry.getKey();	//return the component keyed to the constraints
			}
		}
		return null;	//indicate that we couldn't find a component attached to the given ID
	}
	
	/**Metadata about individual component layout.
	These constraints specify an identifier to which a component should be bound.
	@author Garret Wilson
	*/
	public static class Constraints extends AbstractFlowLayout.Constraints
	{
		/**The reference ID to which the component should be bound.*/
		private final String id;

			/**@return The reference ID to which the component should be bound.*/
			public String getID() {return id;}

		/**Creates constraints with a reference ID to which a component should be bound.
		@param id The ID to which the component should be bound.
		@exception NullPointerException if the given ID is <code>null</code>.
		*/
		public Constraints(final String id)
		{
			this.id=checkNull(id, "ID cannot be null");
		}
	}

}
