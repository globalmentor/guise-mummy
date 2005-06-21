package com.garretwilson.guise.event;

/**An event indicating an action should take place.
@author Garret Wilson
*/
public class ActionEvent<S> extends GuiseEvent<S>
{

	/**Source constructor.
	@param source The object on which the event initially occurred.
	@exception IllegalArgumentException if source is <code>null</code>.
	*/
	public ActionEvent(final S source)
	{
		super(source);	//construct the parent class
	}

}
