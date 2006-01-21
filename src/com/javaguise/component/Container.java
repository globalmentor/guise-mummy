package com.javaguise.component;

import static com.garretwilson.lang.ClassUtilities.*;

import java.util.Iterator;

import com.javaguise.component.layout.Layout;
import com.javaguise.event.ContainerListener;

/**Component that allows for addition and removal of child components.
@author Garret Wilson
*/
public interface Container<C extends Container<C>> extends CompositeComponent<C>
{

	/**The bound property of the layout.*/
	public final static String LAYOUT_PROPERTY=getPropertyName(Container.class, "layout");

	/**@return An iterator to contained components in reverse order.*/
	public Iterator<Component<?>> reverseIterator();

	/**Adds a component to the container with default constraints.
	@param component The component to add.
	@exception IllegalArgumentException if the component already has a parent.
	@exception IllegalStateException the installed layout does not support default constraints.
	*/
	public void add(final Component<?> component);

	/**Adds a component to the container along with constraints.
	@param component The component to add.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@exception IllegalArgumentException if the component already has a parent.
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	*/
	public void add(final Component<?> component, final Layout.Constraints constraints);

	/**Removes a component from the container.
	@param component The component to remove.
	@exception IllegalArgumentException if the component is not a member of the container.
	*/
	public void remove(final Component<?> component);

	/**@return The number of child components in this container.*/
	public int size();

	/**Determines whether this container contains the given component.
	@param component The component to check.
	@return <code>true</code> if this container contains the given component.
	*/
	public boolean contains(final Object component);

	/**Returns the index in the container of the first occurrence of the specified component.
	@param component The component the index of which should be returned.
	@return The index in this container of the first occurrence of the specified component, or -1 if this container does not contain the given component.
	*/
	public int indexOf(final Component<?> component);

  /**Returns the component at the specified index in the container.
  @param index The index of component to return.
	@return The component at the specified position in this container.
	@exception IndexOutOfBoundsException if the index is out of range.
	*/
	public Component<?> get(int index);

	/**Removes all of the components from this container.*/
	public void clear();

	/**@return The layout definition for the container.*/
	public Layout<?> getLayout();

	/**Sets the layout definition for the container.
	The layout definition can only be changed if the container currently has no child components.
	This is a bound property.
	@param newLayout The new layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	@exception IllegalStateException if a new layout is requested while this container has one or more children.
	@see Container#LAYOUT_PROPERTY 
	*/
	public <T extends Layout.Constraints> void setLayout(final Layout<T> newLayout);

	/**Adds a container listener.
	@param containerListener The container listener to add.
	*/
	public void addContainerListener(final ContainerListener containerListener);

	/**Removes a container listener.
	@param containerListener The container listener to remove.
	*/
	public void removeContainerListener(final ContainerListener containerListener);

}
