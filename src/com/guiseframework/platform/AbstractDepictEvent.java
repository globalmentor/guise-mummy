package com.guiseframework.platform;

/**The base class for events to or from a depicted object on some platform.
The source of the event is the depicted object.
@author Garret Wilson
*/
public abstract class AbstractDepictEvent extends AbstractPlatformEvent implements DepictEvent
{

	/**@return The depicted object on which the event initially occurred.*/
	public DepictedObject getDepictedObject() {return (DepictedObject)getSource();}
	
	/**Depicted object constructor.
	@param depictedObject The depicted object on which the event initially occurred.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	*/
	public AbstractDepictEvent(final DepictedObject depictedObject)
	{
		super(depictedObject);	//construct the parent class
	}

}
