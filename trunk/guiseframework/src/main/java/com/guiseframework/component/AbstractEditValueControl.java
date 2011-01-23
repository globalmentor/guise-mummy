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

import com.globalmentor.beans.*;
import com.guiseframework.event.*;
import com.guiseframework.model.*;

/**Abstract value control that is an edit component.
@param <V> The type of value to represent.
@author Garret Wilson
*/
public abstract class AbstractEditValueControl<V> extends AbstractValueControl<V> implements EditComponent
{

	/**Whether the value is editable and the control will allow the the user to change the value.*/
	private boolean editable=true;

		/**@return Whether the value is editable and the control will allow the the user to change the value.*/
		public boolean isEditable() {return editable;}

		/**Sets whether the value is editable and the control will allow the the user to change the value.
		This is a bound property of type <code>Boolean</code>.
		@param newEditable <code>true</code> if the control should allow the user to change the value.
		@see #EDITABLE_PROPERTY
		*/
		public void setEditable(final boolean newEditable)
		{
			if(editable!=newEditable)	//if the value is really changing
			{
				final boolean oldEditable=editable;	//get the old value
				editable=newEditable;	//actually change the value
				firePropertyChange(EDITABLE_PROPERTY, Boolean.valueOf(oldEditable), Boolean.valueOf(newEditable));	//indicate that the value changed
			}			
		}

	/**Info model, value model, and enableable constructor.
	@param infoModel The component info model.
	@param valueModel The component value model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given info model, value model, and/or enableable object is <code>null</code>.
	*/
	public AbstractEditValueControl(final InfoModel infoModel, final ValueModel<V> valueModel, final Enableable enableable)
	{
		super(infoModel, valueModel, enableable);	//construct the parent class
		addPropertyChangeListener(VALUE_PROPERTY, new AbstractGenericPropertyChangeListener<V>()	//listen for the value changing, and when the value changes fire an edit event
				{
					public void propertyChange(final GenericPropertyChangeEvent<V> propertyChangeEvent)	//when the value changes
					{
						fireEdited();	//indicate that editing has occurred
					}
				});
	}

		//EditComponent implementation

	/**Adds an edit listener.
	@param editListener The edit listener to add.
	*/
	public void addEditListener(final EditListener editListener)
	{
		getEventListenerManager().add(EditListener.class, editListener);	//add the listener
	}

	/**Removes an edit listener.
	@param editListener The edit listener to remove.
	*/
	public void removeEditListener(final EditListener editListener)
	{
		getEventListenerManager().remove(EditListener.class, editListener);	//remove the listener
	}

	/**Fires an edit event to all registered edit listeners.
	This method delegates to {@link #fireEdited(EditEvent)}.
	@see EditListener
	@see EditEvent
	*/
	protected void fireEdited()
	{
		if(getEventListenerManager().hasListeners(EditListener.class))	//if there are edit listeners registered
		{
			fireEdited(new EditEvent(this));	//create and fire a new edit event
		}
	}

	/**Fires a given edit event to all registered edit listeners.
	@param editEvent The edit event to fire.
	*/
	protected void fireEdited(final EditEvent editEvent)
	{
		for(final EditListener editListener:getEventListenerManager().getListeners(EditListener.class))	//for each edit listener
		{
			editListener.edited(editEvent);	//dispatch the edit event to the listener
		}
	}

}
