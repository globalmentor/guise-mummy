package com.guiseframework.platform;

/**An event to or from a depicted object on some platform.
The source of the event is the depicted object.
@param <O> The type of depicted object.
@author Garret Wilson
*/
public interface DepictEvent<O extends DepictedObject> extends PlatformEvent
{

	/**@return The depicted object on which the event initially occurred.*/
	public O getDepictedObject();

}
