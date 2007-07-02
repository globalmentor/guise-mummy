package com.guiseframework.component;

import com.guiseframework.component.layout.CardLayout;

/**A tabbed panel with a card layout.
The panel's value model reflects the currently selected component, if any.
@author Garret Wilson
@see CardLayout
*/
public class TabbedPanel extends AbstractCardPanel
{

	/**Default constructor.*/
	public TabbedPanel()
	{
		this(new CardLayout());	//construct the panel using a default layout
	}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	protected TabbedPanel(final CardLayout layout)
	{
		super(layout);	//construct the parent class, using the card layout's value model
	}

}
