package com.javaguise.component.effect;

import com.javaguise.GuiseSession;

/**An effect for simple delay.
@author Garret Wilson
*/
public class DelayEffect extends AbstractEffect
{

	/**Session constructor with no delay.
	@param session The Guise session that owns this effect.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DelayEffect(final GuiseSession session)
	{
		this(session, 0);	//construct the effect with no delay
	}

	/**Session and delay constructor.
	@param session The Guise session that owns this effect.
	@param delay The delay in milliseconds.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public DelayEffect(final GuiseSession session, final int delay)
	{
		super(session, delay);	//construct the parent class
	}

}
