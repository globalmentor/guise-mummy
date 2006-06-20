package com.guiseframework.component;

import static com.guiseframework.theme.Theme.ICON_BUSY;
import static com.guiseframework.theme.Theme.MESSAGE_BUSY;

import com.guiseframework.component.layout.*;

/**The default panel used to indicate Guise busy status.
@author Garret Wilson
*/
public class DefaultBusyPanel extends LayoutPanel
{

	/**Default constructor.*/
	public DefaultBusyPanel()
	{
		super(new RegionLayout());	//construct the parent class with a region layout
		final Label label=new Label();	//create a new label
		label.setIcon(ICON_BUSY);	//show the busy icon
		label.setLabel(MESSAGE_BUSY);	//show the busy message
		add(label, new RegionConstraints(Region.CENTER));	//put the label in the center
	}

}
