package com.garretwilson.guise.component;

import java.util.*;

import com.garretwilson.guise.component.layout.Layout;
import static com.garretwilson.lang.ObjectUtilities.*;
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
	@exception IllegalArgumentException if the component is already a member of a container.
	*/
	public void add(final Component component)
	{
		if(component.getParent()!=null)	//if this component has already been added to container
		{
			throw new IllegalArgumentException("Component "+component+" is already a member of a container, "+component.getParent()+".");
		}
		getComponentList().add(component);	//add the component to the list
		component.setParent(this);	//tell the component who its parent is
	}

	/**Determines whether this container contains the given component.
	@param component The component to check.
	@return <code>true</code> if this container contains the given component.
	*/
	public boolean contains(final Component component)
	{
		return componentList!=null ? componentList.contains(component) : false;	//if we have a component list, ask it whether it contains this component
	}

	/**The layout definition for the container.*/
	private final Layout layout;

		/**@return The layout definition for the container.*/
		public Layout getLayout() {return layout;}

	/**ID constructor.
	@param id The component identifier.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given identifier or layout is <code>null</code>.
	*/
	public AbstractContainer(final String id, final Layout layout)
	{
		super(id);	//construct the parent class
		this.layout=checkNull(layout, "Layout cannot be null.");	//save the layout
	}

}
