package com.guiseframework.platform.web;

import com.guiseframework.component.SelectableLabel;
import com.guiseframework.style.Color;

/**Strategy for rendering a label component that is selectable.
@param <C> The type of component being depicted.
@author Garret Wilson
*/
public class WebSelectableLabelDepictor<C extends SelectableLabel> extends WebLabelDepictor<C>
{

	/**Determines the color for rendering the component.
	This version uses the selected color if available and the component is selected.
	@return The color to use for this component.
	@see SelectableLabel#getSelectedBackgroundColor()
	*/
	protected Color getColor()
	{
		return super.getColor();	//TODO fix
	}

	/**Determines the background color for rendering the component.
	This version uses the selected background color if available and the component is selected.
	@return The background color to use for this component.
	@see SelectableLabel#getSelectedBackgroundColor()
	*/
	protected Color getBackgroundColor()
	{
		final C component=getDepictedObject();	//get the component
		if(component.isSelected())	//if the component is selected
		{
			final Color selectedBackgroundColor=component.getSelectedBackgroundColor();	//get the selected background color
			if(selectedBackgroundColor!=null)	//if there is a backgrond color for the selected state
			{
				return selectedBackgroundColor;	//return the defined selected background color
			}
		}
		return super.getBackgroundColor();	//return the default background color if the component isn't selected or we couldn't find a selected background color
	}

}
