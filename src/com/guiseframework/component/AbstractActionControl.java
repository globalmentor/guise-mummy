package com.guiseframework.component;

import java.util.Iterator;
import java.util.MissingResourceException;

import com.guiseframework.GuiseSession;
import com.guiseframework.event.*;
import com.guiseframework.model.Model;

/**Abstract control with an action model.
@author Garret Wilson
*/
public abstract class AbstractActionControl<C extends ActionControl<C>> extends AbstractControl<C> implements ActionControl<C>
{

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param model The component data model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractActionControl(final GuiseSession session, final String id, final Model model)
	{
		super(session, id, model);	//construct the parent class
	}

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
			final ActionEvent actionEvent=new ActionEvent(getSession(), this);	//create a new action event
			getSession().queueEvent(new PostponedActionEvent(getEventListenerManager(), actionEvent));	//tell the Guise session to queue the event
		}
	}
}
