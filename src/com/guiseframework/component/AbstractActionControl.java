package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import java.util.Iterator;

import com.guiseframework.GuiseSession;
import com.guiseframework.event.*;
import com.guiseframework.model.ActionModel;

/**Abstract control with an action model.
@author Garret Wilson
*/
public abstract class AbstractActionControl<C extends ActionControl<C>> extends AbstractControl<C> implements ActionControl<C>
{

	/**The action model used by this component.*/
	private final ActionModel actionModel;

		/**@return The action model used by this component.*/
		protected ActionModel getActionModel() {return actionModel;}

	/**Session, ID, and model constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param actionModel The component action model.
	@exception NullPointerException if the given session and/or model is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public AbstractActionControl(final GuiseSession session, final String id, final ActionModel actionModel)
	{
		super(session, id);	//construct the parent class
		this.actionModel=checkNull(actionModel, "Action model cannot be null.");	//save the action model
		this.actionModel.addActionListener(new ActionListener()	//create an action repeater to forward events to this component's listeners
				{
					public void actionPerformed(final ActionEvent actionEvent)	//if the action is performed
					{
						fireAction();	//fire an action with this component as the source
					}
				});
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
