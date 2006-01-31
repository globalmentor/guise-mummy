package com.javaguise.component.effect;

import com.javaguise.GuiseSession;
import com.javaguise.event.GuiseBoundPropertyObject;

/**An abstract implementation of a component effect.
@author Garret Wilson
*/
public abstract class AbstractEffect extends GuiseBoundPropertyObject implements Effect	//TODO listen for changes in effect properties to dirty the view
{

	/**The delay, in milliseconds, before the effect takes place.*/
	private int delay=0;

		/**@return The delay, in milliseconds, before the effect takes place.*/
		public int getDelay() {return delay;}

		/**Sets the delay before the effect takes place.
		This is a bound property of type <code>Integer</code>.
		@param newDelay The delay, in milliseconds, before the effect takes place.
		@exception IllegalArgumentException if the given delay is negative.
		@see Effect#DELAY_PROPERTY
		*/
		public void setDelay(final int newDelay)
		{
			if(newDelay<0)	//if the delay is negative
			{
				throw new IllegalArgumentException("Delay "+newDelay+" cannot be negative.");
			}
			if(delay!=newDelay)	//if the value is really changing
			{
				final int oldDelay=delay;	//get the current value
				delay=newDelay;	//update the value
				firePropertyChange(DELAY_PROPERTY, new Integer(oldDelay), new Integer(newDelay));
			}
		}

	/**Session constructor with no delay.
	@param session The Guise session that owns this effect.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractEffect(final GuiseSession session)
	{
		this(session, 0);	//construct the effect with no delay
	}

	/**Session and delay constructor.
	@param session The Guise session that owns this effect.
	@param delay The delay in milliseconds.
	@exception NullPointerException if the given session is <code>null</code>.
	@exception IllegalArgumentException if the given delay is negative.
	*/
	public AbstractEffect(final GuiseSession session, final int delay)
	{
		super(session);	//construct the parent class
		if(delay<0)	//if the delay is negative
		{
			throw new IllegalArgumentException("Delay "+delay+" cannot be negative.");
		}
		this.delay=delay;	//save the delay
	}

}
