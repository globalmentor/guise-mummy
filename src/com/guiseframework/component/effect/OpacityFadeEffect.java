package com.guiseframework.component.effect;

/**An effect for fading from one opacity to another.
@author Garret Wilson
*/
public class OpacityFadeEffect extends AbstractEffect
{

	/**Default constructor with no delay.*/
	public OpacityFadeEffect()
	{
		this(0);	//construct the effect with no delay
	}

	/**Delay constructor.
	@param delay The delay in milliseconds.
	@exception IllegalArgumentException if the given delay is negative.
	*/
	public OpacityFadeEffect(final int delay)
	{
		super(delay);	//construct the parent class
	}

}
