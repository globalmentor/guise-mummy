package com.garretwilson.guise.component;

import java.util.*;

import com.garretwilson.util.EmptyIterator;

/**Abstract implementation of a container component.
This implementation uses a lazily-created list of child components, making empty containers lightweight.
@author Garret Wilson
*/
public class AbstractContainer extends AbstractComponent implements Container
{

	/**The lazily-created list of child components.*/ 
	private List<Component> componentList=null;

	/**@return The lazily-created list of child components.*/ 
	private List<Component> getComponentList()
	{
		if(componentList==null)	//if there is no component list
		{
			componentList=new ArrayList<Component>();	//create a new component list
		}
		return componentList;	//return the list of components
	}

	/**@return An iterator to contained components.*/
	public Iterator<Component> iterator()
	{
		return componentList!=null ? componentList.iterator() : new EmptyIterator<Component>();	//return an iterator to the components, returning an empty iterator if the component list has not been created
	}

	/**Adds a component to the container
	@param component The component to add.
	*/
	public void add(final Component component)
	{
		getComponentList().add(component);	//add the component to the list
	}

	/**ID constructor.
	@param id The component identifier.
	@exception NullPointerException if the given identifier is <code>null</code>.
	*/
	public AbstractContainer(final String id)
	{
		super(id);	//construct the parent class
	}

}
