package com.guiseframework.component.layout;

import com.guiseframework.GuiseSession;

/**Constraints on individual menu layout.
@author Garret Wilson
*/
public class MenuConstraints extends AbstractFlowConstraints
{

	/**Session constructor.
	@param session The Guise session that owns these constraints.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public MenuConstraints(final GuiseSession session)
	{
		super(session);	//construct the parent class
	}

}
