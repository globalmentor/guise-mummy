package com.guiseframework.component;

import com.guiseframework.component.layout.CardLayout;

/**A panel with a card layout.
The panel's value model reflects the currently selected component, if any.
@author Garret Wilson
@see CardLayout
*/
public class CardPanel extends AbstractCardPanel<CardPanel>
{

	/**Default constructor.*/
	public CardPanel()
	{
		this(new CardLayout());	//construct the panel using a default layout
	}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the layout is <code>null</code>.
	*/
	protected CardPanel(final CardLayout layout)
	{
		super(layout);	//construct the parent class
	}
}
