package com.guiseframework.component;

import com.guiseframework.component.layout.*;
import com.guiseframework.prototype.Prototype;

/**A panel that holds components used as tools.
@author Garret Wilson
*/
public class Toolbar extends AbstractPanel
{
	
	/**Default constructor with a default horizontal flow layout.*/
	public Toolbar()
	{
		this(new FlowLayout(Flow.LINE));	//default to flowing horizontal
	}

	/**Layout constructor.
	@param layout The layout definition for the container.
	@exception NullPointerException if the given layout is <code>null</code>.
	*/
	public Toolbar(final Layout<?> layout)
	{
		super(layout);	//construct the parent class
	}

	/**Creates a component appropriate for the context of this component from the given prototype.
	This implementation creates a default component and then displays or hides the label as appropriate.
	@param prototype The prototype of the component to create.
	@return A new component based upon the given prototype.
	@exception IllegalArgumentException if no component can be created from the given prototype
	*/
	public Component createComponent(final Prototype prototype)
	{
		final Component component=super.createComponent(prototype);	//create a default component
		if(component instanceof LabelDisplayableComponent)	//if this component can modify its label displayed status
		{
			((LabelDisplayableComponent)component).setLabelDisplayed(false);	//turn off the label TODO make this customizable
		}
		return component;	//return the component
	}
}
