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

package com.guiseframework.model;

import java.beans.PropertyVetoException;
import java.util.List;

import com.guiseframework.event.*;

/**A model for selecting one or more values from a list.
The model must be thread-safe, synchronized on itself. Any iteration over values should include synchronization on the instance of this interface.
When the selection is changed, a {@link com.guiseframework.event.ListSelectionEvent} is fired.
When the state of a value (besides its selection, such as its enabled status) changes, a {@link com.globalmentor.beans.GenericPropertyChangeEvent} event is fired referencing the value the state of which has changed.
@param <V> The type of values contained in the model.
@author Garret Wilson
*/
public interface ListSelectModel<V> extends SelectModel<V>, List<V>, ListListenable<V>
{

	/**@return The selection policy for this model.*/
	public ListSelectionPolicy<V> getSelectionPolicy();

	/**Determines the selected index.
	If more than one index is selected, the lead selected index will be returned.
	@return The index currently selected, or -1 if no index is selected.
	@see #getSelectedValue()
	*/
	public int getSelectedIndex();
	
	/**Determines the selected indices.
	@return The indices currently selected.
	@see #getSelectedValues()
	*/
	public int[] getSelectedIndexes();
	
	/**Sets the selected indices.
	Invalid and duplicate indices will be ignored.
	If the value change is vetoed by the installed validator, the validation exception will be accessible via {@link PropertyVetoException#getCause()}.
	@param indexes The indices to select.
	@throws PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	@see ListSelectionPolicy#getSetSelectedIndices(ListSelectModel, int[])
	@see #setSelectedValues(V[])
	@see #addSelectedIndexes(int...)
	*/
	public void setSelectedIndexes(int... indexes) throws PropertyVetoException;
	
	/**Adds a selection at the given indices.
	Any invalid indices will be ignored.
	If the value change is vetoed by the installed validator, the validation exception will be accessible via {@link PropertyVetoException#getCause()}.
	@param indexes The indices to add to the selection.
	@throws PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	@see ListSelectionPolicy#getAddSelectedIndices(ListSelectModel, int[])
	@see #setSelectedIndexes(int[])
	*/
	public void addSelectedIndexes(int... indexes) throws PropertyVetoException;
	
	/**Removes a selection at the given indices.
	Any invalid indices will be ignored.
	If the value change is vetoed by the installed validator, the validation exception will be accessible via {@link PropertyVetoException#getCause()}.
	@param indexes The indices to remove from the selection.
	@throws PropertyVetoException if the provided value is not valid or the change has otherwise been vetoed.
	@see ListSelectionPolicy#getRemoveSelectedIndices(ListSelectModel, int[])
	@see #setSelectedIndexes(int[])
	*/
	public void removeSelectedIndexes(int... indexes) throws PropertyVetoException;

	/**Determines the displayed status of the first occurrence of a given value.
	@param value The value for which the displayed status is to be determined.
	@return <code>true</code> if the value is displayed, else <code>false</code>.
	@throws IndexOutOfBoundsException if the given value does not occur in the model.
	*/
	public boolean isValueDisplayed(final V value);

	/**Sets the displayed status of the first occurrence of a given value.
	This is a bound value state property.
	@param value The value to display.
	@param newDisplayed Whether the value should be displayed.
	@see #DISPLAYED_PROPERTY
	*/
	public void setValueDisplayed(final V value, final boolean newDisplayed);	//TODO update comments after property firing is fixed	//TODO fix property change event 

	/**Determines the displayed status of a given index.
	@param index The index of the value for which the displayed status is to be determined.
	@return <code>true</code> if the value at the given index is displayed, else <code>false</code>.
	*/
	public boolean isIndexDisplayed(final int index);
	
	/**Sets the displayed status of a given index.
	This is a bound value state property.
	@param index The index of the value to display.
	@param newDisplayed Whether the value at the given index should be displayed.
	@see #DISPLAYED_PROPERTY
	@throws IndexOutOfBoundsException if the given index is not within the range of the list.
	*/
	public void setIndexDisplayed(final int index, final boolean newDisplayed);	//TODO fix property change event 
	
	/**Determines the enabled status of the first occurrence of a given value.
	@param value The value for which the enabled status is to be determined.
	@return <code>true</code> if the value is enabled, else <code>false</code>.
	@throws IndexOutOfBoundsException if the given value does not occur in the model.
	*/
	public boolean isValueEnabled(final V value);

	/**Sets the enabled status of the first occurrence of a given value.
	This is a bound value state property.
	@param value The value to enable or disable.
	@param newEnabled Whether the value should be enabled.
	@see #ENABLED_PROPERTY
	*/
	public void setValueEnabled(final V value, final boolean newEnabled);	//TODO fix property change event 

	/**Determines the enabled status of a given index.
	@param index The index of the value for which the enabled status is to be determined.
	@return <code>true</code> if the value at the given index is enabled, else <code>false</code>.
	*/
	public boolean isIndexEnabled(final int index);
	
	/**Sets the enabled status of a given index.
	This is a bound value state property.
	@param index The index of the value to enable or disable.
	@param newEnabled Whether the value at the given index should be enabled.
	@see #ENABLED_PROPERTY
	@throws IndexOutOfBoundsException if the given index is not within the range of the list.
	*/
	public void setIndexEnabled(final int index, final boolean newEnabled);	//TODO fix property change event 

	/**Adds a list listener.
	@param listListener The list listener to add.
	*/
	public void addListListener(final ListListener<V> listListener);

	/**Removes a list listener.
	@param listListener The list listener to remove.
	*/
	public void removeListListener(final ListListener<V> listListener);

	/**Adds a list selection listener.
	@param selectionListener The selection listener to add.
	*/
	public void addListSelectionListener(final ListSelectionListener<V> selectionListener);

	/**Removes a list selection listener.
	@param selectionListener The selection listener to remove.
	*/
	public void removeListSelectionListener(final ListSelectionListener<V> selectionListener);

}
