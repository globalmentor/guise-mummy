package com.javaguise.component.effect;

import static com.garretwilson.lang.ClassUtilities.*;

/**Encapsulates information for a component effect.
@author Garret Wilson
*/
public interface Effect
{
	/**The delay bound property.*/
	public final static String DELAY_PROPERTY=getPropertyName(Effect.class, "delay");

	/**@return The delay, in milliseconds, before the effect takes place.*/
	public int getDelay();

	/**Sets the delay before the effect takes place.
	This is a bound property of type <code>Integer</code>.
	@param newDelay The delay, in milliseconds, before the effect takes place.
	@exception IllegalArgumentException if the given delay is negative.
	@see #DELAY_PROPERTY
	*/
	public void setDelay(final int newDelay);

}
