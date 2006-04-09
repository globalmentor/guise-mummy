package com.guiseframework.event;


/**An event indicating an action should take place.
@author Garret Wilson
*/
public class ActionEvent extends AbstractGuiseEvent
{

	/**Aource constructor.
	@param source The object on which the event initially occurred.
	@exception NullPointerException if the given source is <code>null</code>.
	*/
	public ActionEvent(final Object source)
	{
		super(source);	//construct the parent class
	}

}
