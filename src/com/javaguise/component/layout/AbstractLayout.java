package com.javaguise.component.layout;

import java.beans.PropertyChangeEvent;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.garretwilson.beans.BoundPropertyObject;
import com.garretwilson.lang.ObjectUtilities;
import com.garretwilson.util.Debug;
import com.javaguise.GuiseSession;
import com.javaguise.component.Component;
import com.javaguise.component.Container;
import com.javaguise.event.*;

/**Abstract implementation of layout information for a container.
@param <T> The type of layout constraints associated with each component.
This class and subclasses represent layout definitions, not layout implementations.
@author Garret Wilson
*/
public abstract class AbstractLayout<T extends Layout.Constraints> extends GuiseBoundPropertyObject implements Layout<T>
{

	/**The lazily-created listener of constraint property changes.*/
	private ConstraintsPropertyChangeListener constraintsPropertyChangeListener=null;

		/**@return The lazily-created listener of constraint property changes.*/
		protected ConstraintsPropertyChangeListener getConstraintsPropertyChangeListener()
		{
			if(constraintsPropertyChangeListener==null)	//if we haven't yet created a property change listener for constraints
			{
				constraintsPropertyChangeListener=new ConstraintsPropertyChangeListener();	//create a new constraints property change listener
			}
			return constraintsPropertyChangeListener;	//return the listener of constraints properties
		}

	/**The container that owns this layout, or <code>null</code> if this layout has not been installed into a container.*/
	private Container<?> container=null;

		/**@return The container that owns this layout, or <code>null</code> if this layout has not been installed into a container.*/
		public Container<?> getContainer() {return container;}

		/**Sets the container that owns this layout
		This method is managed by containers, and normally should not be called by applications.
//TODO del		A layout cannot be given a container if it is already installed in another container. Once a layout is installed in a container, it cannot be uninstalled.
		A layout cannot be given a container if it is already installed in another container.
		A layout cannot be given a container unless that container already recognizes this layout as its layout.
		If a layout is given the same container it already has, no action occurs.
		@param newContainer The new container for this layout.
//TODO del		@exception NullPointerException if the given container is <code>null</code>.
		@exception IllegalStateException if a different container is provided and this layout already has a container.
		@exception IllegalArgumentException if a different container is provided and the given container does not already recognize this layout as its layout.
		*/
		public void setContainer(final Container<?> newContainer)
		{
			final Container<?> oldContainer=container;	//get the old component
			if(oldContainer!=newContainer)	//if the component is really changing
			{
/*TODO fix
				checkNull(newContainer, "Container cannot be null.");
				if(oldContainer!=null)	//if we already have a parent
				{
					throw new IllegalStateException("Layout "+this+" already has container: "+oldContainer);
				}
*/
				if(newContainer!=null && newContainer.getLayout()!=this)	//if the container that is not really our owner
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
		final T oldConstraints=componentConstraintsMap.put(component, checkNull(constraints, "Constraints cannot be null"));	//put the metadata in the map, keyed to the component
		final ConstraintsPropertyChangeListener constraintsPropertyChangeListener=getConstraintsPropertyChangeListener();	//get the constraints property change listener
		if(oldConstraints!=null)	//if there were constraints before
		{
			oldConstraints.removePropertyChangeListener(constraintsPropertyChangeListener);	//stop listening for constraint property changes
		}
		constraints.addPropertyChangeListener(constraintsPropertyChangeListener);	//listen for constraint property changes for the new constraints
		return oldConstraints;	//return the old constraints, if any
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
	public T removeConstraints(final Component<?> component)	//TODO update comment; a component should always have associated constraints
	{
		final Container<?> container=getContainer();	//get the layout's container
		if(container==null)	//if we haven't been installed into a container
		{
			throw new IllegalStateException("Layout does not have container.");
		}
		final T constraints=componentConstraintsMap.remove(component);	//remove the metadata from the map and return the old metadata
		constraints.removePropertyChangeListener(constraintsPropertyChangeListener);	//stop listening for constraint property changes		
		return constraints;	//return the removed constraints
	}

	/**Session constructor.
	@param session The Guise session that owns this layout.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractLayout(final GuiseSession session)
	{
		super(session);	//construct the parent class
	}

	/**Reports that the bound property of a component's constraints has changed.
	No event is fired if old and new are both <code>null</code> or are both non-<code>null</code> and equal according to the {@link Object#equals(java.lang.Object)} method.
	No event is fired if no listeners are registered for the given property.
	This method delegates actual firing of the event to {@link #firePropertyChange(PropertyChangeEvent)}.
	@param component The component for which a constraint value changed.
	@param constraints The constraints for which a value changed.
	@param propertyName The name of the property being changed.
	@param oldValue The old property value.
	@param newValue The new property value.
	@see LayoutConstraintsPropertyChangeEvent
	*/
	protected <V> void fireConstraintsPropertyChange(final Component<?> component, final T constraints, final String propertyName, final V oldValue, final V newValue)
	{
		if(hasListeners(propertyName)) //if we have listeners registered for this property
		{
			if(!ObjectUtilities.equals(oldValue, newValue))	//if the values are different
			{					
				firePropertyChange(new LayoutConstraintsPropertyChangeEvent<T, V>(this, component, constraints, propertyName, oldValue, newValue));	//create and fire a layout constraints property change event
			}
		}
	}

	/**A property change listener that listens for changes in a constraint object's properties and fires a layout constraints property change event in response.
	A {@link LayoutConstraintsPropertyChangeEvent} will be fired for each component associated with the constraints for which a property changed
	@author Garret Wilson
	@see LayoutConstraintsPropertyChangeEvent
	*/
	protected class ConstraintsPropertyChangeListener extends AbstractGuisePropertyChangeListener<Object>
	{

		/**Called when a bound property is changed.
		This implementation fires a {@link LayoutConstraintsPropertyChangeEvent} indicating the constraints and associated component. 
		@param propertyChangeEvent An event object describing the event source, the property that has changed, and its old and new values.
		*/
		public void propertyChange(final GuisePropertyChangeEvent<Object> propertyChangeEvent)
		{
			final T constraints=(T)propertyChangeEvent.getSource();	//get the constraints for which a property changed TODO improve cast
				//find the component for these constraints
			for(final Map.Entry<Component<?>, T> componentConstraintsEntry:componentConstraintsMap.entrySet())	//for each entry in the map of constraints
			{
				if(componentConstraintsEntry.getValue()==constraints)	//if this component was associated with the constraints
				{
					refirePropertyChange(componentConstraintsEntry.getKey(), constraints, propertyChangeEvent.getPropertyName(), propertyChangeEvent.getOldValue(), propertyChangeEvent.getNewValue());	//refire the event
				}
			}
		}
	
		/**Refires a constraint property change event for the layout in the form of a {@link LayoutConstraintsPropertyChangeEvent}.
		@param component The component for which a constraint value changed.
		@param constraints The constraints for which a value changed.
		@param propertyName The name of the property being changed.
		@param oldValue The old property value.
		@param newValue The new property value.
		*/
		protected <V> void refirePropertyChange(final Component<?> component, final T constraints, final String propertyName, final V oldValue, final V newValue)
		{
//TODO del Debug.trace("Ready to fire an event indicating that component", componentConstraintsEntry.getKey(), "changed property", propertyChangeEvent.getPropertyName(), "to value", propertyChangeEvent.getNewValue());	//TODO del
			//fire an event indicating that the constraints for this component changed one if its properties
			fireConstraintsPropertyChange(component, constraints, propertyName, oldValue, newValue);								
			
		}
	}
	
	/**An abstract implementation of metadata about individual component layout.
	@author Garret Wilson
	*/
	public static abstract class AbstractConstraints extends BoundPropertyObject implements Constraints
	{
		
	}

}
