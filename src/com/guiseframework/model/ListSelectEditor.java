package com.guiseframework.model;

import com.guiseframework.prototype.ActionPrototype;

/**An object for editing values in a list model.
Prototypes are provided for common edit functionality.
@param <V> The type of values contained in the model.
@author Garret Wilson
*/
public interface ListSelectEditor<V>
{

	/**@return The list select model being edited.*/
	public ListSelectModel<V> getListSelectModel();

	/**@return The prototype for inserting a value into the list.*/
	public ActionPrototype getInsertActionPrototype();

	/**@return The prototype for editing a value in the list.*/
	public ActionPrototype getEditActionPrototype();

	/**@return The prototype for removing a value from the list.*/
	public ActionPrototype getRemoveActionPrototype();

	/**Creates and allows the user to edit a new value.
	If the user accepts the changes, the value is inserted before the currently selected value in the list,
	or at the end of the list if no value is selected.
	*/
	public void insertValue();

	/**Edits the currently selected value in the list.
	If no value is selected in the list, no action occurs.
	*/
	public void editValue();

	/**Removes the currently selected value in the list.
	If no value is selected in the list, no action occurs.
	*/
	public void removeValue();
}
