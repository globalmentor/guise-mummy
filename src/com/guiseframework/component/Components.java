package com.guiseframework.component;

import java.util.*;

/**Utility methods for working with components.
@author Garret Wilson
*/
public class Components
{

	/**Retrieves all components, including the given component, that are instances of the of the given class.
	If <var>deep</var> is set to <code>true</code>, the component's child components are recursively searched if the component is a composite component.
	If <var>below</var> is set to <code>true</code>, the child components of any composite component that is an instance of the given class are also recursively searched.
	@param component The component to search.
	@param componentCollection The collection into which the components will be collected.
	@param deep <code>true</code> if the children of composite components should resursively be searched.
	@param below <code>true</code> if the children of composite components that are instances of the given class should resursively be searched, if <var>deep</var> is set to <code>true</code>.
	@exception NullPointerException if the given component, component class, and/or collection is <code>null</code>.
	@return The component collection.
	*/
	public static <C extends Component, T extends Collection<C>> T getComponents(final Component component, final Class<C> componentClass, final T componentCollection, final boolean deep, final boolean below)
	{
		final boolean match=componentClass.isInstance(component);	//determine if this component is an instance of the given class
		if(match)	//if the component is an instance of the given class
		{
			componentCollection.add(componentClass.cast(component));	//add the component to the collection
		}
		if(deep && (!match || below) && component instanceof CompositeComponent)	//if we should go deep, see if this is not a match (or they want to search matches, too) and if this is is a composite component
		{
			getChildComponents((CompositeComponent)component, componentClass, componentCollection, deep, below);	//get the components of the child components
		}
		return componentCollection;	//return the collection
	}

	/**Retrieves all child components that are instances of the of the given class.
	If <var>deep</var> is set to <code>true</code>, a component's child components are recursively searched if that component is a composite component.
	If <var>below</var> is set to <code>true</code>, the child components of any composite component that is an instance of the given class are also recursively searched.
	@param component The component to search.
	@param componentCollection The collection into which the components will be collected.
	@param deep <code>true</code> if the children of composite components should resursively be searched.
	@param below <code>true</code> if the children of composite components that are instances of the given class should resursively be searched, if <var>deep</var> is set to <code>true</code>.
	@exception NullPointerException if the given component, component class, and/or collection is <code>null</code>.
	@return The component collection.
	*/
	public static <C extends Component, T extends Collection<C>> T getChildComponents(final CompositeComponent compositeComponent, final Class<C> componentClass, final T componentCollection, final boolean deep, final boolean below)
	{
		for(final Component childComponent:compositeComponent.getChildComponents())	//for each child component
		{
			getComponents(childComponent, componentClass, componentCollection, deep, below);	//set the editable status of the child component
		}
		return componentCollection;	//return the collection
	}

	/**Sets the component's editable status if the component is an {@link EditComponent}.
	Otherwise, if <var>deep</var> is specified, recursively sets the editable status of all child components.
	Edit components are assumed to manage their own children if needed, so this method never changes the editable status of child components of edit components.
	@param component The component the editable status of which to set.
	@param editable <code>true</code> if the component should allow the user to change the value.
	@param deep <code>true</code> if all non-edit components child components should resursively have their editable status set.
	*/
/*TODO del; slightly-slower getComponents() methods can be used equivalently
	public static void setEditable(final Component component, final boolean editable, final boolean deep)
	{
		if(component instanceof EditComponent)	//if the component is an edit component
		{
			((EditComponent)component).setEditable(editable);	//set the editable status
		}
		else if(deep && component instanceof CompositeComponent)	//if this is not an edit component, if we should go deep and this is a composite component
		{
			setChildComponentsEditable((CompositeComponent)component, editable, deep);	//set the editable status of the child components
		}
	}
*/
	/**Sets the editable status of each child component that is an {@link EditComponent}.
	@param compositeComponent The component the editable status of the children of which to set.
	@param editable <code>true</code> if the component should allow the user to change the value.
	@param deep <code>true</code> if all child components should resursively have their editable status set.
	*/
/*TODO del; slightly-slower getChildComponents() methods can be used equivalently
	public static void setChildComponentsEditable(final CompositeComponent compositeComponent, final boolean editable, final boolean deep)
	{
		for(final Component childComponent:compositeComponent.getChildComponents())	//for each child component
		{
			setEditable(childComponent, editable, deep);	//set the editable status of the child component
		}
	}
*/

}
