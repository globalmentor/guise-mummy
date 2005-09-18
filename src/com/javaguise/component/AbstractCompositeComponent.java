package com.javaguise.component;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.javaguise.session.GuiseSession;

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

	/**Session constructor.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalStateException if no controller is registered for this component type.
	*/
	public AbstractCompositeComponent(final GuiseSession session)
	{
		this(session, null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalStateException if no controller is registered for this component type.
	*/
	public AbstractCompositeComponent(final GuiseSession session, final String id)
	{
		super(session, id);	//construct the parent class
	}

	/**Determines whether the models of this component and all of its child components are valid.
	This version returns <code>true</code> if all its child components are valid.
	@return Whether the models of this component and all of its child components are valid.
	*/
	public boolean isValid()	//TODO don't we need to check the cached controller components as well?
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
