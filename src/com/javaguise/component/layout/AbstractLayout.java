package com.javaguise.component.layout;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.javaguise.component.Component;
import com.javaguise.component.Container;
import com.javaguise.event.GuiseBoundPropertyObject;
import com.javaguise.session.GuiseSession;

/**Abstract implementation of layout information for a container.
@param <T> The type of layout constraints associated with each component.
This class and subclasses represent layout definitions, not layout implementations.
@author Garret Wilson
*/
public abstract class AbstractLayout<T extends Layout.Constraints> extends GuiseBoundPropertyObject implements Layout<T>
{

	/**The container that owns this layout, or <code>null</code> if this layout has not been installed into a container.*/
	private Container<?> container=null;

		/**@return The container that owns this layout, or <code>null</code> if this layout has not been installed into a container.*/
		public Container<?> getContainer() {return container;}

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
		public void setContainer(final Container<?> newContainer)
		{
			final Container<?> oldContainer=container;	//get the old component
			if(oldContainer!=newContainer)	//if the component is really changing
			{
				checkNull(newContainer, "Container cannot be null.");
				if(oldContainer!=null)	//if we already have a parent
				{
					throw new IllegalStateException("Layout "+this+" already has container: "+oldContainer);
				}
				if(newContainer.getLayout()!=this)	//if the container that is not really our owner
				{
					throw new IllegalArgumentException("Provided container "+newContainer+" is not really owner of layout "+this);
				}
				container=newContainer;	//this is really our component; make a note of it
			}
		}

	/**Lays out the associated container.
	This version does nothing.
	@exception IllegalStateException if this layout has not yet been installed into a container.
	*/
/*TODO del if not needed
	public void layout()
	{		
	}
*/
		
	/**The thread-safe map of layout metadata associated with components.*/
	protected final Map<Component<?>, T> componentConstraintsMap=new ConcurrentHashMap<Component<?>, T>();

	/**Associates layout metadata with a component.
	Any metadata previously associated with the component will be removed.
	@param component The component for which layout metadata is being specified.
	@param constraints Layout information specifically for the component.
	@return The layout information previously associated with the component, or <code>null</code> if the component did not previously have metadata specified.
	@exception NullPointerException if the given constraints object is <code>null</code>.
	@exception IllegalStateException if this layout has not yet been installed into a container.
	*/
	public T setConstraints(final Component<?> component, final T constraints)
	{
		final Container<?> container=getContainer();	//get the layout's container
		if(container==null)	//if we haven't been installed into a container
		{
			throw new IllegalStateException("Layout does not have container.");
		}
		return componentConstraintsMap.put(component, checkNull(constraints, "Constraints cannot be null"));	//put the metadata in the map, keyed to the component
	}

	/**Determines layout metadata associated with a component.
	@param component The component for which layout metadata is being requested.
	@return The layout information associated with the component, or <code>null</code> if the component does not have metadata specified.
	@exception IllegalStateException if this layout has not yet been installed into a container.
	*/
	public T getConstraints(final Component<?> component)
	{
		final Container<?> container=getContainer();	//get the layout's container
		if(container==null)	//if we haven't been installed into a container
		{
			throw new IllegalStateException("Layout does not have container.");
		}
		return componentConstraintsMap.get(component);	//return any metadata associated with the component
	}

	/**Removes any layout metadata associated with a component.
	@param component The component for which layout metadata is being removed.
	@return The layout information previously associated with the component, or <code>null</code> if the component did not previously have metadata specified.
	@exception IllegalStateException if this layout has not yet been installed into a container.
	*/
	public T removeConstraints(final Component<?> component)
	{
		final Container<?> container=getContainer();	//get the layout's container
		if(container==null)	//if we haven't been installed into a container
		{
			throw new IllegalStateException("Layout does not have container.");
		}
		return componentConstraintsMap.remove(component);	//remove the metadata from the map and return the old metadata
	}

	/**Session constructor.
	@param session The Guise session that owns this layout.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractLayout(final GuiseSession<?> session)
	{
		super(session);	//construct the parent class
	}

}
