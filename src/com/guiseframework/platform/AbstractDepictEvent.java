package com.guiseframework.platform;

/**The base class for events to or from a depicted object on some platform.
The source of the event is the depicted object.
@param <O> The type of depicted object.
@author Garret Wilson
*/
public abstract class AbstractDepictEvent<O extends DepictedObject> extends AbstractPlatformEvent implements DepictEvent<O>
{

	/**@return The depicted object on which the event initially occurred.*/
	@SuppressWarnings("unchecked")
	public O getDepictedObject() {return (O)getSource();}
	
	/**Depicted object constructor.
	@param depictedObject The depicted object on which the event initially occurred.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	*/
	public AbstractDepictEvent(final O depictedObject)
	{
		super(depictedObject);	//construct the parent class
	}

}
