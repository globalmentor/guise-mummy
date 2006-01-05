package com.javaguise.component;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.javaguise.GuiseSession;
import com.javaguise.model.Model;

/**An abstract implementation of a composite component.
@author Garret Wilson
*/
public abstract class AbstractCompositeComponent<C extends CompositeComponent<C>> extends AbstractComponent<C> implements CompositeComponent<C>
{

	/**The map of components keyed to IDs.*/
	private final Map<String, Component<?>> idComponentMap=new ConcurrentHashMap<String, Component<?>>();

	/**@return Whether this component has children.*/
	public boolean hasChildren() {return !idComponentMap.isEmpty();}

	/**@return An iterator to child components.*/
	public Iterator<Component<?>> iterator() {return idComponentMap.values().iterator();}

	/**Adds a child component.
	This version adds the component to the component map.
	Any class that overrides this method must call this version.
	@param component The component to add to this component.
	*/
	protected void addComponent(final Component<?> component)
	{
		idComponentMap.put(component.getID(), component);	//add this component to the map
	}

	/**Removes a child component.
	This version removes the component from the component map.
	Any class that overrides this method must call this version.
	@param component The component to remove from this component.
	*/
	protected void removeComponent(final Component<?> component)
	{
		idComponentMap.remove(component.getID());	//remove this component from the map
	}

	/**Retrieves the child component with the given ID.
	@param id The ID of the component to return.
	@return The child component with the given ID, or <code>null</code> if there is no child component with the given ID. 
	*/
	public Component<?> getComponent(final String id)
	{
		return idComponentMap.get(id);	//return the component with the given ID
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
	public AbstractCompositeComponent(final GuiseSession session, final String id, final Model model)
	{
		super(session, id, model);	//construct the parent class
	}

	/**Determines whether the models of this component and all of its child components are valid.
	This version returns <code>true</code> if all its child components are valid.
	@return Whether the models of this component and all of its child components are valid.
	*/
	public boolean isValid()	//TODO reconcile this design with the new control isValid() semantics
	{
		if(!super.isValid())	//if the component doesn't pass the default checks
		{
			return false;	//this component isn't valid
		}
		for(final Component<?> childComponent:this)	//for each child component
		{
			if(!childComponent.isValid())	//if this child component isn't valid
			{
				return false;	//indicate that this component is consequently not valid
			}
		}
		return true;	//indicate that all child components are valid
	}

	/**Validates the model of this component and all child components.
	The component will be updated with error information.
	This version validates the this component and all child components.
	@exception ComponentExceptions if there was one or more validation error.
	*/
	public void validate() throws ComponentExceptions
	{
		ComponentExceptions componentExceptions=null;	//we'll store any component exceptions here and keep going
		try
		{
			super.validate();	//validate the component normally
		}
		catch(final ComponentExceptions superComponentExceptions)	//if the super version returns an error
		{
			if(componentExceptions==null)	//if this is our first component exception
			{
				componentExceptions=superComponentExceptions;	//store the exception and continue processing events with other child components
			}
			else	//if we already have component exceptions
			{
				componentExceptions.addAll(superComponentExceptions);	//add all the exceptions to the exception we already have
			}
		}
		for(final Component<?> childComponent:this)	//for each child component
		{
			try
			{
				childComponent.validate();	//validate the child
			}
			catch(final ComponentExceptions childComponentExceptions)	//if a child returns an error
			{
				if(componentExceptions==null)	//if this is our first component exception
				{
					componentExceptions=childComponentExceptions;	//store the exception and continue processing events with other child components
				}
				else	//if we already have component exceptions
				{
					componentExceptions.addAll(childComponentExceptions);	//add all the child component exceptions to the exception we already have
				}
			}
		}
		if(componentExceptions!=null)	//if we encountered one or more component exceptions
		{
			throw componentExceptions;	//throw the exception, which may contain multiple exceptions
		}
	}

	/**Retrieves a child component with the given ID.
	@return The component with the given ID, or <code>null</code> if this component and all descendant components do not have the given ID. 
	*/
/*TODO fix or del; don't check the ID of this component, now that we have AbstractComponent.getComponentByID()
	public Component<?> getDescendantComponentByID(final String id)
	{
		if(getID().equals(id))	//if this component has the correct ID
		{
			return this;	//return this component
		}
		else	//if this component doesn't have the correct ID
		{
			for(final Component<?> childComponent:this)	//for each child component
			{
				if(childComponent instanceof CompositeComponent)	//if this child component is a composite component
				{
					final Component<?> matchingComponent=((CompositeComponent<?>)childComponent).getComponentByID(id);	//ask this child component for a component with a matching ID
					if(matchingComponent!=null)	//if we found a matching component
					{
						return matchingComponent;	//return the matching component
					}
				}
				else if(childComponent.getID().equals(id))	//if the child component isn't a composite component but it has the correct ID
				{
					return childComponent;	//return the child component
				}
			}
		}
		return null;
	}
*/

	/**Retrieves a child component with the given ID.
	@param component The component the children of which should be checked for the given ID.
	@param deep <code>true</code> if all descendants of the component should be checked.
	@return The component with the given ID, or <code>null</code> if no component with the given ID could be found. 
	*/
/*TODO del if not needed
	public static Component<?> getChildComponentByID(final CompositeComponent<?> component, final String id, final boolean deep)
	{
		for(final Component<?> childComponent:component)	//for each child component
		{
			if(childComponent.getID().equals(id))	//if this component has the correct ID
			{
				return childComponent;	//return the child component
			}
			else if(deep)	//if we should do deep searching
			{
				final Component<?> matchingComponent=getComponentByID(childComponent, id);	//see if we can find a component in this tree
				if(matchingComponent!=null)	//if we found a matching component
				{
					return matchingComponent;	//return the matching component
				}
			}
		}
		return null;
	}
*/
}
