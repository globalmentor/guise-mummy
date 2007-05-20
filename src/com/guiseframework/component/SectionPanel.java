package com.guiseframework.component;

import com.guiseframework.component.layout.Flow;
import com.guiseframework.component.layout.FlowLayout;
import com.guiseframework.component.layout.Layout;

/**A panel that demarcates a semantically signifcant area of the a parent component.
@author Garret Wilson
*/
public class SectionPanel extends AbstractPanel<SectionPanel>
{
	
	/**Default constructor with a default vertical flow layout.*/
	public SectionPanel()
	{
		this(new FlowLayout(Flow.PAGE));	//default to flowing vertically
	}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public SectionPanel(final Layout<?> layout)
	{
		super(layout);	//construct the parent class
	}
}
