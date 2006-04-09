package com.guiseframework.component.effect;

/**An effect for simple delay.
@author Garret Wilson
*/
public class DelayEffect extends AbstractEffect
{

	/**Default constructor with no delay.*/
	public DelayEffect()
	{
		this(0);	//construct the effect with no delay
	}

	/**Delay constructor.
	@param session The Guise session that owns this effect.
	@param delay The delay in milliseconds.
	@exception IllegalArgumentException if the given delay is negative.
	*/
	public DelayEffect(final int delay)
	{
		super(delay);	//construct the parent class
	}

}
