package com.javaguise.component;

import java.util.*;

import static com.garretwilson.util.CollectionUtilities.*;
import com.javaguise.GuiseSession;
import com.javaguise.component.layout.*;
import com.javaguise.event.ContainerEvent;
import com.javaguise.event.ContainerListener;
import com.javaguise.event.PostponedContainerEvent;
import com.javaguise.model.Model;

import static com.garretwilson.lang.ObjectUtilities.*;

/**Abstract implementation of a container component.
Iterating over child components is thread safe.
@author Garret Wilson
*/
public abstract class AbstractContainer<C extends Container<C>> extends AbstractListCompositeComponent<C> implements Container<C>
{

	/**Adds a component to the container with default constraints.
	@param component The component to add.
	@exception IllegalArgumentException if the component already has a parent.
	@exception IllegalStateException the installed layout does not support default constraints.
	*/
	public void add(final Component<?> component)
	{
		add(component, null);	//add the component, indicating default constraints should be used
	}

	/**Adds a component to the container along with constraints.
	@param component The component to add.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@exception IllegalArgumentException if the component already has a parent.
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	*/
	public void add(final Component<?> component, final Layout.Constraints constraints)
	{
		add(component, getLayout(), constraints);	//add the component with the given constraints
	}

	/**Adds a component to the container.
	@param component The component to add.
	@param layout The currently installed layout.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@exception IllegalArgumentException if the component already has a parent.
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	*/
	protected <T extends Layout.Constraints> void add(final Component<?> component, final Layout<T> layout, final Object constraints)	//TODO check ClassCastException---it will not be thrown here
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
	}

	/**Removes a component from the container.
	@param component The component to remove.
	@exception IllegalArgumentException if the component is not a member of the container.
	*/
	public void remove(final Component<?> component)
	{
		if(component.getParent()!=this)	//if this component is not a member of this container
		{
			throw new IllegalArgumentException("Component "+component+" is not member of container "+this+".");
		}
		final int index=indexOf(component);	//get the index of the component TODO do we want to see if the component is actually in the container?
		getLayout().removeConstraints(component);	//remove the constraints for this component
		removeComponent(component);	//remove the component from the list
//TODO del; moved to AbstractdCompositeComponent		component.setParent(null);	//tell the component it no longer has a parent
		fireContainerModified(index, null, component);	//indicate the component was removed from the index
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
		public <T extends Layout.Constraints> void setLayout(final Layout<T> newLayout)
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
					newLayout.setConstraints(childComponent, newLayout.createDefaultConstraints());	//
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

	/**Session, ID, layout, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@param model The component data model.
	@exception NullPointerException if the given session, layout, and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractContainer(final GuiseSession session, final String id, final Layout<?> layout, final Model model)
	{
		super(session, id, model);	//construct the parent class
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
