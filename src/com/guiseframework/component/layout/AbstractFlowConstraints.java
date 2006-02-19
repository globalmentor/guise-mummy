package com.guiseframework.component.layout;

import com.guiseframework.GuiseSession;

/**Abstract constraints on individual component flow.
@author Garret Wilson
*/
public class AbstractFlowConstraints extends AbstractConstraints
{

	/**Session constructor.
	@param session The Guise session that owns these constraints.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public AbstractFlowConstraints(final GuiseSession session)
	{
		super(session);	//construct the parent class
	}

}
