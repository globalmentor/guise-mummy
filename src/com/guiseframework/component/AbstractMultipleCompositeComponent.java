package com.guiseframework.component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.guiseframework.model.LabelModel;

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

	/**@return An iterable to child components.*/
	public Iterable<Component<?>> getChildren() {return idComponentMap.values();}

	/**Adds a child component.
	This version adds the component to the component map.
	Any class that overrides this method must call this version.
	@param component The component to add to this component.
	@return <code>true</code> if the child components changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	*/
	protected boolean addComponent(final Component<?> component)
	{
		if(idComponentMap.put(component.getID(), component)!=component)	//add this component to the map; if that resulted in a map change
		{
			super.addComponent(component);	//initialize the child component as needed
			component.setParent(this);	//tell the component who its parent is
			updateValid();	//update the valid status
			return true;	//indicate that the components changed
		}
		else	//if the component was already in the map
		{
			return false;	//indicate that no components changed
		}
	}

	/**Removes a child component.
	This version removes the component from the component map.
	Any class that overrides this method must call this version.
	@param component The component to remove from this component.
	@return <code>true</code> if the child components changed as a result of the operation.
	@exception IllegalArgumentException if the component is not a member of this composite component.
	*/
	protected boolean removeComponent(final Component<?> component)
	{
		final Component<?> removedComponent=idComponentMap.remove(component.getID());	//remove this component from the map
		if(removedComponent!=null)	//if a component was removed
		{
			if(removedComponent!=component)	//if there was another component with the same ID
			{
				throw new IllegalStateException("Another component "+removedComponent+" stored with same ID "+component.getID()+" as component "+component);
			}
			super.removeComponent(component);	//uninitialize the child component as needed
			component.setParent(null);	//tell the component it no longer has a parent
			updateValid();	//update the valid status
			return true;	//indicate that the child components changed
		}
		else	//if no component was removed
		{
			return false;	//there was no change
		}
	}

	/**Retrieves the child component with the given ID.
	@param id The ID of the component to return.
	@return The child component with the given ID, or <code>null</code> if there is no child component with the given ID. 
	*/
	public Component<?> getComponent(final String id)	//TODO perhaps remove; the speed may not be sufficient to outweigh the overhead; this is only a single-level search, anyway
	{
		return idComponentMap.get(id);	//return the component with the given ID
	}

	/**Label model constructor.
	@param labelModel The component label model.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public AbstractMultipleCompositeComponent(final LabelModel labelModel)
	{
		super(labelModel);	//construct the parent class
	}
}
