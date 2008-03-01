package com.guiseframework.event;


import static com.globalmentor.java.Objects.*;

import com.globalmentor.beans.*;
import com.guiseframework.model.Displayable;

/**A {@link Boolean} property change listener that synchronizes a {@link Displayable} source's {@link Displayable#DISPLAYED_PROPERTY}
by calling {@link Displayable#setDisplayed(boolean)} with any new value that it receives.
If there is no {@link Displayable} event source or no new value for any given event, no action occurs.
@author Garret Wilson
*/
public class SynchronizeDisplayedPropertyChangeListener extends AbstractGenericPropertyChangeListener<Boolean>	//TODO turn this into a singleton
{

	/**Called when a bound property is changed.
	@param genericPropertyChangeEvent An event object describing the event source, the property that has changed, and its old and new values.
	*/
	public void propertyChange(final GenericPropertyChangeEvent<Boolean> genericPropertyChangeEvent)
	{
		final Displayable displayableSource=asInstance(genericPropertyChangeEvent.getSource(), Displayable.class);	//get the source as a Displayable
		if(displayableSource!=null)	//if the source is displayable
		{
			final Boolean newValue=genericPropertyChangeEvent.getNewValue();	//get the new value
			if(newValue!=null)	//if there is a new value
			{
				displayableSource.setDisplayed(newValue.booleanValue());	//update the displayable's displayed status to match
			}
		}
	}
}
