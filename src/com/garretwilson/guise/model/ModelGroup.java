package com.garretwilson.guise.model;


/**A group of similar models for providing such functions as communication and mutual exclusion.
@param <M> The type of model contained in the group.
@author Garret Wilson.
*/
public interface ModelGroup<M extends Model<M>>
{

	/**Determines whether this group contains the given model.
	@param model The model being checked for group inclusion.
	@return <code>true</code> if the model is contained in this group, else <code>false</code>.
	*/
	public boolean contains(final Object model);

	/**Adds a model to the group.
	The provided model is notified that it has been added to the group.
	If the model is already included in the group, no action occurs.
	@param model The model to add to the group.
	*/
	public void add(final M model);

	/**Removes a model from the group.
	The provided model is notified that it has been removed from the group.
	If the model is not included in this group, no action occurs.
	@param model The model to remove from the group.
	*/
	public void remove(final M model);

}
