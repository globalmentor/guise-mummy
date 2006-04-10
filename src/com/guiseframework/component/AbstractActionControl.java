package com.guiseframework.component;

import static com.garretwilson.lang.ObjectUtilities.*;

import com.guiseframework.event.*;
import com.guiseframework.model.*;

/**Abstract control with an action model.
@author Garret Wilson
*/
public abstract class AbstractActionControl<C extends ActionControl<C>> extends AbstractControl<C> implements ActionControl<C>
{

	/**The action model used by this component.*/
	private final ActionModel actionModel;

		/**@return The action model used by this component.*/
		protected ActionModel getActionModel() {return actionModel;}

	/**Action model constructor.
	@param actionModel The component action model.
	@exception NullPointerException if the given action model is <code>null</code>.
	*/
/*TODO del
	public AbstractActionControl(final ActionModel actionModel)
	{
		this.actionModel=checkInstance(actionModel, "Action model cannot be null.");	//save the action model
		this.actionModel.addActionListener(new ActionListener()	//create an action repeater to forward events to this component's listeners
				{
					public void actionPerformed(final ActionEvent actionEvent)	//if the action is performed
					{
						fireActionPerformed();	//fire an action with this component as the source
					}
				});
	}
*/

	/**Label model and enableable object constructor.
	@param labelModel The component label model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model and/or enableable is <code>null</code>.
	*/
/*TODO del
	public AbstractActionControl(final LabelModel labelModel, final Enableable enableable)
	{
		this(label)
	}
*/

	/**Default constructor.*/
	public AbstractActionControl()
	{
		this(new DefaultLabelModel(), new DefaultActionModel(), new DefaultEnableable());	//construct the class with default models
	}

	/**Label model, action model, and enableable object constructor.
	@param labelModel The component label model.
	@param actionModel The component action model.
	@param enableable The enableable object in which to store enabled status.
	@exception NullPointerException if the given label model, action model, and/or enableable object is <code>null</code>.
	*/
	public AbstractActionControl(final LabelModel labelModel, final ActionModel actionModel, final Enableable enableable)
	{
		super(labelModel, enableable);	//construct the parent class
		this.actionModel=checkInstance(actionModel, "Action model cannot be null.");	//save the action model
		this.actionModel.addActionListener(new ActionListener()	//create an action repeater to forward events to this component's listeners
				{
					public void actionPerformed(final ActionEvent actionEvent)	//if the action is performed
					{
						fireActionPerformed();	//fire an action with this component as the source
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
	public Iterable<ActionListener> getActionListeners()
	{
		return getEventListenerManager().getListeners(ActionListener.class);
	}

	/**Performs the action.
	This implementation delegates to the installed {@link ActionModel}.
	*/
	public void performAction()
	{
		getActionModel().performAction();	//delegate to the installed action model, which will fire an event which we will catch and queue for refiring
	}

	/**Fires an action event to all registered action listeners.
	This implementation queues a postponed action event.
	@see ActionListener
	@see ActionEvent
	*/
	protected void fireActionPerformed()
	{
		if(getEventListenerManager().hasListeners(ActionListener.class))	//if there are action listeners registered
		{
			final ActionEvent actionEvent=new ActionEvent(this);	//create a new action event
			getSession().queueEvent(new PostponedActionEvent(getEventListenerManager(), actionEvent));	//tell the Guise session to queue the event
		}
	}
}
