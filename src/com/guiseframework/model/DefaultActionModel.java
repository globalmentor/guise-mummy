package com.guiseframework.model;

import java.util.Iterator;

import com.guiseframework.event.*;

/**A default implementation of a button model.
@author Garret Wilson
*/
public class DefaultActionModel extends AbstractModel implements ActionModel
{

	/**Adds an action listener.
	@param actionListener The action listener to add.
	*/
	public void addActionListener(final ActionListener actionListener)
	{
		getEventListenerManager().add(ActionListener.class, actionListener);	//add the listener
	}

	/**Removes an action listener.
	@param actionListener The action listener to remove.
	*/
	public void removeActionListener(final ActionListener actionListener)
	{
		getEventListenerManager().remove(ActionListener.class, actionListener);	//remove the listener
	}

	/**@return all registered action listeners.*/
	@SuppressWarnings("unchecked")
	public Iterator<ActionListener> getActionListeners()
	{
		return (Iterator<ActionListener>)(Object)getEventListenerManager().getListeners(ActionListener.class);	//remove the listener TODO find out why we have to use the double cast for JDK 1.5 to compile
	}

	/**Fires an action to all registered action listeners.
	@see ActionListener
	@see ActionEvent
	*/
	public void fireAction()
	{
		if(getEventListenerManager().hasListeners(ActionListener.class))	//if there are action listeners registered
		{
			final ActionEvent actionEvent=new ActionEvent(this);	//create a new action event
			getSession().queueEvent(new PostponedActionEvent(getEventListenerManager(), actionEvent));	//tell the Guise session to queue the event
		}
	}

	/**Default constructor.*/
	public DefaultActionModel()
	{
		super();	//construct the parent class
	}

}
