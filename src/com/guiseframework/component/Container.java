package com.guiseframework.component;

import static com.garretwilson.lang.ClassUtilities.*;

import java.util.Iterator;

import com.guiseframework.component.layout.Layout;
import com.guiseframework.component.layout.Constraints;
import com.guiseframework.event.ContainerListener;

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
	@return <code>true</code> if this container changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception IllegalStateException if the installed layout does not support default constraints.
	*/
	public boolean add(final Component<?> component);

	/**Adds a component to the container along with constraints.
	@param component The component to add.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@return <code>true</code> if this container changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	*/
//TODO del; recomment main add method	public boolean add(final Component<?> component, final Layout.Constraints constraints);

	/**Adds a component to the container along with constraints.
	This is a convenience method that first set the constraints of the component. 
	@param component The component to add.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@return <code>true</code> if this container changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	*/
	public boolean add(final Component<?> component, final Constraints constraints);

	/**Removes a component from the container.
	@param object The component to remove.
	@return <code>true</code> if this collection changed as a result of the operation.
	@throws ClassCastException if given element is not a component.
	@exception IllegalArgumentException if the component is not a member of the container.
	*/
	public boolean remove(final Object component);

	/**Removes the child component at the specified position in this container.
	@param index The index of the component to removed.
	@return The value previously at the specified position.
	@exception IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
	*/
	public Component<?> remove(final int index);

	/**@return Whether this container contains no child components.*/
	public boolean isEmpty();

	/**Determines whether this container contains the given component.
	@param component The component to check.
	@return <code>true</code> if this container contains the given component.
	*/
	public boolean contains(final Object component);

	/**Returns the index in the container of the first occurrence of the specified component.
	@param component The component the index of which should be returned.
	@return The index in this container of the first occurrence of the specified component, or -1 if this container does not contain the given component.
	*/
	public int indexOf(final Object component);

	/**Returns the index in this container of the last occurrence of the specified compoent.
	@param component The component the last index of which should be returned.
	@return The index in this container of the last occurrence of the specified component, or -1 if this container does not contain the given component.
	*/
	public int lastIndexOf(final Object component);

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
	public <T extends Constraints> void setLayout(final Layout<T> newLayout);

	/**Adds a container listener.
	@param containerListener The container listener to add.
	*/
	public void addContainerListener(final ContainerListener containerListener);

	/**Removes a container listener.
	@param containerListener The container listener to remove.
	*/
	public void removeContainerListener(final ContainerListener containerListener);

}
