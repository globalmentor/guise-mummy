package com.guiseframework.component;

import com.garretwilson.beans.*;

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

	/**Label model, value model, and enableable constructor.
	@param labelModel The component label model.
	@param valueModel The component value model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model, value model, and/or enableable object is <code>null</code>.
	*/
	public AbstractEditValueControl(final LabelModel labelModel, final ValueModel<V> valueModel, final Enableable enableable)
	{
		super(labelModel, valueModel, enableable);	//construct the parent class
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
		final EventListenerManager eventListenerManager=getEventListenerManager();	//get event listener support
		if(eventListenerManager.hasListeners(EditListener.class))	//if there are edit listeners registered
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