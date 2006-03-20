package com.guiseframework.coupler;

import static com.garretwilson.lang.ClassUtilities.*;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.Constraints;
import com.guiseframework.component.layout.TaskCardConstraints;
import com.guiseframework.event.*;
import com.guiseframework.model.*;
import com.guiseframework.validator.ValidationException;

/**Associates an action control with a card in a card control.
When the action is initiated, the specified card within the card control will be selected.
When the associated card is selected, if the action implements {@link Selectable} the action will be selected.
If the card's constraints implement {@link Displayable}, the action will be displayed based upon the card constraints' displayed status.
If the card's constraints implement {@link Enableable}, the action will be enabled based upon the card constraints' enabled status.
If a card's constraints implement {@link TaskCardConstraints} and the action implements {@link ActionValueControl} and represents a {@link TaskStatus} value,
	the action's contained value will reflect any changes in the card constraints task status.
If the action implements {@link SelectActionControl} its auto-select status will be turned off when installed.
This coupler is only functional when the given card is contained within a {@link CardControl}.
@author Garret Wilson
*/
public class ActionCardCoupler extends GuiseBoundPropertyObject	//TODO listen for the card control changing
{

	/**The bound property of the action.*/
	public final static String ACTION_PROPERTY=getPropertyName(ActionCardCoupler.class, "action");
	/**The bound property of the card.*/
	public final static String CARD_PROPERTY=getPropertyName(ActionCardCoupler.class, "card");

	/**The action listener to listen for the action event.*/
	private final ActionListener actionListener=new ActionListener()
			{
				public void actionPerformed(final ActionEvent actionEvent)	//if the action occurs
				{
					selectCard();	//select the card, if any
				}
			};

	/**The property change listener to listen for the selected card changing.*/
	private final GuisePropertyChangeListener<Component<?>> selectedCardChangeListener=new AbstractGuisePropertyChangeListener<Component<?>>()
		{
			public void propertyChange(final GuisePropertyChangeEvent<Component<?>> propertyChangeEvent)	//if the selected card changes
			{
				updateSelectedCard();	//update the selected card
			}		
		};

	/**The property change listener to listen for the card displayed status changing and change the action accordingly.*/
	private final GuisePropertyChangeListener<Boolean> displayedChangeListener=new AbstractGuisePropertyChangeListener<Boolean>()
		{
			public void propertyChange(final GuisePropertyChangeEvent<Boolean> propertyChangeEvent)	//if the displayed status changes
			{
				updateDisplayed(propertyChangeEvent.getNewValue().booleanValue());	//update the action with the new displayed status
			}
		};

	/**The property change listener to listen for the card enabled status changing and reflect that value in the action.*/
	private final GuisePropertyChangeListener<Boolean> enabledChangeListener=new AbstractGuisePropertyChangeListener<Boolean>()
		{
			public void propertyChange(final GuisePropertyChangeEvent<Boolean> propertyChangeEvent)	//if the enabled status changes
			{
				updateEnabled(propertyChangeEvent.getNewValue().booleanValue());	//update the action with the new enabled status
			}
		};

	/**The property change listener to listen for the card task status changing and reflect that value in the action.*/
	private final GuisePropertyChangeListener<TaskStatus> taskStatusChangeListener=new AbstractGuisePropertyChangeListener<TaskStatus>()
		{
			public void propertyChange(final GuisePropertyChangeEvent<TaskStatus> propertyChangeEvent)	//if the task status changes
			{
				updateTaskStatus(propertyChangeEvent.getNewValue());	//update the action with the new task status
			}
		};

	/**The property change listener to listen for card constraints changing.*/
	private final GuisePropertyChangeListener<Constraints> constraintsChangeListener=new AbstractGuisePropertyChangeListener<Constraints>()
		{
			public void propertyChange(final GuisePropertyChangeEvent<Constraints> propertyChangeEvent)	//if the constrants change
			{
				setCardConstraints(propertyChangeEvent.getNewValue());	//update our copy of the constraints
			}		
		};

	/**The action to connect to the card, or <code>null</code> if the action is not coupled with a card.*/
	private ActionControl<?> action=null;

		/**@return The action to connect to the card, or <code>null</code> if the action is not coupled with a card.*/
		public ActionControl<?> getAction() {return action;}

		/**Sets the connected action.
		This is a bound property.
		@param newAction The new action to connect to the card, or <code>null</code> if the action should not be coupled with a card.
		@see #ACTION_PROPERTY
		*/
		public void setAction(final ActionControl<?> newAction)
		{
			if(action!=newAction)	//if the value is really changing
			{
				final ActionControl<?> oldAction=action;	//get the old value
				if(oldAction!=null)	//if there is an old action
				{
					oldAction.removeActionListener(actionListener);	//stop listening for actions
				}
				action=newAction;	//actually change the value
				if(action instanceof SelectActionControl)	//if the action is a select action control
				{
					((SelectActionControl<?>)action).setAutoSelect(false);	//turn off its auto-select status, because we will be controlling when it is selected based upon the connected card
				}
				if(newAction!=null)	//if there is a new action
				{
					newAction.addActionListener(actionListener);	//listen for actions
				}
				firePropertyChange(ACTION_PROPERTY, oldAction, newAction);	//indicate that the value changed
				updateSelectedCard();	//update the action control based upon the selected card
				updateDisplayed();	//update the displayed status based upon the selected card
				updateEnabled();	//update the enabled status based upon the selected card
				updateTaskStatus();	//update the task status based upon the selected card
			}			
		}

	/**The card to connect to the action, or <code>null</code> if the action is not coupled with a card.*/
	private Component<?> card=null;

		/**@return The card to connect to the action, or <code>null</code> if the action is not coupled with a card.*/
		public Component<?> getCard() {return card;}

		/**Sets the connected card.
		This is a bound property.
		@param newCard The new card to connect to the action, or <code>null</code> if the action should not be coupled with a card.
		@see #CARD_PROPERTY
		*/
		public void setCard(final Component<?> newCard)
		{
			if(card!=newCard)	//if the value is really changing
			{
				final Component<?> oldCard=card;	//get the old value
				if(oldCard!=null)	//if there was an old card
				{
					oldCard.removePropertyChangeListener(Component.CONSTRAINTS_PROPERTY, constraintsChangeListener);	//stop listening for the component changing constraints
				}				
				card=newCard;	//actually change the value
				if(newCard!=null)	//if there is a new card
				{
					newCard.addPropertyChangeListener(Component.CONSTRAINTS_PROPERTY, constraintsChangeListener);	//listen for the component changing constraints
				}
				final CompositeComponent<?> newParent=newCard!=null ? newCard.getParent() : null;	//get the new card's parent, if there is a new card
				final CardControl<?> newCardControl=newParent instanceof CardControl ? (CardControl<?>)newParent : null;	//get the new card control, if any
				setCardControl(newCardControl);	//change the card control if needed
				setCardConstraints(newCard!=null ? newCard.getConstraints() : null);	//update the card constraints
				firePropertyChange(CARD_PROPERTY, oldCard, newCard);	//indicate that the value changed
				updateSelectedCard();	//update the action control based upon the selected card
				updateDisplayed();	//update the displayed status based upon the selected card
				updateEnabled();	//update the enabled status based upon the selected card
				updateTaskStatus();	//update the task status based upon the selected card
			}			
		}

	/**A convenience reference to the connected card's parent card control, if any.*/
	private CardControl<?> cardControl=null;

		/**@return A convenience reference to the connected card's parent card control, if any.*/
		protected CardControl<?> getCardControl() {return cardControl;}

		/**Sets the convenience reference to the card control.
		@param newCardControl The new card control, or <code>null</code> if the card does not have a parent card control.
		*/
		private void setCardControl(final CardControl<?> newCardControl)
		{
			if(cardControl!=newCardControl)	//if the value is really changing
			{
				final CardControl<?> oldCardControl=cardControl;	//get the old value
				if(oldCardControl!=null)	//if there was an old card control
				{
					oldCardControl.removePropertyChangeListener(CardControl.VALUE_PROPERTY, selectedCardChangeListener);	//stop listening for selected card changes in the old card control
				}
				cardControl=newCardControl;	//update the card control reference in case it changed				
				if(newCardControl!=null)	//if there is a new card control
				{
					newCardControl.addPropertyChangeListener(CardControl.VALUE_PROPERTY, selectedCardChangeListener);	//listen for selected card changes in the new card control
				}
			}			
		}

	/**A convenience reference to the connected card's constraints, if any.*/
	private Constraints cardConstraints=null;

		/**@return A convenience reference to the connected card's constraints, if any.*/
		protected Constraints getCardConstraints() {return cardConstraints;}

		/**Sets the convenience reference to the card constraints.
		@param newCardConstraints The new card constraints, or <code>null</code> if the card does not have constraints.
		*/
		private void setCardConstraints(final Constraints newCardConstraints)
		{
			if(cardConstraints!=newCardConstraints)	//if the value is really changing
			{
				final Constraints oldCardConstraints=cardConstraints;	//get the old value
				if(oldCardConstraints instanceof Displayable)	//if the old constraints were displayable
				{
					oldCardConstraints.removePropertyChangeListener(Displayable.DISPLAYED_PROPERTY, displayedChangeListener);	//stop listening for changes in displayed status
				}
				if(oldCardConstraints instanceof Enableable)	//if the old constraints were enableable
				{
					oldCardConstraints.removePropertyChangeListener(Enableable.ENABLED_PROPERTY, enabledChangeListener);	//stop listening for changes in enabled status
				}
				if(oldCardConstraints instanceof TaskCardConstraints)	//if the old constraints were task card constraints
				{
					oldCardConstraints.removePropertyChangeListener(TaskCardConstraints.TASK_STATUS_PROPERTY, taskStatusChangeListener);	//stop listening for changes in task status
				}
				cardConstraints=newCardConstraints;	//update the value				
				if(newCardConstraints instanceof Displayable)	//if the new constraints are displayable
				{
					newCardConstraints.addPropertyChangeListener(Displayable.DISPLAYED_PROPERTY, displayedChangeListener);	//listen for changes in displayed status 
				}
				if(newCardConstraints instanceof Enableable)	//if the new constraints are enableable
				{
					newCardConstraints.addPropertyChangeListener(Enableable.ENABLED_PROPERTY, enabledChangeListener);	//listen for changes in enabled status 
				}
				if(newCardConstraints instanceof TaskCardConstraints)	//if the new constraints are task card constraints
				{
					newCardConstraints.addPropertyChangeListener(TaskCardConstraints.TASK_STATUS_PROPERTY, taskStatusChangeListener);	//listen for changes in task status 
				}
			}			
		}

	/**Session constructor.
	@param session The Guise session that owns these constraints.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public ActionCardCoupler(final GuiseSession session)
	{
		this(session, null, null);	//construct the class with no action or card
	}
	
	/**Session, action, and card constructor.
	@param session The Guise session that owns these constraints.
	@param actionControl The new action to connect to the card, or <code>null</code> if the action should not be coupled with a card.
	@param card The new card to connect to the action, or <code>null</code> if the action should not be coupled with a card.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public ActionCardCoupler(final GuiseSession session, final ActionControl<?> actionControl, final Component<?> card)
	{
		super(session);	//construct the parent class
		setAction(actionControl);	//set the action control
		setCard(card);	//set the card
	}

	/**Selects the connected card.
	If no card is connected or the card has no parent card control, no action occurs.
	This method calls {@link #selectCard(CardControl, Component)}.
	*/
	protected void selectCard()
	{
		final Component<?> card=getCard();	//get the connected card
		final CardControl<?> cardControl=getCardControl();	//get the connected card card control
		if(card!=null && cardControl!=null)	//if a card and a card control are both connected
		{
			selectCard(cardControl, card);	//select the card
		}
	}

	/**Selects the connected card.
	@param The card control to use in selected the card.
	@param card The card to select.
	*/
	protected void selectCard(final CardControl<?> cardControl, final Component<?> card)
	{
		try
		{
			cardControl.setValue(card);	//select the card
		}
		catch(final ValidationException validationException)	//if the card can't be selected, just ignore the error and assume that the card control reported the error
		{
		}
	}

	/**Updates the action with the current displayed status based upon the constraints of the connected card.
	If no action control is connected to this coupler, no action occurs.
	If no card with {@link Displayable} constraints is connected to this coupler, no action occurs.
	This method calls {@link #updateDisplayed(boolean)}.
	*/
	protected void updateDisplayed()
	{
		final Constraints constraints=getCardConstraints();	//get the card constraints, if any
		if(constraints instanceof Displayable)	//if the constraints are displayable
		{
			updateEnabled(((Displayable)constraints).isDisplayed());	//update the displayed status of the action
		}
	}

	/**Updates the action with the current displayed status.
	If no action control is connected to this coupler, no action occurs.
	@param displayed The new displayed status.
	*/
	protected void updateDisplayed(final boolean displayed)
	{
		final ActionControl<?> action=getAction();	//get the action
		if(action!=null)	//if there is an action
		{
			action.setDisplayed(displayed);	//update the action's displayed status
		}
	}

	/**Updates the action with the current enabled status based upon the constraints of the connected card.
	If no action control is connected to this coupler, no action occurs.
	If no card with {@link Enableable} constraints is connected to this coupler, no action occurs.
	This method calls {@link #updateEnabled(boolean)}.
	*/
	protected void updateEnabled()
	{
		final Constraints constraints=getCardConstraints();	//get the card constraints, if any
		if(constraints instanceof Enableable)	//if the constraints are enableable
		{
			updateEnabled(((Enableable)constraints).isEnabled());	//update the enabled status of the action
		}
	}

	/**Updates the action with the current enabled status.
	If no action control is connected to this coupler, no action occurs.
	@param enabled The new enabled status.
	*/
	protected void updateEnabled(final boolean enabled)
	{
		final ActionControl<?> action=getAction();	//get the action
		if(action!=null)	//if there is an action
		{
			action.setEnabled(enabled);	//update the action's enabled status
		}
	}
	
	/**Updates the action with the current task status based upon the constraints of the connected card.
	If no value action control representing a task status is connected to this coupler, no action occurs.
	If no card with constraints of {@link TaskCardConstraints} is connected to this coupler, no action occurs.
	This method calls {@link #updateTaskStatus(TaskStatus)}.
	*/
	protected void updateTaskStatus()
	{
		final Constraints constraints=getCardConstraints();	//get the card constraints, if any
		if(constraints instanceof TaskCardConstraints)	//if the constraints indicate task status
		{
			updateTaskStatus(((TaskCardConstraints)constraints).getTaskStatus());	//update the task status of the action
		}
	}

	/**Updates the action with the current task status.
	If no value action control representing a task status is connected to this coupler, no action occurs.
	@param taskStatus The new task status, or <code>null</code> if there is no task status.
	*/
	@SuppressWarnings("unchecked")	//we check the type of value class contained in any ActionValueControl, so our cast is logically correct
	protected void updateTaskStatus(final TaskStatus taskStatus)
	{
		final ActionControl<?> action=getAction();	//get the action
		if(action instanceof ActionValueControl)	//if the action is also a value control
		{
			final ActionValueControl<?, ?> actionValueControl=(ActionValueControl<?, ?>)action;	//get the action as an action value control
			if(TaskStatus.class.isAssignableFrom(actionValueControl.getValueClass()))	//if the action represents a task status
			{
				try
				{
					((ActionValueControl<TaskStatus, ?>)actionValueControl).setValue(taskStatus);	//update the action with the new task status
				}
				catch(final ValidationException validationException)
				{
					throw new AssertionError(validationException);	//TODO improve
				}
			}
		}
	}

	/**Updates the action with the currently selected card.
	If no action control or no card control is connected to this coupler, or the card has no parent card control, no action occurs.
	This method calls {@link #updateSelectedCard(Component)}.
	*/
	protected void updateSelectedCard()
	{
		final ActionControl<?> action=getAction();	//get the action
		final Component<?> card=getCard();	//get the connected card
		final CardControl<?> cardControl=getCardControl();	//get the card control
		if(action!=null && card!=null && cardControl!=null)	//if there is an action and card connected, and the card has a parent card control
		{
			updateSelectedCard(cardControl.getSelectedValue());	//update the selected card based upon the card control's value
		}
	}

	/**Called when a card is selected.
	This method is only called when both an action control and a card is connected to this coupler.
	If the associated action implements {@link Selectable}, it will be set as selected or unselected based upon the new selected card.
	@param selectedCard The new card selected, or <code>null</code> if no card is selected.
	*/
	protected void updateSelectedCard(final Component<?> selectedCard)
	{
		final ActionControl<?> action=getAction();	//get the action
		//TODO probably revert to checking action and card specifically
		assert action!=null : "Expected non-null action control";
		final Component<?> card=getCard();	//get the connected card
		assert card!=null : "Expected non-null card";
		if(action instanceof Selectable)	//if the action is selectable
		{
			((Selectable)action).setSelected(card==selectedCard);	//update its selected status based upon whether the newly selected card is the connected card
		}
	}

}
