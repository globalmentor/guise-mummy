package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import java.util.*;

/**A composite component that represents the state of its child components.
@param <T> The type of object being represented.
@param <S> The component state of each object.
@author Garret Wilson
*/
public abstract class AbstractCompositeStateComponent<T, S extends AbstractCompositeStateComponent.ComponentState, C extends CompositeComponent<C>> extends AbstractMultipleCompositeComponent<C>	//TODO fire events when component states are added or removed so that AJAX updates can be sent
{

	/**The map of component state for each object.*/
	private final Map<T, S> componentStateMap=new HashMap<T, S>();

	/**Retrieves a component state for the given object.
	@param object The object for which a representation component should be returned.
	@return The state of the child component to represent the given object, or <code>null</code> if there is no component for the given object.
	*/
	protected S getComponentState(final T object)
	{
		return componentStateMap.get(object);	//get the component state keyed to this object
	}

	/**Retrieves the component for the given object.
	@param object The object for which a representation component should be returned.
	@return The child component representing the given object, or <code>null</code> if there is no component representing the given object.
	*/
	public Component<?> getComponent(final T object)
	{
		final S componentState=getComponentState(object);	//get the component state for this object
		return componentState!=null ? componentState.getComponent() : null;	//get the component stored in the component state, if there is a component state
	}

	/**Stores a child component state for the given object.
	@param object The object with which the component state is associated.
	@param componentState The child component state to represent the given object, or <code>null</code> if there is no component for the given object.
	@return The child component that previously represented the given tree node, or <code>null</code> if there was previously no component for the given object.	
	*/
	protected S putComponentState(final T object, final S componentState)
	{
		final S oldComponentState=componentStateMap.put(object, componentState);	//associate the component state with this object
		if(oldComponentState!=null)	//if there was a component state before
		{
			removeComponent(oldComponentState.getComponent());	//remove the old component from the set of components
		}
		addComponent(componentState.getComponent());	//put the new component in the component set
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
		for(final Component<?> component:getChildren())	//for each component in the container
		{
			removeComponent(component);	//remove this component
		}
		componentStateMap.clear();	//remove all component states
	}

	/**An encapsulation of the state of a representation component.
	@author Garret Wilson
	*/ 
	protected abstract static class ComponentState
	{
		/**The representation component.*/
		private final Component<?> component;

			/**@return The representation component.*/
			public Component<?> getComponent() {return component;}

		/**Constructor
		@param component The representation component.
		@exception NullPointerException if the given component is <code>null</code>.
		*/
		public ComponentState(final Component<?> component)
		{
			this.component=checkInstance(component, "Component cannot be null.");
		}
	}

}
