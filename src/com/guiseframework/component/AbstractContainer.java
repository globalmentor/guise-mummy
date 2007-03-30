package com.guiseframework.component;

import java.util.*;

import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;

/**Abstract implementation of a container component.
Iterating over child components is thread safe.
@author Garret Wilson
*/
public abstract class AbstractContainer<C extends Container<C>> extends AbstractLayoutComponent<C> implements Container<C>
{

	/**@return The number of child components in this container.*/
	public int size() {return super.size();}

	/**@return Whether this container contains no child components.*/
	public boolean isEmpty() {return super.isEmpty();}

	/**Determines whether this container contains the given component.
	@param component The component to check.
	@return <code>true</code> if this container contains the given component.
	*/
	public boolean contains(final Object component) {return super.contains(component);}

	/**Returns the index in the container of the first occurrence of the specified component.
	@param component The component the index of which should be returned.
	@return The index in this container of the first occurrence of the specified component, or -1 if this container does not contain the given component.
	*/
	public int indexOf(final Object component) {return super.indexOf(component);}

	/**Returns the index in this container of the last occurrence of the specified compoent.
	@param component The component the last index of which should be returned.
	@return The index in this container of the last occurrence of the specified component, or -1 if this container does not contain the given component.
	*/
	public int lastIndexOf(final Object component) {return super.lastIndexOf(component);}

  /**Returns the component at the specified index in the container.
  @param index The index of the component to return.
	@return The component at the specified position in this container.
	@exception IndexOutOfBoundsException if the index is out of range.
	*/
	public Component<?> get(final int index) {return super.get(index);}

	/**Adds a component to the container with default constraints.
	@param component The component to add.
	@return <code>true</code> if this container changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception IllegalStateException if the installed layout does not support default constraints.
	*/
	public boolean add(final Component<?> component)
	{
		final boolean result=addComponent(component);	//add the component normally
		fireContainerModified(indexOf(component), component, null);	//indicate the component was added at the index TODO promote
		return result;	//return the result
	}

	/**Adds a component to the container along with constraints.
	This is a convenience method that first set the constraints of the component. 
	@param component The component to add.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@return <code>true</code> if this container changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	*/
/*TODO del if needed
	public boolean add(final Component<?> component, final Constraints constraints)
	{
		return super.add(component, constraints);	//add the component normally
	}
*/

	/**Adds a component to the container along with constraints.
	This is a convenience method that first set the constraints of the component. 
	@param component The component to add.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@return <code>true</code> if this container changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	*/
	public boolean add(final Component<?> component, final Constraints constraints)
	{
		component.setConstraints(constraints);	//set the constraints in the component
		return add(component);	//add the component, now that its constraints have been set
	}

	/**Removes a component from the container.
	@param componentObject The component to remove.
	@return <code>true</code> if this collection changed as a result of the operation.
	@throws ClassCastException if given element is not a component.
	@exception IllegalArgumentException if the component is not a member of the container.
	*/
	public boolean remove(final Object componentObject)
	{
		final Component<?> component=(Component<?>)componentObject;	//cast the object to a component
		final int index=indexOf(component);	//get the index of the component
		final boolean result=removeComponent(component);	//remove the component normally
		assert index>=0 : "Component successfully removed from container, yet previous index is negative.";
		fireContainerModified(index, null, component);	//indicate the component was removed from the index TODO promote
		return result;	//return the result
	}

	/**Removes the child component at the specified position in this container.
	@param index The index of the component to removed.
	@return The value previously at the specified position.
	@exception IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
	*/
/*TODO del
	public Component<?> remove(final int index)
	{
		return super.remove(index);	//remove the component normally
	}
*/

	/**Removes the child component at the specified position in this container.
	@param index The index of the component to removed.
	@return The value previously at the specified position.
	@exception IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
	*/
	public Component<?> remove(final int index)
	{
		final Component<?> component=get(index);	//get the component at this index
		remove(component);	//remove the component
		return component;	//return the component that was removed
	}

	/**Removes all of the components from this container.*/
	public void clear()
	{
		for(final Component<?> component:this)	//for each component in the container
		{
			remove(component);	//remove this component
		}
	}

	/**@return An iterator to child components.*/
	public Iterator<Component<?>> iterator() {return getComponentList().iterator();}

	/**Returns a list of children.
	This method along with {@link #setChildren()} provides a <code>children</code> property for alternate children access.
	@return A list of container children in order.
	@see #iterator()
	*/
	public List<Component<?>> getChildren()
	{
		return new ArrayList<Component<?>>(getComponentList());	//create and return a copy of the list
	}

	/**Sets the children in this container.
	This method along with {@link #getChildren()} provides a <code>children</code> property for alternate children access.
	@param children The new children for this container in order.
	@see #clear()
	@see #add(Component)
	*/
	public void setChildren(final List<Component<?>> children)
	{
		clear();	//remove all children from the container
		for(final Component<?> child:children)	//for each child
		{
			add(child);	//add this child
		}
	}

	/**Sets the layout definition for the container.
//TODO fix		The layout definition can only be changed if the container currently has no child components.
	This is a bound property.
	@param newLayout The new layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
//TODO fix		@exception IllegalStateException if a new layout is requested while this container has one or more children.
	@see #LAYOUT_PROPERTY 
	*/
	public <T extends Constraints> void setLayout(final Layout<T> newLayout)
	{
		super.setLayout(newLayout);	//delegate to the parent class
	}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public AbstractContainer(final Layout<?> layout)
	{
		super(layout);	//construct the parent class
	}

	/**Adds a container listener.
	@param containerListener The container listener to add.
	*/
	public void addContainerListener(final ContainerListener containerListener)
	{
		getEventListenerManager().add(ContainerListener.class, containerListener);	//add the listener
	}

	/**Removes a container listener.
	@param containerListener The container listener to remove.
	*/
	public void removeContainerListener(final ContainerListener containerListener)
	{
		getEventListenerManager().remove(ContainerListener.class, containerListener);	//remove the listener
	}

	/**Fires an event to all registered container listeners indicating the components in the container changed.
	@param index The index at which a component was added and/or removed, or -1 if the index is unknown.
	@param addedComponent The component that was added to the container, or <code>null</code> if no component was added or it is unknown whether or which components were added.
	@param removedComponent The component that was removed from the container, or <code>null</code> if no component was removed or it is unknown whether or which components were removed.
	@see ContainerListener
	@see ContainerEvent
	*/
	protected void fireContainerModified(final int index, final Component<?> addedComponent, final Component<?> removedComponent)
	{
		final ContainerEvent containerEvent=new ContainerEvent(getThis(), index, addedComponent, removedComponent);	//create a new event
		getSession().queueEvent(new PostponedContainerEvent(getEventListenerManager(), containerEvent));	//tell the Guise session to queue the event
	}

}
