package com.guiseframework.coupler;

import static com.garretwilson.lang.ClassUtilities.*;

import static java.util.Arrays.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Collections.*;

import com.garretwilson.lang.ObjectUtilities;
import com.guiseframework.component.*;
import com.guiseframework.component.layout.*;
import com.guiseframework.event.*;
import com.guiseframework.model.*;
import com.guiseframework.validator.ValidationException;

/**Abstract coupler to one or more cards in a {@link CardControl}.
This coupler is only functional when the given card is contained within a {@link CardControl}.
This coupler can behave as if a single card or multiple cards are connected, firing both the {@link #CARD_PROPERTY} and {@link #CARDS_PROPERTY} property change events when cards are changed.
If the card change results in the same card in the first position in the list, the {@link #CARD_PROPERTY} is not fired.
@author Garret Wilson
*/
public class AbstractCardCoupler extends GuiseBoundPropertyObject	//TODO listen for the card control changing
{

	/**The bound property of the connected card.*/
	public final static String CARD_PROPERTY=getPropertyName(AbstractCardCoupler.class, "card");
	/**The bound property of the connected cards.*/
	public final static String CARDS_PROPERTY=getPropertyName(AbstractCardCoupler.class, "cards");

	/**The flag indicating if we are currently updaing the selected state, so that we can prevent selecting an incorrect card if another card is being selected; access to this variable should be synchronized on this.*/
	private boolean updatingSelected=false;
	
	/**The property change listener to listen for the selected card changing.*/
	private final GuisePropertyChangeListener<Component<?>> selectedCardChangeListener=new AbstractGuisePropertyChangeListener<Component<?>>()
		{
			public void propertyChange(final GuisePropertyChangeEvent<Component<?>> propertyChangeEvent)	//if the selected card changes
			{
				updateSelected();	//update the selected status
			}		
		};

	/**The property change listener to listen for the card displayed status changing and change the action accordingly.*/
	private final GuisePropertyChangeListener<Boolean> displayedChangeListener=new AbstractGuisePropertyChangeListener<Boolean>()
		{
			public void propertyChange(final GuisePropertyChangeEvent<Boolean> propertyChangeEvent)	//if the displayed status changes
			{
				updateDisplayed();	//update the displayed status based upon all the cards
			}
		};

	/**The property change listener to listen for the card enabled status changing and reflect that value in the action.*/
	private final GuisePropertyChangeListener<Boolean> enabledChangeListener=new AbstractGuisePropertyChangeListener<Boolean>()
		{
			public void propertyChange(final GuisePropertyChangeEvent<Boolean> propertyChangeEvent)	//if the enabled status changes
			{
				updateEnabled();	//update the enabled status based upon all the cards
			}
		};

	/**The property change listener to listen for the card task status changing and reflect that value in the action.*/
	private final GuisePropertyChangeListener<TaskStatus> taskStatusChangeListener=new AbstractGuisePropertyChangeListener<TaskStatus>()
		{
			public void propertyChange(final GuisePropertyChangeEvent<TaskStatus> propertyChangeEvent)	//if the task status changes
			{
				updateTaskStatus();	//update the task status based upon all the cards
			}
		};

	/**The property change listener to listen for card constraints changing.*/
	private final GuisePropertyChangeListener<Constraints> constraintsChangeListener=new AbstractGuisePropertyChangeListener<Constraints>()
		{
			public void propertyChange(final GuisePropertyChangeEvent<Constraints> propertyChangeEvent)	//if the constrants change
			{
				final Constraints oldCardConstraints=propertyChangeEvent.getOldValue();	//get the old card constraints
				if(oldCardConstraints!=null)	//if there were old card constraints
				{
					uninstallCardConstraints(oldCardConstraints);	//uninstall the old card constraints
				}
				final Constraints newCardConstraints=propertyChangeEvent.getNewValue();	//get the new card constraints
				if(newCardConstraints!=null)	//if there are new card constraints
				{
					installCardConstraints(newCardConstraints);	//install the old card constraints
				}
			}		
		};

	/**The thread-safe list of connected cards.*/
	private List<Component<?>> cards=emptyList(); 
		
		/**@return The connected cards.*/
		public List<Component<?>> getCards() {return new ArrayList<Component<?>>(cards);}	//return a copy of the cards list 

		/**@return The first connected card, or <code>null</code> if there are no connected cards.*/
		public Component<?> getCard() {return !cards.isEmpty() ? cards.get(0) : null;}	//return the first card, if there is one 

		/**Sets the connected card.
		This is a bound property.
		@param newCard The new card to be connected.
		@see #CARD_PROPERTY
		*/
		public void setCard(final Component<?> newCard)
		{
			final List<Component<?>> newCards=new ArrayList<Component<?>>();	//create a new list
			newCards.add(newCard);	//add the card to the list
			setCards(newCards);	//set the cards
		}

		/**Sets the connected cards.
		This is a bound property.
		@param newCards The new cards to be connected.
		@exception NullPointerException if the given cards is <code>null</code>.
		@see #CARDS_PROPERTY
		*/
		public void setCards(final List<Component<?>> newCards)
		{			
			if(!ObjectUtilities.equals(cards, newCards))	//if the value is really changing
			{
				final List<Component<?>> oldCards=cards;	//get the old value
				for(final Component<?> oldCard:oldCards)	//for each old card
				{
					oldCard.removePropertyChangeListener(Component.CONSTRAINTS_PROPERTY, constraintsChangeListener);	//stop listening for the component changing constraints
					final Constraints oldCardConstraints=oldCard.getConstraints();	//get the old constraints
					if(oldCardConstraints!=null)	//if there were old card constraints
					{
						uninstallCardConstraints(oldCardConstraints);	//uninstall the old card constraints
					}
				}				
				cards=new CopyOnWriteArrayList<Component<?>>(newCards);	//actually change the value, making a thread-safe copy of the new list
				for(final Component<?> newCard:newCards)	//for each new card
				{
					newCard.addPropertyChangeListener(Component.CONSTRAINTS_PROPERTY, constraintsChangeListener);	//listen for the component changing constraints
					final Constraints newCardConstraints=newCard.getConstraints();	//get the new constraints
					if(newCardConstraints!=null)	//if there are new card constraints
					{
						installCardConstraints(newCardConstraints);	//install the old card constraints
					}
				}
				final CompositeComponent<?> newParent=!newCards.isEmpty() ? newCards.get(0).getParent() : null;	//get the first new card's parent, if there is a new card TODO make sure all parents are the same
				final CardControl<?> newCardControl=newParent instanceof CardControl ? (CardControl<?>)newParent : null;	//get the new card control, if any
				setCardControl(newCardControl);	//change the card control if needed
				final Component<?> oldCard=!oldCards.isEmpty() ? oldCards.get(0) : null;	//get the old card, if any
				final Component<?> newCard=!newCards.isEmpty() ? newCards.get(0) : null;	//get the new card, if any
				firePropertyChange(CARDS_PROPERTY, oldCards, newCards);	//indicate that the cards value changed
				firePropertyChange(CARD_PROPERTY, oldCard, newCard);	//indicate that the card value changed
				updateSelected();	//update the control selection based upon the selected card
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

	/**Installs appropriate listeners in a card's constraints.
	@param constraints The card constraints being installed.
	*/
	protected void installCardConstraints(final Constraints constraints)
	{
		if(constraints instanceof Displayable)	//if the new constraints are displayable
		{
			constraints.addPropertyChangeListener(Displayable.DISPLAYED_PROPERTY, displayedChangeListener);	//listen for changes in displayed status 
		}
		if(constraints instanceof Enableable)	//if the new constraints are enableable
		{
			constraints.addPropertyChangeListener(Enableable.ENABLED_PROPERTY, enabledChangeListener);	//listen for changes in enabled status 
		}
		if(constraints instanceof TaskCardConstraints)	//if the new constraints are task card constraints
		{
			constraints.addPropertyChangeListener(TaskCardConstraints.TASK_STATUS_PROPERTY, taskStatusChangeListener);	//listen for changes in task status 
		}		
	}

	/**Uninstalls appropriate listeners from a card's constraints.
	@param constraints The card constraints being uninstalled.
	*/
	protected void uninstallCardConstraints(final Constraints constraints)
	{
		if(constraints instanceof Displayable)	//if the old constraints were displayable
		{
			constraints.removePropertyChangeListener(Displayable.DISPLAYED_PROPERTY, displayedChangeListener);	//stop listening for changes in displayed status
		}
		if(constraints instanceof Enableable)	//if the old constraints were enableable
		{
			constraints.removePropertyChangeListener(Enableable.ENABLED_PROPERTY, enabledChangeListener);	//stop listening for changes in enabled status
		}
		if(constraints instanceof TaskCardConstraints)	//if the old constraints were task card constraints
		{
			constraints.removePropertyChangeListener(TaskCardConstraints.TASK_STATUS_PROPERTY, taskStatusChangeListener);	//stop listening for changes in task status
		}
	}

	/**Card constructor.
	@param cards The new cards to connect, if any.
	*/
	public AbstractCardCoupler(final Component<?>... cards)
	{
		setCards(asList(cards));	//set the cards
	}

	/**Selects the first connected card that is displayed and enabled.
	If no card is connected or the card has no parent card control, no action occurs.
	This method calls {@link #selectCard(CardControl, Component)}.
	@exception ValidationException if the appropriate card could not be selected.
	*/
	protected void selectCard() throws ValidationException
	{
		synchronized(this)	//prevent race conditions accessing updatingSelected
		{
			if(!updatingSelected)	//if we're not already updating the selected state (if we are, then this change could be in response to one of our cards being selected, and if we select a new card we could select the wrong one)
			{
				final CardControl<?> cardControl=getCardControl();	//get the connected card card control
				if(cardControl!=null)	//there a card control is connected
				{
					for(final Component<?> card:cards)	//for each card
					{
						final Constraints constraints=card.getConstraints();	//get the card constraints, if any
						if(!(constraints instanceof Enableable) || ((Enableable)constraints).isEnabled())	//if the constraints indicate enabled
						{
							if(!(constraints instanceof Displayable) || ((Displayable)constraints).isDisplayed())	//if the constraints indicate displayed
							{
								selectCard(cardControl, card);	//select the card
								break;	//only select the first card
							}
						}
					}
				}
			}
		}
	}

	/**Selects the specified card.
	@param The card control to use in selected the card.
	@param card The card to select.
	@exception ValidationException if the provided card could not be selected.
	*/
	protected void selectCard(final CardControl<?> cardControl, final Component<?> card) throws ValidationException
	{
		cardControl.setValue(card);	//select the card
	}	
	
	/**Performs any needed updates based upon the displayed status of the constraints of the connected cards.
	The new displayed status will be considered <code>true</code> unless the constraints of all connected cards implement {@link Displayable} and return <code>false</code> for {@link Displayable#isDisplayed()}.
	This method calls {@link #updateDisplayed(boolean)} with the result.
	*/
	protected void updateDisplayed()
	{
		boolean displayed=false;	//start by assuming the new status is undisplayed
		for(final Component<?> card:cards)	//for each card
		{
			final Constraints constraints=card.getConstraints();	//get the card constraints, if any
			if(!(constraints instanceof Displayable) || ((Displayable)constraints).isDisplayed())	//if the constraints indicate displayed
			{
				displayed=true;	//the new status is displayed
				break;	//only one displayed status is needed
			}
		}
		updateDisplayed(displayed);	//update the displayed status
	}

	/**Updates the current displayed status.
	This implementation does nothing.
	@param displayed The new displayed status.
	*/
	protected void updateDisplayed(final boolean displayed)
	{
	}

	/**Performs any needed updates based upon the enabled status of the constraints of the connected cards.
	The new enabled status will be considered <code>true</code> unless the constraints of all connected cards implement {@link Enableable} and return <code>false</code> for {@link Enableable#isEnabled()}.
	This method calls {@link #updateEnabled(boolean)} with the result.
	*/
	protected void updateEnabled()
	{
		boolean enabled=false;	//start by assuming the new status is disabled
		for(final Component<?> card:cards)	//for each card
		{
			final Constraints constraints=card.getConstraints();	//get the card constraints, if any
			if(!(constraints instanceof Enableable) || ((Enableable)constraints).isEnabled())	//if the constraints indicate enabled
			{
				enabled=true;	//the new status is enabled
				break;	//only one enabled status is needed
			}
		}
		updateEnabled(enabled);	//update the enabled status
	}

	/**Updates the current enabled status.
	This implementation does nothing.
	@param enabled The new enabled status.
	*/
	protected void updateEnabled(final boolean enabled)
	{
	}
	
	/**Performs any needed updates based upon the current task status of the constraints of the connected cards.
	This implementation uses the first available task status from the connected cards.
	This method calls {@link #updateTaskStatus(TaskStatus)}.
	*/
	protected void updateTaskStatus()
	{
		TaskStatus taskStatus=null;
		for(final Component<?> card:cards)	//for each card
		{
			final Constraints constraints=card.getConstraints();	//get the card constraints, if any
			if(constraints instanceof TaskCardConstraints)	//if the constraints indicate task status
			{
				taskStatus=((TaskCardConstraints)constraints).getTaskStatus();	//get the task status from the constraints
				break;	//use the first available task status
			}
		}
		updateTaskStatus(taskStatus);	//update the task status
	}

	/**Updates the current task status.
	This implementation does nothing.
	@param taskStatus The new task status, or <code>null</code> if there is no task status.
	*/
	protected void updateTaskStatus(final TaskStatus taskStatus)
	{
	}

	/**Performs any needed updates based upon the currently selected card.
	This implementation determines the selected status based upon whether the currently selected card in the connected card control is one of the connected cards.
	This method calls {@link #updateSelected(boolean)}.
	*/
	protected void updateSelected()
	{
		synchronized(this)	//prevent race conditions accessing updatingSelected
		{
			if(!updatingSelected)	//if we're not already updating the selected state
			{
				updatingSelected=true;	//show that we're updating our selected state
				try
				{
					boolean selected=false;	//start by assuming no connected card is selected
					final CardControl<?> cardControl=getCardControl();	//get the card control
					if(cardControl!=null)	//there is a parent card control
					{
						final Component<?> selectedCard=cardControl.getSelectedValue();	//get the selected card
						for(final Component<?> card:cards)	//for each card
						{
							if(card==selectedCard)	//if this connected cards is selected
							{
								selected=true;	//show that a connected card is selected
								break;	//it only takes one selected card do set the selected status
							}
						}
					}
					updateSelected(selected);	//update the selected status
				}
				finally
				{
					updatingSelected=false;	//always show that we are done updating the selected state
				}
			}
		}
	}

	/**Updates the current selected status.
	This implementation does nothing.
	@param selected The new selected status.
	*/
	protected void updateSelected(final boolean selected)
	{
	}

}
