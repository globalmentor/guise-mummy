package com.guiseframework.component;

import java.util.*;

import com.garretwilson.util.Debug;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.model.DefaultLabelModel;
import com.guiseframework.model.LabelModel;
import com.guiseframework.prototype.*;

/**Abstract implementation of a container component.
Iterating over child components is thread safe.
@author Garret Wilson
*/
public abstract class AbstractContainer<C extends Container<C>> extends AbstractLayoutComponent<C> implements Container<C>
{

	/**@return The number of child components in this container.*/
	public int size() {return super.size();}

	/**@return Whether this container contains no child components.*/
	public boolean isEmpty() {return super.isEmpty();}

	/**Determines whether this container contains the given component.
	@param component The component to check.
	@return <code>true</code> if this container contains the given component.
	*/
	public boolean contains(final Object component) {return super.contains(component);}

	/**Returns the index in the container of the first occurrence of the specified component.
	@param component The component the index of which should be returned.
	@return The index in this container of the first occurrence of the specified component, or -1 if this container does not contain the given component.
	*/
	public int indexOf(final Object component) {return super.indexOf(component);}

	/**Returns the index in this container of the last occurrence of the specified compoent.
	@param component The component the last index of which should be returned.
	@return The index in this container of the last occurrence of the specified component, or -1 if this container does not contain the given component.
	*/
	public int lastIndexOf(final Object component) {return super.lastIndexOf(component);}

  /**Returns the component at the specified index in the container.
  @param index The index of the component to return.
	@return The component at the specified position in this container.
	@exception IndexOutOfBoundsException if the index is out of range.
	*/
	public Component<?> get(final int index) {return super.get(index);}

	/**Adds a component to the container with default constraints.
	@param component The component to add.
	@return <code>true</code> if this container changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception IllegalStateException if the installed layout does not support default constraints.
	*/
	public boolean add(final Component<?> component)
	{
		final boolean result=addComponent(component);	//add the component normally
		fireContainerModified(indexOf(component), component, null);	//indicate the component was added at the index TODO promote
		return result;	//return the result
	}

	/**Adds a component to the container along with constraints.
	This is a convenience method that first sets the constraints of the component. 
	@param component The component to add.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@return <code>true</code> if this container changed as a result of the operation.
	@exception IllegalArgumentException if the component already has a parent.
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	*/
	public boolean add(final Component<?> component, final Constraints constraints)
	{
		component.setConstraints(constraints);	//set the constraints in the component
		return add(component);	//add the component, now that its constraints have been set
	}

	/**Adds a component based upon the given prototype to the container with default constraints.
	This implementation delegates to {@link #add(Component)}.
	@param prototype The prototype of the component to add.
	@return The component created to represent the given prototype.
	@exception IllegalArgumentException if no component can be created from the given prototype
	@exception IllegalStateException if the installed layout does not support default constraints.
	@see #createComponent(Prototype)
	*/
	public Component<?> add(final Prototype prototype)
	{
		final Component<?> component=createComponent(prototype);	//create a component from the prototype
		add(component);	//add the component to the container
		return component;	//return the component we created
	}

	/**Adds a component based upon the given prototype to the container along with constraints.
	This implementation delegates to {@link #add(Component, Constraints)}.
	@param prototype The prototype of the component to add.
	@param constraints The constraints for the layout, or <code>null</code> if default constraints should be used.
	@return The component created to represent the given prototype.
	@exception IllegalArgumentException if no component can be created from the given prototype
	@exception ClassCastException if the provided constraints are not appropriate for the installed layout.
	@exception IllegalStateException if no constraints were provided and the installed layout does not support default constraints.
	@see #createComponent(Prototype)
	*/
	public Component<?> add(final Prototype prototype, final Constraints constraints)
	{
		final Component<?> component=createComponent(prototype);	//create a component from the prototype
		add(component, constraints);	//add the component to the container
		return component;	//return the component we created
	}

	/**Creates a component appropriate for the context of this component from the given prototype.
	This implementation creates the following components, in order of priority:
	<dl>
		<dt>{@link ActionPrototype}</dt> <dd>{@link Button}</dd>
		<dt>{@link LabelPrototype}</dt> <dd>{@link Label}</dd>	
		<dt>{@link MenuPrototype}</dt> <dd>{@link DropMenu}</dd>	
		<dt>{@link ValuePrototype}&lt;{@link Boolean}&gt;</dt> <dd>{@link CheckControl}</dd>	
		<dt>{@link ValuePrototype}&lt;?&gt;</dt> <dd>{@link TextControl}</dd>	
	</dl>
	@param prototype The prototype of the component to create.
	@return A new component based upon the given prototype.
	@exception IllegalArgumentException if no component can be created from the given prototype
	*/
	public Component<?> createComponent(final Prototype prototype)
	{
		if(prototype instanceof ActionPrototype)	//action prototypes
		{
			return new Button((ActionPrototype)prototype);
		}
		else if(prototype instanceof LabelPrototype)	//label prototypes
		{
			return new Label((LabelPrototype)prototype);
		}
		else if(prototype instanceof MenuPrototype)	//menu prototypes
		{
			return new DropMenu((MenuPrototype)prototype, Flow.PAGE);
		}
		else if(prototype instanceof ValuePrototype)	//value prototypes
		{
			final Class<?> valueClass=((ValuePrototype<?>)prototype).getValueClass();	//get the type of value represented
			if(Boolean.class.isAssignableFrom(valueClass))	//if a boolean value is represented
			{
				return new CheckControl((ValuePrototype<Boolean>)prototype);
			}
			else	//if the prototype is unrecognized
			{
				throw new IllegalArgumentException("Unrecognized prototype: "+prototype.getClass());
			}		
/*TODO finish
			else	//for any other value type
			{
				return new TextControl<V>()
			}
*/
		}
		else	//if the prototype is unrecognized
		{
			throw new IllegalArgumentException("Unrecognized prototype: "+prototype.getClass());
		}		
	}

	/**Creates a component appropriate for the context of this component from the given prototype.
	This implementation creates the following components, in order of priority:
	<dl>
		<dt>{@link ActionPrototype}</dt> <dd>{@link Button}</dd>
		<dt>{@link LabelPrototype}</dt> <dd>{@link Label}</dd>	
		<dt>{@link MenuPrototype}</dt> <dd>{@link DropMenu}</dd>	
		<dt>{@link ValuePrototype}&lt;{@link Boolean}&gt;</dt> <dd>{@link CheckControl}</dd>	
		<dt>{@link ValuePrototype}&lt;?&gt;</dt> <dd>{@link TextControl}</dd>	
	</dl>
	@param prototype The prototype of the component to create.
	@return A new component based upon the given prototype.
	@exception IllegalArgumentException if no component can be created from the given prototype
	*/
/*TODO del if not needed
	public <V> ValueControl<V, ?> createValueControl(final ValuePrototype<V> valuePrototype)
	{
		final Class<V> valueClass=valuePrototype.getValueClass();	//get the type of value represented
		if(Boolean.class.isAssignableFrom(valueClass))	//if a boolean value is represented
		{
			return new CheckControl((ValuePrototype<Boolean>)valuePrototype);
		}
		else	//for any other value type
		{
			return new TextControl<V>()
		}
		
	}
*/

	/**Removes a component from the container.
	@param componentObject The component to remove.
	@return <code>true</code> if this collection changed as a result of the operation.
	@throws ClassCastException if given element is not a component.
	@exception IllegalArgumentException if the component is not a member of the container.
	*/
	public boolean remove(final Object componentObject)
	{
		final Component<?> component=(Component<?>)componentObject;	//cast the object to a component
		final int index=indexOf(component);	//get the index of the component
		final boolean result=removeComponent(component);	//remove the component normally
		assert index>=0 : "Component successfully removed from container, yet previous index is negative.";
		fireContainerModified(index, null, component);	//indicate the component was removed from the index TODO promote
		return result;	//return the result
	}

	/**Removes the child component at the specified position in this container.
	@param index The index of the component to removed.
	@return The value previously at the specified position.
	@exception IndexOutOfBoundsException if the index is out of range (<var>index</var> &lt; 0 || <var>index</var> &gt;= <code>size()</code>).
	*/
	public Component<?> remove(final int index)
	{
		final Component<?> component=get(index);	//get the component at this index
		remove(component);	//remove the component
		return component;	//return the component that was removed
	}

	/**Removes all of the components from this container.*/
	public void clear()
	{
		for(final Component<?> component:this)	//for each component in the container
		{
			remove(component);	//remove this component
		}
	}

	/**@return An iterator to child components.*/
	public Iterator<Component<?>> iterator() {return getComponentList().iterator();}

	/**Returns a list of children.
	This method along with {@link #setChildren()} provides a <code>children</code> property for alternate children access.
	@return A list of container children in order.
	@see #iterator()
	*/
	public List<Component<?>> getChildren()
	{
		return new ArrayList<Component<?>>(getComponentList());	//create and return a copy of the list
	}

	/**Sets the children in this container.
	This method along with {@link #getChildren()} provides a <code>children</code> property for alternate children access.
	@param children The new children for this container in order.
	@see #clear()
	@see #add(Component)
	*/
	public void setChildren(final List<Component<?>> children)
	{
		clear();	//remove all children from the container
		for(final Component<?> child:children)	//for each child
		{
			add(child);	//add this child
		}
	}

	/**Sets the layout definition for the component.
	This is a bound property.
	The layout is specified as not yet having a theme applied, as the specific theme rules applied to the layout may depend on the layout's owner.
	@param newLayout The new layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	@see #LAYOUT_PROPERTY
	@see #setThemeApplied(boolean) 
	*/
	public <T extends Constraints> void setLayout(final Layout<T> newLayout)
	{
		super.setLayout(newLayout);	//delegate to the parent class
	}

	/**Layout constructor with a default label model.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public AbstractContainer(final Layout<?> layout)
	{
		this(new DefaultLabelModel(), layout);	//construct the class with a default label model
	}

	/**Label model and layout constructor.
	@param labelModel The component label model.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given label model and/or layout is <code>null</code>.
	*/
	public AbstractContainer(final LabelModel labelModel, final Layout<?> layout)
	{
		super(labelModel, layout);	//construct the parent class
	}

	/**Adds a container listener.
	@param containerListener The container listener to add.
	*/
	public void addContainerListener(final ContainerListener containerListener)
	{
		getEventListenerManager().add(ContainerListener.class, containerListener);	//add the listener
	}

	/**Removes a container listener.
	@param containerListener The container listener to remove.
	*/
	public void removeContainerListener(final ContainerListener containerListener)
	{
		getEventListenerManager().remove(ContainerListener.class, containerListener);	//remove the listener
	}

	/**Fires an event to all registered container listeners indicating the components in the container changed.
	@param index The index at which a component was added and/or removed, or -1 if the index is unknown.
	@param addedComponent The component that was added to the container, or <code>null</code> if no component was added or it is unknown whether or which components were added.
	@param removedComponent The component that was removed from the container, or <code>null</code> if no component was removed or it is unknown whether or which components were removed.
	@see ContainerListener
	@see ContainerEvent
	*/
	protected void fireContainerModified(final int index, final Component<?> addedComponent, final Component<?> removedComponent)
	{
		final ContainerEvent containerEvent=new ContainerEvent(getThis(), index, addedComponent, removedComponent);	//create a new event
		getSession().queueEvent(new PostponedContainerEvent(getEventListenerManager(), containerEvent));	//tell the Guise session to queue the event
	}

}
