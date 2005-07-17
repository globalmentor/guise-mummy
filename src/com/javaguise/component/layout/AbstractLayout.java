package com.javaguise.component.layout;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.javaguise.component.Component;

/**Abstract implementation of layout information for a container.
@param <T> The type of layout metadata associated with each component.
This class and subclasses represent layout definitions, not layout implementations.
@author Garret Wilson
*/
public abstract class AbstractLayout<T> implements Layout<T>
{

	/**The thread-safe map of layout metadata associated with components.*/
	final Map<Component<?>, T> componentConstraintsMap=new ConcurrentHashMap<Component<?>, T>();

	/**Associates layout metadata with a component.
	Any metadata previously associated with the component will be removed.
	@param component The component for which layout metadata is being specified.
	@param metadata Layout information specifically for the component.
	@Return metadata The layout information previously associated with the component, or <code>null</code> if the component did not previously have metadata specified.
	*/
	public T setConstraints(final Component<?> component, final T metadata)
	{
		return componentConstraintsMap.put(component, metadata);	//put the metadata in the map, keyed to the component
	}

	/**Determines layout metadata associated with a component.
	@param component The component for which layout metadata is being requested.
	@return metadata The layout information associated with the component, or <code>null</code> if the component does not have metadata specified.
	*/
	public T getConstraints(final Component<?> component)
	{
		return componentConstraintsMap.get(component);	//return any metadata associated with the component
	}

	/**Removes any layout metadata associated with a component.
	@param component The component for which layout metadata is being removed.
	@return metadata The layout information previously associated with the component, or <code>null</code> if the component did not previously have metadata specified.
	*/
	public T removeConstraints(final Component<?> component)
	{
		return componentConstraintsMap.remove(component);	//remove the metadata from the map and return the old metadata
	}

}
