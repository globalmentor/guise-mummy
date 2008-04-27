/*
 * Copyright © 2005-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guiseframework.component;

import java.util.*;

/**Utility methods for working with components.
@author Garret Wilson
*/
public class Components
{

	/**Retrieves all components, including the given component and all descendant components.
	@param component The component to search.
	@exception NullPointerException if the given component is <code>null</code>.
	@return The collection of components.
	*/
	public static Collection<Component> getComponents(final Component component)
	{
		return getComponents(component, new HashSet<Component>());	//return all components in a hash set
	}

	/**Retrieves all components, including the given component and all descendant components.
	@param component The component to search.
	@param componentCollection The collection into which the components will be collected.
	@exception NullPointerException if the given component and/or collection is <code>null</code>.
	@return The component collection.
	*/
	public static <T extends Collection<Component>> T getComponents(final Component component, final T componentCollection)
	{
		return getComponents(component, Component.class, componentCollection, true, true);	//get all components deeply
	}

	/**Retrieves all components, including the given component, that are instances of the of the given class.
	If <var>deep</var> is set to <code>true</code>, the component's child components are recursively searched if the component is a composite component.
	If <var>below</var> is set to <code>true</code>, the child components of any composite component that is an instance of the given class are also recursively searched.
	@param component The component to search.
	@param componentClass The type of component to retrieve.
	@param componentCollection The collection into which the components will be collected.
	@param deep <code>true</code> if the children of composite components should resursively be searched.
	@param below <code>true</code> if the children of composite components that are instances of the given class should resursively be searched, if <var>deep</var> is set to <code>true</code>.
	@exception NullPointerException if the given component, component class, and/or collection is <code>null</code>.
	@return The component collection.
	*/
	public static <C, T extends Collection<C>> T getComponents(final Component component, final Class<C> componentClass, final T componentCollection, final boolean deep, final boolean below)
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

	/**Retrieves all descendant components.
	@param component The component to search.
	@exception NullPointerException if the given component is <code>null</code>.
	@return The collection of components.
	*/
	public static Collection<Component> getDescendantComponents(final CompositeComponent compositeComponent)
	{
		return getDescendantComponents(compositeComponent, new HashSet<Component>());	//return all descendant components in a hash set
	}

	/**Retrieves all descendant components.
	@param compositeComponent The component to search.
	@param componentCollection The collection into which the components will be collected.
	@exception NullPointerException if the given component and/or collection is <code>null</code>.
	@return The component collection.
	*/
	public static <T extends Collection<Component>> T getDescendantComponents(final CompositeComponent compositeComponent, final T componentCollection)
	{
		return getChildComponents(compositeComponent, Component.class, componentCollection, true, true);	//get all child components deeply
	}

	/**Retrieves all child components that are instances of the of the given class.
	If <var>deep</var> is set to <code>true</code>, a component's child components are recursively searched if that component is a composite component.
	If <var>below</var> is set to <code>true</code>, the child components of any composite component that is an instance of the given class are also recursively searched.
	@param compositeComponent The component to search.
	@param componentClass The type of component to retrieve.
	@param componentCollection The collection into which the components will be collected.
	@param deep <code>true</code> if the children of composite components should resursively be searched.
	@param below <code>true</code> if the children of composite components that are instances of the given class should resursively be searched, if <var>deep</var> is set to <code>true</code>.
	@exception NullPointerException if the given component, component class, and/or collection is <code>null</code>.
	@return The component collection.
	*/
	public static <C, T extends Collection<C>> T getChildComponents(final CompositeComponent compositeComponent, final Class<C> componentClass, final T componentCollection, final boolean deep, final boolean below)
	{
		for(final Component childComponent:compositeComponent.getChildComponents())	//for each child component
		{
			getComponents(childComponent, componentClass, componentCollection, deep, below);	//get the components under the child component
		}
		return componentCollection;	//return the collection
	}

	/**Determines if the given component is or has as a descendant the given other component.
	@param component The component to search.
	@param hasComponent The component to find.
	@exception NullPointerException if one of the given components is <code>null</code>.
	@return <code>true</code> if the given component is the composite component or is a descendant of the given composite component.
	*/
	public static boolean hasComponent(final Component component, final Component hasComponent)
	{
		return hasComponent(component, hasComponent, true);	//do a deep search for the component
	}

	/**Determines if the given component is or has as a descendant the given other component.
	@param component The component to search.
	@param hasComponent The component to find.
	@param deep <code>true</code> if the children of composite components should resursively be searched.
	@exception NullPointerException if one of the given components is <code>null</code>.
	@return <code>true</code> if the given component is the composite component or is a descendant of the given composite component.
	*/
	public static boolean hasComponent(final Component component, final Component hasComponent, final boolean deep)
	{
		if(component.equals(hasComponent))	//if the components are equal
		{
			return true;	//we found the component
		}
		else if(deep && component instanceof CompositeComponent)	//if we should search deeply and this is a composite component
		{
			return hasChildComponent((CompositeComponent)component, hasComponent, deep);	//check the children
		}
		return false;	//this is not the component, and we were asked not to check deeply
	}

	/**Determines if the given composite component has the given component as one of its descendants.
	@param compositeComponent The component to search.
	@param component The component to find.
	@exception NullPointerException if the given composite component or component is <code>null</code>.
	@return Whether the given component is a descendant of the given composite component.
	*/
	public static boolean hasChildComponent(final CompositeComponent compositeComponent, final Component component)
	{
		return hasChildComponent(compositeComponent, component, true);	//do a deep search on the children
	}

	/**Determines if the given composite component has the given component as one of its descendants.
	@param compositeComponent The component to search.
	@param component The component to find.
	@param deep <code>true</code> if the children of composite components should resursively be searched.
	@exception NullPointerException if the given composite component or component is <code>null</code>.
	@return Whether the given component is a descendant of the given composite component.
	*/
	public static boolean hasChildComponent(final CompositeComponent compositeComponent, final Component component, final boolean deep)
	{
		if(deep)	//if we should search deeply, do this a tree at a time so as not to redundantly iterate
		{
			for(final Component childComponent:compositeComponent.getChildComponents())	//for each child component
			{
				if(hasComponent(childComponent, component, deep))	//if this child is or has the component
				{
					return true;	//we found the component
				}
			}
		}
		else	//if we should not search deeply
		{
			for(final Component childComponent:compositeComponent.getChildComponents())	//for each child component
			{
				if(compositeComponent.equals(component))	//if the components are equal
				{
					return true;	//we found the component
				}
			}
		}
		return false;	//we couldn't find the component
	}

}
