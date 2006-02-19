package com.guiseframework.component.layout;

import com.guiseframework.GuiseSession;
import com.guiseframework.event.GuiseBoundPropertyObject;

/**An abstract implementation of constraints of individual component layout.
@author Garret Wilson
*/
public abstract class AbstractConstraints extends GuiseBoundPropertyObject implements Constraints
{

	/**Session constructor.
	@param session The Guise session that owns these constraints.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractConstraints(final GuiseSession session)
	{
		super(session);	//construct the parent class
	}

}
