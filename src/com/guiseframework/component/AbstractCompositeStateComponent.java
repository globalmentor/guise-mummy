package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import java.io.IOException;
import java.util.*;

import com.guiseframework.model.LabelModel;

import static java.util.Collections.*;

/**A composite component that represents the state of its child components.
@param <T> The type of object being represented.
@param <S> The component state of each object.
@author Garret Wilson
*/
public abstract class AbstractCompositeStateComponent<T, S extends AbstractCompositeStateComponent.ComponentState> extends AbstractMultipleCompositeComponent	//TODO fire events when component states are added or removed so that AJAX updates can be sent
{

	/**The map of component state for each object.*/
	private final Map<T, S> componentStateMap=synchronizedMap(new HashMap<T, S>());

	/**@return An iterable of component states.*/
	protected Iterable<S> getComponentStates()
	{
		synchronized(componentStateMap)	//don't allow the map to be modified while we access it
		{
			return new ArrayList<S>(componentStateMap.values());	//copy the component state values and return them
		}
	}
	
	/**Retrieves a component state for the given object.
	@param object The object for which a representation component should be returned.
	@return The state of the child component to represent the given object, or <code>null</code> if there is no component for the given object.
	*/
	protected S getComponentState(final T object)
	{
		return componentStateMap.get(object);	//get the component state keyed to this object
	}

	/**Retrieves a component state for the given object.
	If no component yet exists for the given object, one will be created.
	@param object The object for which a representation component should be returned.
	@return The state of the child component to represent the given object.
	@exception IllegalArgumentException if the given object is not an appropriate object for a component state to be created.
	@see #getComponentState(Object)
	@see #createComponentState(Object)
	@see #putComponentState(Object, com.guiseframework.component.AbstractCompositeStateComponent.ComponentState)
	*/
	protected S determineComponentState(final T object)
	{
		S componentState;	//we'll find a component state and store it here
		synchronized(componentStateMap)	//don't allow the map to be modified while we access it
		{
			componentState=getComponentState(object);	//get the component state for this object
			if(componentState==null)	//if there is no component state
			{
				componentState=createComponentState(object);	//create a new component state for the object
				putComponentState(object, componentState);	//store the component state in the map for next time			
			}
		}
		return componentState;	//return the component state
	}

	/**Stores a child component state for the given object.
	The component's properties will be initialized immediately, as the component state is likely to be generated dynamically during component view update.
	@param object The object with which the component state is associated.
	@param componentState The child component state to represent the given object, or <code>null</code> if there is no component for the given object.
	@return The child component that previously represented the given tree node, or <code>null</code> if there was previously no component for the given object.
	@see Component#initializeProperties()
	*/
	protected S putComponentState(final T object, final S componentState)
	{
		final S oldComponentState=componentStateMap.put(object, componentState);	//associate the component state with this object
		if(oldComponentState!=null)	//if there was a component state before
		{
			removeComponent(oldComponentState.getComponent());	//remove the old component from the set of components
		}
		final Component component=componentState.getComponent();	//get the component state
		addComponent(component);	//put the new component in the component set
		try
		{
			component.initializeProperties();	//initialize the component properties
		}
		catch(final IOException ioException)	//if there is a problem initializing the properties
		{
			throw new IllegalStateException(ioException);	//TODO improve error
		}
		return oldComponentState;	//return whatever component state was previously in the map
	}

	/**Removes the child component state for the given object.
	@param object The object with which the representation component is associated.
	@return The child component state that previously represented the given object, or <code>null</code> if there was previously no component for the given object.	
	*/
	protected ComponentState removeComponentState(final T object)
	{
		final ComponentState oldComponentState=componentStateMap.remove(object);	//remove the component state associated with this object
		if(oldComponentState!=null)	//if there was a component state before
		{
			removeComponent(oldComponentState.getComponent());	//remove the old component from the set of components
		}
		return oldComponentState;	//return whatever component state was previously in the map
	}

	/**Removes all child component states.*/
	protected void clearComponentStates()	//TODO make sure this and related routines doesn't leak components or component states
	{
		for(final Component component:getChildren())	//for each component in the container
		{
			removeComponent(component);	//remove this component
		}
		componentStateMap.clear();	//remove all component states
	}

	/**Retrieves the component for the given object.
	If no component yet exists for the given object, one will be created.
	@param object The object for which a representation component should be returned.
	@return The child component representing the given object.
	@exception IllegalArgumentException if the given object is not an appropriate object for a component to be created.
	*/
	protected Component getComponent(final T object)
	{
		return determineComponentState(object).getComponent();	//get the component stored in the component state, creating one if needed
	}

	/**Retrieves the object for the given component.
	@param component The child component representing an object.
	@param object The object for which a representation component should be returned.
	@return The object the child component represents.
	@exception NullPointerException if the given component is <code>null</code>.
	@exception IllegalArgumentException if the given component does not represent any object.
	*/
	protected T getObject(final Component component)
	{
		checkInstance(component, "Component cannot be null.");
		synchronized(componentStateMap)	//don't allow the map to be modified while we access it
		{
			for(final Map.Entry<T, S> componentStateEntry:componentStateMap.entrySet())	//for all the map entries
			{
				if(componentStateEntry.getValue().getComponent()==component)	//if we found the component
				{
					return componentStateEntry.getKey();	//return the object being represented
				}
			}
		}
		throw new IllegalArgumentException("Component "+component+" not representing any object.");
	}

	/**Label model constructor.
	@param labelModel The component label model.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public AbstractCompositeStateComponent(final LabelModel labelModel)
	{
		super(labelModel);	//construct the parent class
	}

	/**Creates a component state to represent the given object.
	@param object The object with which the component state is to be associated.
	@return The component state to represent the given object.
	@exception IllegalArgumentException if the given object is not an appropriate object for a component state to be created.
	*/
	protected abstract S createComponentState(final T object);

	/**An encapsulation of the state of a representation component.
	@author Garret Wilson
	*/ 
	protected abstract static class ComponentState
	{
		/**The representation component.*/
		private final Component component;

			/**@return The representation component.*/
			public Component getComponent() {return component;}

		/**Constructor
		@param component The representation component.
		@exception NullPointerException if the given component is <code>null</code>.
		*/
		public ComponentState(final Component component)
		{
			this.component=checkInstance(component, "Component cannot be null.");
		}
	}

}
