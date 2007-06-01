package com.guiseframework.component;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import com.guiseframework.model.LabelModel;

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

	/**@return The number of child components in this container.*/
	protected int size() {return getComponentList().size();}

	/**@return Whether this container contains no child components.*/
	protected boolean isEmpty() {return getComponentList().isEmpty();}

	/**Determines whether this container contains the given component.
	@param component The component to check.
	@return <code>true</code> if this container contains the given component.
	*/
	protected boolean contains(final Object component) {return getComponentList().contains(component);}

	/**Returns the index in the container of the first occurrence of the specified component.
	@param component The component the index of which should be returned.
	@return The index in this container of the first occurrence of the specified component, or -1 if this container does not contain the given component.
	*/
	protected int indexOf(final Object component) {return getComponentList().indexOf(component);}

	/**Returns the index in this container of the last occurrence of the specified compoent.
	@param component The component the last index of which should be returned.
	@return The index in this container of the last occurrence of the specified component, or -1 if this container does not contain the given component.
	*/
	protected int lastIndexOf(final Object component) {return getComponentList().lastIndexOf(component);}

  /**Returns the component at the specified index in the container.
  @param index The index of the component to return.
	@return The component at the specified position in this container.
	@exception IndexOutOfBoundsException if the index is out of range.
	*/
	protected Component<?> get(final int index) {return getComponentList().get(index);}

	/**Adds a child component.
	This version adds the component to the component list.
	Any class that overrides this method must call this version.
	@param component The component to add to this component.
	@return <code>true</code> if the child components changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	*/
	protected boolean addComponent(final Component<?> component)
	{
		if(component.getParent()!=null)	//if this component has already been added to container; do this check before we add the component to the list, because the super class' version of this only comes after the component is added to the list
		{
			throw new IllegalArgumentException("Component "+component+" is already a member of a composite component, "+component.getParent()+".");
		}
		if(componentList.add(component))	//add the component to the list; if the component list changed
		{
			super.addComponent(component);	//do the default adding
			return true;	//indicate that the child components changed
		}
		else	//if the component list did not change
		{
			return false;	//indicate that the chidl components did not change
		}
	}

	/**Removes a child component.
	This version removes the component from the component list.
	Any class that overrides this method must call this version.
	@param component The component to remove from this component.
	@return <code>true</code> if the child components changed as a result of the operation.
	*/
	protected boolean removeComponent(final Component<?> component)
	{
		if(componentList.remove(component))	//remove the component from the list
		{
			super.removeComponent(component);	//do the default removal
			return true;	//indicate that the child components changed
		}
		else	//if the component list did not change
		{
			return false;	//indicate that the chidl components did not change
		}
	}

	/**@return An iterable to contained components.*/
	public Iterable<Component<?>> getChildren() {return componentList;}

	/**@return Whether this component has children. This implementation delegates to the component list.*/
	public boolean hasChildren() {return !componentList.isEmpty();}

	/**Label model constructor.
	@param labelModel The component label model.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public AbstractListCompositeComponent(final LabelModel labelModel)
	{
		super(labelModel);	//construct the parent class
	}

}
