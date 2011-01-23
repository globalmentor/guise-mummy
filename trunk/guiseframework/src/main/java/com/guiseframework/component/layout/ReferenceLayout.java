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

package com.guiseframework.component.layout;

import java.util.*;

import com.guiseframework.component.Component;

/**A layout for components bound to component references such as IDs.
@author Garret Wilson
*/
public class ReferenceLayout extends AbstractLayout<ReferenceConstraints>
{

	/**The lazily-created map of components mapped to reference IDs.*/
	private Map<String, Component> referenceIDComponentMap=null;

		/**@return The lazily-created map of components mapped to reference IDs.*/
		protected Map<String, Component> getReferenceIDComponentMap()
		{
			if(referenceIDComponentMap==null)	//if the map hasn't been created, yet
			{
				referenceIDComponentMap=new HashMap<String, Component>();	//create a new map
			}
			return referenceIDComponentMap;	//return the map of components keyed to IDs
		}

	/**@return The class representing the type of constraints appropriate for this layout.*/
	public Class<? extends ReferenceConstraints> getConstraintsClass() {return ReferenceConstraints.class;}

	/**Indicates that the constraints for a component have changed.
	This method is also called when the component is first added to the layout.
	This version adds or removes the component reference ID to the map.
	@param component The component for which constraints have changed.
	@param oldConstraints The old component constraints, or <code>null</code> if there were no constraints previously.
	@param newConstraints The new component constraints, or <code>null</code> if the component now has no constraints.
	*/
	protected void componentConstraintsChanged(final Component component, final Constraints oldConstraints, final Constraints newConstraints)
	{
		final Class<? extends ReferenceConstraints> constraintsClass=getConstraintsClass();	//get the type of constraints we expect
		if(constraintsClass.isInstance(oldConstraints))	//if the old constraints is of the type of we expect
		{
			getReferenceIDComponentMap().remove(constraintsClass.cast(oldConstraints).getID());	//remove the ID/component association from the map
		}
		if(constraintsClass.isInstance(newConstraints))	//if the new constraints is of the type of we expect
		{
			getReferenceIDComponentMap().put(constraintsClass.cast(newConstraints).getID(), component);	//associate the component with the reference ID
		}
	}

	/**Creates default constraints for the container.
	@return New default constraints for the container.
	@exception IllegalStateException if this layout does not support default constraints.
	*/
	public ReferenceConstraints createDefaultConstraints()
	{
		throw new IllegalStateException("Component cannot have default constraints; it must be bound to some ID.");
	}

	/**Retrieves a component bound to a given ID.
	@param id The ID with which a component may be bound.
	@return A component with constraints specifying the given ID, or <code>null</code> if there is no component bound to the given ID.
	*/
	public Component getComponentByID(final String id)
	{
		return getReferenceIDComponentMap().get(id);	//look up the component by its reference ID
	}
	
}
