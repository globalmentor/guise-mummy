package com.guiseframework.component;

import com.guiseframework.component.layout.*;

/**A general panel with a default page flow layout.
This class, which has no particular semantics, is used for laying out child components.
@author Garret Wilson
*/
public class LayoutPanel extends AbstractPanel<LayoutPanel>
{
	
	/**Default constructor with a default vertical flow layout.*/
	public LayoutPanel()
	{
		this(new FlowLayout(Flow.PAGE));	//default to flowing vertically
	}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public LayoutPanel(final Layout<?> layout)
	{
		super(layout);	//construct the parent class
	}
}
