package com.guiseframework.component;

import java.util.*;

import static com.garretwilson.util.CollectionUtilities.*;

import com.garretwilson.util.ReverseIterator;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.ContainerEvent;
import com.guiseframework.event.ContainerListener;
import com.guiseframework.event.PostponedContainerEvent;

import static com.garretwilson.lang.ObjectUtilities.*;

/**Abstract implementation of a container component.
Iterating over child components is thread safe.
@author Garret Wilson
*/
public abstract class AbstractContainer<C extends Container<C>> extends AbstractListCompositeComponent<C> implements Container<C>
{

	/**@return An iterator to contained components in reverse order.*/
	public Iterator<Component<?>> reverseIterator()
	{
		final List<Component<?>> componentList=getComponentList();	//get the list of components
		return new ReverseIterator<Component<?>>(componentList.listIterator(componentList.size()));
	}

	/**@return The number of child components in this container.*/
	public int size() {return getComponentList().size();}

	/**@return Whether this container contains no child components.*/
	public boolean isEmpty() {return getComponentList().isEmpty();}

	/**Determines whether this container contains the given component.
	@param component The component to check.
	@return <code>true</code> if this container contains the given component.
	*/
	public boolean contains(final Object component) {return getComponentList().contains(component);}

	/**Returns the index in the container of the first occurrence of the specified component.
	@param component The component the index of which should be returned.
	@return The index in this container of the first occurrence of the specified component, or -1 if this container does not contain the given component.
	*/
	public int indexOf(final Object component) {return getComponentList().indexOf(component);}

	/**Returns the index in this container of the last occurrence of the specified compoent.
	@param component The component the last index of which should be returned.
	@return The index in this container of the last occurrence of the specified component, or -1 if this container does not contain the given component.
	*/
	public int lastIndexOf(final Object component) {return getComponentList().lastIndexOf(component);}

  /**Returns the component at the specified index in the container.
  @param index The index of the component to return.
	@return The component at the specified position in this container.
	@exception IndexOutOfBoundsException if the index is out of range.
	*/
	public Component<?> get(final int index) {return getComponentList().get(index);}

	/**Adds a component to the container with default constraints.
	@param component The component to add.
	@return <code>true</code> if this container changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception IllegalStateException if the installed layout does not support default constraints.
	*/
	public boolean add(final Component<?> component)
	{
//TODO del when works		return add(component, null);	//add the component, indicating default constraints should be used
		if(component.getParent()!=null)	//if this component has already been added to container
		{
			throw new IllegalArgumentException("Component "+component+" is already a member of a component, "+component.getParent()+".");
		}
		addComponent(component);	//add the component to the list
		getLayout().addComponent(component);	//add the component to the layout
		fireContainerModified(indexOf(component), component, null);	//indicate the component was added at the index
		return true;	//TODO improve to determine if the container was actually modified
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
	public boolean add(final Component<?> component, final Constraints constraints)
	{
		component.setConstraints(constraints);	//set the constraints in the component
		return add(component);	//add the component, now that its constraints have been set
	}

	/**Adds a component to the container along with constraints.
	@param component The component to add.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@return <code>true</code> if this container changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	*/
/*TODO del
	public boolean add(final Component<?> component, final Layout.Constraints constraints)
	{
		return add(component, getLayout(), constraints);	//add the component with the given constraints
	}
*/

	/**Adds a component to the container.
	@param component The component to add.
	@param layout The currently installed layout.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@return <code>true</code> if this container changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	*/
/*TODO del
	protected <T extends Layout.Constraints> boolean add(final Component<?> component, final Layout<T> layout, final Object constraints)	//TODO check ClassCastException---it will not be thrown here
	{
		if(component.getParent()!=null)	//if this component has already been added to container
		{
			throw new IllegalArgumentException("Component "+component+" is already a member of a component, "+component.getParent()+".");
		}
		final T layoutConstraints=constraints!=null ? (T)constraints : layout.createDefaultConstraints();	//create default constraints if we need to TODO use the layout constraints class to cast the value
		addComponent(component);	//add the component to the list
//TODO del; moved to AbstractCompositeComponent		component.setParent(this);	//tell the component who its parent is
		layout.setConstraints(component, layoutConstraints);	//tell the layout the constraints
		fireContainerModified(indexOf(component), component, null);	//indicate the component was added at the index
		return true;	//TODO improve to determine if the container was actually modified
	}
*/

	/**Removes a component from the container.
	@param componentObject The component to remove.
	@return <code>true</code> if this collection changed as a result of the operation.
	@throws ClassCastException if given element is not a component.
	@exception IllegalArgumentException if the component is not a member of the container.
	*/
	public boolean remove(final Object componentObject)
	{
		final Component<?> component=(Component<?>)componentObject;	//cast the object to a component
		if(component.getParent()!=this)	//if this component is not a member of this container
		{
			throw new IllegalArgumentException("Component "+component+" is not member of container "+this+".");
		}
		final int index=indexOf(component);	//get the index of the component TODO do we want to see if the component is actually in the container?
		getLayout().removeComponent(component);	//remove the component from the layout
//TODO del when works		getLayout().removeConstraints(component);	//remove the constraints for this component
		removeComponent(component);	//remove the component from the list
//TODO del; moved to AbstractdCompositeComponent		component.setParent(null);	//tell the component it no longer has a parent
		fireContainerModified(index, null, component);	//indicate the component was removed from the index
		return true;	//TODO improve to determine if the container was actually modified
	}

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

	/**The layout definition for the container.*/
	private Layout<?> layout;

		/**@return The layout definition for the container.*/
		public Layout<?> getLayout() {return layout;}

		/**Sets the layout definition for the container.
//TODO fix		The layout definition can only be changed if the container currently has no child components.
		This is a bound property.
		@param newLayout The new layout definition for the container.
		@exception NullPointerException if the given layout is <code>null</code>.
//TODO fix		@exception IllegalStateException if a new layout is requested while this container has one or more children.
		@see Container#LAYOUT_PROPERTY 
		*/
		public <T extends Constraints> void setLayout(final Layout<T> newLayout)
		{
			if(layout!=checkNull(newLayout, "Layout cannot be null."))	//if the value is really changing
			{
/*TODO testing
				if(size()!=0)	//if this container has children
				{
					throw new IllegalArgumentException("Layout may not change if container has children.");
				}
*/
				final Layout<?> oldLayout=layout;	//get the old value
				oldLayout.setContainer(null);	//tell the old layout it is no longer installed
				layout=newLayout;	//actually change the value
				layout.setContainer(this);	//tell the new layout which container owns it
				for(final Component<?> childComponent:this)	//for each child component
				{
//TODO del when works---why are we setting the constraints to null? maybe they were already compatible					childComponent.setConstraints(null);	//TODO improve
					newLayout.getConstraints(childComponent);	//make sure the constraints of all components are compatible with the layout TODO do we even need to do this? why not wait until later? but this may be OK---perhaps we can assume that if components are installed before the layout, they will be used with this layout and not another
//TODO del when works					newLayout.setConstraints(childComponent, newLayout.createDefaultConstraints());	//
				}
				firePropertyChange(LAYOUT_PROPERTY, oldLayout, newLayout);	//indicate that the value changed
			}			
		}

	/**Returns a list of children.
	This method along with {@link #setChildren()} provides an <code>children</code> property for alternate children access.
	@return A list of container children in order.
	@see #iterator()
	*/
	@SuppressWarnings("unchecked")	//a cast is needed because a generic wildcard list cannot be created
	public List<Component<?>> getChildren()
	{
		final List<Component<?>> children=(List<Component<?>>)new ArrayList<Component>(size());	//create a list of child components---we already know how many
		addAll(children, iterator());	//add all our children to the list
		return children;	//return the list of children
	}

	/**Sets the children in this container.
	This method along with {@link #getChildren()} provides an <code>children</code> property for alternate children access.
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

	/**Session, ID, and layout constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session, and/or layout is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractContainer(final GuiseSession session, final String id, final Layout<?> layout)
	{
		super(session, id);	//construct the parent class
		this.layout=checkNull(layout, "Layout cannot be null.");	//save the layout
		layout.setContainer(this);	//tell the layout which container owns it
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
		final ContainerEvent containerEvent=new ContainerEvent(getSession(), getThis(), index, addedComponent, removedComponent);	//create a new event
		getSession().queueEvent(new PostponedContainerEvent(getEventListenerManager(), containerEvent));	//tell the Guise session to queue the event
	}

}
