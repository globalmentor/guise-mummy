package com.guiseframework.component;

import com.guiseframework.component.layout.Layout;
import com.guiseframework.component.layout.Constraints;
import com.guiseframework.event.ContainerListener;
import com.guiseframework.prototype.*;

/**Component that allows for addition and removal of child components.
A layout component is iterable over its child components, and can be used in short <code>for(:)</code> form. 
@author Garret Wilson
*/
public interface Container extends LayoutComponent, Iterable<Component>
{

	/**@return The number of child components in this composite component.*/
	public int size();

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
	public Component get(int index);

	/**Adds a component with default constraints to the container.
	@param component The component to add to this container.
	@return <code>true</code> if this container changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception IllegalStateException if the installed layout does not support default constraints.
	*/
	public boolean add(final Component component);

	/**Adds a component along with constraints to the container at the specified index.
	This is a convenience method that first sets the constraints of the component. 
	@param index The index at which the component should be added.
	@param component The component to add to this container.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@exception IllegalArgumentException if the component already has a parent.
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	@exception IndexOutOfBoundsException if the index is less than zero or greater than the number of child components.
	*/
	public void add(final int index, final Component component, final Constraints constraints);

	/**Adds a component along with constraints to the container.
	This is a convenience method that first sets the constraints of the component. 
	@param component The component to add to this container.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@return <code>true</code> if this container changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	*/
	public boolean add(final Component component, final Constraints constraints);

	/**Adds a component based upon the given prototype to the container with default constraints at the specified index.
	@param index The index at which the component should be added.
	@param prototype The prototype of the component to add.
	@return The component created to represent the given prototype.
	@exception IllegalArgumentException if no component can be created from the given prototype
	@exception IllegalStateException if the installed layout does not support default constraints.
	@exception IndexOutOfBoundsException if the index is less than zero or greater than the number of child components.
	@see #createComponent(Prototype)
	*/
	public Component add(final int index, final Prototype prototype);

	/**Adds a component based upon the given prototype to the container with default constraints.
	@param prototype The prototype of the component to add.
	@return The component created to represent the given prototype.
	@exception IllegalArgumentException if no component can be created from the given prototype
	@exception IllegalStateException if the installed layout does not support default constraints.
	@see #createComponent(Prototype)
	*/
	public Component add(final Prototype prototype);

	/**Adds a component based upon the given prototype to the container along with constraints at the specified index.
	@param index The index at which the component should be added.
	@param prototype The prototype of the component to add.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@return The component created to represent the given prototype.
	@exception IllegalArgumentException if no component can be created from the given prototype
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	@exception IndexOutOfBoundsException if the index is less than zero or greater than the number of child components.
	@see #createComponent(Prototype)
	*/
	public Component add(final int index, final Prototype prototype, final Constraints constraints);

	/**Adds a component based upon the given prototype to the container along with constraints.
	@param prototype The prototype of the component to add.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@return The component created to represent the given prototype.
	@exception IllegalArgumentException if no component can be created from the given prototype
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	@see #createComponent(Prototype)
	*/
	public Component add(final Prototype prototype, final Constraints constraints);

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
	public Component remove(final int index);

	/**Removes all of the components from this container.*/
	public void clear();

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
