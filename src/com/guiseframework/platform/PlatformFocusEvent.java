package com.guiseframework.platform;

/**Indicates that a component has received focus on the platform.
@author Garret Wilson
*/
public class PlatformFocusEvent extends AbstractDepictEvent
{

	/**Constructs a focus control event.
	@param depictedObject The depicted object on which the event initially occurred.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	*/
	public PlatformFocusEvent(final DepictedObject depictedObject)
	{
		super(depictedObject);	//construct the parent class
	}
}
