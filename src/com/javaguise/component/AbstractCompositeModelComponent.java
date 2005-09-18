package com.javaguise.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

import com.garretwilson.util.EmptyIterator;
import com.javaguise.model.Model;
import com.javaguise.session.GuiseSession;

/**A composite component that represents data by its child components.
@param <M> The type of model contained in the component.
@param <T> The type of object being represented.
@param <S> The component state of each object.
@author Garret Wilson
*/
public abstract class AbstractCompositeModelComponent<M extends Model, T, S extends AbstractCompositeModelComponent.ComponentState, C extends CompositeComponent<C> & ModelComponent<M, C>> extends AbstractCompositeComponent<C> implements ModelComponent<M, C>	//TODO fire events when component states are added or removed so that AJAX updates can be sent
{

	/**The model used by this component.*/
	private final M model;

		/**@return The model used by this component.*/
		public M getModel() {return model;}

	/**Determines whether the models of this component and all of its child components are valid.
	This version checks to ensure its model is valid.
	@return Whether this component's model is valid along with those of all of its child components.
	*/
	public boolean isValid()
	{
		return getModel().isValid() ? super.isValid() : false;	//make sure the model is valid, along with the default checks
	}

	/**The map of component state for each object.*/
	private final Map<T, S> componentStateMap=new HashMap<T, S>();

	/**The set of components that are being represented.*/
//TODO del	private final Set<Component<?>> componentSet=new CopyOnWriteArraySet<Component<?>>();

	/**@return Whether this component has children.*/
//TODO del	public boolean hasChildren() {return !componentSet.isEmpty();}

	/**@return An iterator to child components.*/
//TODO del	public Iterator<Component<?>> iterator() {return componentSet.iterator();}

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

	/**Session constructor.
	@param session The Guise session that owns this component.
	@param model The component data model.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalStateException if no controller is registered for this component type.
	*/
	public AbstractCompositeModelComponent(final GuiseSession session, final M model)
	{
		this(session, null, model);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	@exception IllegalStateException if no controller is registered for this component type.
	*/
	public AbstractCompositeModelComponent(final GuiseSession session, final String id, final M model)
	{
		super(session, id);	//construct the parent class
		this.model=checkNull(model, "Model cannot be null.");	//save the model
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
			this.component=checkNull(component, "Component cannot be null.");
		}
	}

}
