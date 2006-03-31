package com.guiseframework.component;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.guiseframework.GuiseSession;

/**An abstract implementation of a composite component that can contain multiple components.
Every child component must be added or removed using {@link #addComponent(Component)} and {@link #removeComponent(Component)}, although other actions may take place.
The component's validity is updated whenever a child comonent is added or removed from the component.
@author Garret Wilson
*/
public abstract class AbstractMultipleCompositeComponent<C extends CompositeComponent<C>> extends AbstractCompositeComponent<C>
{

	/**The map of components keyed to IDs.*/
	private final Map<String, Component<?>> idComponentMap=new ConcurrentHashMap<String, Component<?>>();	//TODO perhaps remove; the speed may not be sufficient to outweigh the overhead; this is only a single-level search, anyway

	/**@return Whether this component has children.*/
	public boolean hasChildren() {return !idComponentMap.isEmpty();}

	/**@return The number of child components in this composite component.*/
	public int size() {return idComponentMap.size();}
	
	/**@return An iterator to child components.*/
	public Iterator<Component<?>> iterator() {return idComponentMap.values().iterator();}

	/**Adds a child component.
	This version adds the component to the component map.
	Any class that overrides this method must call this version.
	@param component The component to add to this component.
	*/
	protected void addComponent(final Component<?> component)
	{
		super.addComponent(component);	//initialize the child component as needed
		idComponentMap.put(component.getID(), component);	//add this component to the map
		component.setParent(this);	//tell the component who its parent is
		updateValid();	//update the valid status
	}

	/**Removes a child component.
	This version removes the component from the component map.
	Any class that overrides this method must call this version.
	@param component The component to remove from this component.
	*/
	protected void removeComponent(final Component<?> component)
	{
		super.removeComponent(component);	//uninitialize the child component as needed
		idComponentMap.remove(component.getID());	//remove this component from the map
		component.setParent(null);	//tell the component it no longer has a parent
		updateValid();	//update the valid status
	}

	/**Retrieves the child component with the given ID.
	@param id The ID of the component to return.
	@return The child component with the given ID, or <code>null</code> if there is no child component with the given ID. 
	*/
	public Component<?> getComponent(final String id)	//TODO perhaps remove; the speed may not be sufficient to outweigh the overhead; this is only a single-level search, anyway
	{
		return idComponentMap.get(id);	//return the component with the given ID
	}

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalStateException if no controller is registered for this component type.
	@exception IllegalStateException if no view is registered for this component type.
	*/
	public AbstractMultipleCompositeComponent(final GuiseSession session, final String id)
	{
		super(session, id);	//construct the parent class
	}

}
