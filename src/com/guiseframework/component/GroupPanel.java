package com.guiseframework.component;

import com.guiseframework.component.layout.*;

/**A panel for grouping multiple components with a default page flow layout.
@author Garret Wilson
*/
public class GroupPanel extends AbstractBox<GroupPanel> implements Panel<GroupPanel>	//TODO maybe add a label model constructor parameter; see old revisions
{

	/**Default constructor with a default vertical flow layout.*/
	public GroupPanel()
	{
		this(new FlowLayout(Flow.PAGE));	//default to flowing vertically
	}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public GroupPanel(final Layout<?> layout)
	{
		super(layout);	//construct the parent class
	}

}
