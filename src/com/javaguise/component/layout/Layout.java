package com.javaguise.component.layout;

import com.garretwilson.beans.PropertyBindable;
import com.javaguise.component.Component;
import com.javaguise.component.Container;

/**Contains layout information for a container.
@param <T> The type of layout constraints associated with each component.
This interface and subclasses represent layout definitions, not layout implementations.
If the property of a component's constraints changes, a subclass of {@link LayoutConstraintsPropertyChangeEvent} will be fired indicating the associated component and constraints for which the value changed.
@author Garret Wilson
*/
public interface Layout<T extends Layout.Constraints> extends PropertyBindable
{

	/**@return The container that owns this layout, or <code>null</code> if this layout has not been installed into a container.*/
	public Container<?> getContainer();

	/**Sets the container that owns this layout
	This method is managed by containers, and normally should not be called by applications.
	A layout cannot be given a container if it is already installed in another container. Once a layout is installed in a container, it cannot be uninstalled.
	A layout cannot be given a container unless that container already recognizes this layout as its layout.
	If a layout is given the same container it already has, no action occurs.
	@param newContainer The new container for this layout.
	@exception NullPointerException if the given container is <code>null</code>.
	@exception IllegalStateException if a different container is provided and this layout already has a container.
	@exception IllegalArgumentException if a different container is provided and the given container does not already recognize this layout as its layout.
	*/
	public void setContainer(final Container<?> newContainer);

	/**Associates layout metadata with a component.
	Any metadata previously associated with the component will be removed.
	@param component The component for which layout metadata is being specified.
	@param constraints Layout information specifically for the component.
	@return The layout information previously associated with the component, or <code>null</code> if the component did not previously have metadata specified.
	@exception NullPointerException if the given constraints object is <code>null</code>.
	@exception IllegalStateException if this layout has not yet been installed into a container.
	*/
	public T setConstraints(final Component<?> component, final T constraints);

	/**Determines layout metadata associated with a component.
	@param component The component for which layout metadata is being requested.
	@return metadata The layout information associated with the component, or <code>null</code> if the component does not have metadata specified.
	@exception IllegalStateException if this layout has not yet been installed into a container.
	*/
	public T getConstraints(final Component<?> component);

	/**Removes any layout metadata associated with a component.
	@param component The component for which layout metadata is being removed.
	@return metadata The layout information previously associated with the component, or <code>null</code> if the component did not previously have metadata specified.
	@exception IllegalStateException if this layout has not yet been installed into a container.
	*/
	public T removeConstraints(final Component<?> component);

	/**Creates default constraints for the container.
	@return New default constraints for the container.
	@exception IllegalStateException if this layout does not support default constraints.
	*/
	public T createDefaultConstraints();

	/**Metadata about individual component layout.
	@author Garret Wilson
	*/
	public interface Constraints extends PropertyBindable
	{
	}

}
