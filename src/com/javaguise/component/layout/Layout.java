package com.javaguise.component.layout;

import com.javaguise.component.Component;

/**Contains layout information for a container.
@param <T> The type of layout constraints associated with each component.
This interface and subclasses represent layout definitions, not layout implementations.
@author Garret Wilson
*/
public interface Layout<T extends Layout.Constraints>
{
	/**Associates layout metadata with a component.
	Any metadata previously associated with the component will be removed.
	@param component The component for which layout metadata is being specified.
	@param metadata Layout information specifically for the component.
	@Return metadata The layout information previously associated with the component, or <code>null</code> if the component did not previously have metadata specified.
	*/
	public T setConstraints(final Component<?> component, final T metadata);

	/**Determines layout metadata associated with a component.
	@param component The component for which layout metadata is being requested.
	@return metadata The layout information associated with the component, or <code>null</code> if the component does not have metadata specified.
	*/
	public T getConstraints(final Component<?> component);

	/**Removes any layout metadata associated with a component.
	@param component The component for which layout metadata is being removed.
	@return metadata The layout information previously associated with the component, or <code>null</code> if the component did not previously have metadata specified.
	*/
	public T removeConstraints(final Component<?> component);

	/**Creates default constraints for the given component.
	@param component The component for which constraints should be provided.
	@return New default constraints for the given component.
	@exception IllegalStateException if this layout does not support default constraints.
	*/
	public T createDefaultConstraints(final Component<?> component);

	/**Metadata about individual component layout.
	@author Garret Wilson
	*/
	public interface Constraints
	{
	}

}
