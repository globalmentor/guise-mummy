package com.guiseframework.coupler;

import static com.globalmentor.java.Classes.*;

import java.beans.PropertyVetoException;

import com.guiseframework.component.*;
import com.guiseframework.event.*;
import com.guiseframework.model.*;

/**Associates an action control with a card in a card control.
When the action is initiated, the first displayed and enabled specified card within the card control will be selected.
When any of the the associated cards are selected, if the action implements {@link Selectable} the action will be selected.
If the card's constraints implement {@link Displayable}, the action will be displayed based upon the card constraints' displayed status.
If the card's constraints implement {@link Enableable}, the action will be enabled based upon the card constraints' enabled status.
If a card's constraints implement {@link TaskCardConstraints} and the action implements {@link ActionValueControl} and represents a {@link TaskState} value,
	the action's contained value will reflect any changes in the card constraints task state.
If the action implements {@link SelectActionControl} its auto-select status will be turned off when installed.
This coupler is only functional when the given card is contained within a {@link CardControl}.
@author Garret Wilson
*/
public class ActionCardCoupler extends AbstractCardCoupler	//TODO now that constraints throw repeated events, see if we need to do this complicated listening for constraints
{

	/**The bound property of the action.*/
	public final static String ACTION_PROPERTY=getPropertyName(ActionCardCoupler.class, "action");

	/**The action listener to listen for the action event.*/
	private final ActionListener actionListener=new ActionListener()
			{
				public void actionPerformed(final ActionEvent actionEvent)	//if the action occurs
				{
					try
					{
						selectCard();	//select a connected card
					}
					catch(final PropertyVetoException propertyVetoException)	//if the card can't be selected, just ignore the error and assume that the card control reported the error
					{
					}
				}
			};

	/**The action to connect to the card, or <code>null</code> if the action is not coupled with a card.*/
	private ActionControl action=null;

		/**@return The action to connect to the card, or <code>null</code> if the action is not coupled with a card.*/
		public ActionControl getAction() {return action;}

		/**Sets the connected action.
		This is a bound property.
		@param newAction The new action to connect to the card, or <code>null</code> if the action should not be coupled with a card.
		@see #ACTION_PROPERTY
		*/
		public void setAction(final ActionControl newAction)
		{
			if(action!=newAction)	//if the value is really changing
			{
				final ActionControl oldAction=action;	//get the old value
				if(oldAction!=null)	//if there is an old action
				{
					oldAction.removeActionListener(actionListener);	//stop listening for actions
				}
				action=newAction;	//actually change the value
				if(action instanceof SelectActionControl)	//if the action is a select action control
				{
					((SelectActionControl)action).setAutoSelect(false);	//turn off its auto-select status, because we will be controlling when it is selected based upon the connected card
				}
				if(newAction!=null)	//if there is a new action
				{
					newAction.addActionListener(actionListener);	//listen for actions
				}
				firePropertyChange(ACTION_PROPERTY, oldAction, newAction);	//indicate that the value changed
					//TODO replace all this with some sort of update() method in the abstract class
				updateSelected();	//update the control selection based upon the selected card
				updateDisplayed();	//update the displayed status based upon the selected card
				updateEnabled();	//update the enabled status based upon the selected card
				updateTaskState();	//update the task state based upon the selected card
			}			
		}

	/**Default constructor.*/
	public ActionCardCoupler()
	{
		this(null);	//construct the class with no action or card
	}
	
	/**Action and cards constructor.
	@param actionControl The new action to connect to the card, or <code>null</code> if the action should not be coupled with a card.
	@param cards The new cards to connect, if any.
	*/
	public ActionCardCoupler(final ActionControl actionControl, final Component... cards)
	{
		super(cards);	//construct the parent class
		setAction(actionControl);	//set the action control
	}
	
	/**Updates the current displayed status.
	This implementation updates the action's displayed status.
	If no action is connected, no action occurs.
	@param displayed The new displayed status.
	*/
	protected void updateDisplayed(final boolean displayed)
	{
		final ActionControl action=getAction();	//get the action
		if(action!=null)	//if there is an action
		{
			action.setDisplayed(displayed);	//update the action's displayed status
		}
	}

	/**Updates the current enabled status.
	This implementation updates the action's enabled status.
	If no action is connected, no action occurs.
	@param enabled The new enabled status.
	*/
	protected void updateEnabled(final boolean enabled)
	{
		final ActionControl action=getAction();	//get the action
		if(action!=null)	//if there is an action
		{
			action.setEnabled(enabled);	//update the action's enabled status
		}
	}

	/**Updates the current task state.
	If there is a connected action that implements {@link ActionValueControl} containing a {@link TaskState} value, the action's value will be updated.
	If no action is connected, no action occurs.
	@param taskState The new task state, or <code>null</code> if there is no task state.
	*/
	@SuppressWarnings("unchecked")	//we check the type of value class contained in any ActionValueControl, so our cast is logically correct
	protected void updateTaskState(final TaskState taskState)
	{
		final ActionControl action=getAction();	//get the action
		if(action instanceof ActionValueControl)	//if the action is also a value control
		{
			final ActionValueControl<?> actionValueControl=(ActionValueControl<?>)action;	//get the action as an action value control
			if(TaskState.class.isAssignableFrom(actionValueControl.getValueClass()))	//if the action represents a task state
			{
				try
				{
					((ActionValueControl<TaskState>)actionValueControl).setValue(taskState);	//update the action with the new task state
				}
				catch(final PropertyVetoException propertyVetoException)
				{
//TODO improve					throw new AssertionError(validationException);	//TODO improve
				}
			}
		}
	}
		
	/**Updates the current selected status.
	If there is a connected action that implements {@link Selectable}, its selected status will be updated.
	If no action is connected, no action occurs.
	@param selected The new selected status.
	*/
	protected void updateSelected(final boolean selected)
	{
		final ActionControl action=getAction();	//get the action
		if(action instanceof Selectable)	//if the action is selectable
		{
			((Selectable)action).setSelected(selected);	//update its selected status
		}
	}
	
	
}
