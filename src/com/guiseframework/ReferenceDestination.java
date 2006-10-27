package com.guiseframework;

/**A destination that references another destination.
@author Garret Wilson
*/
public interface ReferenceDestination extends Destination
{

	/**@return The referenced destination.*/
	public Destination getDestination();
}
