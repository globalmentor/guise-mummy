package com.guiseframework.component;

import com.guiseframework.GuiseSession;
import com.guiseframework.component.layout.CardLayout;
import com.guiseframework.validator.ValidationException;

/**A card panel representing a sequence of cards.
@author Garret Wilson
@see CardLayout
*/
public class SequenceCardPanel extends AbstractCardPanel<SequenceCardPanel>
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
	}

	/**Determines if there is a previous step in the sequence.
	This version returns <code>true</code> if there is a selected card and there exists a card before the selected card. 
	@return <code>true</code> if there is a previous step in the sequence.
	*/
	public boolean hasPrevious()
	{
		return getSelectedIndex()>0;	//return whether there is a selected index greater than the minimum selected index, zero
	}

	/**Determines if there is a next step in the sequence.
	This version returns <code>true</code> if there is a selected card and there exists a card after the selected card. 
	@return <code>true</code> if there is a next step in the sequence.
	*/
	public boolean hasNext()
	{
		final int selectedIndex=getSelectedIndex();	//get the selected index
		return selectedIndex>=0 && selectedIndex<size()-1;	//return whether there is a selected index less than the maximum selected index
	}

	/**Goes to the previous step in the sequence.
	If there is no previous step, no action occurs.
	This method calls {@link #hasPrevious()}.
	*/
	public void goPrevious()
	{
		if(hasPrevious())	//if there is a previous step
		{
			try
			{
				final Component<?> selectedCard=getSelectedValue();	//get the selected card
				assert selectedCard!=null : "No card selected, even though hasPrevious() should have returned false if no card is selected.";
				try
				{
					selectedCard.validate();	//validate the selected card
					setSelectedIndexes(getSelectedIndex()-1);	//advance to the previous index
				}
				catch(final ComponentExceptions componentException)
				{
					//TODO improve; inform user
				}				
			}
			catch(final ValidationException validationException)
			{
				throw new AssertionError(validationException);	//TODO improve
			}
		}
	}

	/**Advances to the next step in the sequence.
	If there is no next step, no action occurs.
	This method calls {@link #hasNext()}.
	*/
	public void goNext()
	{
		if(hasNext())	//if there is a next step
		{
			try
			{
				final Component<?> selectedCard=getSelectedValue();	//get the selected card
				assert selectedCard!=null : "No card selected, even though hasNext() should have returned false if no card is selected.";
				try
				{
					selectedCard.validate();	//validate the selected card
					setSelectedIndexes(getSelectedIndex()+1);	//advance to the next index
				}
				catch(final ComponentExceptions componentException)
				{
					//TODO improve; inform user
				}				
			}
			catch(final ValidationException validationException)
			{
				throw new AssertionError(validationException);	//TODO improve
			}
		}
	}

}
