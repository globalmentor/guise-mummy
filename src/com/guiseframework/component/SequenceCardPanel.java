package com.guiseframework.component;

import static com.guiseframework.GuiseResourceConstants.*;

import java.io.IOException;
import java.util.List;

import com.garretwilson.util.Debug;
import com.guiseframework.Bookmark;
import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.CardLayout;
import com.guiseframework.component.layout.Constraints;
import com.guiseframework.component.layout.TaskCardConstraints;
import com.guiseframework.event.AbstractGuisePropertyChangeListener;
import com.guiseframework.event.GuisePropertyChangeEvent;
import com.guiseframework.model.Commitable;
import com.guiseframework.model.Enableable;
import com.guiseframework.model.Notification;
import com.guiseframework.model.TaskStatus;
import com.guiseframework.validator.AbstractValidator;
import com.guiseframework.validator.ValidationException;

/**A card panel representing a sequence of cards.
If any card has constraints of {@link TaskCardConstraints}, this class will update the task status based upon visited and validated status.
@author Garret Wilson
@see CardLayout
*/
public class SequenceCardPanel extends AbstractCardPanel<SequenceCardPanel> implements Commitable
{

	/**Session constructor.
	@param session The Guise session that owns this component.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public SequenceCardPanel(final GuiseSession session)
	{
		this(session, (String)null);	//construct the component, indicating that a default ID should be used
	}

	/**Session and ID constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	public SequenceCardPanel(final GuiseSession session, final String id)
	{
		this(session, id, new CardLayout(session));	//default to flowing vertically
	}

	/**Session and layout constructor.
	@param session The Guise session that owns this component.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session and/or layout is <code>null</code>.
	*/
	public SequenceCardPanel(final GuiseSession session, final CardLayout layout)
	{
		this(session, null, layout);	//construct the component with the layout, indicating that a default ID should be used
	}

	/**Session, ID, and layout constructor.
	@param session The Guise session that owns this component.
	@param id The component identifier, or <code>null</code> if a default component identifier should be generated.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given session and/or layout is <code>null</code>.
	@exception IllegalArgumentException if the given identifier is not a valid component identifier.
	*/
	protected SequenceCardPanel(final GuiseSession session, final String id, final CardLayout layout)
	{
		super(session, id, layout);	//construct the parent class
		setValidator(new SequenceCardValidator(session));	//TODO comment
		addPropertyChangeListener(VALUE_PROPERTY, new AbstractGuisePropertyChangeListener<Component<?>>()
				{
					public void propertyChange(final GuisePropertyChangeEvent<Component<?>> propertyChangeEvent)	//if the selected card changes
					{
						final Component<?> oldCard=propertyChangeEvent.getOldValue();
						final Component<?> newCard=propertyChangeEvent.getNewValue();	//TODO comment all this
						if(oldCard!=null)	//if there was an old card
						{
							final Constraints constraints=oldCard.getConstraints();
							if(constraints instanceof TaskCardConstraints)
							{
								final TaskCardConstraints taskCardConstraints=(TaskCardConstraints)constraints;
								if(taskCardConstraints.getTaskStatus()==TaskStatus.ERROR && oldCard.isValid())	//if there was an error but the old card is now valid
								{
									taskCardConstraints.setTaskStatus(TaskStatus.INCOMPLETE);
								}
								final int oldIndex=indexOf(oldCard);	//get the index of the old card
								assert oldIndex>=0 : "Expected old card to be present in the container.";
								final int newIndex=indexOf(newCard);	//see what index the new value has
								if(newIndex>oldIndex)	//if we advanced to a new card
								{
									taskCardConstraints.setTaskStatus(TaskStatus.COMPLETE);	//show that the old task is complete
								}
							}
						}						
						if(newCard!=null)	//if there is a new card
						{
							final Constraints constraints=newCard.getConstraints();
							if(constraints instanceof TaskCardConstraints)
							{
								final TaskCardConstraints taskCardConstraints=(TaskCardConstraints)constraints;
								if(taskCardConstraints.getTaskStatus()==null)
								{
									taskCardConstraints.setTaskStatus(TaskStatus.INCOMPLETE);
								}
							}
						}
					}		
				});
	}

	/**Called when the {@link Component#VALID_PROPERTY} of a child component changes.
	This version updates the error status of the child component's contraints if those constraints implement {@link TaskCardConstraints}.
	@param childComponent The child component the valid property of which changed.
	@param oldValid The old valid property.
	@param newValid The new valid property.
	*/
	protected void childComponentValidPropertyChanged(final Component<?> childComponent, final boolean oldValid, final boolean newValid)
	{
		super.childComponentValidPropertyChanged(childComponent, oldValid, newValid);	//call the parent version
		final Constraints constraints=childComponent.getConstraints();	//get the child component constraints
		if(constraints instanceof TaskCardConstraints)	//if these are task card constraints
		{
			final TaskCardConstraints taskCardConstraints=((TaskCardConstraints)constraints);	//get the constraints as task card constraints
			if(taskCardConstraints.getTaskStatus()!=null)	//if the task is started
			{
				taskCardConstraints.setTaskStatus(newValid ? TaskStatus.INCOMPLETE : TaskStatus.ERROR);	//update the task status based upon the new valid state
			}
		}
	}


	/**Sets the new selected card.
	This version validates the input on the currently selected card as needed.
	@param newValue The new selected card.
	@exception ValidationException if the provided value is not valid.
	@see #getValidator()
	@see #VALUE_PROPERTY
	*/
/*TODO del
	public void setValue(final Component<?> newValue) throws ValidationException
	{
		final Component<?> currentCard=getValue();	//get the currently selected card
		if(currentCard!=null)	//if there is a selected card, do validation if we need to
		{
			final int selectedIndex=indexOf(currentCard);	//get the index of the selected card
			assert selectedIndex>=0 : "Expected selected card to be present in the container.";
			final int newIndex=indexOf(newValue);	//see what index the new value has
			if(newIndex<0)	//if the new value isn't in the container TODO maybe put this in a default card panel validator
			{
				return;	//TODO decide what to do here
			}
			if(newIndex>selectedIndex)	//if we're advancing forward in the sequence
			{
				try
				{
					currentCard.validate();	//validate the currently selected card
				}
				catch(final ComponentExceptions componentException)	//if the current card doesn't validate
				{
					return;	//don't go forward TODO decide what to do here
				}									
			}
		}
		super.setValue(newValue);	//set the value normally
	}
*/

	/**Determines if there is a previous step in the sequence.
	This version returns <code>true</code> if there is a selected card and there exists a card before the selected card. 
	@return <code>true</code> if there is a previous step in the sequence.
	*/
	public boolean hasPrevious()
	{
		return getPrevious()!=null;	//see if there is a previous component
	}

	/**Determines the previous component in the sequence.
	Components that are not displayed or not enabled based upon their associated constraints are skipped.
	@return The previous component in the sequence, or <code>null</code> if there is no previous component in the sequence.
	*/
	public Component<?> getPrevious()
	{
		final Component<?> selectedComponent=getSelectedValue();	//get the selected component
		return selectedComponent!=null ? getPrevious(selectedComponent) : null;	//return the previous component of the selected component, or null if no component is selected
	}

	/**Determines the previous component in the sequence relative to the given component.
	Components that are not displayed or not enabled based upon their associated constraints are skipped.
	@param component The component the previous component to which should be found.
	@return The previous component in the sequence, or <code>null</code> if there is no previous component in the sequence.
	@exception NullPointerException if the given component is <code>null</code>.
	*/
	protected Component<?> getPrevious(final Component<?> component)
	{
			//TODO check for null and throw an exception instead of this lenient check
		final int selectedIndex=indexOf(component);	//get the index of the given component
		if(selectedIndex>=0)	//if a card is selected
		{
			for(int i=selectedIndex-1; i>=0; --i)	//for each previous card
			{
				final Component<?> card=get(i);	//get this card
				if(isDisplayed(card) && isEnabled(card))	//if the card is displayed and enabled
				{
					return card;	//return this card
				}
			}
		}
		return null;	//indicate that there is no previous card
	}

	/**Determines if there is a next step in the sequence.
	This version returns <code>true</code> if there is a selected card and there exists a card after the selected card. 
	@return <code>true</code> if there is a next step in the sequence.
	*/
	public boolean hasNext()
	{
		return getNext()!=null;	//see if there is a next component
	}

	/**Determines the next component in the sequence.
	Components that are not displayed or not enabled based upon their associated constraints are skipped.
	@return The next component in the sequence, or <code>null</code> if there is no next component in the sequence.
	*/
	public Component<?> getNext()
	{
		final int selectedIndex=getSelectedIndex();	//get the selected index
		if(selectedIndex>=0)	//if a card is selected
		{
			final int cardCount=size();	//find out how many cards there are
			for(int i=selectedIndex+1; i<cardCount; ++i)	//for each next card
			{
				final Component<?> card=get(i);	//get this card
				if(isDisplayed(card))	//if the card is displayed
//TODO decide; currently we need to get disabled cards so that we can enable them				if(isDisplayed(card) && isEnabled(card))	//if the card is displayed and enabled
				{
					return card;	//return this card
				}
			}
		}
		return null;	//indicate that there is no next card
	}

	/**Goes to the previous step in the sequence.
	If there is no previous step, no action occurs.
	This method calls {@link #hasPrevious()}.
	*/
	public void goPrevious()
	{
		final Component<?> previousCard=getPrevious();	//get the previous card
		if(previousCard!=null)	//if there is a previous card
		{
			try
			{
				final Component<?> selectedCard=getSelectedValue();	//get the selected card
				assert selectedCard!=null : "No card selected, even though hasPrevious() should have returned false if no card is selected.";
//				try
				{
//					selectedCard.validate();	//validate the selected card
					setValue(previousCard);	//select the previous card
				}
//				catch(final ComponentExceptions componentException)
				{
					//TODO improve; inform user
				}				
			}
			catch(final ValidationException validationException)
			{
//				throw new AssertionError(validationException);	//TODO improve
			}
		}
	}

	/**Advances to the next step in the sequence.
	If the current card passes validation, the next card is enabled before advancing.
	If there is no next step, no action occurs.
	This method calls {@link #getNext()}.
	*/
	public void goNext()
	{
		final Component<?> nextCard=getNext();	//get the next card
		if(nextCard!=null)	//if there is a next card
		{
			final Component<?> selectedCard=getSelectedValue();	//get the selected card
			assert selectedCard!=null : "No card selected, even though getNext() should have returned null if no card is selected.";
//TODO del Debug.trace("ready to validate selected card");
			if(validate())	//validate this panel; if everything, including the selected card, is valid
			{
				try
				{
					commit();	//commit this panel
					final Constraints nextCardConstraints=nextCard.getConstraints();	//get the next card's constraints
					if(nextCardConstraints instanceof Enableable)	//if the next card constraints is enableable
					{
						((Enableable)nextCardConstraints).setEnabled(true);	//enable the next card constraints
					}
					try
					{
						setValue(nextCard);	//select the next card
					}
					catch(final ValidationException validationException)
					{
	//					throw new AssertionError(validationException);	//TODO improve
					}
				}
				catch(final IOException ioException)	//if there is a problem commiting the result
				{
					getSession().notify(new Notification(ioException));	//notify the user
				}
			}
		}
	}

	/**Resets the sequence by navigating to the first card and disabling all subsequent cards.*/
	public void resetSequence()
	{
		resetValue();	//reset the value so that changing the index won't trigger validation
		if(size()>0)	//if there are cards in the sequence
		{
			try
			{
				setSelectedIndexes(0);	//browse to the first index
			}
			catch(final ValidationException validationException)
			{
				throw new AssertionError(validationException);	//TODO improve
			}
			final Component<?> selectedCard=getValue();	//get the selected card
			for(final Component<?> card:this)	//for each card
			{
				final Constraints constraints=card.getConstraints();	//get the card constraints
				if(constraints instanceof Enableable)	//if these constraints can be enabled or disabled
				{
					((Enableable)constraints).setEnabled(card==selectedCard);	//only the selected card should be enabled
				}
			}
		}		
	}

	/**Validates the user input of this component and all child components.
	The component will be updated with error information.
	If the component doesn't validate and there is a selected card the constraints of which implement {@link TaskCardConstraints}, the task status of those constraints will be
		set to {@link TaskStatus#ERROR}.
	The user is also notified of any error, using this component's notification, the first notification in the selected card hierarchy, or a default message.
	@return The current state of {@link #isValid()} as a convenience.
	*/
	public boolean validate()
	{
		if(!super.validate())	//validate the component normally; if the component does not validate
		{
			Notification notification=getNotification();	//see if this panel has any notification
			final Component<?> selectedCard=getValue();	//get the selected card
			if(selectedCard!=null)	//if there is a selected card
			{
				final Constraints constraints=selectedCard.getConstraints();	//get the current card constraints
				if(constraints instanceof TaskCardConstraints)	//if these are task card constraints
				{
					((TaskCardConstraints)constraints).setTaskStatus(TaskStatus.ERROR);	//set the task status to error
				}
				if(notification==null)	//if we don't have a notification
				{
					final List<Notification> notifications=getNotifications(selectedCard);	//get the notifications from the card
					if(!notifications.isEmpty())	//if there are notifications
					{
						notification=notifications.get(0);	//use the first notification
					}
				}
			}
			if(notification==null)	//if we didn't find a custom notification
			{
				notification=new Notification(null, VALIDATION_FALSE_MESSAGE_RESOURCE_KEY, Notification.Severity.ERROR);	//use a general validation notification
			}
			getSession().notify(notification);	//indicate that there was a validation error
		}
		return isValid();	//return the current valid state
	}

	/**Commits the data.
	This version commits the selected card if there is a selected card and it implements {@link Commitable}.
	Subclass versions should call this version.
	@throws IOException if there is an error committing data.
	*/
	public void commit() throws IOException
	{
		final Component<?> selectedCard=getSelectedValue();	//get the selected card
		if(selectedCard instanceof Commitable)	//if the selected card is committable
		{
			((Commitable)selectedCard).commit();	//tell the card to commit itself
		}
	}

	/**Determines the component for navigation based upon the given bookmark.
	This version finds the first previous enabled and displayed card, searching backwards from the requested card, if the requested card is not enabled and displayed.
	This version chooses the first card if no card is requested.
	@param bookmark The bookmark for which a component should be returned, or <code>null</code> if no bookmark is available.
	@return The child component indicated by the given bookmark parameter value, or <code>null</code> if the given bookmark represents the <code>null</code> component value.
	*/
	protected Component<?> getComponent(final Bookmark bookmark)
	{
			//choose the first card if no card was specified
		final String parameterValue;	//see if there is a parameter value
		if(bookmark!=null)	//if there is a bookmark
		{
			final String parameterName=getName();	//use this component's name as the bookmark parameter name
			parameterValue=parameterName!=null ? bookmark.getParameterValue(parameterName) : null;	//get the parameter value if there is a parameter name
		}
		else	//if there is no bookmark
		{
			parameterValue=null;	//there is no parameter value
		}
		if(parameterValue==null)	//if no parameter for this component was specified
		{
			if(size()>0)	//if this panel has components
			{
				return get(0);	//automatically choose the first card
			}
		}
		Component<?> component=super.getComponent(bookmark);	//get the requested component normally
		if(component!=null)	//if a component was requested
		{
			if(!isDisplayed(component) || !isEnabled(component))	//if the component is not displayed or not enabled
			{
				component=getPrevious(component);	//get the previous component
			}			
		}
		return component;	//return the determined component 
	}

	/**A validator that validates cards before changing to new cards.
	@author Garret Wilson
	*/
	protected class SequenceCardValidator extends AbstractValidator<Component<?>>
	{

		/**Session constructor.
		@param session The Guise session that owns this validator.
		@exception NullPointerException if the given session is <code>null</code>.
		*/
		public SequenceCardValidator(final GuiseSession session)
		{
			super(session, true);	//construct the parent class, indicating that values are required
		}		
		
		/**Determines whether a given value is valid.
		This version checks whether a value is provided if values are required.
		Child classes should call this version as a convenience for checking non-<code>null</code> and required status.
		@param value The value to validate.
		@return <code>true</code> if a value is given or a value is not required, else <code>false</code>.
		*/
		public boolean isValid(final Component<?> value)
		{
			if(!super.isValid(value))	//if the card doesn't pass the default checks
			{
				return false;	//the card isn't valid
			}
			final Component<?> currentCard=getValue();	//get the currently selected card
			if(currentCard!=null)	//if there is a selected card, do validation if we need to
			{
				final int selectedIndex=indexOf(currentCard);	//get the index of the selected card
				assert selectedIndex>=0 : "Expected selected card to be present in the container.";
				final int newIndex=indexOf(value);	//see what index the new value has
				if(newIndex<0)	//if the new value isn't in the container TODO maybe put this in a default card panel validator
				{
					return false;	//we can't selecte a card not in the container
				}
				if(newIndex>selectedIndex)	//if we're advancing forward in the sequence
				{
					if(!SequenceCardPanel.this.validate())	//validate the panel; if anything, including the currently selected card, doesn't validate
					{
						return false;	//don't go forward
					}									
				}
			}
			return true;	//the new card passed all the tests
		}
	}

}
