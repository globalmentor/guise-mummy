package com.guiseframework.component.layout;

import com.guiseframework.GuiseSession;

/**Constraints on individual component flow.
@author Garret Wilson
*/
public class FlowConstraints extends AbstractFlowConstraints
{

	/**Session constructor.
	@param session The Guise session that owns these constraints.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public FlowConstraints(final GuiseSession session)
	{
		super(session);	//construct the parent class
	}

}
