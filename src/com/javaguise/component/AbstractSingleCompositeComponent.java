package com.javaguise.component;

import java.util.Iterator;

import com.garretwilson.util.*;
import com.javaguise.GuiseSession;
import com.javaguise.model.Model;

/**An abstract implementation of a composite component that can contain a single component.
@author Garret Wilson
*/
public abstract class AbstractSingleCompositeComponent<C extends CompositeComponent<C>> extends AbstractCompositeComponent<C>	//TODO del class if we don't need
{


	/**The child component, or <code>null</code> if this component does not contain a child component.*/
	private Component<?> component=null;

		/**@return The child component, or <code>null</code> if this component does not contain a child component.*/
		protected Component<?> getComponent() {return component;}
	
		/**Sets the child component.
		This is a bound property.
		@param newComponent The child component, or <code>null</code> if this component does not contain a child component.
//TODO fix if needed		@see #CHECK_TYPE_PROPERTY 
		*/
		public void setComponent(final Component<?> newComponent)
		{
			if(component!=newComponent)	//if the value is really changing
			{
				final Component<?> oldComponent=component;	//get the old value
				component=newComponent;	//actually change the value
				if(oldComponent!=null)	//if there was an old component
				{
					oldComponent.setParent(null);	//tell the old component it no longer has a parent
				}
				if(newComponent!=null)	//if there is a new component
				{
					newComponent.setParent(this);	//tell the component who its parent is					
				}
//TODO fix				firePropertyChange(CHECK_TYPE_PROPERTY, oldComponent, newComponent);	//indicate that the value changed
			}			
		}
	
	/**@return Whether this component has children.*/
	public boolean hasChildren() {return component!=null;}

	/**@return An iterator to child components.*/
	public Iterator<Component<?>> iterator() {return component!=null ? new ObjectIterator<Component<?>>(component) : new EmptyIterator<Component<?>>();}

	/**Retrieves the child component with the given ID.
	@param id The ID of the component to return.
	@return The child component with the given ID, or <code>null</code> if there is no child component with the given ID. 
	*/
	public Component<?> getComponent(final String id)
	{
		return component!=null && component.getID().equals(id) ? component : null;	//return the component if it has the given ID
	}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalStateException if no controller is registered for this component type.
	@exception IllegalStateException if no view is registered for this component type.
	*/
	public AbstractSingleCompositeComponent(final GuiseSession session, final String id, final Model model)
	{
		super(session, id, model);	//construct the parent class
	}

}
