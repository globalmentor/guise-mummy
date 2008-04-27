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

import com.guiseframework.model.*;

import static java.util.Arrays.*;

/**Abstract implementation of a composite component that keeps track of its child components at specific indices in an array.
Child components should not directly call {@link #addComponent(Component)} and {@link #removeComponent(Component)}.
Each index in the array can be <code>null</code>.
Iterating over child components is thread safe.
@author Garret Wilson
*/
public abstract class AbstractArrayCompositeComponent extends AbstractMultipleCompositeComponent
{

	/**The array of child components.*/ 
	private final Component[] componentArray;

  /**Returns the component at the specified index in the array.
  @param index The index of the component to return.
	@return The component at the specified position in this array.
	@exception IndexOutOfBoundsException if the index is out of range.
	*/
	protected Component getComponent(final int index) {return componentArray[index];}
		
  /**Sets the component at the given index.
  If the new component is the same as the old, no action is taken.
  This implementation calls {@link #addComponent(Component)} and {@link #removeComponent(Component)} as necessary.
  @param index The index of the component to set.
  @param newComponent The component to set at the given index.
  @return The component previously at the given index.
	@exception IndexOutOfBoundsException if the index is out of range.
	@see #addComponent(Component)
	@see #removeComponent(Component)
	*/
	protected Component setComponent(final int index, final Component newComponent)
	{		
		final Component oldComponent=componentArray[index];	//get the old value
		if(oldComponent!=newComponent)	//if the value is really changing
		{
			componentArray[index]=newComponent;	//actually change the value
			if(oldComponent!=null)	//if there was an old component
			{
				removeComponent(oldComponent);	//remove the old component
			}
			if(newComponent!=null)	//if there is a new component
			{
				addComponent(newComponent);	//add the component					
			}
		}
		return oldComponent;	//return the component previously at the given index
	}
		
	/**@return An iterable to contained components.*/
	public Iterable<Component> getChildComponents()
	{
		return getChildList();	//create and return a list of children
	}

	/**Retrieves a list of all child components.
	This method can be overridden to provide further child components to the returned list.
	@return A list of child components.
	*/
	protected List<Component> getChildList()
	{
		final List<Component> componentList=new ArrayList<Component>(componentArray.length);	//create a list of components that is large enough to hold all components if we need to
		for(int i=componentArray.length-1; i>=0; --i)	//for each component
		{
			final Component component=componentArray[i];	//get a reference to this component
			if(component!=null)	//if we found another component
			{
				componentList.add(component);	//add the component to our list
			}
		}
		return componentList;	//return our list of components
	}
	
	/**@return Whether this component has children.*/
	public boolean hasChildComponents()
	{
		for(int i=componentArray.length-1; i>=0; --i)	//for each component
		{
			if(componentArray[i]!=null)	//if there is a component at this index
			{
				return true;	//we found a child component
			}
		}
		return false;	//all components in the array are null; we have no child components
	}

	/**Maximum component count constructor with a default label model.
	@param maxComponentCount The maximum number of child components to support.
	*/
	public AbstractArrayCompositeComponent(final int maxComponentCount)
	{
		this(new DefaultLabelModel(), maxComponentCount);	//construct the class with a default label model
	}

	/**Label model and maximum component count constructor.
	@param labelModel The component label model.
	@param maxComponentCount The maximum number of child components to support.
	@exception NullPointerException if the given label model is <code>null</code>.
	*/
	public AbstractArrayCompositeComponent(final LabelModel labelModel, final int maxComponentCount)	//TODO check the range of the maximum component count
	{
		super(labelModel);	//construct the parent class
		componentArray=new Component[maxComponentCount];	//create an array of components of the appropriate length
		fill(componentArray, null);	//fill the array with nulls
	}
}
