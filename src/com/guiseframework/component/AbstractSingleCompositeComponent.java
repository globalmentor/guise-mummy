package com.guiseframework.component;

import java.util.ArrayList;
import java.util.List;

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
					super.removeComponent(component);	//uninitialize the old component as needed
					oldComponent.setParent(null);	//tell the old component it no longer has a parent
				}
				if(newComponent!=null)	//if there is a new component
				{
					super.addComponent(component);	//initialize the new component as needed
					newComponent.setParent(this);	//tell the component who its parent is					
				}
//TODO fix				firePropertyChange(CHECK_TYPE_PROPERTY, oldComponent, newComponent);	//indicate that the value changed
			}			
		}
	
	/**@return Whether this component has children.*/
	public boolean hasChildren() {return component!=null;}

	/**@return An iterable to child components.*/
	public Iterable<Component<?>> getChldren()	//TODO make this more efficient
	{
		final List<Component<?>> components=new ArrayList<Component<?>>();	//create a list of components
		if(component!=null)	//if we have a component
		{
			components.add(component);	//add this component to the list
		}
		return components;	//return the components
//TODO del		return component!=null ? new ObjectIterator<Component<?>>(component) : new EmptyIterator<Component<?>>();
	}

	/**Retrieves the child component with the given ID.
	@param id The ID of the component to return.
	@return The child component with the given ID, or <code>null</code> if there is no child component with the given ID. 
	*/
	public Component<?> getComponent(final String id)
	{
		return component!=null && component.getID().equals(id) ? component : null;	//return the component if it has the given ID
	}

}
