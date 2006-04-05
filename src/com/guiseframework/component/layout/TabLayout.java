package com.guiseframework.component.layout;

import com.guiseframework.GuiseSession;
import com.guiseframework.model.*;

/**A layout that manages child components as a series of tabs.
Only one child component is visible at a time.
The tab layout maintains its own value model that maintains the current selected component.
If a tab implements {@link Activeable} the tab is set as active when selected and set as inactive when the tab is unselected.
@author Garret Wilson
*/
public class TabLayout extends AbstractValueLayout<ControlConstraints>
{

	/**@return The class representing the type of constraints appropriate for this layout.*/
	public Class<? extends ControlConstraints> getConstraintsClass() {return ControlConstraints.class;}

	/**Creates default constraints for the container.
	@return New default constraints for the container.
	*/
	public ControlConstraints createDefaultConstraints()
	{
		return new ControlConstraints(getSession());	//create constraints
	}

	/**Session constructor.
	@param session The Guise session that owns this layout.
	@exception NullPointerException if the given session is <code>null</code>.
	*/
	public TabLayout(final GuiseSession session)
	{
		super(session);	//construct the parent class
	}

}
