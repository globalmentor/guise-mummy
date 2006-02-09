package com.javaguise.component;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.javaguise.GuiseSession;
import com.javaguise.model.Model;

/**An abstract implementation of a composite component that can contain multiple components.
Every child component must be added or removed using {@link #addComponent(Component)} and {@link #removeComponent(Component)}, although other actions may take place.
@author Garret Wilson
*/
public abstract class AbstractMultipleCompositeComponent<C extends CompositeComponent<C>> extends AbstractCompositeComponent<C>
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
		component.setParent(this);	//tell the component who its parent is
	}

	/**Removes a child component.
	This version removes the component from the component map.
	Any class that overrides this method must call this version.
	@param component The component to remove from this component.
	*/
	protected void removeComponent(final Component<?> component)
	{
		idComponentMap.remove(component.getID());	//remove this component from the map
		component.setParent(null);	//tell the component it no longer has a parent
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
	public AbstractMultipleCompositeComponent(final GuiseSession session, final String id, final Model model)
	{
		super(session, id, model);	//construct the parent class
	}

}
