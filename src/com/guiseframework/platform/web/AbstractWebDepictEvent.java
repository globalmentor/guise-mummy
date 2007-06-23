package com.guiseframework.platform.web;

import com.guiseframework.platform.*;

/**The base class for events to or from a depicted object on the web platform.
The source of the event is the depicted object.
@param <O> The type of depicted object.
@author Garret Wilson
*/
public abstract class AbstractWebDepictEvent<O extends DepictedObject> extends AbstractDepictEvent<O> implements WebDepictEvent<O>
{

	/**Depicted object constructor.
	@param depictedObject The depicted object on which the event initially occurred.
	@exception NullPointerException if the given depicted object is <code>null</code>.
	*/
	public AbstractWebDepictEvent(final O depictedObject)
	{
		super(depictedObject);	//construct the parent class
	}

}
