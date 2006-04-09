package com.guiseframework.component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**Abstract implementation of a composite component that keeps track of its child components in sequence.
Iterating over child components is thread safe.
@author Garret Wilson
*/
public abstract class AbstractListCompositeComponent<C extends CompositeComponent<C>> extends AbstractMultipleCompositeComponent<C>
{

	/**The list of child components.*/ 
	private final List<Component<?>> componentList=new CopyOnWriteArrayList<Component<?>>();	//create a new component list, using a thread-safe array that takes into consideration that adding or removing children usually takes place up-front, and most later access will be only reads

		/**@return The list of child components.*/ 
		protected List<Component<?>> getComponentList() {return componentList;}

	/**Adds a child component.
	This version adds the component to the component list.
	Any class that overrides this method must call this version.
	@param component The component to add to this component.
	*/
	protected void addComponent(final Component<?> component)
	{
		componentList.add(component);	//add the component to the list
		super.addComponent(component);	//do the default adding
	}

	/**Removes a child component.
	This version removes the component from the component list.
	Any class that overrides this method must call this version.
	@param component The component to remove from this component.
	*/
	protected void removeComponent(final Component<?> component)
	{
		componentList.remove(component);	//remove the component from the list
		super.removeComponent(component);	//do the default removal
	}

	/**@return An iterable to contained components.*/
	public Iterable<Component<?>> getChildren() {return componentList;}

	/**@return Whether this component has children. This implementation delegates to the component list.*/
	public boolean hasChildren() {return !componentList.isEmpty();}

}
