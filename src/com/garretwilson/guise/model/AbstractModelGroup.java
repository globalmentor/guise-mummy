package com.garretwilson.guise.model;

import java.util.*;
import static java.util.Collections.*;

/**An abstract implementation of a group of similar models for providing such functions as communication and mutual exclusion.
@param <M> The type of model contained in the group.
@author Garret Wilson.
*/
public abstract class AbstractModelGroup<M extends Model<M>> implements ModelGroup<M>
{

	/**The synchronized set of models.*/
	private final Set<M> modelSet=synchronizedSet(new HashSet<M>());

	/**Determines whether this group contains the given model.
	@param model The model being checked for group inclusion.
	@return <code>true</code> if the model is contained in this group, else <code>false</code>.
	*/
	public boolean contains(final Object model)
	{
		return modelSet.contains(model);	//see if the set of models contains this model TODO check for class cast exception
	}

	/**Adds a model to the group.
	The provided model is notified that it has been added to the group.
	If the model is already included in the group, no action occurs.
	@param model The model to add to the group.	
	@exception IllegalArgumentException if the model is already a member of a group.
	*/
	public void add(final M model)
	{
		if(!contains(model))	//if the group doesn't already contain the model
		{
			if(model.getGroup()!=null)	//if this model has already been added to group
			{
				throw new IllegalArgumentException("Model "+model+" is already a member of a group, "+model.getGroup()+".");
			}
			modelSet.add(model);	//add this model to the model set
			model.setGroup(this);	//indicate to the model that it is being added to a group
		}
	}

	/**Removes a model from the group.
	The provided model is notified that it has been removed from the group.
	If the model is not included in this group, no action occurs.
	@param model The model to remove from the group.
	*/
	public void remove(final M model)
	{
		if(contains(model))	//if the group contains the model
		{
			modelSet.remove(model);	//remove this model from the model set
			model.setGroup(null);	//indicate to the model that it is being removed from a group
		}
	}

}
